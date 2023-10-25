package app.cloudgame.web

import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.cloudgame.web.pages.MainPage
import app.cloudgame.web.pages.SettingsPage
import com.king.app.dialog.AppDialog
import com.king.app.dialog.AppDialogConfig
import com.king.app.updater.AppUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


const val PAGE_SETTING = "setting"
class ContentActivity: ComponentActivity() {

    private var hasCheck = false
    override fun onCreate(savedInstanceState: Bundle?) {

        window.statusBarColor = getColor(R.color.transparent)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true

        val page = intent.getStringExtra("page")

        super.onCreate(savedInstanceState)
        setContent {
            if (page == PAGE_SETTING) {
                SettingsPage(
                    saveTombFile = {
                        zipAndSaveTombstones()
                    }
                )
            } else {
                check()
                MainPage()
            }
        }
    }

    private fun check() {
        if (hasCheck) {
            return
        }
        hasCheck = true;
        if (Configuration.getConfiguration().readBooleanValue(Configuration.CHECK_UPDATE_ON_LAUNCH)) {
            UpdateChecker.checkUpdate(this)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
                createZipAndSaveToMediaStore()
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        }

    private fun zipAndSaveTombstones() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
        } else {
            createZipAndSaveToMediaStore()
        }
    }

    private fun createZipAndSaveToMediaStore() {
        CoroutineScope(Dispatchers.IO).launch {
            val filesDir = filesDir
            val tombstonesDir = File(filesDir, "tombstones")

            if (!tombstonesDir.exists()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ContentActivity, "Tombstones directory not found!", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val zipFileName = System.currentTimeMillis().toString() + ".zip"
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, zipFileName)
                put(MediaStore.Files.FileColumns.MIME_TYPE, "application/zip")
                put(
                    MediaStore.Files.FileColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS
                )
            }

            val zipUri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            zipUri?.let {
                resolver.openOutputStream(it)?.use { os ->
                    zip(tombstonesDir, os)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ContentActivity, "Tombstones save at /Documents/${zipFileName}" , Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ContentActivity, "Error creating zip!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun zip(directory: File, zipOutputStream: OutputStream) {
        ZipOutputStream(zipOutputStream).use { zos ->
            directory.walk().forEach { file ->
                if (file.isFile) {
                    val zipEntry = ZipEntry(file.relativeTo(directory).path)
                    zos.putNextEntry(zipEntry)
                    file.inputStream().use { fis ->
                        fis.copyTo(zos)
                    }
                    zos.closeEntry()
                }
            }
        }
    }
}
