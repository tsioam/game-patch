package app.cloudgame.web.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.cloudgame.web.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputDialog(
    title: String = stringResource(R.string.input_title),
    keyName: String,
    defaultValueGetter: () -> String,
    onValueSave: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(defaultValueGetter()) }

    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(keyName)
        Text(
            overflow = TextOverflow.Ellipsis,
            text = textValue,
            modifier = Modifier.width(150.dp).alpha(0.8f).clickable {
                showDialog = true
            },
            maxLines = 2
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                TextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    modifier = Modifier.padding(8.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                }) {
                    onValueSave(textValue)
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDialog = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

