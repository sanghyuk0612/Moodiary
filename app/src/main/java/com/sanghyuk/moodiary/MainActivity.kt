package com.sanghyuk.moodiary

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sanghyuk.designsystem.theme.MoodiaryTheme
import com.sanghyuk.feature.calendar.CalendarRoute
import com.sanghyuk.feature.home.HomeRoute
import com.sanghyuk.feature.statistics.StatisticsRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoodiaryTheme {
                MoodiaryApp()
            }
        }
    }
}

@Composable
private fun MoodiaryApp() {
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.Today) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                BottomTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(text = tab.iconText) },
                        label = { Text(text = stringResource(tab.labelResId)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)

        when (selectedTab) {
            BottomTab.Calendar -> CalendarRoute(modifier = contentModifier)
            BottomTab.Today -> HomeRoute(modifier = contentModifier)
            BottomTab.Statistics -> StatisticsRoute(modifier = contentModifier)
        }
    }
}

private enum class BottomTab(
    @StringRes val labelResId: Int,
    val iconText: String,
) {
    Calendar(labelResId = R.string.bottom_nav_calendar, iconText = "Cal"),
    Today(labelResId = R.string.bottom_nav_today, iconText = "Day"),
    Statistics(labelResId = R.string.bottom_nav_statistics, iconText = "Stat"),
}
