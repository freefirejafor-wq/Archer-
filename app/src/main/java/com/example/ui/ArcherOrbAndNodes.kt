package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrbState
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonMatrixGreen
import com.example.ui.theme.NeonQuantumCyan
import com.example.ui.theme.NeonSolarAmber
import com.example.ui.theme.NeonSoulWhite
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimaryHud
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class HudNodeType {
    MEMORY,
    CHAT,
    SOUL,
    SETTINGS
}

private data class SphereParticle(
    val theta: Float,
    val phi: Float,
    val sizePx: Float,
    val isRightHemisphere: Boolean
)

@Composable
fun ArcherCircuitAndOrbSection(
    orbState: OrbState,
    detectedEmotion: String,
    voiceMuted: Boolean,
    onNodeClick: (HudNodeType) -> Unit,
    onToggleListenOrStop: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_transition")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    OrbState.SPEAKING -> 4200
                    OrbState.LISTENING -> 3400
                    OrbState.THINKING -> 2600
                    OrbState.IDLE -> 9000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "sphere_rotation"
    )

    val pulseFactor by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    OrbState.SPEAKING -> 480
                    OrbState.LISTENING -> 620
                    OrbState.THINKING -> 520
                    OrbState.IDLE -> 1800
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_pulse"
    )

    val particles = remember {
        val list = mutableListOf<SphereParticle>()
        val goldenRatio = (1.0 + kotlin.math.sqrt(5.0)) / 2.0
        val count = 170
        for (i in 0 until count) {
            val theta = (2.0 * PI * i / goldenRatio).toFloat()
            val phi = kotlin.math.acos(1.0 - 2.0 * (i + 0.5) / count).toFloat()
            val isRight = cos(theta) >= 0f
            val sizePx = if (i % 5 == 0) 4.4f else 2.8f
            list.add(SphereParticle(theta, phi, sizePx, isRight))
        }
        list
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            // Background Circuit Trace Connectors from left nodes into the 3D Orb center
            Canvas(modifier = Modifier.fillMaxSize()) {
                val startX = size.width * 0.34f
                val orbCenterX = size.width * 0.70f
                val orbCenterY = size.height * 0.48f
                val midJoinX = size.width * 0.44f

                val nodeYPositions = listOf(
                    size.height * 0.14f,
                    size.height * 0.38f,
                    size.height * 0.62f,
                    size.height * 0.86f
                )
                val nodeColors = listOf(
                    NeonQuantumCyan,
                    NeonSolarAmber,
                    NeonSoulWhite,
                    NeonMatrixGreen
                )
                val busOffsets = listOf(-12f, -4f, 4f, 12f)

                for (idx in 0..3) {
                    val y0 = nodeYPositions[idx]
                    val targetY = orbCenterY + busOffsets[idx]
                    val color = nodeColors[idx]

                    val path = Path().apply {
                        moveTo(startX, y0)
                        lineTo(midJoinX - (idx * 4f), y0)
                        lineTo(midJoinX + 18f, targetY)
                        lineTo(orbCenterX - size.height * 0.34f, targetY)
                    }

                    // Outer soft neon glow
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.28f),
                        style = Stroke(width = 7f, cap = StrokeCap.Round)
                    )
                    // Sharp core circuit trace
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.92f),
                        style = Stroke(width = 2.6f, cap = StrokeCap.Round)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: 4 Interactive Circuit Nodes (MEMORY, CHAT, SOUL, SETTINGS)
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .padding(end = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircuitNodePill(
                        label = "MEMORY",
                        icon = Icons.Default.Lightbulb,
                        accentColor = NeonQuantumCyan,
                        testTag = "node_memory_button",
                        onClick = { onNodeClick(HudNodeType.MEMORY) }
                    )
                    CircuitNodePill(
                        label = "CHAT",
                        icon = Icons.AutoMirrored.Filled.Chat,
                        accentColor = NeonSolarAmber,
                        testTag = "node_chat_button",
                        onClick = { onNodeClick(HudNodeType.CHAT) }
                    )
                    CircuitNodePill(
                        label = "SOUL",
                        icon = Icons.Default.SelfImprovement,
                        accentColor = NeonSoulWhite,
                        testTag = "node_soul_button",
                        onClick = { onNodeClick(HudNodeType.SOUL) }
                    )
                    CircuitNodePill(
                        label = "SETTINGS",
                        icon = Icons.Default.Settings,
                        accentColor = NeonMatrixGreen,
                        testTag = "node_settings_button",
                        onClick = { onNodeClick(HudNodeType.SETTINGS) }
                    )
                }

                // Right Column: 3D Particle Voice Orb Canvas
                Box(
                    modifier = Modifier
                        .weight(0.58f)
                        .height(235.dp)
                        .clip(CircleShape)
                        .clickable { onToggleListenOrStop() }
                        .testTag("archer_voice_orb"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxRadius = (minOf(size.width, size.height) / 2f) * 0.86f
                        val activeRadius = maxRadius * if (orbState == OrbState.IDLE) 0.92f else pulseFactor.coerceIn(0.85f, 1.06f)

                        // 1. Outer enclosing circular HUD bezel
                        drawCircle(
                            color = NeonQuantumCyan.copy(alpha = 0.45f),
                            radius = maxRadius * 1.04f,
                            center = center,
                            style = Stroke(width = 2.2f)
                        )
                        drawCircle(
                            color = NeonMatrixGreen.copy(alpha = 0.22f),
                            radius = maxRadius * 1.09f,
                            center = center,
                            style = Stroke(width = 1.2f)
                        )

                        // 2. Inner volumetric core glow
                        val corePrimaryColor = when (orbState) {
                            OrbState.LISTENING -> NeonMatrixGreen
                            OrbState.SPEAKING -> NeonQuantumCyan
                            OrbState.THINKING -> NeonSolarAmber
                            OrbState.IDLE -> NeonQuantumCyan
                        }
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    corePrimaryColor.copy(alpha = 0.72f),
                                    NeonMatrixGreen.copy(alpha = 0.25f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = activeRadius * 0.95f
                            ),
                            radius = activeRadius * 0.95f,
                            center = center
                        )

                        // 3. Tilted 3D Orbital Gyro Rings
                        rotate(degrees = -18f + (rotationAngle * 15f), pivot = center) {
                            drawOval(
                                color = NeonMatrixGreen.copy(alpha = 0.65f),
                                topLeft = Offset(center.x - activeRadius * 0.36f, center.y - activeRadius * 0.96f),
                                size = Size(activeRadius * 0.72f, activeRadius * 1.92f),
                                style = Stroke(width = 2.2f)
                            )
                        }
                        rotate(degrees = 24f - (rotationAngle * 12f), pivot = center) {
                            drawOval(
                                color = NeonGold.copy(alpha = 0.55f),
                                topLeft = Offset(center.x - activeRadius * 0.48f, center.y - activeRadius * 0.92f),
                                size = Size(activeRadius * 0.96f, activeRadius * 1.84f),
                                style = Stroke(width = 1.8f)
                            )
                        }

                        // 4. 3D Spherical Particle Cloud (Cyan on left, Gold/Emerald on right like screenshot)
                        val sphereRadius = activeRadius * 0.84f
                        for (p in particles) {
                            val currentTheta = p.theta + rotationAngle
                            val x3d = sin(p.phi) * cos(currentTheta)
                            val y3d = cos(p.phi)
                            val z3d = sin(p.phi) * sin(currentTheta)

                            val depthScale = 0.65f + 0.35f * ((z3d + 1f) / 2f)
                            val px = center.x + x3d * sphereRadius
                            val py = center.y + y3d * sphereRadius

                            val particleColor = when {
                                orbState == OrbState.LISTENING && x3d < 0f -> NeonMatrixGreen
                                x3d < -0.05f -> NeonQuantumCyan
                                x3d > 0.15f -> NeonGold
                                else -> NeonMatrixGreen
                            }

                            drawCircle(
                                color = particleColor.copy(alpha = (depthScale * 0.92f).coerceIn(0.25f, 0.98f)),
                                radius = p.sizePx * depthScale * if (orbState == OrbState.SPEAKING) pulseFactor else 1f,
                                center = Offset(px, py)
                            )
                        }
                    }

                    // Bottom status label inside the orb ("• SPEAKING •" / "• LISTENING •")
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "• ${orbState.label} •",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimaryHud,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.6.sp
                        )
                        Text(
                            text = "MOOD: ${detectedEmotion.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonMatrixGreen
                        )
                    }
                }
            }
        }

        // Controls row below the 3D Orb (matching the red-bordered STOP pill + cyan speaker button in the screenshot)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, end = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isActiveVoice = orbState == OrbState.LISTENING || orbState == OrbState.SPEAKING || orbState == OrbState.THINKING
            val pillBorderColor = if (isActiveVoice) NeonCrimson else NeonMatrixGreen

            Surface(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onToggleListenOrStop() }
                    .testTag("voice_listen_stop_button"),
                shape = RoundedCornerShape(24.dp),
                color = ObsidianSurface,
                border = BorderStroke(2.dp, pillBorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isActiveVoice) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isActiveVoice) "Stop Voice" else "Start Listening",
                        tint = if (isActiveVoice) NeonCrimson else NeonMatrixGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isActiveVoice) "STOP" else "TALK / বলুন",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimaryHud,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onToggleMute() }
                    .testTag("voice_mute_toggle_button"),
                shape = RoundedCornerShape(14.dp),
                color = ObsidianCard,
                border = BorderStroke(1.5.dp, if (voiceMuted) NeonCrimson else NeonQuantumCyan)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (voiceMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (voiceMuted) "Unmute AI Voice" else "Mute AI Voice",
                        tint = if (voiceMuted) NeonCrimson else NeonQuantumCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CircuitNodePill(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(24.dp),
        color = ObsidianDepthPillColor,
        border = BorderStroke(1.6.dp, accentColor.copy(alpha = 0.85f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.16f),
                border = BorderStroke(1.2.dp, accentColor)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimaryHud,
                maxLines = 1
            )
        }
    }
}

private val ObsidianDepthPillColor = Color(0xFF0A1712)
