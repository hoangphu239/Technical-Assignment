package com.phule.assignmenttest.presentation.home

import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.phule.assignmenttest.R
import com.phule.assignmenttest.common.PHONE_NUM_OF_COLUMNS
import com.phule.assignmenttest.common.TABLET_NUM_OF_COLUMNS
import com.phule.assignmenttest.common.TABLET_SCREEN_WIDTH_DP
import com.phule.assignmenttest.common.TagPosition
import com.phule.assignmenttest.data.remote.model.Image
import com.phule.assignmenttest.data.remote.model.Video
import com.phule.assignmenttest.domain.use_case.PAGE_SIZE
import com.phule.assignmenttest.presentation.utils.LoadingShimmerEffect
import com.phule.assignmenttest.presentation.utils.PlayerListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val lazyGridState = rememberLazyStaggeredGridState()
    val pullToRefreshState = rememberPullToRefreshState()
    val phoneAspectRatios = remember { listOf(0.8f, 1f, 1.2f, 1f) }
    val tabletAspectRatios = remember { listOf(0.6f, 0.8f, 1.2f, 1.4f, 1.2f, 1f) }
    var isRefreshing by rememberSaveable { mutableStateOf(false) }

    val columnCount =
        if (LocalConfiguration.current.screenWidthDp >= TABLET_SCREEN_WIDTH_DP) TABLET_NUM_OF_COLUMNS else PHONE_NUM_OF_COLUMNS
    val dataState = viewModel.dataState.collectAsLazyPagingItems()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        LazyVerticalStaggeredGrid(
            state = lazyGridState,
            columns = StaggeredGridCells.Fixed(columnCount),
            verticalItemSpacing = 10.dp,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(
                count = dataState.itemCount,
                key = { index ->
                    dataState[index]?.id ?: index
                },
                span = { index ->
                    val resourceItem = dataState[index]
                    if (resourceItem?.video != null) {
                        StaggeredGridItemSpan.FullLine
                    } else {
                        StaggeredGridItemSpan.SingleLane
                    }
                }
            ) { index ->
                val resourceItem = dataState[index]
                if (resourceItem != null) {
                    val video = resourceItem.video
                    val image = resourceItem.image

                    if (video != null) {
                        val indexPaging = index / PAGE_SIZE
                        val isVisible by remember {
                            derivedStateOf {
                                val layoutInfo = lazyGridState.layoutInfo
                                val visibleItems = layoutInfo.visibleItemsInfo
                                visibleItems.any { it.index == index }
                            }
                        }
                        VideoItem(
                            viewModel = viewModel,
                            index = indexPaging,
                            video = video,
                            isVisible = isVisible,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    } else if (image != null) {
                        val indexInPage = (index - 1) % PAGE_SIZE
                        ImageItem(
                            image,
                            if (columnCount == TABLET_NUM_OF_COLUMNS) tabletAspectRatios[indexInPage % tabletAspectRatios.size]
                            else phoneAspectRatios[indexInPage % phoneAspectRatios.size]
                        )
                    }
                }
            }
        }

        when (dataState.loadState.refresh) {
            is LoadState.Loading -> LoadingItem(columnCount, phoneAspectRatios, tabletAspectRatios)
            is LoadState.NotLoading -> Unit
            is LoadState.Error -> ErrorItem()
        }

        LaunchedEffect(pullToRefreshState.isRefreshing) {
            if (pullToRefreshState.isRefreshing) {
                isRefreshing = true
                viewModel.refreshLocalData()
                isRefreshing = false
                pullToRefreshState.endRefresh()
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> viewModel.pausePlayer()
                Lifecycle.Event.ON_START -> viewModel.resumePlayer()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoItem(
    viewModel: HomeViewModel,
    index: Int,
    video: Video,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember(video.id) { viewModel.getSharedPlayer(video, index) }
    val playerView = rememberPlayerView(exoPlayer)

    LaunchedEffect(video.id, isVisible) {
        exoPlayer.playWhenReady = isVisible
    }

    Box(
        modifier = modifier
            .aspectRatio(16 / 9f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { context ->
                ComposeView(context).apply {
                    setContent {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AndroidView(
                                factory = { playerView },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    PlayerListener(
        player = exoPlayer
    ) { event ->
        when (event) {
            Player.EVENT_RENDERED_FIRST_FRAME -> {
                playerView.hideController()
            }

            Player.EVENT_PLAYER_ERROR -> {
                Toast.makeText(context, "Please check your internet connection", Toast.LENGTH_LONG)
                    .show()
            }

            Player.EVENT_PLAYBACK_STATE_CHANGED -> {
                playerView.hideController()
            }
        }
    }

    DisposableEffect(video.id) {
        onDispose { viewModel.savePosition(video.id, exoPlayer.currentPosition) }
    }
}

@Composable
fun ImageItem(image: Image, aspectRatio: Float) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .aspectRatio(1f / aspectRatio)
    ) {
        AsyncImage(
            model = image.url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (image.tag_align != null && image.price != null) {
            PriceItem(image)
        }
    }
}

@Composable
fun PriceItem(image: Image) {
    var showPrice by rememberSaveable { mutableStateOf(false) }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (tag, price) = createRefs()
        val tagModifier = Modifier.constrainAs(tag) {
            when (image.tag_align) {
                TagPosition.TOP_LEFT.value -> {
                    top.linkTo(parent.top, margin = 5.dp)
                    start.linkTo(parent.start, margin = 5.dp)
                }

                TagPosition.TOP_RIGHT.value -> {
                    top.linkTo(parent.top, margin = 5.dp)
                    end.linkTo(parent.end, margin = 5.dp)
                }

                TagPosition.BOTTOM_LEFT.value -> {
                    bottom.linkTo(parent.bottom, margin = 5.dp)
                    start.linkTo(parent.start, margin = 5.dp)
                }

                TagPosition.BOTTOM_RIGHT.value -> {
                    bottom.linkTo(parent.bottom, margin = 5.dp)
                    end.linkTo(parent.end, margin = 5.dp)
                }
            }
        }

        Image(
            painter = painterResource(id = R.drawable.ic_tag),
            contentDescription = "price product",
            modifier = tagModifier
                .size(24.dp)
                .clickable { showPrice = !showPrice },
            contentScale = ContentScale.Crop
        )

        if (showPrice) {
            val priceModifier = Modifier.constrainAs(price) {
                when (image.tag_align) {
                    TagPosition.TOP_LEFT.value -> {
                        top.linkTo(tag.bottom, margin = 5.dp)
                        start.linkTo(tag.start)
                    }

                    TagPosition.TOP_RIGHT.value -> {
                        top.linkTo(tag.bottom, margin = 5.dp)
                        end.linkTo(tag.end)
                    }

                    TagPosition.BOTTOM_LEFT.value -> {
                        bottom.linkTo(tag.top, margin = 5.dp)
                        start.linkTo(tag.start)
                    }

                    TagPosition.BOTTOM_RIGHT.value -> {
                        bottom.linkTo(tag.top, margin = 5.dp)
                        end.linkTo(tag.end)
                    }
                }
            }

            Box(
                modifier = priceModifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$${image.price}",
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LoadingItem(
    columnCount: Int = PHONE_NUM_OF_COLUMNS,
    phoneAspectRatios: List<Float>,
    tabletAspectRatios: List<Float>
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columnCount),
        verticalItemSpacing = 10.dp,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(
            count = 11,
            span = { index ->
                if (index == 0) StaggeredGridItemSpan.FullLine
                else StaggeredGridItemSpan.SingleLane
            }
        ) { index ->
            LoadingShimmerEffect(
                index = index,
                aspectRatio = if (columnCount == TABLET_NUM_OF_COLUMNS) tabletAspectRatios[index % tabletAspectRatios.size]
                else phoneAspectRatios[index % phoneAspectRatios.size]
            )
        }
    }
}

@Composable
fun ErrorItem() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = "An error occurred. Please try again!",
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
    }
}

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun rememberPlayerView(player: Player): PlayerView {
    val context = LocalContext.current
    val playerView = remember {
        PlayerView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
            this.player = player
        }
    }
    DisposableEffect(key1 = player) {
        playerView.player = player
        onDispose {
            playerView.player = null
        }
    }
    return playerView
}