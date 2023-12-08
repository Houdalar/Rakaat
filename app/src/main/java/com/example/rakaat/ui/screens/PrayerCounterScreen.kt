package com.example.rakaat.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rakaat.R
import com.example.rakaat.data.PrayerViewModel
import com.example.rakaat.domain.Prayer
import com.example.rakaat.domain.PrayerType
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PrayerCounterScreen(navController: NavController) {
    val prayerViewModel: PrayerViewModel = hiltViewModel<PrayerViewModel>()
    val currentPrayer = prayerViewModel.currentPrayer.observeAsState().value ?: Prayer(PrayerType.الصبح)
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Dynamic font size based on screen width

    val fontSize = remember(screenWidth) { if (screenWidth < 600.dp) 14.sp else 90.sp }
    val rakaafontSize = remember(screenWidth) { if (screenWidth < 600.dp) 39.sp else 90.sp }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${currentPrayer.type}",
                fontSize = fontSize,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("bluetoothConnectionScreen")}
            )

            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.padding(horizontal = 25.dp)
            ) {
                Text(
                    text = "${currentPrayer.rakatCount}",
                    fontSize = rakaafontSize,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(60.dp)
                )
            }

            Text(
                text = "${currentPrayer.sajdaCount} : السجدة",
                fontSize = fontSize,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}



