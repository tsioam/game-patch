package app.cloudgame.web.pages

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloudgame.web.BuildConfig
import app.cloudgame.web.ContentActivity
import app.cloudgame.web.PAGE_SETTING
import app.cloudgame.web.R
import app.cloudgame.web.webview.LICENSE
import app.cloudgame.web.webview.TG_GROUP
import app.cloudgame.web.webview.isTelegramInstalled

@Composable
fun MenuItem(text: String, onClick: () -> Unit, hideArrow: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, fontSize = 18.sp)

        if (!hideArrow) {
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = text,
                Modifier.size(16.dp).alpha(0.9f)
            )
        }
    }
}

@Composable
fun AboutPage(paddingValues: PaddingValues, onCheckUpdates: () -> Unit) {
    val context = LocalContext.current
    var showLicenseDialog by remember { mutableStateOf(false) }
    val licenseScrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Icon",
            modifier = Modifier
                .size(80.dp)
                .clip(shape = androidx.compose.foundation.shape.CircleShape)
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(12.dp).height(4.dp))
        Text(text = "${stringResource(R.string.app_name)} ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})", fontSize = 16.sp)
        Text(
            text = stringResource(R.string.app_desc),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .padding(PaddingValues(15.dp, 0.dp))
                .shadow(3.dp, shape = RoundedCornerShape(5.dp))
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Menu items
            MenuItem(stringResource(R.string.setting), onClick = {
                val intent = Intent(context, ContentActivity::class.java)
                intent.putExtra("page", PAGE_SETTING)
                context.startActivity(intent)
            })
            Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0f, 0f,0f, 0.1f)))
            MenuItem(stringResource(R.string.opensource_license), onClick = {
                showLicenseDialog = true
            })
            Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0f, 0f,0f, 0.1f)))
            MenuItem(stringResource(R.string.check_update), onClick = onCheckUpdates, hideArrow = true)

            if (isTelegramInstalled(context)) {
                Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0f, 0f,0f, 0.1f)))
                MenuItem("Telegram", hideArrow = true, onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TG_GROUP))
                    context.startActivity(intent)
                })
            }
        }

    }

    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            title = { Text(stringResource(R.string.opensource_license)) },
            text = {
                Box(
                    modifier = Modifier
                        .verticalScroll(licenseScrollState)
                ) {
                    Text(LICENSE)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showLicenseDialog = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
        )
    }
}
