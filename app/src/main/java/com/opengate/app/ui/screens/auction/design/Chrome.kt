package com.opengate.app.ui.screens.auction.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ================================================================ header

@Composable
fun DefaultAuctionLogo() {
    Icon(
        imageVector = Icons.Filled.Gavel,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(30.dp),
    )
}

/**
 * الشريط العلوي الأخضر الداكن: زر رجوع اختياري + الشعار والاسم على جهة البداية،
 * وأزرار الإجراءات (actions) على جهة النهاية، ثم حقل بحث شفاف اختياري تحتها.
 */
@Composable
fun AuctionHeader(
    brandName: String,
    brandTagline: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    logo: @Composable () -> Unit = { DefaultAuctionLogo() },
    actions: @Composable RowScope.() -> Unit = {},
    searchQuery: String? = null,
    onSearchChange: ((String) -> Unit)? = null,
) {
    val colors = AuctionTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.headerDark)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                HeaderIcon(Icons.AutoMirrored.Filled.ArrowBack, onBack)
            } else {
                Spacer(Modifier.width(8.dp))
            }
            Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) { logo() }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = brandName,
                    color = Color.White,
                    style = AuctionTheme.type.cardTitle.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                )
                Text(
                    text = brandTagline,
                    color = Color.White.copy(alpha = 0.8f),
                    style = AuctionTheme.type.nav.copy(fontSize = 10.sp, letterSpacing = 0.8.sp),
                )
            }
            Spacer(Modifier.weight(1f))
            actions()
        }
        if (searchQuery != null && onSearchChange != null) {
            AuctionSearchField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            )
        }
    }
}

@Composable
fun HeaderIcon(icon: ImageVector, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
    }
}

/** حقل بحث حقيقي (يكتب فيه المستخدم) بخلفية بيضاء شفافة فوق الأخضر الداكن. */
@Composable
fun AuctionSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(
                    text = AuctionTheme.strings.searchHint,
                    color = Color.White.copy(alpha = 0.6f),
                    style = AuctionTheme.type.body,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = AuctionTheme.type.body.copy(color = Color.White),
                cursorBrush = SolidColor(Color.White),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                Icons.Filled.Close,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(22.dp).clip(CircleShape).clickable { onValueChange("") },
            )
        }
    }
}

// ================================================================ bottom bar

data class AuctionBarItem(
    val icon: ImageVector,
    val label: String,
    val selected: Boolean = false,
    val onClick: () -> Unit,
)

/**
 * شريط سفلي أخضر مدوّر الزوايا العلوية مع زر إضافة ذهبي بارز في المنتصف.
 * [items]: أول عنصرين قبل زر الإضافة والباقي بعده (الترتيب المنطقي من البداية للنهاية).
 */
@Composable
fun AuctionBottomBar(
    items: List<AuctionBarItem>,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AuctionTheme.colors
    val strings = AuctionTheme.strings
    val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val before = items.take(2)
    val after = items.drop(2)

    Box(modifier.fillMaxWidth().height(92.dp + navInset)) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(76.dp + navInset),
            shape = AuctionTheme.shapes.barTop,
            color = colors.surfaceGreen,
            shadowElevation = 12.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, end = 10.dp, bottom = navInset),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                before.forEach { NavItem(it) }
                Spacer(Modifier.width(84.dp))
                after.forEach { NavItem(it) }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp + navInset),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(6.dp, CircleShape)
                    .background(colors.gold, CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, contentDescription = strings.navAdd, tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Text(
                text = strings.navAdd,
                color = colors.gold,
                style = AuctionTheme.type.nav.copy(fontSize = 13.sp),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun NavItem(item: AuctionBarItem) {
    val colors = AuctionTheme.colors
    val tint = if (item.selected) colors.gold else colors.navInactive
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (item.selected) Color.White.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = item.onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(item.icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
        Text(item.label, color = tint, style = AuctionTheme.type.nav)
    }
}

// ================================================================ support FAB

@Composable
fun SupportFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .size(56.dp)
            .shadow(6.dp, shape)
            .clip(shape)
            .background(AuctionTheme.colors.surfaceGreen)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
    }
}
