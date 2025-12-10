package com.example.pathbuilder.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pathbuilder.ui.components.FilterDialog
import com.example.pathbuilder.ui.components.MapboxMapView
import com.example.pathbuilder.viewmodel.MapLayer
import com.example.pathbuilder.viewmodel.MapViewModel

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onCreateRoute: () -> Unit,
    onOpenRoutes: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf(uiState.searchQuery) }

    Box(modifier = Modifier.fillMaxSize()) {
        MapboxMapView(
            modifier = Modifier.fillMaxSize(),
            layer = uiState.layer,
            routes = uiState.visibleRoutes,
            activePins = emptyList(),
            onMapClick = { /* Map taps on main map do nothing */ }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PathBuilder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            viewModel.updateSearchQuery(it)
                        },
                        placeholder = { Text("Search routes by name") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        ElevatedAssistChip(
                            onClick = { showFilters = true },
                            label = { Text("Filters") },
                            leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = "Filters") }
                        )
                        ElevatedAssistChip(
                            onClick = viewModel::toggleLayer,
                            label = { Text(if (uiState.layer == MapLayer.STANDARD) "Topo" else "Standard") },
                            leadingIcon = { Icon(Icons.Default.Layers, contentDescription = "Toggle Layer") }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Visible routes: ${uiState.visibleRoutes.size}", style = MaterialTheme.typography.bodyMedium)
                    Text("Layer: ${uiState.layer.name}", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(onClick = onCreateRoute) {
                    Icon(Icons.Default.Map, contentDescription = "Create Route")
                }
            }
        }

        if (showFilters) {
            FilterDialog(
                selectedTypes = uiState.selectedTypes,
                selectedDifficulties = uiState.selectedDifficulties,
                selectedElevationRanges = uiState.selectedElevationRanges,
                onToggleType = { viewModel.toggleType(it) },
                onToggleDifficulty = { viewModel.toggleDifficulty(it) },
                onToggleElevationRange = { viewModel.toggleElevationRange(it) },
                onDismiss = { showFilters = false }
            )
        }
    }
}

