package com.opengate.app.ui.screens.auction.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage

// ---------------------------------------------------------------- images

@Composable
fun AuctionImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    if (url.isNullOrBlank()) {
        ImagePlaceholder(modifier)
        return
    }
    SubcomposeAsyncImage(
        model = com.opengate.app.ui.components.parseImageModel(url),
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale,
        loading = { Box(Modifier.fillMaxSize().background(AuctionTheme.colors.placeholder)) },
        error = { ImagePlaceholder(Modifier.fillMaxSize()) },
    )
}

@Composable
private fun ImagePlaceholder(modifier: Modifier) {
    Box(modifier.background(AuctionTheme.colors.placeholder), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Filled.HideImage,
            contentDescription = null,
            tint = Color(0xFF9E9E9E),
            modifier = Modifier.size(40.dp),
        )
    }
}

// ---------------------------------------------------------------- badges

@Composable
fun LiveBadge(modifier: Modifier = Modifier) {
    val colors = AuctionTheme.colors
    Row(
        modifier = modifier
            .background(colors.live, CircleShape)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(AuctionTheme.strings.live, color = Color.White, style = AuctionTheme.type.chip)
        Box(Modifier.size(7.dp).background(Color.White, CircleShape))
    }
}

/** Crimson count badge shown on category tiles. */
@Composable
fun CountBadge(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 28.dp, minHeight = 28.dp)
            .background(AuctionTheme.colors.badgeCrimson, CircleShape)
            .border(1.5.dp, Color(0xFFF0D9C3), CircleShape)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(count.toString(), color = Color.White, style = AuctionTheme.type.chip)
    }
}

/** Small count pill used next to "view all". */
@Composable
fun SoftCountPill(count: Int, modifier: Modifier = Modifier) {
    val colors = AuctionTheme.colors
    Box(
        modifier = modifier
            .background(colors.gold.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(count.toString(), color = colors.goldDeep, style = AuctionTheme.type.chip.copy(fontSize = 15.sp))
    }
}

/** Rounded-rectangle tag: FEATURED, direct sale, ended… */
@Composable
fun StatusTag(
    text: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .background(container, AuctionTheme.shapes.chip)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(15.dp))
        Text(text, color = content, style = AuctionTheme.type.chip)
    }
}

/** Light green category label above the title on list cards. */
@Composable
fun CategoryChip(text: String, modifier: Modifier = Modifier) {
    val colors = AuctionTheme.colors
    Text(
        text = text,
        color = colors.priceGreen,
        style = AuctionTheme.type.caption,
        modifier = modifier
            .background(colors.chipBackground, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

// ---------------------------------------------------------------- buttons

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    height: androidx.compose.ui.unit.Dp = 52.dp,
) {
    Row(
        modifier = modifier
            .height(height)
            .clip(AuctionTheme.shapes.button)
            .background(container)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
        Text(text, color = content, style = AuctionTheme.type.chip.copy(fontSize = 17.sp))
    }
}

/** Green "contact seller" button (WhatsApp-style). */
@Composable
fun ContactButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 52.dp,
) {
    ActionButton(
        text = text,
        onClick = onClick,
        container = AuctionTheme.colors.whatsapp,
        content = Color.White,
        modifier = modifier,
        icon = Icons.Filled.ChatBubble,
        height = height,
    )
}
