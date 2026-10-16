package com.opengate.app.data.repository

import com.opengate.app.data.model.Listing
import com.opengate.app.data.model.ListingImage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import java.util.UUID

class ListingRepository(private val client: SupabaseClient) {

    suspend fun getPublishedListings(platformId: String, categoryId: String? = null): List<Listing> = runCatching {
        val list = client.postgrest["listings"].select {
            filter {
                eq("platform_id", platformId)
                eq("status", "published")   // المراجعة والإخفاء تُفرض في القاعدة؛ لا نعرض إلا المنشور
                categoryId?.let { eq("category_id", it) }
            }
            order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
        }.decodeList<Listing>()
        if (list.isNotEmpty()) {
            com.opengate.app.data.offline.OfflineCacheManager.cacheListings(platformId, list)
        }
        list
    }.getOrElse {
        // عند انقطاع الإنترنت أو العمل أوفلاين، استرجاع الإعلانات المحفوظة محلياً بسلاسة
        val cached = com.opengate.app.data.offline.OfflineCacheManager.getCachedListings(platformId)
        if (categoryId != null) cached.filter { it.category_id == categoryId } else cached
    }

    suspend fun getListingById(id: String): Listing =
        client.postgrest["listings"].select { filter { eq("id", id) } }.decodeSingle()

    suspend fun getListingImages(listingId: String): List<ListingImage> = runCatching {
        client.postgrest["listing_images"]
            .select { filter { eq("listing_id", listingId) } }
            .decodeList<ListingImage>()
    }.getOrDefault(emptyList())

    /**
     * ينشئ الإعلان. الحالة النهائية (منشور أو قيد المراجعة) يحددها الخادم حسب إعداد المنصة requires_moderation،
     * والعميل يرسل فقط "طلب نشر".
     */
    suspend fun createListing(listing: Listing): Listing =
        client.postgrest["listings"].insert(listing) { select() }.decodeSingle()

    /**
     * يرفع صورة الإعلان إلى Storage (باكت listing-images، مسار listings/<listingId>/<uuid>.jpg)
     * ثم يسجّلها في listing_images. يرمي استثناءً إن فشل الرفع (لا حفظ Base64 داخل القاعدة).
     * تُفرض الملكية والحجم والنوع عبر سياسات Storage (sql/002_security_fixes.sql).
     */
    suspend fun uploadListingImage(listingId: String, bytes: ByteArray): String {
        require(bytes.isNotEmpty()) { "الصورة فارغة" }

        val bucket = client.storage["listing-images"]
        val path = "listings/$listingId/${UUID.randomUUID()}.jpg"
        bucket.upload(path, bytes, upsert = false)
        val url = bucket.publicUrl(path)

        // فشل التسجيل في الجدول لا يلغي الصورة: الرابط يُحفظ أيضاً داخل attributes.photos
        runCatching {
            client.postgrest["listing_images"].insert(ListingImage(listing_id = listingId, url = url))
        }
        return url
    }
}
