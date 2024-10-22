package app.lawnchair.qsb.providers

import android.content.Intent
import app.lawnchair.qsb.ThemingMethod
import app.lawnchair.search.FullScreenActivity
import com.android.launcher3.Launcher
import com.android.launcher3.R

data object Youtube : QsbSearchProvider(
    id = "youtube",
    name = R.string.search_provider_youtube,
    icon = R.drawable.ic_youtube,
    themingMethod = ThemingMethod.THEME_BY_LAYER_ID,
    packageName = "com.google.android.youtube",
    action = Intent.ACTION_SEARCH,
    supportVoiceIntent = false,
    website = "https://youtube.com/",
)


data object Yahoo : QsbSearchProvider(
    id = "safesearch",
    name = R.string.search_provider_yahoo,
    icon = R.drawable.ic_yahoo,
    themingMethod = ThemingMethod.THEME_BY_LAYER_ID,
    packageName = "com.google.android.youtube",
    action = Intent.ACTION_SEARCH,
    supportVoiceIntent = false,
    website = "https://startsafe.kidsmode.co/search/?q=",
    type = QsbSearchProviderType.LOCAL,
) {
    override suspend fun launch(launcher: Launcher, forceWebsite: Boolean) {
        val intent = Intent(launcher, FullScreenActivity::class.java)
        launcher.startActivity(intent)
//        launcher.animateToAllApps()
//        launcher.appsView.searchUiManager.editText?.showKeyboard(true)
    }
}
//i=${LawnchairApp.androidId}&
//i update the link again
//https://startsafe.kidsmode.co/search/?i=04793824052e5793&q=hulu
