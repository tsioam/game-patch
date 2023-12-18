package app.cloudgame.web.pages

import android.content.Intent
import android.text.TextUtils
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloudgame.web.Configuration
import app.cloudgame.web.R
import app.cloudgame.web.WebActivity
import app.cloudgame.web.webview.isValidHttpUrl
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.net.URL
import java.util.UUID

enum class EntryType {
    Web,
    Add
}

data class UiEntry(
    val uuid: String,
    val scheme: String,
    val iconUrl: String,
    val name: String,
    var preScript: List<String>? = null,
    val type: EntryType = EntryType.Web
)

private const val ENTRY_KEY = "quick_entry"
private const val ADD_UUID= "07F089E0-D57F-6630-CCA8-1BFEA8B6F640"

val uiEntries = listOf(
    UiEntry(
        uuid = "F8316DB6-C4FA-8E4B-DB8C-5C08475D3576",
        name = "云原神",
        scheme = "https://ys.mihoyo.com/cloud/?autobegin=1&utm_source=default#/",
        iconUrl = "https://ys.mihoyo.com/main/favicon.ico",
        preScript = listOf(Configuration.FORCE_DISABLE_USER_GUIDE),
        type = EntryType.Web
    ),
    UiEntry(
        uuid = "70CBA753-D4DA-6E90-0800-5EC184695BFD",
        name = "Start云游戏",
        scheme = "https://start.qq.com/cloudgame/index.html",
        iconUrl = "https://start.gtimg.com/web/www/favicon.ico",
        type = EntryType.Web
    )
)

fun getCollectionData(): List<UiEntry> {
    val jsonData = Configuration.getConfiguration().getStringValue(ENTRY_KEY, "")
    if (TextUtils.isEmpty(jsonData)) {
        return uiEntries
    }
    return try {
        val res: List<UiEntry> = Gson().fromJson(jsonData, object : TypeToken<List<UiEntry>>() {}.type)
        res
    } catch (e: JsonSyntaxException) {
        uiEntries
    }
}

@Composable
fun EntryItem(index: Int, entry: UiEntry, showModalSheet: (UiEntry, Int) -> Unit, showCreate: () -> Unit) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { /* ... */ },
                    onLongPress = {
                        if (entry.type === EntryType.Add) {
                            return@detectTapGestures
                        }
                        showModalSheet(entry, index)
                    },
                    onTap = {
                        if (entry.type === EntryType.Add) {
                            showCreate()
                            return@detectTapGestures
                        }
                        if (TextUtils.isEmpty(entry.scheme)) {
                            return@detectTapGestures
                        }
                        if (entry.scheme.startsWith("http")) {
                            val launchUrl = entry.scheme
                            val intent = Intent(context, WebActivity::class.java)
                            try {
                                URL(launchUrl)
                                intent.putExtra(WebActivity.URL, launchUrl)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show()
                                return@detectTapGestures
                            }
                            val loadedScripts = ArrayList<String>()
                            entry.preScript?.let {
                                loadedScripts.addAll(it)
                            }
                            val gson = Gson()
                            intent.putExtra(WebActivity.LOADED_SCRIPT, gson.toJson(loadedScripts))
                            context.startActivity(intent)
                        }
                    }
                )
            }
            .clip(RoundedCornerShape(10.dp))
            .padding(12.dp),
    ) {
        if (entry.type === EntryType.Add) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add Entry",
                modifier = Modifier.size(30.dp)
            )
        } else {
            Image(
                painter = rememberAsyncImagePainter(model = entry.iconUrl),
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
        }
        Text(
            color = Color.DarkGray,
            fontSize = 12.sp,
            text = entry.name,
            modifier = Modifier
                .padding(0.dp, 10.dp, 0.dp, 0.dp)
        )
    }
}





@Composable
fun EntryGrid(entries: List<UiEntry>, showModalSheet: (UiEntry, Int) -> Unit, showCreate: () -> Unit) {
    val addEntry = UiEntry(
        name = "新增",
        scheme = "",
        iconUrl = "",
        type = EntryType.Add,
        uuid = ADD_UUID
    )
    val renderEntries = entries + addEntry
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(all = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .border(1.dp, Color(0f ,0f , 0f, 0.1f), RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp)),
        content = {
            items(renderEntries.size, key = {
                renderEntries[it].uuid
            }) { index ->
                EntryItem(index, renderEntries[index], showModalSheet, showCreate)
            }
        }
    )
}

@Composable
fun BottomSheetMenu(entry: UiEntry, onDismiss: () -> Unit, onEditEntry: () -> Unit, onRemoveEntry: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        TextButton(onClick = { onEditEntry(); onDismiss() }) {
            Text(stringResource(R.string.edit), modifier = Modifier.fillMaxWidth())
        }
        TextButton(onClick = { onRemoveEntry() ; onDismiss() }) {
            Text(stringResource(R.string.remove), modifier = Modifier.fillMaxWidth(), color = Color.Red)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CollectionsPage(paddingValues: PaddingValues) {

    val context = LocalContext.current
    val modalBottomSheetState= rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var selectedEntry by remember { mutableStateOf<UiEntry?>(null) }
    var selectedEntryIndex by remember { mutableIntStateOf(0) }
    var entries by remember { mutableStateOf(getCollectionData()) }
    val coroutineScope = rememberCoroutineScope()

    val showEntryDialog = remember { mutableStateOf(false) }
    val isAddDialog = remember { mutableStateOf(false) }
    val nameEditText = remember { mutableStateOf("") }
    val urlEditText = remember { mutableStateOf("") }
    val iconEditText = remember { mutableStateOf("") }

    ModalBottomSheetLayout(
        sheetState =  modalBottomSheetState,
        sheetContent = {
            if (selectedEntry != null) {
                BottomSheetMenu(
                    selectedEntry!!,
                    onDismiss = {
                        coroutineScope.launch { modalBottomSheetState.hide() }
                    },
                    onEditEntry = {
                        if (selectedEntry == null) {
                            return@BottomSheetMenu
                        }
                        nameEditText.value = selectedEntry!!.name
                        urlEditText.value = selectedEntry!!.scheme
                        iconEditText.value = selectedEntry!!.iconUrl
                        showEntryDialog.value = true
                    },
                    onRemoveEntry = {
                        val newList = entries.toMutableList()
                        newList.removeAt(selectedEntryIndex)
                        val json = Gson().toJson(newList)
                        Configuration.getConfiguration().setStringValue(ENTRY_KEY, json)
                        entries = newList
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize().padding(PaddingValues(
            start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
            end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
            top = 0.dp,
            bottom = paddingValues.calculateBottomPadding()
        )),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight().fillMaxWidth().padding(PaddingValues(
                start = 0.dp,
                end = 0.dp,
                top = paddingValues.calculateTopPadding(),
                bottom = 0.dp
            ))
        ) {
            EntryGrid(
                entries,
                showModalSheet = { entry, i ->
                    selectedEntry = entry
                    selectedEntryIndex = i
                    isAddDialog.value = false
                    coroutineScope.launch { modalBottomSheetState.show() }
                },
                showCreate = {
                    nameEditText.value =""
                    urlEditText.value = ""
                    iconEditText.value = ""
                    isAddDialog.value = true
                    showEntryDialog.value = true
                }
            )
        }
    }

    if (showEntryDialog.value) {
        AlertDialog(
            onDismissRequest = { showEntryDialog.value = false },
            title = { Text(stringResource(if (isAddDialog.value) {
                R.string.new_entry
            } else {
                R.string.edit
            }))},
            text = {
                Column (
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.entry_name), modifier = Modifier.padding(6.dp))
                    TextField(
                        placeholder = { Text(stringResource(R.string.entry_name), modifier = Modifier.alpha(0.6f)) },
                        value = nameEditText.value,
                        onValueChange = {
                            nameEditText.value = it
                        }
                    )
                    Text(text = stringResource(R.string.url), modifier = Modifier.padding(6.dp))
                    TextField(
                        placeholder = { Text(stringResource(R.string.url), modifier = Modifier.alpha(0.6f)) },
                        value = urlEditText.value,
                        onValueChange = {
                            urlEditText.value = it
                            try {
                                val uri = URL(it)
                                if (!TextUtils.isEmpty(uri.host)) {
                                    iconEditText.value = "${uri.protocol}://${uri.host}/favicon.ico"
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                    Text(text = stringResource(R.string.icon), modifier = Modifier.padding(6.dp))
                    TextField(
                        placeholder = { Text(
                            "${stringResource(R.string.icon)} URL",
                            modifier = Modifier.alpha(0.6f)
                        ) },
                        value = iconEditText.value,
                        onValueChange = { iconEditText.value = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isAddDialog.value) {
                            if (TextUtils.isEmpty(nameEditText.value)) {
                                Toast.makeText(context, context.getString(R.string.entry_name) + context.getString(R.string.required), Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            if (TextUtils.isEmpty(urlEditText.value)) {
                                Toast.makeText(context, context.getString(R.string.url) + context.getString(R.string.required), Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            if (TextUtils.isEmpty(iconEditText.value)) {
                                Toast.makeText(context, context.getString(R.string.icon) + context.getString(R.string.required), Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            if (!isValidHttpUrl(urlEditText.value)) {
                                Toast.makeText(context, context.getString(R.string.url) + context.getString(R.string.format_error), Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            if (!isValidHttpUrl(iconEditText.value)) {
                                Toast.makeText(context, context.getString(R.string.icon) + context.getString(R.string.format_error), Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            val newList = entries + UiEntry(
                                name = nameEditText.value,
                                scheme = urlEditText.value,
                                iconUrl = iconEditText.value,
                                type = EntryType.Web,
                                uuid = UUID.randomUUID().toString()
                            )
                            val json = Gson().toJson(newList)
                            Configuration.getConfiguration().setStringValue(ENTRY_KEY, json)
                            entries = newList
                            showEntryDialog.value = false
                        } else {
                            val newList = entries.toMutableList()
                            newList[selectedEntryIndex] = UiEntry(
                                name = nameEditText.value,
                                scheme = urlEditText.value,
                                iconUrl = iconEditText.value,
                                uuid = UUID.randomUUID().toString()
                            )
                            val json = Gson().toJson(newList)
                            Configuration.getConfiguration().setStringValue(ENTRY_KEY, json)
                            entries = newList
                            showEntryDialog.value = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEntryDialog.value = false
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

}