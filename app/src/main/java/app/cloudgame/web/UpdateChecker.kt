package app.cloudgame.web

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.king.app.dialog.AppDialog
import com.king.app.dialog.AppDialogConfig
import com.king.app.updater.AppUpdater
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException
import java.lang.Exception

object UpdateChecker {
    private const val GITHUB_RELEASE_URL = "https://api.github.com/repos/tsioam/game-patch/releases"

    fun checkForUpdates(
        currentVersion: String,
        onNoVersion: () -> Unit,
        onNewVersion: (String, String, String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(GITHUB_RELEASE_URL)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                onFailure(e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.body?.let {
                    try {
                        val releasesArray = JSONArray(it.string())
                        if (releasesArray.length() > 0) {
                            val latestReleaseObject = releasesArray.getJSONObject(0)
                            val latestVersion = latestReleaseObject.getString("tag_name")
                            val description = latestReleaseObject.getString("body")
                            if (isNewerVersion(currentVersion, latestVersion)) {
                                val assets = latestReleaseObject.getJSONArray("assets")
                                var apkDownloadLink= "zzz"
                                for (i in 0 until assets.length()) {
                                    val asset = assets.getJSONObject(i)
                                    val assetName = asset.getString("name")
                                    if (assetName.endsWith(".apk")) {
                                        apkDownloadLink = asset.getString("browser_download_url")
                                        break
                                    }
                                }
                                onNewVersion(latestVersion, apkDownloadLink, description)
                            } else {
                                onNoVersion()
                            }
                        } else {
                            onNoVersion()
                        }
                    } catch (e: Exception) {
                        onFailure(e)
                    }
                }
            }
        })
    }

    private fun isNewerVersion(currentVersion: String, newVersion: String): Boolean {
        val currentVersionParts = currentVersion.replace("v", "").split(".")
        val newVersionParts = newVersion.replace("v", "").split(".")

        for (i in 0 until minOf(currentVersionParts.size, newVersionParts.size)) {
            if (newVersionParts[i].toInt() > currentVersionParts[i].toInt()) {
                return true
            } else if (newVersionParts[i].toInt() < currentVersionParts[i].toInt()) {
                return false
            }
        }

        return newVersionParts.size > currentVersionParts.size
    }

    private fun showUpdateDialog(context: Context, latestVersion: String, url: String, desc: String) {
        val config = AppDialogConfig(context)
        config.setTitle("${context.getString(R.string.new_version)} $latestVersion")
            .setConfirm(context.getString(R.string.upgrade))
            .setCancel(context.getString(R.string.cancel))
            .setContent(desc).onClickConfirm = View.OnClickListener {
            AppUpdater.Builder(context)
                .setUrl(url)
                .setShowNotification(true)
                .build()
                .start();
            AppDialog.INSTANCE.dismissDialog();
        }
        AppDialog.INSTANCE.showDialog(context, config)
    }

    fun checkUpdate(context: Context) {
        checkForUpdates(
            BuildConfig.VERSION_NAME,
            onNewVersion = { latestVersion, url, description ->
                Handler(Looper.getMainLooper()).post {
                    showUpdateDialog(context, latestVersion, url, description)
                }
            },
            onFailure = { error ->
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            },
            onNoVersion = {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, context.getText(R.string.no_new_version), Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}