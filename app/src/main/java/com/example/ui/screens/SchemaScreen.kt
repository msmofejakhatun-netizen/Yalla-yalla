package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.DatabaseSchemas
import com.example.data.models.TableSchema
import com.example.ui.components.CodeBlockView
import com.example.ui.components.HeaderTitleCard

@Composable
fun SchemaScreen(
    modifier: Modifier = Modifier
) {
    var expandedTableName by remember { mutableStateOf<String?>("orders") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderTitleCard(
                title = "Database Schemas & ERD",
                subtitle = "Normalized PostgreSQL Architecture for Orders, Payments, Delivery Assignments, and Tracking Logs",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "Database",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )
        }

        items(DatabaseSchemas.allTables) { table ->
            TableSchemaCard(
                table = table,
                isExpanded = expandedTableName == table.tableName,
                onToggleExpand = {
                    expandedTableName = if (expandedTableName == table.tableName) null else table.tableName
                }
            )
        }
    }
}

@Composable
private fun TableSchemaCard(
    table: TableSchema,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "table: ${table.tableName}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = table.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🔑 Primary Key: ${table.primaryKey}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (table.foreignKeys.isNotEmpty()) {
                                Text(
                                    text = "🔗 Foreign Keys: ${table.foreignKeys.joinToString(", ")}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Columns Definition",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    table.fields.forEach { field ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = field.name,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = field.dataType,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                        }
                        Text(
                            text = field.description,
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CodeBlockView(
                        codeText = table.sqlDdl,
                        title = "PostgreSQL DDL SQL"
                    )
                }
            }
        }
    }
}
