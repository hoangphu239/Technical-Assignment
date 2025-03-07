import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phule.assignmenttest.common.DEFAULT_PAGE
import com.phule.assignmenttest.common.FIRST_PAGE
import com.phule.assignmenttest.common.TOTAL_PAGES
import com.phule.assignmenttest.domain.model.Content
import com.phule.assignmenttest.domain.repository.Repository
import com.phule.assignmenttest.presentation.utils.AppUtils
import com.phule.assignmenttest.presentation.utils.PrefsUtil
import java.util.UUID
import javax.inject.Inject


class ContentPagingSource @Inject constructor(
    context: Context,
    private val repository: Repository,
    private val isPullRefreshed: Boolean,
) : PagingSource<Int, Content>() {

    private val prefsUtil = PrefsUtil(context)

    override fun getRefreshKey(state: PagingState<Int, Content>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Content> {
        return try {
            val page = params.key ?: DEFAULT_PAGE
            val pageCurrent =
                if (page == FIRST_PAGE || page == DEFAULT_PAGE && !isPullRefreshed) 0
                else if (page == TOTAL_PAGES && !isPullRefreshed) 1
                else page

            if (pageCurrent == 0) prefsUtil.resetAdIndex()
            var adIndex = prefsUtil.getAdIndex()

            val contentList = repository.fetchContent(page)
            val adList = repository.fetchAdvertisement()
            val resultList = contentList.toMutableList()

            contentList.forEachIndexed { indexInPage, _ ->
                val indexGlobal = indexInPage + (pageCurrent * contentList.size)
                if (indexGlobal > 1 && AppUtils.isFibonacci(indexGlobal - 1) && adIndex < adList.size) {
                    resultList.add(
                        indexInPage,
                        Content(
                            id = UUID.randomUUID().toString(),
                            image = adList[adIndex++],
                            video = null
                        )
                    )
                }
            }

            prefsUtil.saveAdIndex(adIndex)

            LoadResult.Page(
                data = resultList,
                prevKey = if (page == FIRST_PAGE || page == DEFAULT_PAGE && !isPullRefreshed) null else page - 1,
                nextKey = if (page == TOTAL_PAGES) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

