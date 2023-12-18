package app.cloudgame.web.pages

import android.content.Intent
import android.text.TextUtils
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloudgame.web.Configuration
import app.cloudgame.web.R
import app.cloudgame.web.UpdateChecker
import app.cloudgame.web.WebActivity
import app.cloudgame.web.components.FormSwitch
import com.google.gson.Gson
import java.net.URL

data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

fun isYsHost(url: String): Boolean {
    return try {
        Configuration.YS_DOMAIN == URL(url).host
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(paddingValues: PaddingValues) {
    val context = LocalContext.current
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(paddingValues)
    ) {

        var skipYsUserGuide by remember { mutableStateOf(Configuration.getConfiguration().readBooleanValue(Configuration.FORCE_DISABLE_USER_GUIDE)) }
        var launchUrl by remember { mutableStateOf(Configuration.getConfiguration().getStringValue(Configuration.LAUNCH_URL, Configuration.DEFAULT_URL)) }

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            TextField(
                value = launchUrl,
                onValueChange = { launchUrl = it },
                label = { Text(stringResource(R.string.url)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .shadow(2.dp),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(
                        Configuration.DEFAULT_URL,
                        fontSize = 10.sp,
                        modifier = Modifier.alpha(0.4f)
                    )
                }
            )
            Text(
                text = stringResource(R.string.reset_to_default),
                modifier = Modifier
                    .clickable {
                        launchUrl = Configuration.DEFAULT_URL
                    }
                    .align(Alignment.TopEnd)
                    .padding(PaddingValues(10.dp, 6.dp)),
                fontSize = 12.sp,
                textDecoration = TextDecoration.Underline
            )
        }

        Spacer(Modifier.height(2.dp))

        AnimatedVisibility(
            visible = TextUtils.isEmpty(launchUrl) || isYsHost(launchUrl),
            enter = slideInVertically(
                initialOffsetY = { -40 },
                animationSpec = tween(300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { -40 },
                animationSpec = tween(300)
            )
        ) {
            FormSwitch(
                name = stringResource(R.string.skip_user_guide),
                key = Configuration.FORCE_DISABLE_USER_GUIDE,
                onCheckedChanged = {
                    skipYsUserGuide = it
                }
            )
        }

        Button(
            modifier = Modifier.clip(
                RoundedCornerShape(15.dp)
            ),
            onClick = {
                val intent = Intent(context, WebActivity::class.java)
                try {
                    URL(launchUrl)
                    intent.putExtra(WebActivity.URL, launchUrl)
                } catch (e: Exception) {
                    Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val loadedScripts = ArrayList<String>()
                if (skipYsUserGuide) {
                    loadedScripts.add(Configuration.FORCE_DISABLE_USER_GUIDE)
                }
                val gson = Gson()
                intent.putExtra(WebActivity.LOADED_SCRIPT, gson.toJson(loadedScripts))
                Configuration.getConfiguration().setStringValue(Configuration.LAUNCH_URL, launchUrl)
                context.startActivity(intent)
            }
        ) {
            Text(stringResource(R.string.launch))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val bottomNavItems = listOf(
        BottomNavItem(stringResource(R.string.nav_home), Icons.Default.Home),
        BottomNavItem(stringResource(R.string.shortcut), Icons.Default.Favorite),
        BottomNavItem(stringResource(R.string.nav_about), Icons.Default.Info),
    )

    Scaffold(
        bottomBar = {
            BottomNavigation(
                backgroundColor = Color.White,
                contentColor = Color.Black
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    BottomNavigationItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                        }
                    )
                }
            }
        },
        content = {
            when (selectedTab) {
                0 -> Home(it)
                1 -> CollectionsPage(it)
                2 -> AboutPage(
                    paddingValues = it,
                    onCheckUpdates = {
                        UpdateChecker.checkUpdate(context)
                    }
                )
            }
        }
    )
}