package com.example.simplelauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class AppInfo(
    val label: String,
    val packageName: String
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinimalLauncherTheme {
                val context = LocalContext.current
                val apps = remember { getInstalledApps(context.packageManager) }
                val scaffoldState = rememberBottomSheetScaffoldState()

                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 40.dp,
                    sheetContainerColor = Color.Black,
                    sheetContentColor = Color.White,
                    sheetContent = {
                        AppDrawer(apps) { pkg ->
                            launchApp(context, pkg)
                        }
                    },
                    containerColor = Color.Black
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TypographicClock()
                            BatteryStatus()
                            Spacer(modifier = Modifier.height(48.dp))
                            FavoriteApps { pkg -> launchApp(context, pkg) }
                            Spacer(modifier = Modifier.weight(1f))
                            TodoMinimal()
                        }
                        
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            QuickActions(
                                onDialer = { launchApp(context, "com.android.dialer") },
                                onWhatsApp = { launchApp(context, "com.whatsapp") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MinimalLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color.White,
            background = Color.Black,
            surface = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}

@Composable
fun TypographicClock() {
    var time by remember { mutableStateOf(LocalDateTime.now()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            time = LocalDateTime.now()
            delay(1000)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
            fontSize = 100.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = time.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")).uppercase(),
            fontSize = 18.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun BatteryStatus() {
    val context = LocalContext.current
    var batteryLevel by remember { mutableIntStateOf(0) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                batteryLevel = (level * 100 / scale.toFloat()).toInt()
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Text(
        text = "$batteryLevel% PWR",
        fontSize = 14.sp,
        color = Color.Gray,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun FavoriteApps(onLaunch: (String) -> Unit) {
    val favorites = listOf(
        "NOTES" to "com.google.android.keep",
        "CALENDAR" to "com.google.android.calendar",
        "CAMERA" to "android.media.action.STILL_IMAGE_CAMERA", // Intent action or specific package
        "MAPS" to "com.google.android.apps.maps",
        "MUSIC" to "com.google.android.apps.youtube.music"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        favorites.forEach { (label, target) ->
            Text(
                text = label,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                modifier = Modifier.clickable { 
                    if (label == "CAMERA") {
                        // Handle Camera specially if it's an action
                        onLaunch("CAMERA_ACTION")
                    } else {
                        onLaunch(target)
                    }
                }
            )
        }
    }
}

@Composable
fun TodoMinimal() {
    val todos = remember { mutableStateListOf<String>() }
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        todos.forEach { todo ->
            Text(
                text = todo,
                color = Color.LightGray,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { todos.remove(todo) }
            )
        }
        
        OutlinedTextField(
            value = text,
            onValueChange = { text = it.uppercase() },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("ADD TASK", color = Color.DarkGray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (text.isNotBlank()) {
                    todos.add(text)
                    text = ""
                }
            })
        )
    }
}

@Composable
fun QuickActions(onDialer: () -> Unit, onWhatsApp: () -> Unit) {
    Row(
        modifier = Modifier.padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        IconButton(onClick = onDialer) {
            Icon(Icons.Default.Call, contentDescription = "Dialer", tint = Color.White)
        }
        IconButton(onClick = onWhatsApp) {
            Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color.White)
        }
    }
}

@Composable
fun AppDrawer(apps: List<AppInfo>, onAppClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(apps) { app ->
            Text(
                text = app.label.uppercase(),
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAppClick(app.packageName) }
                    .padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

fun getInstalledApps(packageManager: PackageManager): List<AppInfo> {
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    return packageManager.queryIntentActivities(intent, 0)
        .map { 
            AppInfo(
                label = it.loadLabel(packageManager).toString(),
                packageName = it.activityInfo.packageName
            )
        }
        .sortedBy { it.label.lowercase() }
}

fun launchApp(context: Context, target: String) {
    try {
        if (target == "CAMERA_ACTION") {
            val intent = Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            context.startActivity(intent)
            return
        }
        
        val intent = context.packageManager.getLaunchIntentForPackage(target)
        if (intent != null) {
            context.startActivity(intent)
        } else if (target == "com.android.dialer") {
            // Fallback for dialer if package name differs
            val dialIntent = Intent(Intent.ACTION_DIAL)
            context.startActivity(dialIntent)
        }
    } catch (e: Exception) {
        // Handle failure
    }
}
