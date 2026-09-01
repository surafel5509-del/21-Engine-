package com.example.studio.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.model.GameNode
import com.example.studio.storage.CustomAsset
import com.example.studio.storage.CustomAssetManager

@Composable
fun AssetLibraryDock(
    selectedNode: GameNode?,
    onAssignAssetToNode: (CustomAsset) -> Unit,
    onPlaySound: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var subTab by remember { mutableStateOf(0) } // 0: Textures & Sprites, 1: Audio SFX
    var assetsState by remember { mutableStateOf(CustomAssetManager.assetList.toList()) }

    // Android Photo Picker Launcher (Zero-permission storage access)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val imported = CustomAssetManager.importImageUri(
                context = context,
                uri = uri,
                name = "Sprite_${System.currentTimeMillis() % 1000}"
            )
            if (imported != null) {
                assetsState = CustomAssetManager.assetList.toList()
                Toast.makeText(context, "Asset '${imported.name}' Imported!", Toast.LENGTH_SHORT).show()
                if (selectedNode != null) {
                    onAssignAssetToNode(imported)
                }
            } else {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
            .padding(10.dp)
    ) {
        // Sub Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { subTab = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (subTab == 0) Color(0xFF0284C7) else Color.Transparent,
                    contentColor = Color.White
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sprites & Uploads", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { subTab = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (subTab == 1) Color(0xFF0284C7) else Color.Transparent,
                    contentColor = Color.White
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Audio Synthesizer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (subTab == 0) {
            // SPRITES & UPLOADS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Asset File Manager", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Pick custom PNG/JPG from your phone storage", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(assetsState) { asset ->
                    val bmp = CustomAssetManager.getBitmap(asset.id)
                    val isAssigned = selectedNode?.customTextureKey == asset.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAssignAssetToNode(asset)
                                Toast.makeText(context, "Applied to '${selectedNode?.name ?: "Node"}'", Toast.LENGTH_SHORT).show()
                            },
                        colors = CardDefaults.cardColors(containerColor = if (isAssigned) Color(0xFF0C4A6E) else Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAssigned) Color(0xFF38BDF8) else Color(0xFF334155)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color(0xFF0F172A), shape = RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF334155), shape = RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = asset.name,
                                        modifier = Modifier.size(54.dp)
                                    )
                                } else {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF64748B))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = asset.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "${asset.width}x${asset.height} (${asset.type})",
                                color = Color(0xFF94A3B8),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        } else {
            // AUDIO SYNTHESIZER
            Text("Real-time procedural audio engine. Tap to test sounds:", color = Color(0xFF94A3B8), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
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
}
