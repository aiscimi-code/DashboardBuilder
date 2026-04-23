package com.dashboard.builder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dashboard.builder.data.model.TabManager
import com.dashboard.builder.viewmodel.TabPagination
import com.dashboard.builder.viewmodel.UiState

@Composable
fun TabBar(
    state: UiState,
    pagination: TabPagination,
    onTabSelected: (String) -> Unit,
    onAddTab: () -> Unit,
    onPreviousPage: () -> Unit = {},
    onNextPage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left scroll button (if not on first page)
            if (pagination.currentPage > 0) {
                IconButton(
                    onClick = onPreviousPage,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous page",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(32.dp))
            }
            
            // Tabs - using simple Row since only 8 at a time
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                pagination.visibleTabIds.forEach { tabId ->
                    TabItem(
                        tabId = tabId,
                        isSelected = tabId == state.selectedTabId,
                        onClick = { onTabSelected(tabId) }
                    )
                }
                
                // Add button
                if (pagination.showAddButton) {
                    TabItem(
                        tabId = "+",
                        isSelected = false,
                        isAddButton = true,
                        onClick = onAddTab
                    )
                }
            }
            
            // Right scroll button (if not on last page)
            if (pagination.currentPage < pagination.totalPages - 1) {
                IconButton(
                    onClick = onNextPage,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next page",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(32.dp))
            }
            
            // Tab counter
            Text(
                text = "${state.appState.tabs.size}/${TabManager.MAX_TABS}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun TabItem(
    tabId: String,
    isSelected: Boolean,
    isAddButton: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isAddButton -> MaterialTheme.colorScheme.secondaryContainer
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isAddButton -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 36.dp)
            .background(backgroundColor, shape = MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isAddButton) {
            Text(
                text = "+",
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = tabId,
                color = textColor,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}