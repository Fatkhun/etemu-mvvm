package com.fatkhun.core.ui

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.fatkhun.core.model.LostFoundForm
import com.fatkhun.core.model.LostFoundItemList
import com.fatkhun.core.network.RetrofitRoutes
import retrofit2.HttpException
import java.io.IOException

class LostFoundPagingSource(
    private val service: suspend () -> RetrofitRoutes,
    private val form: LostFoundForm
): PagingSource<Int, LostFoundItemList>() {

    override fun getRefreshKey(state: PagingState<Int, LostFoundItemList>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor)
        return page?.prevKey?.let { it + (page.data.size) } ?: page?.nextKey?.let { it - (page.data.size) }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LostFoundItemList> {
        val offset = params.key ?: 0
        val req = form.copy(offset = offset) // <- aman, no side-effect

        val datas: HashMap<String, String> = hashMapOf()
        datas["q"] = req.keyword
        datas["category_id"] = req.category_id.toString()
        datas["status"] = req.status
        datas["type"] = req.type
        datas["limit"] = req.limit.toString()
        datas["offset"] = req.offset.toString()

        val api = service()
        return try {
            val data = api.getLostFoundPaging(datas)
            val items = data.data.items

            val nextKey = if (items.isEmpty()) null else offset + items.size
            val prevKey = if (offset == 0) null else maxOf(0, offset - items.size)
            LoadResult.Page(
                data = items,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            val raw = (exception.response()?.raw() as okhttp3.Response)
            val url = raw.request.url
            return LoadResult.Error(exception)
        }
    }

}