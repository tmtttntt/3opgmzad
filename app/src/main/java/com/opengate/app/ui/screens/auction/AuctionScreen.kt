package com.opengate.app.ui.screens.auction

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opengate.app.data.model.Auction
import com.opengate.app.util.PhoneUtils
import com.opengate.app.ui.screens.auction.design.ActionButton
import com.opengate.app.ui.screens.auction.design.AuctionImage
import com.opengate.app.ui.screens.auction.design.AuctionTheme
import com.opengate.app.ui.screens.auction.design.CategoryChip
import com.opengate.app.ui.screens.auction.design.ContactButton
import com.opengate.app.ui.screens.auction.design.LiveBadge
import com.opengate.app.ui.screens.auction.design.SoftCountPill
import com.opengate.app.ui.screens.auction.design.StatusTag

@Composable
fun AuctionScreen(viewModel: AuctionViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()

    AuctionTheme {
        val colors = AuctionTheme.colors
        val auction = state.auction
        when {
            state.loading -> Box(
                Modifier.fillMaxSize().background(colors.background),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = colors.priceGreen) }

            auction == null -> LoadFailed(state.error, onBack)

            else -> AuctionContent(
                state = state,
                auction = auction,
                onBack = onBack,
                onBidChange = viewModel::onBidInputChange,
                onPlaceBid = viewModel::placeBid,
            )
        }
    }
}

@Composable
private fun LoadFailed(error: String?, onBack: () -> Unit) {
    val colors = AuctionTheme.colors
    Column(
        Modifier.fillMaxSize().background(colors.background).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = error ?: "تعذّر تحميل المزاد",
            color = colors.badgeCrimson,
            style = AuctionTheme.type.body,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        ActionButton("رجوع", onBack, colors.priceGreen, Color.White)
    }
}

@Composable
private fun AuctionContent(
    state: AuctionUiState,
    auction: Auction,
    onBack: () -> Unit,
    onBidChange: (String) -> Unit,
    onPlaceBid: () -> Unit,
) {
    val colors = AuctionTheme.colors
    val strings = AuctionTheme.strings
    val context = LocalContext.current
    val info = state.info
    val currency = info?.currency

    val endsEpoch = parseEpochSeconds(auction.ends_at)
    val ended = auction.status == "ended" || auction.status == "cancelled" ||
        (endsEpoch != null && state.remainingSeconds <= 0 && endsEpoch <= System.currentTimeMillis() / 1000)
    val isLive = !ended && auction.status == "live"
    val canBid = isLive

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BidBar(
                state = state,
                currency = currency,
                canBid = canBid,
                onBidChange = onBidChange,
                onPlaceBid = onPlaceBid,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .verticalScroll(rememberScrollState()),
        ) {
            Gallery(
                imageUrls = info?.imageUrls.orEmpty(),
                isLive = isLive,
                ended = ended,
                onBack = onBack,
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (!info?.city.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = colors.priceGreen, modifier = Modifier.size(20.dp))
                        Text(info?.city.orEmpty(), color = colors.priceGreen, style = AuctionTheme.type.body.copy(fontSize = 16.sp))
                    }
                }
                val categoryName = info?.categories?.name_ar
                if (!categoryName.isNullOrBlank()) CategoryChip(categoryName)

                Text(
                    text = info?.title?.ifBlank { null } ?: "مزاد معلن",
                    color = colors.textPrimary,
                    style = AuctionTheme.type.listTitle.copy(fontSize = 28.sp, lineHeight = 38.sp),
                )

                PriceCard(
                    priceText = formatMoney(auction.current_price ?: auction.starting_price, currency),
                    hasBids = state.bids.isNotEmpty(),
                    countdown = formatCountdown(state.remainingSeconds, ended),
                    ended = ended,
                    minIncrementText = formatMoney(auction.min_increment, currency),
                )

                val phone = info?.contact_phone
                if (!phone.isNullOrBlank()) {
                    ContactButton(
                        text = strings.contactSeller,
                        onClick = {
                            PhoneUtils.whatsAppIntent(phone)?.let { runCatching { context.startActivity(it) } }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                val description = info?.description
                if (!description.isNullOrBlank()) {
                    val shape = RoundedCornerShape(18.dp)
                    Text(
                        text = description,
                        color = colors.textPrimary,
                        style = AuctionTheme.type.body,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.card, shape)
                            .border(1.5.dp, colors.textMuted.copy(alpha = 0.5f), shape)
                            .padding(18.dp),
                    )
                }

                BidsSection(state)
            }
        }
    }
}

// ---------------------------------------------------------------- gallery

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Gallery(imageUrls: List<String>, isLive: Boolean, ended: Boolean, onBack: () -> Unit) {
    val colors = AuctionTheme.colors
    val strings = AuctionTheme.strings
    val pageCount = maxOf(1, imageUrls.size)
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Box(Modifier.fillMaxWidth().height(320.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            AuctionImage(imageUrls.getOrNull(page), Modifier.fillMaxSize())
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Brush.verticalGradient(listOf(Color(0x99000000), Color.Transparent))),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.weight(1f))
            when {
                ended -> StatusTag(strings.ended, Color(0xFF7A7A7A), Color.White, Modifier.padding(end = 8.dp))
                isLive -> LiveBadge(Modifier.padding(end = 8.dp))
                else -> StatusTag(strings.upcoming, colors.gold, colors.priceGreen, Modifier.padding(end = 8.dp))
            }
        }
        if (imageUrls.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .background(Color(0x99000000), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text("${pagerState.currentPage + 1} / $pageCount", color = Color.White, style = AuctionTheme.type.chip.copy(fontSize = 16.sp))
            }
        }
    }
}

// ---------------------------------------------------------------- price card

@Composable
private fun PriceCard(
    priceText: String,
    hasBids: Boolean,
    countdown: String,
    ended: Boolean,
    minIncrementText: String,
) {
    val colors = AuctionTheme.colors
    val strings = AuctionTheme.strings
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AuctionTheme.shapes.card)
            .background(Brush.horizontalGradient(listOf(colors.gradientEnd, colors.gradientStart)))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = if (hasBids) strings.currentBid else strings.startingPrice,
            color = Color.White.copy(alpha = 0.75f),
            style = AuctionTheme.type.body.copy(fontSize = 14.sp),
        )
        Text(priceText, color = colors.gold, style = AuctionTheme.type.price.copy(fontSize = 36.sp, lineHeight = 44.sp))
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = if (ended) Color(0xFFFFB4B4) else Color.White,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = countdown,
                    color = if (ended) Color(0xFFFFB4B4) else Color.White,
                    style = AuctionTheme.type.chip.copy(fontSize = 15.sp),
                )
            }
            Text(
                text = "أقل زيادة: $minIncrementText",
                color = Color.White.copy(alpha = 0.75f),
                style = AuctionTheme.type.caption,
            )
        }
    }
}

// ---------------------------------------------------------------- live bids

@Composable
private fun BidsSection(state: AuctionUiState) {
    val colors = AuctionTheme.colors
    val strings = AuctionTheme.strings
    val currency = state.info?.currency

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(strings.bidsLive, color = colors.textPrimary, style = AuctionTheme.type.sectionTitle)
            if (state.bids.isNotEmpty()) SoftCountPill(state.bids.size)
        }
        if (state.bids.isEmpty()) {
            Text(strings.noBidsYet, color = colors.textMuted, style = AuctionTheme.type.body)
        }
        state.bids.forEachIndexed { index, bid ->
            val mine = bid.bidder_id == state.myUserId
            val shape = RoundedCornerShape(16.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (mine) colors.chipBackground else colors.card, shape)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier.size(40.dp).background(if (mine) colors.priceGreen else colors.chipBackground, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Gavel,
                        contentDescription = null,
                        tint = if (mine) Color.White else colors.priceGreen,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = if (mine) strings.you else strings.bidder,
                    color = colors.textPrimary,
                    style = AuctionTheme.type.cardTitle.copy(fontSize = 16.sp),
                    modifier = Modifier.weight(1f),
                )
                if (index == 0) StatusTag(strings.topBid, colors.gold, colors.priceGreen)
                Text(
                    text = formatMoney(bid.amount, currency),
                    color = colors.priceGreen,
                    style = AuctionTheme.type.priceSmall.copy(fontSize = 17.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ---------------------------------------------------------------- bid bar

@Composable
private fun BidBar(
    state: AuctionUiState,
    currency: String?,
    canBid: Boolean,
    onBidChange: (String) -> Unit,
    onPlaceBid: () -> Unit,
) {
    val colors = AuctionTheme.colors
    val strings = AuctionTheme.strings
    Surface(color = colors.card, shadowElevation = 16.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            state.error?.let {
                Text(it, color = colors.badgeCrimson, style = AuctionTheme.type.caption, modifier = Modifier.padding(bottom = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.bidInput,
                    onValueChange = onBidChange,
                    enabled = canBid,
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "${strings.minNextBid}: ${formatMoney(state.minNextBid, currency)}",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.priceGreen,
                        unfocusedBorderColor = colors.textMuted.copy(alpha = 0.5f),
                    ),
                    modifier = Modifier.weight(1f),
                )
                ActionButton(
                    text = if (state.placingBid) "..." else strings.bidAction,
                    onClick = { if (canBid && !state.placingBid) onPlaceBid() },
                    container = if (canBid) colors.gold else colors.placeholder,
                    content = colors.priceGreen,
                    icon = Icons.Filled.Gavel,
                    height = 56.dp,
                )
            }
        }
    }
}

private fun formatCountdown(totalSeconds: Long, ended: Boolean): String {
    if (ended || totalSeconds <= 0) return "انتهى المزاد"
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "متبقٍ: %02d:%02d:%02d".format(h, m, s) else "متبقٍ: %02d:%02d".format(m, s)
}
