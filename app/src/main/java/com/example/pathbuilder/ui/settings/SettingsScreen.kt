package com.example.pathbuilder.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var metricUnits by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        RowSetting(
            title = "Notifications",
            description = "Toggle trip reminders and saves",
            checked = notificationsEnabled,
            onCheckedChange = { notificationsEnabled = it }
        )
        RowSetting(
            title = "Units",
            description = if (metricUnits) "Kilometers / meters" else "Miles / feet",
            checked = metricUnits,
            onCheckedChange = { metricUnits = it }
        )
        Divider()
        Button(onClick = { /* TODO: hook up profile editing */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Edit Profile")
        }
        Button(onClick = { /* TODO: sign out placeholder */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Sign Out")
        }
        Button(onClick = { /* TODO: bug report placeholder */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Report a Bug")
        }
    }
}

@Composable
private fun RowSetting(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

