package com.phule.assignmenttest.presentation.home

import android.view.ViewGroup
import android.widget.FrameLayout
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
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
import androidx.media3.ui.PlayerView
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.phule.assignmenttest.R
import com.phule.assignmenttest.common.TagPosition
import com.phule.assignmenttest.data.remote.Image
import com.phule.assignmenttest.data.remote.Video
import com.phule.assignmenttest.domain.use_case.PAGE_SIZE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val lazyGirdState = rememberLazyStaggeredGridState()
    val pullToRefreshState = rememberPullToRefreshState()
    val phoneAspectRatios = remember { listOf(0.8f, 1f, 1.2f, 1f) }
    val tabletAspectRatios = remember { listOf(0.6f, 0.8f, 1.2f, 1.4f, 1.2f, 1f) }
    var isRefreshing by rememberSaveable { mutableStateOf(false) }

    var columnCount = 2
    var isTabletDevice = false
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    if (screenWidthDp >= 600) {
        isTabletDevice = true
        columnCount = 3
    }

    val dataState = viewModel.dataState.collectAsLazyPagingItems()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        LazyVerticalStaggeredGrid(
            state = lazyGirdState,
            columns = StaggeredGridCells.Fixed(columnCount),
            verticalItemSpacing = 10.dp,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val loadState = dataState.loadState.mediator
            if (loadState?.refresh == LoadState.Loading) {
                item { LoadingItem() }
            }
            if (loadState?.append == LoadState.Loading) {
                item { LoadingItem() }
            }
            if (loadState?.refresh is LoadState.Error || loadState?.append is LoadState.Error) {
                item {
                    val isPaginatingError =
                        (loadState.append is LoadState.Error) || dataState.itemCount > 1
                    val error = if (loadState.append is LoadState.Error)
                        (loadState.append as LoadState.Error).error
                    else
                        (loadState.refresh as LoadState.Error).error
                    ErrorItem(isPaginatingError, error)
                }
            } else {
                items(
                    count = dataState.itemCount,
                    key = { index -> dataState[index]?.id ?: index },
                    span = { index ->
                        val resourceItem = dataState[index]
                        if (resourceItem?.videos != null) {
                            StaggeredGridItemSpan.FullLine
                        } else {
                            StaggeredGridItemSpan.SingleLane
                        }
                    }
                ) { index ->
                    val isVisible by produceState(initialValue = false, lazyGirdState) {
                        snapshotFlow { lazyGirdState.layoutInfo.visibleItemsInfo.any { it.index == index } }
                            .collect { value = it }
                    }

                    val resourceItem = dataState[index]
                    resourceItem?.videos?.let { video ->
                        VideoItem(
                            viewModel = viewModel,
                            video = video,
                            isVisible = isVisible,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    resourceItem?.images?.let { image ->
                        val indexPaging = (index - (index / PAGE_SIZE + 1)) % PAGE_SIZE
                        val aspectRatio =
                            if (isTabletDevice) tabletAspectRatios[indexPaging % tabletAspectRatios.size]
                            else phoneAspectRatios[indexPaging % phoneAspectRatios.size]
                        ImageItem(
                            image = image,
                            aspectRatio = aspectRatio
                        )
                    }
                }
            }
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
                Lifecycle.Event.ON_STOP -> viewModel.pauseAllPlayers()
                Lifecycle.Event.ON_START -> viewModel.resumeAllPlayers()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun VideoItem(
    viewModel: HomeViewModel,
    video: Video,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember { viewModel.getPlayer(context, video) }

    LaunchedEffect(isVisible) {
        exoPlayer.playWhenReady = isVisible
    }

    Card(
        colors = CardDefaults.cardColors(Color.Black),
        modifier = modifier
            .aspectRatio(16 / 9f)
            .clip(RoundedCornerShape(8.dp)),
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    DisposableEffect(video.id) {
        onDispose {
            viewModel.savePosition(video.id, exoPlayer.currentPosition)
            viewModel.releasePlayer(video.id)
        }
    }
}

@Composable
fun ImageItem(
    image: Image,
    aspectRatio: Float
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .wrapContentSize()
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
fun LoadingItem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ErrorItem(isPaginatingError: Boolean, error: Throwable) {
    val modifier = if (isPaginatingError) {
        Modifier.padding(8.dp)
    } else {
        Modifier.fillMaxSize()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!isPaginatingError) {
            Icon(
                modifier = Modifier
                    .size(64.dp),
                imageVector = Icons.Rounded.Warning, contentDescription = null
            )
        }
        Text(
            text = error.message ?: error.toString(),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
    }
}



