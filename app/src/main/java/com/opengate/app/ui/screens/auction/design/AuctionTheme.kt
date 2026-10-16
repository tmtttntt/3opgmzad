package com.opengate.app.ui.screens.auction.design

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Suggested font: Cairo (free, SIL OFL) – put cairo_regular / medium / bold / extrabold
 * in res/font and replace the line below, e.g.:
 *
 * val AuctionFontFamily = FontFamily(
 *     Font(R.font.cairo_regular, FontWeight.Normal),
 *     Font(R.font.cairo_medium, FontWeight.Medium),
 *     Font(R.font.cairo_bold, FontWeight.Bold),
 *     Font(R.font.cairo_extrabold, FontWeight.ExtraBold),
 * )
 */
val AuctionFontFamily: FontFamily = FontFamily.Default

// ---------------------------------------------------------------- colors

@Immutable
data class AuctionColors(
    val headerDark: Color = Color(0xFF0F3B33),      // top bar
    val surfaceGreen: Color = Color(0xFF1B4F47),    // bottom bar, drawer header, support FAB
    val gradientStart: Color = Color(0xFF1F5A4E),   // "add item" banner
    val gradientEnd: Color = Color(0xFF3F7D6F),
    val gold: Color = Color(0xFFD4A574),            // accent: active tab, add button, featured
    val goldDeep: Color = Color(0xFFB98A57),
    val background: Color = Color(0xFFF8F9FB),
    val card: Color = Color.White,
    val live: Color = Color(0xFFEF4444),
    val badgeCrimson: Color = Color(0xFF9B1C31),    // category count badge
    val priceGreen: Color = Color(0xFF0F4A40),
    val textPrimary: Color = Color(0xFF14181B),
    val textMuted: Color = Color(0xFF8E9AAF),
    val chipBackground: Color = Color(0xFFE6EFEC),
    val placeholder: Color = Color(0xFFE5E5E5),
    val whatsapp: Color = Color(0xFF25D366),
    val navInactive: Color = Color(0xFF9DB8B1),
)

// ---------------------------------------------------------------- shapes

@Immutable
data class AuctionShapes(
    val card: Shape = RoundedCornerShape(24.dp),
    val bigCard: Shape = RoundedCornerShape(28.dp),
    val tile: Shape = RoundedCornerShape(20.dp),
    val thumb: Shape = RoundedCornerShape(20.dp),
    val chip: Shape = RoundedCornerShape(12.dp),
    val button: Shape = RoundedCornerShape(18.dp),
    val barTop: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    val pill: Shape = CircleShape,
)

// ---------------------------------------------------------------- typography

@Immutable
data class AuctionType(
    val sectionTitle: TextStyle,
    val cardTitle: TextStyle,
    val listTitle: TextStyle,
    val price: TextStyle,
    val priceSmall: TextStyle,
    val body: TextStyle,
    val caption: TextStyle,
    val chip: TextStyle,
    val nav: TextStyle,
)

fun auctionType(family: FontFamily): AuctionType = AuctionType(
    sectionTitle = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 28.sp),
    cardTitle = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 21.sp),
    listTitle = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 32.sp),
    price = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp),
    priceSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 26.sp),
    body = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 24.sp),
    caption = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp),
    chip = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 16.sp),
    nav = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp),
)

// ---------------------------------------------------------------- strings
// Defaults are Arabic. Feed these from stringResource(...) in your app so the
// existing language toggle keeps working.

@Immutable
data class AuctionStrings(
    val searchHint: String = "ابحث عن مزاد...",
    val addItemTitle: String = "ابدأ مزاداً جديداً",
    val addItemSubtitle: String = "اعرض سلعتك للمزايدة المباشرة بين المشترين",
    val viewAll: String = "عرض الكل",
    val allAuctions: String = "جميع المزادات",
    val noBids: String = "لا توجد مزايدات بعد",
    val currentBid: String = "المزايدة الحالية",
    val bidsUnit: String = "مزايدات",
    val live: String = "LIVE",
    val upcoming: String = "قادم",
    val bidsLive: String = "المزايدات الحية",
    val noBidsYet: String = "كن أول من يزايد على هذه السلعة",
    val startingPrice: String = "سعر البداية",
    val minNextBid: String = "أقل مزايدة مقبولة",
    val you: String = "أنت",
    val bidder: String = "مزايد",
    val topBid: String = "الأعلى",
    val bidAction: String = "زايد",
    val auctionEnded: String = "انتهى المزاد",
    val searchResults: String = "نتائج البحث",
    val emptyTitle: String = "لا توجد مزادات حالياً",
    val emptyBody: String = "كن أول من يطلق مزاداً مباشراً الآن",
    val refresh: String = "تحديث",
    val directSale: String = "بيع مباشر",
    val featured: String = "مميز",
    val ended: String = "انتهى",
    val contactSeller: String = "تواصل مع البائع",
    val bidNow: String = "زايد الآن",
    val navHome: String = "الرئيسية",
    val navWallet: String = "محفظتي",
    val navMyAuctions: String = "مزاداتي",
    val navAccount: String = "حسابي",
    val navAdd: String = "إضافة مزاد",
)

// ---------------------------------------------------------------- locals

private val LocalAuctionColors = staticCompositionLocalOf { AuctionColors() }
private val LocalAuctionShapes = staticCompositionLocalOf { AuctionShapes() }
private val LocalAuctionType = staticCompositionLocalOf { auctionType(AuctionFontFamily) }
private val LocalAuctionStrings = staticCompositionLocalOf { AuctionStrings() }

object AuctionTheme {
    val colors: AuctionColors
        @Composable @ReadOnlyComposable get() = LocalAuctionColors.current
    val shapes: AuctionShapes
        @Composable @ReadOnlyComposable get() = LocalAuctionShapes.current
    val type: AuctionType
        @Composable @ReadOnlyComposable get() = LocalAuctionType.current
    val strings: AuctionStrings
        @Composable @ReadOnlyComposable get() = LocalAuctionStrings.current
}

/**
 * لفّ شاشات منصة المزادات فقط بهذا الثيم؛ بقية المنصات تبقى بثيم OpenGateTheme دون أي تغيير.
 * الاتجاه الافتراضي RTL لأن نصوص المنصة عربية بالكامل.
 */
@Composable
fun AuctionTheme(
    fontFamily: FontFamily = AuctionFontFamily,
    colors: AuctionColors = AuctionColors(),
    strings: AuctionStrings = AuctionStrings(),
    layoutDirection: LayoutDirection = LayoutDirection.Rtl,
    content: @Composable () -> Unit,
) {
    val type = remember(fontFamily) { auctionType(fontFamily) }
    CompositionLocalProvider(
        LocalAuctionColors provides colors,
        LocalAuctionShapes provides AuctionShapes(),
        LocalAuctionType provides type,
        LocalAuctionStrings provides strings,
        LocalLayoutDirection provides layoutDirection,
    ) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = colors.priceGreen,
                onPrimary = Color.White,
                background = colors.background,
                onBackground = colors.textPrimary,
                surface = colors.card,
                onSurface = colors.textPrimary,
                secondary = colors.gold,
                primaryContainer = colors.chipBackground,
                onPrimaryContainer = colors.priceGreen,
                surfaceVariant = colors.chipBackground,
                onSurfaceVariant = colors.textMuted,
                outline = colors.textMuted,
                error = colors.badgeCrimson,
                errorContainer = Color(0xFFFBE4E8),
                onErrorContainer = colors.badgeCrimson,
            ),
            content = content,
        )
    }
}
