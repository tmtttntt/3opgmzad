package com.opengate.app.ui.screens.auction.design

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ================================================================ hero pager

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroBannerPager(
    banners: List<HeroBannerUi>,
    onClick: (HeroBannerUi) -> Unit,
    modifier: Modifier = Modifier,
    autoScrollMillis: Long = 4_000L,
) {
    if (banners.isEmpty()) return
    val colors = AuctionTheme.colors
    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(pagerState, banners.size) {
        if (banners.size < 2) return@LaunchedEffect
        while (true) {
            delay(autoScrollMillis)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % banners.size)
        }
    }

    Box(modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 8.dp,
            modifier = Modifier.fillMaxWidth().height(112.dp),
        ) { page ->
            val banner = banners[page]
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onClick(banner) },
            ) {
                AuctionImage(banner.imageUrl, Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.30f)))
                Column(
                    modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(banner.title, color = Color.White, style = AuctionTheme.type.sectionTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (banner.subtitle != null) {
                        Text(banner.subtitle, color = Color.White.copy(alpha = 0.75f), style = AuctionTheme.type.caption, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        if (banners.size > 1) {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(banners.size) { index ->
                    val active = index == pagerState.currentPage
                    Box(
                        Modifier
                            .height(8.dp)
                            .width(if (active) 28.dp else 8.dp)
                            .background(if (active) colors.gold else Color.White.copy(alpha = 0.6f), CircleShape),
                    )
                }
            }
        }
    }
}

// ================================================================ add-item banner

@Composable
fun AddItemBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = AuctionTheme.strings.addItemTitle,
    subtitle: String = AuctionTheme.strings.addItemSubtitle,
) {
    val colors = AuctionTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AuctionTheme.shapes.card)
            // physical gradient: darker on the right, lighter on the left (as in the reference)
            .background(Brush.horizontalGradient(listOf(colors.gradientEnd, colors.gradientStart)))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = Color.White, style = AuctionTheme.type.sectionTitle)
            Text(subtitle, color = Color.White.copy(alpha = 0.75f), style = AuctionTheme.type.body.copy(fontSize = 14.sp))
        }
        Box(
            Modifier.size(56.dp).background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.AddCircleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
        }
    }
}

// ================================================================ category mosaic

private fun packRows(items: List<AuctionCategoryUi>, unitsPerRow: Int = 3): List<List<AuctionCategoryUi>> {
    val rows = mutableListOf<List<AuctionCategoryUi>>()
    var current = mutableListOf<AuctionCategoryUi>()
    var used = 0
    for (item in items) {
        val units = item.span.units
        if (used + units > unitsPerRow && current.isNotEmpty()) {
            rows += current
            current = mutableListOf()
            used = 0
        }
        current += item
        used += units
    }
    if (current.isNotEmpty()) rows += current
    return rows
}

/**
 * Three-unit mosaic: Small = 1 unit, Wide = 2 units. Categories are fully data-driven,
 * so showing / hiding a category is just adding / removing it from the list.
 */
@Composable
fun CategoryMosaic(
    categories: List<AuctionCategoryUi>,
    onClick: (AuctionCategoryUi) -> Unit,
    modifier: Modifier = Modifier,
    rowHeight: androidx.compose.ui.unit.Dp = 128.dp,
    gap: androidx.compose.ui.unit.Dp = 10.dp,
) {
    val rows = remember(categories) { packRows(categories) }
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val unit = (maxWidth - gap * 2) / 3
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    row.forEach { category ->
                        val units = category.span.units
                        CategoryTile(
                            category = category,
                            onClick = { onClick(category) },
                            modifier = Modifier.width(unit * units + gap * (units - 1)).height(rowHeight),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTile(
    category: AuctionCategoryUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = AuctionTheme.shapes.tile
    Box(
        modifier = modifier
            .shadow(4.dp, shape)
            .clip(shape)
            .border(1.dp, AuctionTheme.colors.gold.copy(alpha = 0.25f), shape)
            .clickable(onClick = onClick),
    ) {
        if (category.imageUrl.isNullOrBlank()) {
            // لا صورة للفئة: تدرج أخضر بدل المربع الرمادي
            val c = AuctionTheme.colors
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(c.gradientEnd, c.headerDark))))
        } else {
            AuctionImage(category.imageUrl, Modifier.fillMaxSize())
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0x33000000), Color(0xB3000000)))),
        )
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = category.title,
                color = Color.White,
                style = AuctionTheme.type.cardTitle.copy(fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (category.subtitle != null && category.span.units > 1) {
                Text(
                    text = category.subtitle,
                    color = Color.White.copy(alpha = 0.75f),
                    style = AuctionTheme.type.caption,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (category.count > 0) {
            CountBadge(category.count, Modifier.align(Alignment.TopStart).padding(8.dp))
        }
    }
}

// ================================================================ section header

@Composable
fun SectionHeader(
    title: String,
    count: Int?,
    modifier: Modifier = Modifier,
    onViewAll: (() -> Unit)? = null,
) {
    val colors = AuctionTheme.colors
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = colors.textPrimary,
            style = AuctionTheme.type.sectionTitle,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (onViewAll != null) {
            Row(
                modifier = Modifier.clip(CircleShape).clickable(onClick = onViewAll).padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (count != null) SoftCountPill(count)
                Text(AuctionTheme.strings.viewAll, color = colors.gold, style = AuctionTheme.type.chip.copy(fontSize = 16.sp))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.gold)
            }
        }
    }
}
