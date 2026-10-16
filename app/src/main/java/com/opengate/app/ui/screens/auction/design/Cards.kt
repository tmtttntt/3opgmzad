package com.opengate.app.ui.screens.auction.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GlassBlack = Color(0x8C000000)

/** شارة الحالة فوق الصورة: LIVE للمباشر، "قادم" للمجدول. */
@Composable
fun AuctionStatusBadge(item: AuctionItemUi, modifier: Modifier = Modifier) {
    val colors = AuctionTheme.colors
    when {
        item.isLive -> LiveBadge(modifier)
        item.isUpcoming -> StatusTag(AuctionTheme.strings.upcoming, colors.gold, colors.priceGreen, modifier)
        item.isEnded -> StatusTag(AuctionTheme.strings.ended, Color(0xFF7A7A7A), Color.White, modifier)
    }
}

// ================================================================ compact card (carousels)

@Composable
fun AuctionCompactCard(
    item: AuctionItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AuctionTheme.colors
    val strings = AuctionTheme.strings
    val shape = AuctionTheme.shapes.card
    Column(
        modifier = modifier
            .width(168.dp)
            .shadow(2.dp, shape)
            .clip(shape)
            .background(colors.card)
            .clickable(onClick = onClick),
    ) {
        Box(Modifier.fillMaxWidth().height(138.dp)) {
            AuctionImage(item.coverUrl, Modifier.fillMaxSize())
            AuctionStatusBadge(item, Modifier.align(Alignment.TopStart).padding(8.dp))
            if (item.timeLeft != null) {
                GlassPill(Modifier.align(Alignment.BottomCenter).padding(8.dp)) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Text(item.timeLeft, color = Color.White, style = AuctionTheme.type.caption)
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().height(118.dp).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = item.title,
                color = colors.textPrimary,
                style = AuctionTheme.type.cardTitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Column {
                Text(
                    text = if (item.bidsCount == 0) strings.noBids else "${item.bidsCount} ${strings.bidsUnit}",
                    color = colors.textMuted,
                    style = AuctionTheme.type.caption,
                )
                if (item.priceText != null) {
                    Text(item.priceText, color = colors.priceGreen, style = AuctionTheme.type.priceSmall.copy(fontSize = 18.sp))
                }
            }
        }
    }
}

// ================================================================ large list card ("all auctions")

@Composable
fun AuctionListCard(
    item: AuctionItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteClick: (() -> Unit)? = null,
) {
    val colors = AuctionTheme.colors
    val strings = AuctionTheme.strings
    val shape = AuctionTheme.shapes.bigCard

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, shape)
            .clip(shape)
            .background(colors.card)
            .clickable(onClick = onClick),
    ) {
        // ---- معرض الصور: صورة كبيرة + صورتان مصغّرتان فوق بعض
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.placeholder)
                .padding(12.dp)
                .height(152.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.weight(1.5f).fillMaxHeight().clip(AuctionTheme.shapes.thumb)) {
                AuctionImage(item.coverUrl, Modifier.fillMaxSize())
                AuctionStatusBadge(item, Modifier.align(Alignment.TopStart).padding(10.dp))
                if (!item.isEnded && (item.timeLeft != null || item.bidsCount > 0)) {
                    GlassPill(Modifier.align(Alignment.BottomCenter).padding(10.dp)) {
                        Icon(Icons.Filled.Gavel, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("${item.bidsCount} ${strings.bidsUnit}", color = Color.White, style = AuctionTheme.type.chip.copy(fontSize = 15.sp))
                        if (item.timeLeft != null) {
                            Box(Modifier.width(1.dp).height(18.dp).background(Color.White.copy(alpha = 0.4f)))
                            Icon(Icons.Filled.Schedule, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(item.timeLeft, color = Color.White, style = AuctionTheme.type.chip.copy(fontSize = 15.sp))
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(Modifier.weight(1f).fillMaxWidth().clip(AuctionTheme.shapes.thumb)) {
                    AuctionImage(item.imageUrls.getOrNull(1), Modifier.fillMaxSize())
                    if (onFavoriteClick != null) {
                        FavoriteButton(
                            isFavorite = item.isFavorite,
                            onClick = onFavoriteClick,
                            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                        )
                    }
                }
                Box(Modifier.weight(1f).fillMaxWidth().clip(AuctionTheme.shapes.thumb)) {
                    AuctionImage(item.imageUrls.getOrNull(2), Modifier.fillMaxSize())
                    val extra = item.imageUrls.size - 3
                    if (extra > 0) {
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                            Text("$extra+", color = Color.White, style = AuctionTheme.type.listTitle)
                        }
                    }
                }
            }
        }

        // ---- بيانات المزاد
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (item.categoryName != null) CategoryChip(item.categoryName)
            Text(item.title, color = colors.textPrimary, style = AuctionTheme.type.listTitle, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (item.subtitle != null) {
                Text(item.subtitle, color = colors.textMuted, style = AuctionTheme.type.body.copy(fontSize = 17.sp))
            }
            if (item.priceText != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (item.bidsCount == 0) strings.startingPrice else strings.currentBid,
                    color = colors.textMuted,
                    style = AuctionTheme.type.body.copy(fontSize = 14.sp),
                )
                Text(item.priceText, color = colors.priceGreen, style = AuctionTheme.type.price.copy(fontSize = 32.sp))
            }
        }
    }
}

@Composable
fun GlassPill(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier
            .background(GlassBlack, CircleShape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = null,
            tint = if (isFavorite) AuctionTheme.colors.live else Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}
