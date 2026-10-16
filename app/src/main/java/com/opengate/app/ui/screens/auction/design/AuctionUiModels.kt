package com.opengate.app.ui.screens.auction.design

import androidx.compose.runtime.Immutable

/*
 * نماذج واجهة بحتة (بلا اعتماد على Supabase). التحويل من AuctionRow يتم في AuctionUiMapper.kt.
 * الأسعار والوقت المتبقي نصوص جاهزة، فالتصميم لا يفترض عملة معينة.
 */

enum class CategorySpan(val units: Int) { Small(1), Wide(2) }

@Immutable
data class HeroBannerUi(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
)

@Immutable
data class AuctionCategoryUi(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val count: Int = 0,
    val span: CategorySpan = CategorySpan.Small,
)

@Immutable
data class AuctionItemUi(
    val id: String,                    // معرّف المزاد
    val listingId: String,             // معرّف الإعلان (للمفضلة)
    val title: String,
    val subtitle: String? = null,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val imageUrls: List<String> = emptyList(),
    val isLive: Boolean = false,
    val isUpcoming: Boolean = false,
    val isEnded: Boolean = false,
    val isFavorite: Boolean = false,
    val bidsCount: Int = 0,
    val timeLeft: String? = null,
    val priceText: String? = null,     // السعر الحالي أو سعر البداية
    val location: String? = null,
) {
    val coverUrl: String? get() = imageUrls.firstOrNull()
}

@Immutable
data class AuctionSectionUi(
    val id: String,
    val title: String,
    val count: Int,
    val items: List<AuctionItemUi>,
)
