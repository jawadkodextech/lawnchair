package app.lawnchair.search.searchsuggestion

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.lawnchair.search.FullScreenIconShortcutActivity
import app.lawnchair.search.openURLInBrowser
import app.lawnchair.search.searchsuggestion.avataricons.AvatarConstants
import app.lawnchair.search.searchsuggestion.avataricons.AvatarGenerator
import com.android.launcher3.R
import com.android.launcher3.databinding.ItemShortcutFolderBinding
import com.bumptech.glide.Glide

class FolderShortCutAdapter(
    var mRootNewLinks: RootNewLinks? = null,
    val mContext: Context,
) :
    RecyclerView.Adapter<FolderShortCutVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderShortCutVH {
        val item = FolderShortCutVH(
            LayoutInflater.from(mContext).inflate(
                R.layout.item_shortcut_folder, parent,
                false,
            ),
        )

        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidth = displayMetrics.widthPixels // Get screen width in pixels
        val itemWidth = screenWidth / 5 // Divide the screen width by 4

        val params = item.itemView.layoutParams
        params.width = itemWidth//ViewGroup.LayoutParams.WRAP_CONTENT
        item.itemView.layoutParams = params
        return item
    }

    override fun getItemCount(): Int {
        if (mRootNewLinks == null) {
            return 0
        } else {
            return (mRootNewLinks?.root_links?.count()
                ?: 0) + (mRootNewLinks?.folders?.count() ?: 0)//(if (mRootNewLinks?.folders?.isNotEmpty() == true) 1 else 0)
        }
    }

    override fun onBindViewHolder(holder: FolderShortCutVH, position: Int) {
        try {
            val itemRootLink = mRootNewLinks?.root_links?.get(position)
            val iconImg = AvatarGenerator.AvatarBuilder(holder.itemView.context)
                .setLabel(itemRootLink?.name ?: "S")
                .setAvatarSize(70)
                .setTextSize(14)
                .toCircle()
                .setBackgroundColor(Color.parseColor("#FFCA28"))
                .build()
            holder.binding.llFolder.isVisible = false
            Glide.with(holder.imgView)
                .load(if (itemRootLink?.icon == "R.drawable.safe_icon") R.drawable.safe_icon else itemRootLink?.icon)
                .placeholder(iconImg)
                .error(iconImg)
                .into(holder.imgView)
            holder.tvName.text = itemRootLink?.name ?: ""
            holder.itemView.setOnClickListener {

                if (itemRootLink?.url?.isNullOrEmpty() == true) {
                    // open folder
                    val intent = Intent(mContext, FullScreenIconShortcutActivity::class.java)
                    intent.putExtra("FolderNew", itemRootLink)
                    mContext.startActivity(intent)
                } else {
                    // open url
                    itemRootLink?.url?.let { link ->
                        openURLInBrowser(
                            mContext, link,
                            holder.itemView.clipBounds,
                            null,
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val itemRootLink = mRootNewLinks?.folders?.get(position - (mRootNewLinks?.root_links?.count() ?: 0))
            var labelName = "S"
            var url = ""
            var icon = ""
            var links: List<LinkNew>? = null

            labelName = itemRootLink?.name ?: "F"
            url = itemRootLink?.url ?: ""
            links = itemRootLink?.links
            icon = itemRootLink?.icon ?: ""

            val iconImg = AvatarGenerator.AvatarBuilder(holder.itemView.context)
                .setLabel(labelName)
                .setAvatarSize(70)
                .setTextSize(14)
                .toCircle()
                .setBackgroundColor(Color.parseColor("#FFCA28"))
                .build()


            holder.binding.llFolder.isVisible = true
            holder.imgView.setImageResource(R.drawable.bg_placeholder)
            Glide.with(holder.imgView)
                .load(R.drawable.bg_placeholder)
                .into(holder.imgView)
//            holder.tvName.text = "Folder"
            holder.binding.iv1.isVisible = false
            holder.binding.iv2.isVisible = false
            holder.binding.iv3.isVisible = false
            holder.binding.iv4.isVisible = false


            holder.imgView.setImageResource(R.drawable.bg_placeholder)
            Glide.with(holder.imgView)
                .load(R.drawable.bg_placeholder)
                .into(holder.imgView)

            holder.binding.llFolder.isVisible = true
            val count = links?.count() ?: 0
            holder.binding.iv1.isVisible = false
            holder.binding.iv2.isVisible = false
            holder.binding.iv3.isVisible = false
            holder.binding.iv4.isVisible = false
            when (count) {
                1 -> {
                    holder.binding.sp1.isVisible = false
                    holder.binding.sp2.isVisible = false
                    holder.binding.iv1.isVisible = true
                    holder.binding.iv2.isVisible = false
                    holder.binding.iv3.isVisible = false
                    holder.binding.iv4.isVisible = false
                    setImageData(
                        holder.binding.iv1,
                        links?.get(0)?.icon,
                        links?.get(0)?.name,
                    )
                }

                2 -> {
                    holder.binding.sp1.isVisible = true
                    holder.binding.sp2.isVisible = false

                    holder.binding.iv1.isVisible = true
                    holder.binding.iv2.isVisible = true
                    holder.binding.iv3.isVisible = false
                    holder.binding.iv4.isVisible = false
                    setImageData(
                        holder.binding.iv2,
                        links?.get(1)?.icon,
                        links?.get(1)?.name,
                    )
                    setImageData(
                        holder.binding.iv1,
                        links?.get(0)?.icon,
                        links?.get(0)?.name,
                    )
                }

                3 -> {
                    holder.binding.sp1.isVisible = true
                    holder.binding.sp2.isVisible = false

                    holder.binding.iv1.isVisible = true
                    holder.binding.iv2.isVisible = true
                    holder.binding.iv3.isVisible = true
                    holder.binding.iv4.isVisible = false
                    setImageData(
                        holder.binding.iv3,
                        links?.get(2)?.icon,
                        links?.get(2)?.name,
                    )
                    setImageData(
                        holder.binding.iv2,
                        links?.get(1)?.icon,
                        links?.get(1)?.name,
                    )
                    setImageData(
                        holder.binding.iv1,
                        links?.get(0)?.icon,
                        links?.get(0)?.name,
                    )
                }

                else -> {
                    holder.binding.sp1.isVisible = true
                    holder.binding.sp2.isVisible = true

                    holder.binding.iv1.isVisible = true
                    holder.binding.iv2.isVisible = true
                    holder.binding.iv3.isVisible = true
                    holder.binding.iv4.isVisible = true
                    setImageData(
                        holder.binding.iv4,
                        links?.get(3)?.icon,
                        links?.get(3)?.name,
                    )
                    setImageData(
                        holder.binding.iv3,
                        links?.get(2)?.icon,
                        links?.get(2)?.name,
                    )
                    setImageData(
                        holder.binding.iv2,
                        links?.get(1)?.icon,
                        links?.get(1)?.name,
                    )
                    setImageData(
                        holder.binding.iv1,
                        links?.get(0)?.icon,
                        links?.get(0)?.name,
                    )
                }
            }
            holder.tvName.text = labelName
            holder.itemView.setOnClickListener {

                if (url.isEmpty() == true) {
                    // open folder
                    val intent = Intent(mContext, FullScreenIconShortcutActivity::class.java)
                    if (itemRootLink is FolderNew) {
                        intent.putExtra("FolderNew", itemRootLink)
                    }
                    mContext.startActivity(intent)
                } else {
                    // open url
                    url.let { link ->
                        openURLInBrowser(
                            mContext, link,
                            holder.itemView.clipBounds,
                            null,
                        )
                    }
                }
            }

//            when (count) {
//                1 -> {
//                    holder.binding.sp1.isVisible = false
//                    holder.binding.sp2.isVisible = false
//                    holder.binding.iv1.isVisible = true
//                    holder.binding.iv2.isVisible = false
//                    holder.binding.iv3.isVisible = false
//                    holder.binding.iv4.isVisible = false
//                    setImageData(
//                        holder.binding.iv1,
//                        mRootNewLinks?.folders?.get(0)?.icon,
//                        mRootNewLinks?.folders?.get(0)?.name,
//                    )
//                }
//
//                2 -> {
//                    holder.binding.sp1.isVisible = true
//                    holder.binding.sp2.isVisible = false
//
//                    holder.binding.iv1.isVisible = true
//                    holder.binding.iv2.isVisible = true
//                    holder.binding.iv3.isVisible = false
//                    holder.binding.iv4.isVisible = false
//                    setImageData(
//                        holder.binding.iv2,
//                        mRootNewLinks?.folders?.get(1)?.icon,
//                        mRootNewLinks?.folders?.get(1)?.name,
//                    )
//                    setImageData(
//                        holder.binding.iv1,
//                        mRootNewLinks?.folders?.get(0)?.icon,
//                        mRootNewLinks?.folders?.get(0)?.name,
//                    )
//                }
//
//                3 -> {
//                    holder.binding.sp1.isVisible = true
//                    holder.binding.sp2.isVisible = false
//
//                    holder.binding.iv1.isVisible = true
//                    holder.binding.iv2.isVisible = true
//                    holder.binding.iv3.isVisible = true
//                    holder.binding.iv4.isVisible = false
//                    setImageData(
//                        holder.binding.iv3,
//                        mRootNewLinks?.folders?.get(2)?.icon,
//                        mRootNewLinks?.folders?.get(2)?.name,
//                    )
//                    setImageData(
//                        holder.binding.iv2,
//                        mRootNewLinks?.folders?.get(1)?.icon,
//                        mRootNewLinks?.folders?.get(1)?.name,
//                    )
//                    setImageData(
//                        holder.binding.iv1,
//                        mRootNewLinks?.folders?.get(0)?.icon,
//                        mRootNewLinks?.folders?.get(0)?.name,
//                    )
//                }
//
//                else -> {
//                    holder.binding.sp1.isVisible = true
//                    holder.binding.sp2.isVisible = true
//
//                    holder.binding.iv1.isVisible = true
//                    holder.binding.iv2.isVisible = true
//                    holder.binding.iv3.isVisible = true
//                    holder.binding.iv4.isVisible = true
//                    setImageData(
//                        holder.binding.iv4,
//                        mRootNewLinks?.folders?.get(3)?.icon,
//                        mRootNewLinks?.folders?.get(3)?.name,
//                    )
//                    setImageData(
//                        holder.binding.iv3,
//                        mRootNewLinks?.folders?.get(2)?.icon,
//                        mRootNewLinks?.folders?.get(2)?.name,
//                    )
//                    setImageData(
//                        holder.binding.iv2,
//                        mRootNewLinks?.folders?.get(1)?.icon,
//                        mRootNewLinks?.folders?.get(1)?.name,
//                    )
//                    setImageData(
//                        holder.binding.iv1,
//                        mRootNewLinks?.folders?.get(0)?.icon,
//                        mRootNewLinks?.folders?.get(0)?.name,
//                    )
//                }
//            }

//            holder.itemView.setOnClickListener {
//                // open folder
//                val intent = Intent(mContext, FullScreenIconShortcutActivity::class.java)
//                mRootNewLinks?.folders?.let { list ->
//                    intent.putExtra("FolderNewList", Gson().toJson(list))
//                    mContext.startActivity(intent)
//                }
//
//            }
        }
    }

    fun setImageData(imgView: ImageView, path: String?, name: String?) {
        Glide.with(imgView)
            .load(path)//R.drawable.icons8folder192
            .error(
                AvatarGenerator.avatarImage(
                    imgView.context,
                    12,
                    AvatarConstants.CIRCLE,
                    name ?: "S",
                ),
            )
            .error(
                AvatarGenerator.avatarImage(
                    imgView.context,
                    12,
                    AvatarConstants.CIRCLE,
                    name ?: "S",
                ),
            )
            .into(imgView)
    }
}

class FolderShortCutVH(view: View) : RecyclerView.ViewHolder(view) {
    var binding: ItemShortcutFolderBinding = ItemShortcutFolderBinding.bind(view)
    val imgView: ImageView = view.findViewById(R.id.ivIconFolder)
    val tvName: TextView = view.findViewById(R.id.tvName)
}

class FolderShortCutScreenAdapter(
    val folders: List<*>?,
    val mContext: Context,
) :
    RecyclerView.Adapter<FolderShortCutVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderShortCutVH {
        val item = FolderShortCutVH(
            LayoutInflater.from(mContext).inflate(
                R.layout.item_shortcut_folder, parent,
                false,
            ),
        )
//        val params = item.itemView.layoutParams
        val params =
            item.itemView.layoutParams as ViewGroup.MarginLayoutParams // Cast to MarginLayoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.setMargins(0, 36, 0, 36) // (left, top, right, bottom)
        item.itemView.layoutParams = params


        return item
    }

    override fun getItemCount(): Int {
        return folders?.count() ?: 0
    }

    override fun onBindViewHolder(holder: FolderShortCutVH, position: Int) {
        try {
            // Get the text size from dimens.xml
            val textSize =
                mContext.resources.getDimension(R.dimen.size_txt) // Returns size in pixels
// Set the text size to the TextView
            holder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            val itemRootLink = folders?.get(position)
            var labelName = "S"
            var url = ""
            var icon = ""
            var links: List<LinkNew>? = null
            if (itemRootLink is FolderNew) {
                labelName = itemRootLink?.name ?: "F"
                url = itemRootLink?.url ?: ""
                links = itemRootLink?.links
                icon = itemRootLink?.icon ?: ""
            } else if (itemRootLink is LinkNew) {
                labelName = itemRootLink?.name ?: "S"
                url = itemRootLink?.url ?: ""
                links = itemRootLink?.links
                icon = itemRootLink?.icon ?: ""
            }

            val iconImg = AvatarGenerator.AvatarBuilder(holder.itemView.context)
                .setLabel(labelName)
                .setAvatarSize(70)
                .setTextSize(14)
                .toCircle()
                .setBackgroundColor(Color.parseColor("#FFCA28"))
                .build()
            if (links?.isEmpty() == true || url.isNotEmpty()) {
                Glide.with(holder.imgView)
                    .load(icon)
                    .placeholder(iconImg)
                    .error(iconImg)
                    .into(holder.imgView)
                holder.binding.llFolder.isVisible = false
            } else {

                holder.imgView.setImageResource(R.drawable.bg_placeholder)
                Glide.with(holder.imgView)
                    .load(R.drawable.bg_placeholder)
                    .into(holder.imgView)

                holder.binding.llFolder.isVisible = true
                val count = links?.count() ?: 0
                holder.binding.iv1.isVisible = false
                holder.binding.iv2.isVisible = false
                holder.binding.iv3.isVisible = false
                holder.binding.iv4.isVisible = false
                when (count) {
                    1 -> {
                        holder.binding.sp1.isVisible = false
                        holder.binding.sp2.isVisible = false
                        holder.binding.iv1.isVisible = true
                        holder.binding.iv2.isVisible = false
                        holder.binding.iv3.isVisible = false
                        holder.binding.iv4.isVisible = false
                        setImageData(
                            holder.binding.iv1,
                            links?.get(0)?.icon,
                            links?.get(0)?.name,
                        )
                    }

                    2 -> {
                        holder.binding.sp1.isVisible = true
                        holder.binding.sp2.isVisible = false

                        holder.binding.iv1.isVisible = true
                        holder.binding.iv2.isVisible = true
                        holder.binding.iv3.isVisible = false
                        holder.binding.iv4.isVisible = false
                        setImageData(
                            holder.binding.iv2,
                            links?.get(1)?.icon,
                            links?.get(1)?.name,
                        )
                        setImageData(
                            holder.binding.iv1,
                            links?.get(0)?.icon,
                            links?.get(0)?.name,
                        )
                    }

                    3 -> {
                        holder.binding.sp1.isVisible = true
                        holder.binding.sp2.isVisible = false

                        holder.binding.iv1.isVisible = true
                        holder.binding.iv2.isVisible = true
                        holder.binding.iv3.isVisible = true
                        holder.binding.iv4.isVisible = false
                        setImageData(
                            holder.binding.iv3,
                            links?.get(2)?.icon,
                            links?.get(2)?.name,
                        )
                        setImageData(
                            holder.binding.iv2,
                            links?.get(1)?.icon,
                            links?.get(1)?.name,
                        )
                        setImageData(
                            holder.binding.iv1,
                            links?.get(0)?.icon,
                            links?.get(0)?.name,
                        )
                    }

                    else -> {
                        holder.binding.sp1.isVisible = true
                        holder.binding.sp2.isVisible = true

                        holder.binding.iv1.isVisible = true
                        holder.binding.iv2.isVisible = true
                        holder.binding.iv3.isVisible = true
                        holder.binding.iv4.isVisible = true
                        setImageData(
                            holder.binding.iv4,
                            links?.get(3)?.icon,
                            links?.get(3)?.name,
                        )
                        setImageData(
                            holder.binding.iv3,
                            links?.get(2)?.icon,
                            links?.get(2)?.name,
                        )
                        setImageData(
                            holder.binding.iv2,
                            links?.get(1)?.icon,
                            links?.get(1)?.name,
                        )
                        setImageData(
                            holder.binding.iv1,
                            links?.get(0)?.icon,
                            links?.get(0)?.name,
                        )
                    }
                }


            }
            holder.tvName.text = labelName
            holder.itemView.setOnClickListener {

                if (url.isEmpty() == true) {
                    // open folder
                    val intent = Intent(mContext, FullScreenIconShortcutActivity::class.java)
                    if (itemRootLink is FolderNew) {
                        intent.putExtra("FolderNew", itemRootLink)
                    }
                    mContext.startActivity(intent)
                } else {
                    // open url
                    url.let { link ->
                        openURLInBrowser(
                            mContext, link,
                            holder.itemView.clipBounds,
                            null,
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    fun setImageData(imgView: ImageView, path: String?, name: String?) {
        Glide.with(imgView)
            .load(path)//R.drawable.icons8folder192
            .error(
                AvatarGenerator.avatarImage(
                    imgView.context,
                    12,
                    AvatarConstants.CIRCLE,
                    name ?: "S",
                ),
            )
            .error(
                AvatarGenerator.avatarImage(
                    imgView.context,
                    12,
                    AvatarConstants.CIRCLE,
                    name ?: "S",
                ),
            )
            .into(imgView)
    }
}
