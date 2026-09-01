package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.model.GameProject
import com.example.studio.templates.ProjectTemplates

data class TemplateItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeColor: Color,
    val loadProject: () -> GameProject
)

@Composable
fun TemplatesDock(
    onLoadTemplate: (GameProject) -> Unit,
    modifier: Modifier = Modifier
) {
    val templates = listOf(
        TemplateItem(
            title = "2D Platformer Adventure",
            description = "Complete platformer with knight character, jump physics, coins, moving platforms and hazards.",
            icon = Icons.Default.SportsEsports,
            badgeColor = Color(0xFF0284C7),
            loadProject = { ProjectTemplates.createPlatformerTemplate() }
        ),
        TemplateItem(
            title = "Galactic Space Defender",
            description = "Top-down 2D starship shooter with photon lasers, asteroid physics and UFO waves.",
            icon = Icons.Default.RocketLaunch,
            badgeColor = Color(0xFF8B5CF6),
            loadProject = { ProjectTemplates.createSpaceShooterTemplate() }
        ),
        TemplateItem(
            title = "Physics Sandbox & Dominoes",
            description = "Dynamic rigidbodies, domino chain reactions, crate towers and high-bounciness balls.",
            icon = Icons.Default.SportsBasketball,
            badgeColor = Color(0xFFF59E0B),
            loadProject = { ProjectTemplates.createPhysicsSandboxTemplate() }
        ),
        TemplateItem(
            title = "Sky Flap Quest",
            description = "One-touch airborne obstacle dodging physics with score gap triggers.",
            icon = Icons.Default.Flight,
            badgeColor = Color(0xFF10B981),
            loadProject = { ProjectTemplates.createFlappyTemplate() }
        ),
        TemplateItem(
            title = "Blank 2D World",
            description = "Empty starter canvas with camera and base ground to construct your own custom game from scratch.",
            icon = Icons.Default.AddBox,
            badgeColor = Color(0xFF64748B),
            loadProject = { ProjectTemplates.createBlankTemplate() }
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Dashboard, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Game Templates & Presets", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Load a complete Godot-style 2D template in 1-tap to edit or play:", color = Color(0xFF94A3B8), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(templates) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLoadTemplate(item.loadProject()) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(item.badgeColor.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = null, tint = item.badgeColor, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(item.description, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 15.sp)
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}
