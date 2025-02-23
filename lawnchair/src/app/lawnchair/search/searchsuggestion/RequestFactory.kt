package app.lawnchair.search.searchsuggestion

import okhttp3.HttpUrl
import okhttp3.Request

/**
 * A factory that creates a GET [Request] on the provided [HttpUrl] with the specified encoding.
 */
interface RequestFactory {

    /**
     * Create a [Request] for the provided [HttpUrl] with the specified [encoding].
     */
    fun createSuggestionsRequest(httpUrl: HttpUrl, encoding: String): Request

}
