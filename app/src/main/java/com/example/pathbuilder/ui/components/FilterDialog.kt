package com.example.pathbuilder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FilterDialog(
    selectedTypes: Set<String>,
    selectedDifficulties: Set<String>,
    selectedElevationRanges: Set<String>,
    onToggleType: (String) -> Unit,
    onToggleDifficulty: (String) -> Unit,
    onToggleElevationRange: (String) -> Unit,
    onDismiss: () -> Unit
)
{
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        },
        title = { Text("Filters") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Type")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("walk", "drive").forEach { type ->
                        FilterChip(
                            selected = selectedTypes.contains(type),
                            onClick = { onToggleType(type) },
                            label = { Text(type.replaceFirstChar { it.titlecase() }) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Difficulty")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Easy", "Moderate", "Hard").forEach { diff ->
                        FilterChip(
                            selected = selectedDifficulties.contains(diff),
                            onClick = { onToggleDifficulty(diff) },
                            label = { Text(diff) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Elevation Gain")
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedElevationRanges.contains("Flat"),
                            onClick = { onToggleElevationRange("Flat") },
                            label = { Text("Flat (<100 ft)") }
                        )
                        FilterChip(
                            selected = selectedElevationRanges.contains("Low"),
                            onClick = { onToggleElevationRange("Low") },
                            label = { Text("Low (100-500 ft)") }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedElevationRanges.contains("Medium"),
                            onClick = { onToggleElevationRange("Medium") },
                            label = { Text("Medium (500-1500 ft)") }
                        )
                        FilterChip(
                            selected = selectedElevationRanges.contains("High"),
                            onClick = { onToggleElevationRange("High") },
                            label = { Text("High (>1500 ft)") }
                        )
                    }
                }
            }
        },
        modifier = Modifier.padding(8.dp)
    )
}

