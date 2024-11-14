package app.lawnchair.qsb.providers

import android.content.Intent
import app.lawnchair.LawnchairApp
import app.lawnchair.qsb.ThemingMethod
import app.lawnchair.search.FullScreenActivity
import com.android.launcher3.Launcher
import com.android.launcher3.R
import org.json.JSONObject

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

//ic_yahoo
data object Yahoo : QsbSearchProvider(
    id = "safesearch",
    name = R.string.search_provider_yahoo,
    icon = R.drawable.safe_icon_small,
    themingMethod = ThemingMethod.THEME_BY_LAYER_ID,
    packageName = "com.google.android.youtube",
    action = Intent.ACTION_SEARCH,
    supportVoiceIntent = false,
    website = "https://startsafe.kidsmode.co/search/?q=",
    type = QsbSearchProviderType.LOCAL,
) {
    override suspend fun launch(launcher: Launcher, forceWebsite: Boolean) {

        val props = JSONObject(LawnchairApp.instance.jSOnEvent.toString())//()
        props.put("UserClicked", true)
        LawnchairApp.instance?.mp?.track("ClickSearchBar", props)
        LawnchairApp.instance?.mp?.flush()

        val intent = Intent(launcher, FullScreenActivity::class.java)
        launcher.startActivity(intent)
//        launcher.animateToAllApps()
//        launcher.appsView.searchUiManager.editText?.showKeyboard(true)
    }
}
