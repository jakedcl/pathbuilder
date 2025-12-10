package com.example.pathbuilder.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pathbuilder.model.Waypoint
import com.example.pathbuilder.ui.components.MapboxMapView
import com.example.pathbuilder.viewmodel.CreateRouteViewModel
import com.example.pathbuilder.viewmodel.MapLayer

@Composable
fun CreateRouteScreen(
    viewModel: CreateRouteViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var nameDraft by rememberSaveable { mutableStateOf(uiState.routeName) }
    var typeDraft by rememberSaveable { mutableStateOf(uiState.routeType ?: "walk") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Build a new route", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (uiState.isCalculating) {
                    Text("Calculating...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            MapboxMapView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                layer = uiState.layer,
                routes = emptyList(),
                activePins = uiState.waypoints,
                activeRouteGeometry = uiState.routeGeometry,
                onMapClick = { waypoint -> 
                    if (uiState.routeType != null) {
                        viewModel.addPin(waypoint) 
                    }
                }
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(onClick = viewModel::startNewRoute) {
                            Text("Start New")
                        }
                        IconButton(
                            onClick = viewModel::undo,
                            enabled = uiState.canUndo
                        ) {
                            Icon(
                                Icons.Default.Undo,
                                contentDescription = "Undo",
                                tint = if (uiState.canUndo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                        IconButton(
                            onClick = viewModel::redo,
                            enabled = uiState.canRedo
                        ) {
                            Icon(
                                Icons.Default.Redo,
                                contentDescription = "Redo",
                                tint = if (uiState.canRedo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }
                    if (uiState.routeType != null) {
                        Text(
                            "Type: ${uiState.routeType?.replaceFirstChar { it.uppercase() } ?: ""}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ElevatedAssistChip(
                        onClick = viewModel::toggleLayer,
                        label = { Text(if (uiState.layer == MapLayer.STANDARD) "Topo" else "Standard") },
                        leadingIcon = { Icon(Icons.Default.Layers, contentDescription = "Toggle Layer") }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("Manual mode", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = uiState.manualMode,
                            onCheckedChange = { viewModel.toggleManualMode() }
                        )
                    }
                }
            }

            Divider()

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        String.format("%.2f mi", uiState.distanceMiles),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        String.format("↑ %.0f ft", uiState.elevationFeet),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${uiState.estimatedTimeMinutes} min",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (uiState.manualMode) {
                    Text("⚠ Manual mode: straight lines", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                if (uiState.saveCompleted) {
                    Text("✓ Route saved!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { showSaveDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.waypoints.isNotEmpty()
            ) {
                Text("Save Route", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Route Type Selection Dialog
    if (uiState.showRouteTypeDialog) {
        AlertDialog(
            onDismissRequest = { /* Don't allow dismissing without selection */ },
            title = { Text("Choose Route Type") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select whether this is a walking or driving route:")
                    Button(
                        onClick = { viewModel.setRouteType("walk") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Walking Route")
                    }
                    Button(
                        onClick = { viewModel.setRouteType("drive") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Driving Route")
                    }
                }
            },
            confirmButton = { }
        )
    }

    // Save Route Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Name your route") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = {
                            nameDraft = it
                            viewModel.updateRouteName(it)
                        },
                        label = { Text("Route name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Type: ${uiState.routeType?.replaceFirstChar { it.uppercase() } ?: "Walk"}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateRouteName(nameDraft)
                        viewModel.saveRoute()
                        showSaveDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

