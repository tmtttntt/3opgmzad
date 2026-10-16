package com.opengate.app.ui.screens.auction

import com.opengate.app.data.currency.AppCurrency
import com.opengate.app.data.currency.CurrencyManager
import com.opengate.app.data.model.AuctionRow
import com.opengate.app.ui.screens.auction.design.AuctionCategoryUi
import com.opengate.app.ui.screens.auction.design.AuctionItemUi
import com.opengate.app.ui.screens.auction.design.AuctionSectionUi
import com.opengate.app.ui.screens.auction.design.CategorySpan
import com.opengate.app.ui.screens.auction.design.HeroBannerUi
import java.time.OffsetDateTime

/* تحويل بيانات Supabase إلى نماذج الواجهة. العملة تأتي من الإعلان نفسه (YER افتراضياً)، لا من التصميم. */

internal fun formatMoney(amount: Double, currencyCode: String?): String =
    "${CurrencyManager.formatNumber(amount)} ${AppCurrency.fromCode(currencyCode).symbolAr}"

internal fun parseEpochSeconds(iso: String?): Long? =
    iso?.let { runCatching { OffsetDateTime.parse(it).toInstant().epochSecond }.getOrNull() }

/** 6ي 21س  |  5س 12د  |  12د  |  أقل من دقيقة */
internal fun formatDuration(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0)
    val days = s / 86_400
    val hours = (s % 86_400) / 3_600
    val minutes = (s % 3_600) / 60
    return when {
        days > 0 -> "${days}ي ${hours}س"
        hours > 0 -> "${hours}س ${minutes}د"
        minutes > 0 -> "${minutes}د"
        else -> "أقل من دقيقة"
    }
}

internal fun AuctionRow.toItemUi(nowSeconds: Long, favoriteIds: Set<String>): AuctionItemUi {
    val ref = listings
    val endsAt = parseEpochSeconds(ends_at)
    val startsAt = parseEpochSeconds(starts_at)
    val ended = status == "ended" || status == "cancelled" || (endsAt != null && endsAt <= nowSeconds)
    val live = !ended && status == "live"
    val upcoming = !ended && status == "scheduled"

    val timeLeft = when {
        ended -> null
        upcoming && startsAt != null -> "يبدأ بعد ${formatDuration(startsAt - nowSeconds)}"
        endsAt != null -> formatDuration(endsAt - nowSeconds)
        else -> null
    }

    return AuctionItemUi(
        id = id,
        listingId = listing_id,
        title = ref?.title?.ifBlank { null } ?: "مزاد معلن",
        subtitle = ref?.city,
        categoryId = ref?.category_id,
        categoryName = ref?.categories?.name_ar?.ifBlank { null },
        imageUrls = ref?.imageUrls.orEmpty(),
        isLive = live,
        isUpcoming = upcoming,
        isEnded = ended,
        isFavorite = listing_id in favoriteIds,
        bidsCount = bidsCount,
        timeLeft = timeLeft,
        priceText = formatMoney(current_price ?: starting_price, ref?.currency),
        location = ref?.city,
    )
}

/** فئات المنصة التي فيها مزادات فعلاً، الأكثر مزادات أولاً؛ صورة الفئة = صورة أول مزاد فيها. */
internal fun buildCategories(items: List<AuctionItemUi>): List<AuctionCategoryUi> =
    items.filter { it.categoryId != null }
        .groupBy { it.categoryId!! }
        .entries
        .sortedByDescending { it.value.size }
        .mapIndexed { index, (categoryId, group) ->
            AuctionCategoryUi(
                id = categoryId,
                title = group.firstNotNullOfOrNull { it.categoryName } ?: "أخرى",
                imageUrl = group.firstNotNullOfOrNull { it.coverUrl },
                count = group.size,
                span = if (index % 5 == 0) CategorySpan.Wide else CategorySpan.Small,
            )
        }

/** بانر علوي من المزادات المصوّرة الأكثر مزايدة ثم الأقرب انتهاءً. */
internal fun buildHero(items: List<AuctionItemUi>): List<HeroBannerUi> =
    items.filter { it.coverUrl != null && !it.isEnded }
        .sortedByDescending { it.bidsCount }
        .take(3)
        .map { HeroBannerUi(id = it.id, title = it.title, subtitle = it.categoryName ?: it.priceText, imageUrl = it.coverUrl) }

internal fun buildSections(items: List<AuctionItemUi>, categories: List<AuctionCategoryUi>): List<AuctionSectionUi> =
    categories.map { category ->
        val group = items.filter { it.categoryId == category.id }
        AuctionSectionUi(id = category.id, title = category.title, count = group.size, items = group.take(12))
    }
