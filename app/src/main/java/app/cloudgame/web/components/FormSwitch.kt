package app.cloudgame.web.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cloudgame.web.Configuration

@Composable
fun FormSwitch(name: String, key: String, onCheckedChanged: ((Boolean) -> Unit)? = null) {

    var status by rememberSaveable { mutableStateOf(Configuration.getConfiguration().readBooleanValue(key)) }

    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(name)
        Switch(
            checked =  status,
            onCheckedChange = {
                status = it
                Configuration.getConfiguration().setBooleanValue(key, it)
                Configuration.getConfiguration().commitConfig()
                if (onCheckedChanged != null) {
                    onCheckedChanged(it)
                }
            }
        )
    }
}