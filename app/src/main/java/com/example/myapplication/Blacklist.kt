package com.example.myapplication

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.os.PersistableBundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection.Companion.Content
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.example.myapplication.ui.theme.MyApplicationTheme

class Blacklist : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun Content() {
    val context = LocalContext.current
    val pm = context.packageManager

//    var showDialog by remember {
//        mutableStateOf(0)
//    }
    var selectedResolveInfo by remember {
        mutableStateOf(-1)
    }
    var secondUsage by remember {
        mutableStateOf("")
    }
    var minuteUsage by remember {
        mutableStateOf("")
    }
    var hourUsage by remember {
        mutableStateOf("")
    }
    val secondChange : (String) -> Unit = { it ->
        secondUsage = it
    }
    val minuteChange : (String) -> Unit = { it ->
        minuteUsage = it
    }
    val hourChange : (String) -> Unit = { it ->
        hourUsage = it
    }
    // Find main activities, disable finding system apps
    val mainIntent = Intent(Intent.ACTION_MAIN, null)
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

    var resolvedInfo: List<ResolveInfo> = pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
//    resolvedInfo = resolvedInfo.filter { resolveInfo -> resolveInfo.resolvePackageName != null }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Choose apps to blacklist")
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            itemsIndexed(resolvedInfo) {
                idx, item -> ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                onClick = {
                    selectedResolveInfo = idx
                },
                modifier = Modifier
                    .size(width = 240.dp, height = 30.dp),
            ) {
                    Row {
                        Image(
                            bitmap = getAppImageBitmap(pm, item),
                            contentDescription = "app icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = getAppName(pm, item),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(alignment = Alignment.CenterVertically)
                        )
    //                    Text(text = item.resolvePackageName)
                    }
                }
            }
        }
    }
    if (selectedResolveInfo != -1) {
        val resolvedInfoItem = resolvedInfo[selectedResolveInfo]
        Dialog(
            onDismissRequest = { selectedResolveInfo = -1 }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(375.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Image(
                        bitmap = getAppImageBitmap(pm, resolvedInfoItem),
                        contentDescription = "Card image"
                    )
                    Text(
                        getAppName(pm, resolvedInfoItem),
                        modifier = Modifier.padding(all = 16.dp)
                    )
                    Text(
                        "Set daily time limit",
                        modifier = Modifier.padding(all = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            label = { Text(text = "Hour")},
                            value=hourUsage,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            onValueChange = hourChange,
                            modifier = Modifier
                                .weight(.2f)
                                .background(Color.White),
//                            isError = {hourUsage -> hourUsage.all { char -> char.isDigit() }}
//                            isError = { hourUsage.all { char -> char.isDigit() }}
                        )
                        OutlinedTextField(
                            label = { Text(text = "Minute")},
                            value=minuteUsage,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            onValueChange = minuteChange,
                            modifier = Modifier.weight(.2f)
                        )
                        OutlinedTextField(
                            label = { Text(text = "Second")},
                            value=secondUsage,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            onValueChange = secondChange,
                            modifier = Modifier.weight(.2f)
                        )
                    }
                    // TODO: Get usage statistics here from UsageService
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ){
                        TextButton(
                            onClick = { selectedResolveInfo = -1 }
                        ) {
                            Text("Dismiss")
                        }
                        TextButton(
                            onClick = {
                                /*TODO: Set usage limit*/
                            }
                        ) {
                            Text("Set usage")
                        }
                    }
                }
            }
        }
    }
}

fun isValidTime(text: String): Boolean {
    return true
//    return text.matche
}

fun getAppImageBitmap(pm: PackageManager, resolveInfo: ResolveInfo): ImageBitmap {
    return resolveInfo.loadIcon(pm).toBitmap().asImageBitmap()
}

fun getAppName(pm: PackageManager, resolveInfo: ResolveInfo): String {
    val resources =  pm.getResourcesForApplication(resolveInfo.activityInfo.applicationInfo)
    if (resolveInfo.activityInfo.labelRes != 0) {
        return resources.getString(resolveInfo.activityInfo.labelRes)
    } else {
        return resolveInfo.activityInfo.applicationInfo.loadLabel(pm).toString()
    }
}
