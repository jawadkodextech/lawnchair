package app.lawnchair.search

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.lawnchair.LawnchairApp
import app.lawnchair.LawnchairLauncher
import app.lawnchair.checkNotificationPermission
import app.lawnchair.isDefaultLauncher
import app.lawnchair.resetLauncherViaFakeActivity
import app.lawnchair.search.searchsuggestion.AndroidLogger
import app.lawnchair.search.searchsuggestion.RVSuggestionAdapter
import app.lawnchair.search.searchsuggestion.RequestFactory
import app.lawnchair.search.searchsuggestion.SearchEngineProvider
import app.lawnchair.search.searchsuggestion.SuggestionsAdapter
import app.lawnchair.search.searchsuggestion.WebPage
import app.lawnchair.showLauncherSelector
import app.lawnchair.showNotification
import com.android.launcher3.Launcher.TAG
import com.android.launcher3.R
import com.android.launcher3.databinding.ActivityFullScreenBinding
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.CacheControl
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class FullScreenActivity : ComponentActivity(), AppsFlyerRequestListener {
    private lateinit var binding: ActivityFullScreenBinding
    private val IS_FROM_PUSH: Boolean
        get() {
            return intent?.extras?.getBoolean("IS_FROM_PUSH", false) ?: false
        }
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val rlYahooSearch by lazy { findViewById<ConstraintLayout>(R.id.rlYahooSearch)!! }
    private val btnCrossGoBack by lazy { findViewById<ImageView>(R.id.btnCrossGoBack)!! }
    private val etYahooSearch by lazy { findViewById<EditText>(R.id.etYahooSearch)!! }
    private val btnCrossField by lazy { findViewById<ImageView>(R.id.btnCrossField)!! }
    private val btnSearch by lazy { findViewById<Button>(R.id.btnSearch)!! }
    private val rlSuggestion by lazy { findViewById<RecyclerView>(R.id.rlSuggestion)!! }

    //    private val rvShortcutFolders by lazy { findViewById<RecyclerView>(R.id.rvShortcutFolders)!! }
    private val llExtras by lazy { findViewById<LinearLayout>(R.id.llExtras)!! }
    private var mList: ArrayList<WebPage> = arrayListOf()
    private val txtSearch: String
        get() {
            return etYahooSearch.text?.toString()?.trim() ?: ""
        }
    private var mDisposable: Disposable? = null
    private var mRVSuggestionAdapter: RVSuggestionAdapter? = null

    // Function to check if the keyboard is closed
    private fun isKeyboardClosed(rootLayout: View): Boolean {
        val rect = Rect()
        rootLayout.getWindowVisibleDisplayFrame(rect)
        val screenHeight = rootLayout.rootView.height
        val keypadHeight = screenHeight - rect.bottom

        // If the keypad height is less than 100, the keyboard is closed
        return keypadHeight < 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenBinding.inflate(layoutInflater)
        // Make the activity full screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        )

        // Set the content view
        setContentView(binding.root)

        // Optional: Ensure keyboard does not resize the screen
//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)


        etYahooSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {

                if (txtSearch.isNotEmpty()) {
                    openURLInBrowser(
                        this,
                        "https://startsafe.kidsmode.co/search/?i=${LawnchairApp.androidId}&q=$txtSearch",
                        rlYahooSearch.clipBounds,
                        null,
                    )
//                    etYahooSearch.clearFocus()
//                    etYahooSearch.setText("")
                }
                true // Return true if you handled the action
            } else {
                false // Return false if you didn't handle the action
            }
        }
        resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { isGranted ->
            if (isGranted.resultCode == RESULT_OK) {
                val eventValues = HashMap<String, Any>()
                eventValues.put(AFInAppEventParameterName.SUCCESS, true)
                AppsFlyerLib.getInstance().logEvent(
                    this@FullScreenActivity,
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
                    this@FullScreenActivity,
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
//                showAlert("Set as Default to continue")
            }
        }
        btnCrossField.setOnClickListener {
            etYahooSearch.setText("")
            LawnchairLauncher.instance?.hideKeyboard()

        }
        btnSearch.setOnClickListener {
            LawnchairLauncher.instance?.hideKeyboard()
//            LawnchairLauncher.instance?.link
            openURLInBrowser(
                this,
                "https://startsafe.kidsmode.co/search/?i=${LawnchairApp.androidId}&q=$txtSearch",
                rlYahooSearch.clipBounds,
                null,
            )
//            etYahooSearch.setText("")
//            etYahooSearch.clearFocus()
//            rlSuggestion.isVisible = false
        }


        // Assuming you're in an Activity or Fragment
        val context: Context = this // Use the current context
        val databaseScheduler = Schedulers.io() // Example for database operations
        val networkScheduler = Schedulers.io() // Example for network operations
        val mainScheduler = AndroidSchedulers.mainThread() // For main thread operations

// Create instances for the parameters of SearchEngineProvider
        val okHttpClient: Single<OkHttpClient> =
            Single.just(OkHttpClient()) // Replace with your OkHttpClient initialization
        val requestFactory = object : RequestFactory {
            override fun createSuggestionsRequest(httpUrl: HttpUrl, encoding: String): Request {
                return Request.Builder().url(httpUrl)
                    .addHeader("Accept-Charset", encoding)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .cacheControl(CacheControl.Builder().build())
                    .build()
            }
        }//RequestFactory() // Create an instance of RequestFactory
//        val application = context.applicationContext as Application // Get Application context
        val logger = AndroidLogger() // Create an instance of Logger
        val searchEngineProvider = SearchEngineProvider(
            okHttpClient,
            requestFactory,
            application,
            logger,
        )
        val suggestionsAdapter = SuggestionsAdapter(
            context,
            databaseScheduler,
            networkScheduler,
            mainScheduler,
            searchEngineProvider,
        )
        mRVSuggestionAdapter = RVSuggestionAdapter(mList, this)
        mRVSuggestionAdapter?.onSuggestionInsertClick = { itemWebPage ->
            Log.d(TAG, "$TAG ${itemWebPage.toString()}")
            etYahooSearch.setText(itemWebPage.title)
            etYahooSearch.selectAll()
            //"https://search.yahoo.com/search?q=${itemWebPage.url}"
            openURLInBrowser(
                this,
                "https://startsafe.kidsmode.co/search/?i=${LawnchairApp.androidId}&q=${itemWebPage.title}",
                rlYahooSearch.clipBounds,
                null,
            )
            etYahooSearch.clearFocus()
//            LawnchairLauncher.instance?.hideKeyboard()
//            rlSuggestion.isVisible = false
        }
        rlSuggestion.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rlSuggestion.adapter = mRVSuggestionAdapter

        etYahooSearch.addTextChangedListener {
            llExtras.isVisible = txtSearch.isNotEmpty()
            rlSuggestion.isVisible = txtSearch.isNotEmpty()
            if (llExtras.isVisible) {
                mDisposable = suggestionsAdapter.getSearchResults(txtSearch) {
                    mList.clear()
                    mList.addAll(it)//.take(5)
                    mRVSuggestionAdapter?.notifyDataSetChanged()
                }
            } else {
                mDisposable?.dispose()
            }
        }
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            if (isKeyboardClosed(binding.root)) {
//                finish() // Close the activity when keyboard is closed
            }
        }
        etYahooSearch.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
//                finish()
            }
        }
        btnCrossGoBack.setOnClickListener {
            finish()
        }
        if (IS_FROM_PUSH) {
            val props = LawnchairApp.instance.jSOnEvent//JSONObject()
            props.put("FromPush", true)
            LawnchairApp.instance?.mp?.track("ClickSearchBar", props)
            LawnchairApp.instance?.mp?.flush()
            showSetDefaultLauncher()
        }
    }

    private fun showKeyboard(editText: EditText) {
        // Request focus for the EditText
        editText.requestFocus()

        // Show the soft keyboard
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onStart() {
        super.onStart()
        showKeyboard(etYahooSearch)
    }

    private fun showSetDefaultLauncher() {
        if (isDefaultLauncher() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            resetLauncherViaFakeActivity()
        } else {
            showLauncherSelector(resultLauncher)
        }
    }

    override fun onSuccess() {
        Log.d(LawnchairApp.TAG,"onSuccess()")
    }

    override fun onError(p0: Int, p1: String) {
        Log.d(LawnchairApp.TAG,"onError() $p0 , $p1")
    }
}

fun openURLInBrowser(context: Context, url: String?, sourceBounds: Rect?, options: Bundle?) {
    val props = LawnchairApp.instance.jSOnEvent//JSONObject()
//    props.put("properties_query_search", url)
    props.put("searchsource", "custom_search_screen")
    LawnchairApp.instance?.mp?.track("Search", props)
    LawnchairApp.instance?.mp?.flush()

    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (context !is AppCompatActivity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent.sourceBounds = sourceBounds
        if (options == null) {
            context.startActivity(intent)
        } else {
            context.startActivity(intent, options)
        }
    } catch (exc: ActivityNotFoundException) {
//        Toast.makeText(context, R.string.error_no_browser, Toast.LENGTH_SHORT).show()
    }
}
