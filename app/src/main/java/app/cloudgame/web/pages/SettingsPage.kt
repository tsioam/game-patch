package app.cloudgame.web.pages

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.cloudgame.web.Configuration
import app.cloudgame.web.R
import app.cloudgame.web.components.FormSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(saveTombFile: () -> Unit) {
    val context = LocalContext.current
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