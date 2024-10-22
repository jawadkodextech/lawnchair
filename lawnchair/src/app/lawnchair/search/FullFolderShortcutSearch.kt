package app.lawnchair.search

import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.lawnchair.search.searchsuggestion.FolderNew
import app.lawnchair.search.searchsuggestion.FolderShortCutScreenAdapter
import com.android.launcher3.R
import com.android.launcher3.databinding.ActivityFolderShortcutScreenBinding
import com.google.gson.Gson

class FullScreenIconShortcutActivity : ComponentActivity() {
    private lateinit var binding: ActivityFolderShortcutScreenBinding
    private var mFolderShortCutAdapter: FolderShortCutScreenAdapter? = null
    private val btnCrossGoBack by lazy { findViewById<ImageView>(R.id.btnCrossGoBack)!! }
    private val etYahooSearch by lazy { findViewById<TextView>(R.id.tvYahooSearch)!! }
    private val rvShortcuts by lazy { findViewById<RecyclerView>(R.id.rvShortcutFolders)!! }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderShortcutScreenBinding.inflate(layoutInflater)
        // Make the activity full screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        )


        // Set the content view
        setContentView(binding.root)

        val mFolderNew = intent?.extras?.getParcelable("FolderNew") as? FolderNew
        val mFolderNewList = Gson().fromJson(intent?.extras?.getString("FolderNewList") ?: "",Array<FolderNew>::class.java) // as? Array<FolderNew>
//        FolderNewList
        etYahooSearch.text = mFolderNew?.name ?: "Shortcut Folder"
        mFolderShortCutAdapter = FolderShortCutScreenAdapter(mFolderNew?.links ?: mFolderNewList?.toList(),this)
        rvShortcuts.layoutManager = GridLayoutManager(this,4, RecyclerView.VERTICAL,false)
        rvShortcuts.adapter = mFolderShortCutAdapter
        btnCrossGoBack.setOnClickListener {
            finish()
        }
    }


}
