package com.opengate.app.data.repository

import com.opengate.app.data.model.Auction
import com.opengate.app.data.model.AuctionRow
import com.opengate.app.data.model.Bid
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * ⚠️ التحقق الحقيقي من كل مزايدة (أعلى من السعر الحالي، المزاد جارٍ، لا مزايدة على مزادك)
 * وتحديث السعر الحالي يحصلان داخل القاعدة عبر trigger باسم guard_bid_insert (ملف sql/002_security_fixes.sql)،
 * وإنشاء المزاد يتم ذرّياً عبر الدالة create_auction. أي تلاعب من جهاز المستخدم لن ينجح.
 */
class AuctionRepository(private val client: SupabaseClient) {

    /** أعمدة العرض من الأغنى للأبسط؛ نجرّب بالترتيب كي لا تُفرغ القائمة إن لم يدعم الخادم تضميناً معيناً */
    private val listColumns = listOf(
        "*, listings(title, city, category_id, currency, listing_images(url, sort_order), categories(name_ar)), bids(count)",
        "*, listings(title, city, category_id, currency, listing_images(url, sort_order), categories(name_ar))",
        "*, listings(title)"
    )

    private val detailColumns = listOf(
        "*, listings(title, description, city, category_id, currency, contact_phone, listing_images(url, sort_order), categories(name_ar)), bids(count)",
        "*, listings(title, description, city, category_id, currency, contact_phone, listing_images(url, sort_order), categories(name_ar))",
        "*, listings(title)"
    )

    suspend fun getLiveAuctions(): List<AuctionRow> {
        // تحديث حالات المزادات (انتهى/بدأ) في القاعدة قبل العرض؛ فشلها لا يمنع العرض
        runCatching { client.postgrest.rpc("refresh_auction_statuses") }
        for (columns in listColumns) {
            val result = runCatching {
                client.postgrest["auctions"]
                    .select(Columns.raw(columns)) {
                        filter {
                            or {
                                eq("status", "live")
                                eq("status", "scheduled")
                            }
                            gt("ends_at", java.time.Instant.now().toString())
                        }
                        order("starts_at", Order.DESCENDING)
                    }.decodeList<AuctionRow>()
            }
            if (result.isSuccess) return result.getOrThrow()
        }
        return emptyList()
    }

    /** مزاد واحد مع بيانات إعلانه وصوره (لشاشة المزاد المباشر) */
    suspend fun getAuctionDetail(id: String): AuctionRow {
        var last: Throwable? = null
        for (columns in detailColumns) {
            val result = runCatching {
                client.postgrest["auctions"]
                    .select(Columns.raw(columns)) { filter { eq("id", id) } }
                    .decodeSingle<AuctionRow>()
            }
            if (result.isSuccess) return result.getOrThrow()
            last = result.exceptionOrNull()
        }
        throw last ?: IllegalStateException("المزاد غير موجود")
    }

    suspend fun getAuctionById(id: String): Auction =
        client.postgrest["auctions"].select { filter { eq("id", id) } }.decodeSingle()

    /**
     * ينشئ سلعة المزاد والمزاد في معاملة واحدة داخل القاعدة (دالة create_auction).
     * قد يبقى الإعلان بانتظار المراجعة إن كانت منصة المزادات تتطلب مراجعة.
     */
    suspend fun startNewAuction(
        title: String,
        startingPrice: Double,
        minIncrement: Double,
        durationHours: Long,
        city: String?,
        contactPhone: String?
    ): String = client.postgrest.rpc(
        "create_auction",
        buildJsonObject {
            put("p_title", title.trim())
            put("p_starting_price", startingPrice)
            put("p_min_increment", minIncrement)
            put("p_duration_hours", durationHours)
            put("p_city", city?.trim()?.ifBlank { null })
            put("p_phone", contactPhone?.trim()?.ifBlank { null })
        }
    ).decodeAs<String>()

    suspend fun getBids(auctionId: String, limit: Long = 30): List<Bid> =
        client.postgrest["bids"].select {
            filter { eq("auction_id", auctionId) }
            order("created_at", Order.DESCENDING)
            limit(limit)
        }.decodeList()

    /** يرمي استثناءً برسالة القاعدة نفسها (مثال: "المزايدة يجب أن تكون 550 على الأقل") إن رُفضت */
    suspend fun placeBid(auctionId: String, bidderId: String, amount: Double) {
        client.postgrest["bids"].insert(Bid(auction_id = auctionId, bidder_id = bidderId, amount = amount))
    }

    /** يستمع لتحديثات السعر (UPDATE على auctions) ولكل مزايدة جديدة (INSERT على bids) لحظياً */
    fun subscribeToAuction(
        auctionId: String,
        scope: CoroutineScope,
        onPriceUpdate: (Auction) -> Unit,
        onNewBid: (Bid) -> Unit
    ): RealtimeChannel {
        val channel = client.realtime.channel("auction-$auctionId")

        // يجب تسجيل التدفقات قبل subscribe() وبشكل متزامن لضمان عدم ضياع أي حدث
        val priceFlow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") { table = "auctions" }
        val bidFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") { table = "bids" }

        scope.launch {
            priceFlow
                .map { it.decodeRecord<Auction>() }
                .filter { it.id == auctionId }
                .onEach(onPriceUpdate)
                .collect()
        }
        scope.launch {
            bidFlow
                .map { it.decodeRecord<Bid>() }
                .filter { it.auction_id == auctionId }
                .onEach(onNewBid)
                .collect()
        }
        scope.launch { channel.subscribe() }
        return channel
    }

    suspend fun leaveChannel(channel: RealtimeChannel) {
        // removeChannel() يُلغي الاشتراك *ويزيل* القناة من قائمة Realtime.subscriptions معاً —
        // استخدام unsubscribe() وحدها يوقف الاستماع لكن يُبقي القناة مسجّلة، مما يسبب تسرّب ذاكرة بسيط
        // مع تكرار فتح/إغلاق شاشات الدردشة أو المزاد.
        runCatching { client.realtime.removeChannel(channel) }
    }
}
