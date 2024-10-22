package app.lawnchair.search.searchsuggestion

import com.android.launcher3.R


class BrowseSafeSearch : BaseSearchEngine(
    "file:///android_asset/ic_safe.png",
    "https://search.yahoo.com/search?vm=r&p=",
    R.string.app_name
)
