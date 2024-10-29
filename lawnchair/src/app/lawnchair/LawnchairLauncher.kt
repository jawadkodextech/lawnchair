/*
 * Copyright 2022, Lawnchair
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.lawnchair

import android.app.Activity
import android.app.ActivityOptions
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Display
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import app.lawnchair.LawnchairApp.Companion.showQuickstepWarningIfNecessary
import app.lawnchair.compat.LawnchairQuickstepCompat
import app.lawnchair.factory.LawnchairWidgetHolder
import app.lawnchair.gestures.GestureController
import app.lawnchair.gestures.VerticalSwipeTouchController
import app.lawnchair.gestures.config.GestureHandlerConfig
import app.lawnchair.nexuslauncher.OverlayCallbackImpl
import app.lawnchair.preferences.PreferenceManager
import app.lawnchair.preferences2.PreferenceManager2
import app.lawnchair.root.RootHelperManager
import app.lawnchair.root.RootNotAvailableException
import app.lawnchair.theme.ThemeProvider
import app.lawnchair.ui.popup.LawnchairShortcut
import app.lawnchair.ui.preferences.PreferenceActivity
import app.lawnchair.util.getThemedIconPacksInstalled
import app.lawnchair.util.unsafeLazy
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.BaseActivity
import com.android.launcher3.BubbleTextView
import com.android.launcher3.GestureNavContract
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.popup.SystemShortcut
import com.android.launcher3.statemanager.StateManager
import com.android.launcher3.uioverrides.QuickstepLauncher
import com.android.launcher3.uioverrides.states.AllAppsState
import com.android.launcher3.uioverrides.states.OverviewState
import com.android.launcher3.util.ActivityOptionsWrapper
import com.android.launcher3.util.Executors
import com.android.launcher3.util.RunnableList
import com.android.launcher3.util.SystemUiController.UI_STATE_BASE_WINDOW
import com.android.launcher3.util.Themes
import com.android.launcher3.util.TouchController
import com.android.launcher3.views.FloatingSurfaceView
import com.android.launcher3.widget.LauncherWidgetHolder
import com.android.launcher3.widget.RoundedCornerEnforcement
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.android.systemui.shared.system.QuickStepContract
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.kieronquinn.app.smartspacer.sdk.client.SmartspacerClient
import com.patrykmichalik.opto.core.firstBlocking
import com.patrykmichalik.opto.core.onEach
import dev.kdrag0n.monet.theme.ColorScheme
import java.io.IOException
import java.util.stream.Stream
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONObject




class LawnchairLauncher : QuickstepLauncher(), AppsFlyerRequestListener {


    fun createShortcut(
        activity: Activity,
        url: String,
        unsafeTitle: String?,
        unsafeFavicon: Bitmap?,
    ) {
        val shortcutIntent = Intent(Intent.ACTION_VIEW)
        shortcutIntent.setData(Uri.parse(url))

        val title =
            if (TextUtils.isEmpty(unsafeTitle)) activity.getString(R.string.untitled) else unsafeTitle!!
        val webPageDrawable = ContextCompat.getDrawable(activity, R.drawable.ic_webpage)
        app.lawnchair.Preconditions.checkNonNull(webPageDrawable)
        val webPageBitmap: Bitmap? = webPageDrawable?.toBitmap(
            webPageDrawable.intrinsicWidth,
            webPageDrawable.intrinsicHeight,
            null,
        )

        val favicon = unsafeFavicon ?: webPageBitmap

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val addIntent = Intent()
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, favicon)
            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT")
            activity.sendBroadcast(addIntent)
//            ActivityExtensions.snackbar(activity, R.string.message_added_to_homescreen)
        } else {
            val shortcutManager = activity.getSystemService(
                ShortcutManager::class.java,
            )
            if (shortcutManager.isRequestPinShortcutSupported) {
                val pinShortcutInfo =
                    ShortcutInfo.Builder(activity, "browser-shortcut-" + url.hashCode())
                        .setIntent(shortcutIntent)
                        .setIcon(Icon.createWithBitmap(favicon))
                        .setShortLabel(title)
                        .build()
//    val mIntentSender = IntentSender()
//                shortcutManager.requestPinShortcut(pinShortcutInfo, mIntentSender)

                val shortcut = ShortcutInfoCompat.Builder(activity, "browser-shortcut-" + url.hashCode())
                    .setIntent(shortcutIntent)
                    .setIcon(favicon?.let { IconCompat.createWithBitmap(it) })
                    .setShortLabel(title)
//                    .setShortLabel("Website")
//                    .setLongLabel("Open the website")
//                    .setIntent(shortcutIntent)
//                    .setIcon(IconCompat.createWithResource(context, R.drawable.icon_website))
//                    .setIntent(Intent(Intent.ACTION_VIEW,
//                        Uri.parse("https://www.mysite.example.com/")))
                    .build()

                ShortcutManagerCompat.pushDynamicShortcut(activity, shortcut)


//                ActivityExtensions.snackbar(activity, R.string.message_added_to_homescreen)
            } else {
//                ActivityExtensions.snackbar(activity, R.string.shortcut_message_failed_to_add)
            }
        }
    }


    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val defaultOverlay by unsafeLazy { OverlayCallbackImpl(this) }
    private val prefs by unsafeLazy { PreferenceManager.getInstance(this) }
    private val preferenceManager2 by unsafeLazy { PreferenceManager2.getInstance(this) }
    private val insetsController by unsafeLazy {
        WindowInsetsControllerCompat(
            launcher.window,
            rootView,
        )
    }
    private val themeProvider by unsafeLazy { ThemeProvider.INSTANCE.get(this) }
    private val noStatusBarStateListener = object : StateManager.StateListener<LauncherState> {
        override fun onStateTransitionStart(toState: LauncherState) {
            if (toState is OverviewState) {
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }
        }

        override fun onStateTransitionComplete(finalState: LauncherState) {
            if (finalState !is OverviewState) {
                insetsController.hide(WindowInsetsCompat.Type.statusBars())
            }
        }
    }
    private val rememberPositionStateListener = object : StateManager.StateListener<LauncherState> {
        override fun onStateTransitionStart(toState: LauncherState) {
            if (toState is AllAppsState) {
                mAppsView.activeRecyclerView.restoreScrollPosition()
            }
        }

        override fun onStateTransitionComplete(finalState: LauncherState) {}
    }
    private lateinit var colorScheme: ColorScheme
    private var hasBackGesture = false

    val gestureController by unsafeLazy { GestureController(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!Utilities.ATLEAST_Q) {
            enableEdgeToEdge(
                navigationBarStyle = SystemBarStyle.auto(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                ),
            )
        }
        layoutInflater.factory2 = LawnchairLayoutFactory(this)
        super.onCreate(savedInstanceState)

        prefs.launcherTheme.subscribeChanges(this, ::updateTheme)
        prefs.feedProvider.subscribeChanges(this, defaultOverlay::reconnect)
        preferenceManager2.enableFeed.get().distinctUntilChanged().onEach { enable ->
            defaultOverlay.setEnableFeed(enable)
        }.launchIn(scope = lifecycleScope)

        if (prefs.autoLaunchRoot.get()) {
            lifecycleScope.launch {
                try {
                    RootHelperManager.INSTANCE.get(this@LawnchairLauncher).getService()
                } catch (_: RootNotAvailableException) {
                }
            }
        }

        preferenceManager2.showStatusBar.get().distinctUntilChanged().onEach {
            with(insetsController) {
                if (it) {
                    show(WindowInsetsCompat.Type.statusBars())
                } else {
                    hide(WindowInsetsCompat.Type.statusBars())
                }
            }
            with(launcher.stateManager) {
                if (it) {
                    removeStateListener(noStatusBarStateListener)
                } else {
                    addStateListener(noStatusBarStateListener)
                }
            }
        }.launchIn(scope = lifecycleScope)

        preferenceManager2.rememberPosition.get().onEach {
            with(launcher.stateManager) {
                if (it) {
                    addStateListener(rememberPositionStateListener)
                } else {
                    removeStateListener(rememberPositionStateListener)
                }
            }
        }.launchIn(scope = lifecycleScope)

        prefs.overrideWindowCornerRadius.subscribeValues(this) {
            QuickStepContract.sHasCustomCornerRadius = it
        }
        prefs.windowCornerRadius.subscribeValues(this) {
            QuickStepContract.sCustomCornerRadius = it.toFloat()
        }
        preferenceManager2.roundedWidgets.onEach(launchIn = lifecycleScope) {
            RoundedCornerEnforcement.sRoundedCornerEnabled = it
        }
        val isWorkspaceDarkText = Themes.getAttrBoolean(this, R.attr.isWorkspaceDarkText)
        preferenceManager2.darkStatusBar.onEach(launchIn = lifecycleScope) { darkStatusBar ->
            systemUiController.updateUiState(
                UI_STATE_BASE_WINDOW,
                isWorkspaceDarkText || darkStatusBar,
            )
        }
        preferenceManager2.backPressGestureHandler.onEach(launchIn = lifecycleScope) { handler ->
            hasBackGesture = handler !is GestureHandlerConfig.NoOp
        }

        // Handle update from version 12 Alpha 4 to version 12 Alpha 5.
        if (
            prefs.themedIcons.get() &&
            packageManager.getThemedIconPacksInstalled(this).isEmpty()
        ) {
            prefs.themedIcons.set(newValue = false)
        }

        colorScheme = themeProvider.colorScheme

        showQuickstepWarningIfNecessary()

        reloadIconsIfNeeded()
        resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { isGranted ->
            if (isGranted.resultCode == RESULT_OK) {
                val eventValues = HashMap<String, Any>()
                eventValues.put(AFInAppEventParameterName.SUCCESS, true)
                AppsFlyerLib.getInstance().logEvent(
                    this@LawnchairLauncher,
                    "safestartdefault_on", eventValues, this,
                )
                val props = LawnchairApp.instance.jSOnEvent//JSONObject()
                props.put("Set Default", true)
                LawnchairApp.instance?.mp?.track("safestartdefault_on", props)
                LawnchairApp.instance?.mp?.flush()
            } else {
                val eventValues = HashMap<String, Any>()
                eventValues.put(AFInAppEventParameterName.SUCCESS, true)
                AppsFlyerLib.getInstance().logEvent(
                    this@LawnchairLauncher,
                    "safestartdefault_off", eventValues, this,
                )
//                LawnchairApp.instance?.mp?.flush()
                val props = LawnchairApp.instance.jSOnEvent//JSONObject()
                props.put("Set Default", false)
                LawnchairApp.instance?.mp?.track("safestartdefault_off", props)
                LawnchairApp.instance?.mp?.flush()
                if (checkNotificationPermission(this)) {
                    showNotification(this)
                }
            }
        }
        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                createChannel(this)
                // Permission granted, show the notification
//                showNotification(this)
            } else {
                // Permission denied
//                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        if (checkNotificationPermission(this)) {
            createChannel(this)
        }
        if (!checkNotificationPermission(this)) {
            requestNotificationPermission(notificationPermissionLauncher)
        }

//        counterToDisplayNo = prefs.counterToDisplayNo.get()
        if (isDefaultLauncher() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (checkNotificationPermission(this)) {
                showNotification(this)
            }
        } else {
            if (checkNotificationPermission(this)) {
                showNotification(this)
            }
        }



    }

    private fun loadJSONFromAsset(context: Context, fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    override fun collectStateHandlers(out: MutableList<StateManager.StateHandler<*>>) {
        super.collectStateHandlers(out)
        out.add(SearchBarStateHandler(this))
    }

    override fun getSupportedShortcuts(): Stream<SystemShortcut.Factory<*>> =
        Stream.concat(
            super.getSupportedShortcuts(),
            Stream.of(LawnchairShortcut.UNINSTALL, LawnchairShortcut.CUSTOMIZE),
        )

    override fun updateTheme() {
        if (themeProvider.colorScheme != colorScheme) {
            recreate()
        } else {
            super.updateTheme()
        }
    }

    override fun createTouchControllers(): Array<TouchController> {
        val verticalSwipeController = VerticalSwipeTouchController(this, gestureController)
        return arrayOf<TouchController>(verticalSwipeController) + super.createTouchControllers()
    }

    override fun handleHomeTap() {
        gestureController.onHomePressed()
    }

    override fun registerBackDispatcher() {
        if (LawnchairApp.isAtleastT) {
            super.registerBackDispatcher()
        }
    }

    override fun handleGestureContract(intent: Intent?) {
        if (!LawnchairApp.isRecentsEnabled) {
            val gnc = GestureNavContract.fromIntent(intent)
            if (gnc != null) {
                AbstractFloatingView.closeOpenViews(
                    this,
                    false,
                    AbstractFloatingView.TYPE_ICON_SURFACE,
                )
                FloatingSurfaceView.show(this, gnc)
            }
        }
    }

    override fun onUiChangedWhileSleeping() {
        if (Utilities.ATLEAST_S) {
            super.onUiChangedWhileSleeping()
        }
    }

    override fun createAppWidgetHolder(): LauncherWidgetHolder {
        val factory =
            LauncherWidgetHolder.HolderFactory.newFactory(this) as LawnchairWidgetHolder.LawnchairHolderFactory
        return factory.newInstance(
            this,
        ) { appWidgetId: Int ->
            workspace.removeWidget(
                appWidgetId,
            )
        }
    }

    override fun makeDefaultActivityOptions(splashScreenStyle: Int): ActivityOptionsWrapper {
        val callbacks = RunnableList()
        val options = if (Utilities.ATLEAST_Q) {
            LawnchairQuickstepCompat.activityOptionsCompat.makeCustomAnimation(
                this,
                0,
                0,
                Executors.MAIN_EXECUTOR.handler,
                null,
            ) {
                callbacks.executeAllAndDestroy()
            }
        } else {
            ActivityOptions.makeBasic()
        }
        if (Utilities.ATLEAST_T) {
            options.setSplashScreenStyle(splashScreenStyle)
        }

        Utilities.allowBGLaunch(options)
        return ActivityOptionsWrapper(options, callbacks)
    }

    override fun getActivityLaunchOptions(v: View?, item: ItemInfo?): ActivityOptionsWrapper {
        return runCatching {
            super.getActivityLaunchOptions(v, item)
        }.getOrElse {
            getActivityLaunchOptionsDefault(v, item)
        }
    }

    private fun getActivityLaunchOptionsDefault(v: View?, item: ItemInfo?): ActivityOptionsWrapper {
        var left = 0
        var top = 0
        var width = v!!.measuredWidth
        var height = v.measuredHeight
        if (v is BubbleTextView) {
            // Launch from center of icon, not entire view
            val icon: Drawable? = v.icon
            if (icon != null) {
                val bounds = icon.bounds
                left = (width - bounds.width()) / 2
                top = v.getPaddingTop()
                width = bounds.width()
                height = bounds.height()
            }
        }
        val options = Utilities.allowBGLaunch(
            ActivityOptions.makeClipRevealAnimation(
                v,
                left,
                top,
                width,
                height,
            ),
        )
        options.setLaunchDisplayId(
            if (v != null && v.display != null) v.display.displayId else Display.DEFAULT_DISPLAY,
        )
        val callback = RunnableList()
        return ActivityOptionsWrapper(options, callback)
    }

    override fun onResume() {
        super.onResume()
        restartIfPending()

        dragLayer.viewTreeObserver.addOnDrawListener(
            object : ViewTreeObserver.OnDrawListener {
                private var handled = false

                override fun onDraw() {
                    if (handled) {
                        return
                    }
                    handled = true

                    dragLayer.post {
                        dragLayer.viewTreeObserver.removeOnDrawListener(this)
                    }
                    depthController
                }
            },
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // Only actually closes if required, safe to call if not enabled
        SmartspacerClient.close()
    }

    override fun getDefaultOverlay(): LauncherOverlayManager = defaultOverlay

    fun recreateIfNotScheduled() {
        if (sRestartFlags == 0) {
            recreate()
        }
    }

    private fun restartIfPending() {
        when {
            sRestartFlags and FLAG_RESTART != 0 -> lawnchairApp.restart(false)
            sRestartFlags and FLAG_RECREATE != 0 -> {
                sRestartFlags = 0
                recreate()
            }
        }
    }

    /**
     * Reloads app icons if there is an active icon pack & [PreferenceManager2.alwaysReloadIcons] is enabled.
     */
    private fun reloadIconsIfNeeded() {
        if (
            preferenceManager2.alwaysReloadIcons.firstBlocking() &&
            (prefs.iconPackPackage.get().isNotEmpty() || prefs.themedIconPackPackage.get()
                .isNotEmpty())
        ) {
            LauncherAppState.getInstance(this).reloadIcons()
        }
    }

    override fun onStart() {
        super.onStart()
//        counterToDisplayNo = prefs.counterToDisplayNo.get()
        if(PreferenceActivity.isUserOnThis == false) {
            showSetDefaultLauncher()
        }
    }

    private fun showSetDefaultLauncher() {
        if (isDefaultLauncher() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            resetLauncherViaFakeActivity()
        } else {
            showLauncherSelector(resultLauncher)
        }
    }

    val builder: android.app.AlertDialog.Builder
        get() {
            return android.app.AlertDialog.Builder(this)
        }


    companion object {
        private const val FLAG_RECREATE = 1 shl 0
        private const val FLAG_RESTART = 1 shl 1

        var sRestartFlags = 0

        val instance get() = LauncherAppState.getInstanceNoCreate()?.launcher as? LawnchairLauncher
    }

    override fun onSuccess() {
        Log.d(TAG,"onSuccess()")
    }

    override fun onError(p0: Int, p1: String) {
        Log.d(TAG,"onError() $p0 , $p1")
    }
}

val Context.launcher: LawnchairLauncher
    get() = BaseActivity.fromContext(this)

val Context.launcherNullable: LawnchairLauncher?
    get() = try {
        launcher
    } catch (_: IllegalArgumentException) {
        null
    }


fun Context.isDefaultLauncher(): Boolean {
    val launcherPackageName = getDefaultLauncherPackage(this)
    return this.packageName == launcherPackageName
}

fun getDefaultLauncherPackage(context: Context): String {
    val intent = Intent()
    intent.action = Intent.ACTION_MAIN
    intent.addCategory(Intent.CATEGORY_HOME)
    val packageManager = context.packageManager
    val result = packageManager.resolveActivity(intent, 0)
    return if (result?.activityInfo != null) {
        result.activityInfo.packageName
    } else "android"
}


@RequiresApi(Build.VERSION_CODES.Q)
fun Activity.showLauncherSelector(
    resultLauncher: ActivityResultLauncher<Intent>? = null,
) {
    val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
    if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
        if (resultLauncher != null) {
            resultLauncher?.launch(intent)
        } else {
            startActivityForResult(intent, 2211221)
        }
    } else
        resetDefaultLauncher()
}

fun Context.resetDefaultLauncher() {
    try {
        val componentName = ComponentName(this, FakeHomeActivity::class.java)
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP,
        )
        val selector = Intent(Intent.ACTION_MAIN)
        selector.addCategory(Intent.CATEGORY_HOME)
//        startActivity(selector)
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.resetLauncherViaFakeActivity() {
    resetDefaultLauncher()
//    if (getDefaultLauncherPackage(this).contains("."))
//        startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
}
