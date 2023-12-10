package com.example.rakaat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.example.rakaat.domain.PrayerTime
import com.example.rakaat.domain.showTimePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat


@Composable
fun PrayerTimeItem(prayerTime: PrayerTime, fragmentManager: FragmentManager, onTimeUpdated: (String) -> Unit) {
    val showPicker = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${prayerTime.prayer.name}: ${prayerTime.time}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )

        IconButton(onClick = { showPicker.value = true }) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
        }
    }

    if (showPicker.value) {
        EditPrayerTimeDialog(
            prayerTime = prayerTime,
            onDismiss = { showPicker.value = false },
            fragmentManager = fragmentManager,
            onTimeSelected = { newTime ->
                onTimeUpdated(newTime)
                showPicker.value = false
            }
        )
    }
}

@Composable
fun EditPrayerTimeDialog(
    prayerTime: PrayerTime,
    onDismiss: () -> Unit,
    fragmentManager: FragmentManager,
    onTimeSelected: (String) -> Unit
) {
    var time by remember { mutableStateOf(prayerTime.time) }
    val showPicker = remember { mutableStateOf(false) }

    if (showPicker.value) {
        showTimePicker(fragmentManager, time) { newTime ->
            time = newTime
            onTimeSelected(newTime)
            showPicker.value = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Time for ${prayerTime.prayer} ", color = Color.White) },
        text = { Text("Selected Time: $time") },
        confirmButton = {
            Button(onClick = { showPicker.value = true }) { Text("Choose Time") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}