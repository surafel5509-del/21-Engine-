package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

@Composable
fun AssetLibraryDock(
    onPlaySound: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sounds = listOf(
        Pair("jump", "Classic Hero Jump"),
        Pair("coin", "Gold Coin Pickup"),
        Pair("laser", "Photon Laser Cannon"),
        Pair("explosion", "Big Bomb Explosion"),
        Pair("hit", "Damage & Hurt Hit"),
        Pair("powerup", "Level Up / Jingle"),
        Pair("click", "UI Button Click")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Audio SFX Synthesizer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Real-time procedural audio engine. Tap to preview sounds:", color = Color(0xFF94A3B8), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(sounds) { (soundKey, soundDesc) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlaySound(soundKey) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF0284C7).copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(soundKey.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(soundDesc, color = Color(0xFF94A3B8), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
