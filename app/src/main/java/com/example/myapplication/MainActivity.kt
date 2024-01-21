package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme


class MainActivity : ComponentActivity() {

    private val OVERLAY_PERMISSION_REQUEST_CODE = 1
    /** Requests an overlay permission to the user if needed. */
    private fun requestOverlayPermission() {
        if (isOverlayGranted()) {
            OverlayService.start(this@MainActivity)
            return
        }
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
    }

    /** Terminates the app if the user does not accept an overlay. */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!isOverlayGranted()) {
                finish()  // Cannot continue if not granted
            } else {
                OverlayService.start(this@MainActivity)
            }
            navigateIfOk(this)
        }
        if (requestCode == 2) {
            navigateIfOk(this)
        }
    }

    /** Checks if the overlay is permitted. */
    private fun isOverlayGranted() =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)

    fun isAccessibilitySettingsOn(mContext: Context): Boolean {
        var accessibilityEnabled = 0
        val service = (mContext.packageName
                + "/com.example.myapplication.MyAccessibilityService")
        val accessibilityFound = false
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                mContext
                    .applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: SettingNotFoundException) {
        }
        val mStringColonSplitter = SimpleStringSplitter(
            ':'
        )
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                mContext
                    .applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessabilityService = mStringColonSplitter.next()
                    if (accessabilityService.equals(service, ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return accessibilityFound
    }

    fun navigateIfOk(context: Context) {
        val overlayOk = isOverlayGranted()
        val accessOk = isAccessibilitySettingsOn(context)
        if (overlayOk && accessOk) {

        context.startActivity(Intent(context, Blacklist::class.java))
    }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(overlayAct = {
                        requestOverlayPermission()
                    }, accessibilityAct = {
                        // Open Accessibility Settings
                        startActivityForResult(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 2)
                    })
                }
            }
        }
    }

    // Equivalent of static methods
    companion object {
        fun getAllApps(pm: PackageManager): List<ResolveInfo> {
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val resolvedInfoList: List<ResolveInfo> =
                pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
            return resolvedInfoList
        }
    }
}

// background-color: #4158D0;
//background-image: linear-gradient(43deg, #4158D0 0%, #C850C0 46%, #FFCC70 100%);

@Composable
fun MainContent(overlayAct: (Context) -> Unit, accessibilityAct: (Context)->Unit) {
    val context = LocalContext.current // Getting the current context

    val listColors = listOf(Color(0xff4158D0), Color(0xffC850C0), Color(0xffEECC70))
    val customBrush =
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return LinearGradientShader(
                    colors = listColors,
                    from = Offset.Zero,
                    to = Offset(size.width, 2.8f* size.height/3.0f),
                )
            }
        }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(customBrush),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = ""
            )
            Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Welcome to ScrLk", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Get your procrastination under control!", color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                overlayAct(context)
            }) {
                Text("Allow Overlay Permissions")
            }
        Button(onClick = {
            accessibilityAct(context)
        }) {
            Text("Allow Accessibility Permissions")
        }
    } }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    MyApplicationTheme {
        MainContent(overlayAct = {}, accessibilityAct = {})
    }
}
