package app.cloudgame.web.pages

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.cloudgame.web.Configuration
import app.cloudgame.web.R
import app.cloudgame.web.components.ConfirmItem
import app.cloudgame.web.components.FormSwitch
import app.cloudgame.web.components.InputDialog
import app.cloudgame.web.webview.clearWebViewUserDataAndCache

val speedList = listOf(0.2f, 0.3f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 2.5f)

fun getMouseSpeed(id: Int): Float {
    return speedList[id]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(saveTombFile: () -> Unit) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(Configuration.getConfiguration().mouseSpeedLevel) }

    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.setting)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (context is Activity) {
                            context.finish()
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(it)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth().clickable {
                    expanded = true
                },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.mouse_rate),
                    modifier = Modifier.width(120.dp)
                )
                Box {
                    Text(
                        text = "${getMouseSpeed(selectedIndex)}"
                    )
                    DropdownMenu(
                        modifier = Modifier.width(80.dp),
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        speedList.forEachIndexed { index, _ ->
                            DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    Configuration.getConfiguration().mouseSpeedLevel = index
                                    selectedIndex = index
                                },
                                text = {
                                    Text(speedList[index].toString())
                                }
                            )
                        }
                    }
                }

            }
            InputDialog(
                defaultValueGetter = {
                    Configuration.getConfiguration().userAgent
                },
                onValueSave = {
                    Configuration.getConfiguration().setStringValue(Configuration.USER_AGENT, it)
                },
                title = stringResource(R.string.input_user_agent),
                keyName = stringResource(R.string.input_user_agent)
            )

            ConfirmItem(
                name = stringResource(R.string.clear_browser_data),
                onConfirm = {
                    clearWebViewUserDataAndCache(context)
                },
                tip = stringResource(R.string.confirm_clear_browser_data)
            )
            FormSwitch(name = stringResource(R.string.inject_console), Configuration.V_CONSOLE)
            FormSwitch(name = stringResource(R.string.check_update_on_launch), Configuration.CHECK_UPDATE_ON_LAUNCH)
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clickable { saveTombFile() },
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.save_tombstones_file))
            }
        }
    }
}