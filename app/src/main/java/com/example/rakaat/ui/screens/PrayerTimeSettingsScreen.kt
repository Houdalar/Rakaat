package com.example.rakaat.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rakaat.R
import com.example.rakaat.data.PrayerViewModel
import com.example.rakaat.domain.PrayerTime
import com.example.rakaat.ui.components.EditPrayerTimeDialog
import com.example.rakaat.ui.components.PrayerTimeItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimeSettingsScreen(navController: NavController, fragmentManager: FragmentManager?) {
    val viewModel: PrayerViewModel = hiltViewModel()
    val prayerTimes = viewModel.prayerTimes.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(prayerTimes.value) {
        println("Debug: Prayer Times - ${prayerTimes.value}")
    }
// Debug log for fragmentManager
    LaunchedEffect(key1 = Unit) {
        println("Debug: FragmentManager - $fragmentManager")
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prayer Time Settings", color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Black),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Image(
                painter = painterResource(id = R.drawable.background_image),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (fragmentManager == null) {
                    Text("Error: Fragment Manager is null", color = Color.Red)
                } else if (prayerTimes.value.isEmpty()) {
                    Text("No Prayer Times Available", color = Color.Red)
                } else {
                prayerTimes.value.forEach { prayerTime ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        if (fragmentManager != null) {
                            PrayerTimeItem(
                                prayerTime = prayerTime,
                                fragmentManager = fragmentManager,
                                onTimeUpdated = { newTime ->
                                    viewModel.updatePrayerTime(prayerTime.prayer, newTime , context )
                                }
                            )
                        }
                    }
                }

                // Button for CSV Upload
                Button(onClick = { /* Trigger file picker and upload logic */ }) {
                    Text("Upload CSV for Schedule")
                }
            }}
        }
    }
}

