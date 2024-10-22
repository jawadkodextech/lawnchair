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

package app.lawnchair.ui.preferences.destinations

//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.lawnchair.hotseat.HotseatMode
import app.lawnchair.hotseat.LawnchairHotseat
import app.lawnchair.preferences.PreferenceAdapter
import app.lawnchair.preferences.getAdapter
import app.lawnchair.preferences.preferenceManager
import app.lawnchair.preferences2.preferenceManager2
import app.lawnchair.qsb.providers.QsbSearchProvider
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.components.NavigationActionPreference
import app.lawnchair.ui.preferences.components.colorpreference.ColorPreference
import app.lawnchair.ui.preferences.components.controls.ListPreference
import app.lawnchair.ui.preferences.components.controls.ListPreferenceEntry
import app.lawnchair.ui.preferences.components.controls.MainSwitchPreference
import app.lawnchair.ui.preferences.components.controls.SliderPreference
import app.lawnchair.ui.preferences.components.controls.SwitchPreference
import app.lawnchair.ui.preferences.components.layout.DividerColumn
import app.lawnchair.ui.preferences.components.layout.ExpandAndShrink
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.ui.preferences.components.layout.PreferenceLayout
import com.android.launcher3.R


object DockRoutes {
    const val SEARCH_PROVIDER = "searchProvider"
}

@Composable
fun DockPreferences(
    modifier: Modifier = Modifier,
) {
    val prefs = preferenceManager()
    val prefs2 = preferenceManager2()
    PreferenceLayout(
        label = stringResource(id = R.string.dock_label),
        backArrowVisible = !LocalIsExpandedScreen.current,
        modifier = modifier,
    ) {
        val isHotseatEnabled = prefs2.isHotseatEnabled.getAdapter()
        val hotseatModeAdapter = prefs2.hotseatMode.getAdapter()
        MainSwitchPreference(
            adapter = isHotseatEnabled,
            label = stringResource(id = R.string.show_hotseat_title),
        ) {
            PreferenceGroup(heading = stringResource(id = R.string.search_bar_label)) {
                HotseatModePreference(
                    adapter = hotseatModeAdapter,
                )
//                HorizontalAppList( apps = sampleAppIcons)
                ExpandAndShrink(visible = hotseatModeAdapter.state.value == LawnchairHotseat) {
                    DividerColumn {
                        val hotseatQsbProviderAdapter by preferenceManager2().hotseatQsbProvider.getAdapter()
                        NavigationActionPreference(
                            label = stringResource(R.string.search_provider),
                            destination = DockRoutes.SEARCH_PROVIDER,
                            subtitle = stringResource(
                                id = QsbSearchProvider.values()
                                    .first { it == hotseatQsbProviderAdapter }
                                    .name,
                            ),
                        )
                        SwitchPreference(
                            adapter = prefs2.themedHotseatQsb.getAdapter(),
                            label = stringResource(id = R.string.apply_accent_color_label),
                        )
                        SliderPreference(
                            label = stringResource(id = R.string.corner_radius_label),
                            adapter = prefs.hotseatQsbCornerRadius.getAdapter(),
                            step = 0.05F,
                            valueRange = 0F..1F,
                            showAsPercentage = true,
                        )
                        SliderPreference(
                            label = stringResource(id = R.string.qsb_hotseat_background_transparency),
                            adapter = prefs.hotseatQsbAlpha.getAdapter(),
                            step = 5,
                            valueRange = 0..100,
                            showUnit = "%",
                        )
                        val qsbHotseatStrokeWidth = prefs.hotseatQsbStrokeWidth.getAdapter()

                        SliderPreference(
                            label = stringResource(id = R.string.qsb_hotseat_stroke_width),
                            adapter = qsbHotseatStrokeWidth,
                            step = 1f,
                            valueRange = 0f..10f,
                            showUnit = "vw",
                        )
                        ExpandAndShrink(visible = qsbHotseatStrokeWidth.state.value > 0f) {
                            ColorPreference(preference = prefs2.strokeColorStyle)
                        }
                    }
                }

            }
            PreferenceGroup(heading = stringResource(id = R.string.grid)) {
                SliderPreference(
                    label = stringResource(id = R.string.dock_icons),
                    adapter = prefs.hotseatColumns.getAdapter(),
                    step = 1,
                    valueRange = 3..10,
                )
                SliderPreference(
                    adapter = prefs2.hotseatBottomFactor.getAdapter(),
                    label = stringResource(id = R.string.hotseat_bottom_space_label),
                    valueRange = 0.0F..1.7F,
                    step = 0.1F,
                    showAsPercentage = true,
                )
            }
        }
    }
}

@Composable
private fun HotseatModePreference(
    adapter: PreferenceAdapter<HotseatMode>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val entries = remember {
        HotseatMode.values().map { mode ->
            ListPreferenceEntry(
                value = mode,
                label = { stringResource(id = mode.nameResourceId) },
                enabled = mode.isAvailable(context = context),
            )
        }
    }

    ListPreference(
        adapter = adapter,
        entries = entries,
        label = stringResource(id = R.string.hotseat_mode_label),
        modifier = modifier,
    )
}


// Example data class for representing an icon (or app)
data class AppIcon(val name: String, val icon: Int)

// Sample data for the horizontal scroll view
val sampleAppIcons = listOf(
    AppIcon("App 1", R.drawable.themed_icon_calendar_1), // Replace with actual icons
    AppIcon("App 2", R.drawable.themed_icon_calendar_2),
    AppIcon("App 3", R.drawable.themed_icon_calendar_3),
    AppIcon("App 4", R.drawable.themed_icon_calendar_4),
    AppIcon("App 5", R.drawable.themed_icon_calendar_5),
)

// Horizontal list (LazyRow) composable
@Composable
fun HorizontalAppList(apps: List<AppIcon>, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(apps) { app ->
            AppIconItem(app)
        }
    }
}

// Composable for rendering each app icon
@Composable
fun AppIconItem(app: AppIcon, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.size(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Replace with Image() for actual app icons, example uses Box
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Gray), // Replace with Image displaying the icon
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = app.name)
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewHorizontalAppList() {
    HorizontalAppList(apps = sampleAppIcons)
}
