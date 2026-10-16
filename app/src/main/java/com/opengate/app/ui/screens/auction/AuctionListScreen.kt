package com.opengate.app.ui.screens.auction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opengate.app.data.favorites.FavoritesStore
import com.opengate.app.ui.screens.auction.design.ActionButton
import com.opengate.app.ui.screens.auction.design.AddItemBanner
import com.opengate.app.ui.screens.auction.design.AuctionBarItem
import com.opengate.app.ui.screens.auction.design.AuctionBottomBar
import com.opengate.app.ui.screens.auction.design.AuctionCompactCard
import com.opengate.app.ui.screens.auction.design.AuctionHeader
import com.opengate.app.ui.screens.auction.design.AuctionListCard
import com.opengate.app.ui.screens.auction.design.AuctionTheme
import com.opengate.app.ui.screens.auction.design.CategoryMosaic
import com.opengate.app.ui.screens.auction.design.HeaderIcon
import com.opengate.app.ui.screens.auction.design.HeroBannerPager
import com.opengate.app.ui.screens.auction.design.SectionHeader
import kotlinx.coroutines.delay

@Composable
fun AuctionListScreen(
    viewModel: AuctionListViewModel,
    onBack: () -> Unit,
    onOpenAuction: (String) -> Unit,
    onOpenFavorites: () -> Unit = {},
    onOpenChats: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val favoriteIds by FavoritesStore.favoriteIds.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    // يُحدَّث الوقت المتبقي كل 30 ثانية دون إعادة تحميل من الخادم
    val nowSeconds by produceState(System.currentTimeMillis() / 1000) {
        while (true) {
            delay(30_000)
            value = System.currentTimeMillis() / 1000
        }
    }

    val auctionItems = remember(state.auctions, nowSeconds, favoriteIds) {
        state.auctions.map { it.toItemUi(nowSeconds, favoriteIds) }
    }
    val categories = remember(auctionItems) { buildCategories(auctionItems) }
    val hero = remember(auctionItems) { buildHero(auctionItems) }
    val showCategories = categories.size >= 2
    val sections = remember(auctionItems, categories) { if (showCategories) buildSections(auctionItems, categories) else emptyList() }

    val query = state.query.trim()
    val selectedCategory = categories.firstOrNull { it.id == state.selectedCategoryId }
    val filtering = query.isNotEmpty() || selectedCategory != null
    val filtered = remember(auctionItems, query, state.selectedCategoryId) {
        auctionItems.filter { item ->
            (state.selectedCategoryId == null || item.categoryId == state.selectedCategoryId) &&
                (query.isEmpty() ||
                    item.title.contains(query, ignoreCase = true) ||
                    (item.categoryName?.contains(query, ignoreCase = true) == true) ||
                    (item.location?.contains(query, ignoreCase = true) == true))
        }
    }

    AuctionTheme {
        val colors = AuctionTheme.colors
        val strings = AuctionTheme.strings

        Scaffold(
            containerColor = colors.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                AuctionHeader(
                    brandName = "المزادات المباشرة",
                    brandTagline = "OPEN GATE · LIVE AUCTIONS",
                    onBack = onBack,
                    actions = { HeaderIcon(Icons.Filled.Refresh, viewModel::loadAuctions) },
                    searchQuery = state.query,
                    onSearchChange = viewModel::onQueryChange,
                )
            },
            bottomBar = {
                AuctionBottomBar(
                    items = listOf(
                        AuctionBarItem(Icons.Filled.Home, strings.navHome, onClick = onBack),
                        AuctionBarItem(Icons.Filled.Favorite, "المفضلة", onClick = onOpenFavorites),
                        AuctionBarItem(Icons.Filled.ChatBubble, "المحادثات", onClick = onOpenChats),
                        AuctionBarItem(Icons.Filled.AccountCircle, strings.navAccount, onClick = onOpenSettings),
                    ),
                    onAddClick = { showCreateDialog = true },
                )
            },
        ) { padding ->
            when {
                state.loading && state.auctions.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = colors.priceGreen) }

                state.auctions.isEmpty() -> EmptyState(
                    errorText = state.error,
                    onStart = { showCreateDialog = true },
                    modifier = Modifier.fillMaxSize().padding(padding),
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding() + 16.dp,
                        bottom = padding.calculateBottomPadding() + 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    state.error?.let { message ->
                        item(key = "error") {
                            Text(
                                text = message,
                                color = colors.badgeCrimson,
                                style = AuctionTheme.type.caption,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth()
                                    .background(androidx.compose.ui.graphics.Color(0xFFFBE4E8), RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                            )
                        }
                    }

                    if (!filtering) {
                        if (hero.isNotEmpty()) {
                            item(key = "hero") { HeroBannerPager(hero, onClick = { onOpenAuction(it.id) }) }
                        }
                        item(key = "add-item") {
                            AddItemBanner(onClick = { showCreateDialog = true }, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                        if (showCategories) {
                            item(key = "categories") {
                                CategoryMosaic(
                                    categories = categories,
                                    onClick = { viewModel.selectCategory(it.id) },
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        }
                        items(sections, key = { "section-${it.id}" }) { section ->
                            Column {
                                SectionHeader(
                                    title = section.title,
                                    count = section.count,
                                    onViewAll = { viewModel.selectCategory(section.id) },
                                )
                                Spacer(Modifier.height(12.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(section.items, key = { it.id }) { item ->
                                        AuctionCompactCard(item = item, onClick = { onOpenAuction(item.id) })
                                    }
                                }
                            }
                        }
                        item(key = "all-title") {
                            Text(
                                text = strings.allAuctions,
                                color = colors.textPrimary,
                                style = AuctionTheme.type.sectionTitle,
                                modifier = Modifier.padding(horizontal = 20.dp),
                            )
                        }
                        items(auctionItems, key = { "auction-${it.id}" }) { item ->
                            AuctionListCard(
                                item = item,
                                onClick = { onOpenAuction(item.id) },
                                onFavoriteClick = { FavoritesStore.toggleFavorite(item.listingId) },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    } else {
                        item(key = "filter-bar") {
                            FilterBar(
                                title = selectedCategory?.title ?: strings.searchResults,
                                count = filtered.size,
                                onClear = {
                                    viewModel.onQueryChange("")
                                    viewModel.selectCategory(null)
                                },
                            )
                        }
                        if (filtered.isEmpty()) {
                            item(key = "no-results") {
                                Text(
                                    text = "لا توجد مزادات مطابقة",
                                    color = colors.textMuted,
                                    style = AuctionTheme.type.body,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                )
                            }
                        }
                        items(filtered, key = { "result-${it.id}" }) { item ->
                            AuctionListCard(
                                item = item,
                                onClick = { onOpenAuction(item.id) },
                                onFavoriteClick = { FavoritesStore.toggleFavorite(item.listingId) },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateAuctionDialog(
                creating = state.creating,
                onDismiss = { showCreateDialog = false },
                onSubmit = { title, startPrice, minInc, hours, city, phone ->
                    viewModel.startAuction(
                        title = title,
                        startingPrice = startPrice,
                        minIncrement = minInc,
                        durationHours = hours,
                        city = city,
                        contactPhone = phone,
                        onSuccess = { newId ->
                            showCreateDialog = false
                            onOpenAuction(newId)
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun FilterBar(title: String, count: Int, onClear: () -> Unit) {
    val colors = AuctionTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$title ($count)",
            color = colors.textPrimary,
            style = AuctionTheme.type.sectionTitle,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(colors.chipBackground)
                .clickable(onClick = onClear)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Filled.Close, contentDescription = null, tint = colors.priceGreen, modifier = Modifier.size(16.dp))
            Text("إلغاء الفلترة", color = colors.priceGreen, style = AuctionTheme.type.chip)
        }
    }
}

@Composable
private fun EmptyState(errorText: String?, onStart: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AuctionTheme.colors
    val strings = AuctionTheme.strings
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(96.dp).background(colors.chipBackground, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Gavel, contentDescription = null, tint = colors.priceGreen, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(strings.emptyTitle, color = colors.textPrimary, style = AuctionTheme.type.sectionTitle, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            text = errorText ?: strings.emptyBody,
            color = if (errorText != null) colors.badgeCrimson else colors.textMuted,
            style = AuctionTheme.type.body.copy(fontSize = 15.sp),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        ActionButton(
            text = "ابدأ أول مزاد",
            onClick = onStart,
            container = colors.gold,
            content = colors.priceGreen,
            icon = Icons.Filled.Gavel,
        )
    }
}
