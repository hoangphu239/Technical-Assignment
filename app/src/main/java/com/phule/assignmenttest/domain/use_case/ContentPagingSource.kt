import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phule.assignmenttest.common.DEFAULT_PAGE
import com.phule.assignmenttest.common.FIRST_PAGE
import com.phule.assignmenttest.common.TOTAL_PAGES
import com.phule.assignmenttest.domain.model.Content
import com.phule.assignmenttest.domain.repository.Repository
import com.phule.assignmenttest.presentation.utils.AppUtils
import java.util.UUID
import javax.inject.Inject

class ContentPagingSource @Inject constructor(
    private val repository: Repository,
    private val isPullRefreshed: Boolean
) : PagingSource<Int, Content>() {

    override fun getRefreshKey(state: PagingState<Int, Content>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Content> {
        return try {
            var adIndex = 0
            val page = params.key ?: DEFAULT_PAGE

            val contentList = repository.fetchContent(page)
            val adList = repository.fetchAdvertisement()
            val resultList = contentList.toMutableList()

            contentList.forEachIndexed { index, _ ->
                if (index > 1 && AppUtils.isFibonacci(index - 1) && adIndex < adList.size) {
                    resultList.add(
                        index,
                        Content(
                            id = UUID.randomUUID().toString(),
                            image = adList[adIndex++],
                            video = null
                        )
                    )
                }
            }

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

