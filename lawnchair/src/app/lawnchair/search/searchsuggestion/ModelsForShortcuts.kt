package app.lawnchair.search.searchsuggestion

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RootNewLinks(
    val root_links: List<LinkNew>?,
    val folders: List<FolderNew>?,
) : Parcelable

@Parcelize
data class FolderNew(
    val name: String?,
    val icon: String?,
    val url: String?,
    val links: List<LinkNew>?,
    val folders: List<FolderNew>?,
) : Parcelable

@Parcelize
data class LinkNew(
    val name: String?,
    val url: String?,
    val icon: String?,
    val links: List<LinkNew>?,
) : Parcelable
