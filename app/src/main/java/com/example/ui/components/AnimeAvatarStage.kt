package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AvatarState
import com.example.data.PendingAction
import com.example.ui.theme.ZiaAcousticCyan
import com.example.ui.theme.ZiaAmberWarning
import com.example.ui.theme.ZiaCardSurface
import com.example.ui.theme.ZiaEmeraldOnline
import com.example.ui.theme.ZiaNeuralViolet
import com.example.ui.theme.ZiaObsidianBg
import com.example.ui.theme.ZiaSapphireBlue
import com.example.ui.theme.ZiaTextMuted
import com.example.ui.theme.ZiaTextSecondary
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom Anime-Style Female AI Assistant Character ("ZIA") rendered via lightweight
 * Jetpack Compose Canvas with state-driven expressions (blinking, lip sync, cyber-headset LEDs,
 * and radial acoustic rings) optimized for Android 10 / Redmi 9 battery efficiency.
 */
@Composable
fun AnimeAvatarStage(
    avatarState: AvatarState,
    audioLevelRms: Float,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 216.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "zia_avatar_motion")

    // Lightweight slow breathing & orbital rotation
    val breathPhase by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    val orbitalAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbital_angle"
    )

    // Periodic natural eye blink cycle (0..1, blinks briefly when > 0.93)
    val blinkCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blink_cycle"
    )

    // Speaking mouth openness oscillation
    val speakMouthWave by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speak_wave"
    )

    val dynamicAudioBoost by animateFloatAsState(
        targetValue = if (avatarState == AvatarState.LISTENING) audioLevelRms.coerceIn(0.1f, 1f) else 0f,
        animationSpec = tween(120),
        label = "audio_rms"
    )

    val stateAccentColor = when (avatarState) {
        AvatarState.IDLE -> ZiaSapphireBlue
        AvatarState.LISTENING -> ZiaAcousticCyan
        AvatarState.THINKING -> ZiaNeuralViolet
        AvatarState.SPEAKING -> ZiaEmeraldOnline
        AvatarState.UNAVAILABLE -> ZiaAmberWarning
    }

    Column(
        modifier = modifier.testTag("anime_avatar_stage"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(avatarSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(avatarSize)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val maxR = size.minDimension / 2f

                // 1. Outer ambient state halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            stateAccentColor.copy(alpha = 0.26f),
                            ZiaNeuralViolet.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = maxR * breathPhase
                    ),
                    radius = maxR * breathPhase,
                    center = Offset(cx, cy)
                )

                // 2. State-specific reactive outer ring
                val ringRadius = maxR * 0.88f
                when (avatarState) {
                    AvatarState.LISTENING -> {
                        val pulseRadius = ringRadius * (1f + dynamicAudioBoost * 0.09f)
                        drawCircle(
                            color = ZiaAcousticCyan.copy(alpha = 0.85f),
                            radius = pulseRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 3.5.dp.toPx())
                        )
                        // Acoustic wave ticks around ring
                        for (i in 0 until 24) {
                            val angleRad = Math.toRadians((i * 15.0) + orbitalAngle * 0.3)
                            val waveLen = (6.dp.toPx()) + (dynamicAudioBoost * 12.dp.toPx() * ((i % 3) + 1) / 3f)
                            val startR = ringRadius - 2.dp.toPx()
                            val endR = startR + waveLen
                            drawLine(
                                color = ZiaAcousticCyan,
                                start = Offset(
                                    cx + (startR * cos(angleRad)).toFloat(),
                                    cy + (startR * sin(angleRad)).toFloat()
                                ),
                                end = Offset(
                                    cx + (endR * cos(angleRad)).toFloat(),
                                    cy + (endR * sin(angleRad)).toFloat()
                                ),
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    AvatarState.THINKING -> {
                        drawCircle(
                            color = ZiaNeuralViolet.copy(alpha = 0.35f),
                            radius = ringRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawArc(
                            color = ZiaNeuralViolet,
                            startAngle = orbitalAngle,
                            sweepAngle = 110f,
                            useCenter = false,
                            topLeft = Offset(cx - ringRadius, cy - ringRadius),
                            size = Size(ringRadius * 2f, ringRadius * 2f),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = ZiaAcousticCyan,
                            startAngle = orbitalAngle + 180f,
                            sweepAngle = 90f,
                            useCenter = false,
                            topLeft = Offset(cx - ringRadius, cy - ringRadius),
                            size = Size(ringRadius * 2f, ringRadius * 2f),
                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    AvatarState.SPEAKING -> {
                        val spkRadius = ringRadius * (0.98f + speakMouthWave * 0.04f)
                        drawCircle(
                            color = ZiaEmeraldOnline.copy(alpha = 0.85f),
                            radius = spkRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 3.5.dp.toPx())
                        )
                    }

                    AvatarState.UNAVAILABLE -> {
                        drawCircle(
                            color = ZiaAmberWarning.copy(alpha = 0.7f),
                            radius = ringRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }

                    AvatarState.IDLE -> {
                        drawCircle(
                            color = ZiaSapphireBlue.copy(alpha = 0.45f),
                            radius = ringRadius * breathPhase,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.2.dp.toPx())
                        )
                    }
                }

                // 3. Portrait Circular Backdrop
                val portraitR = maxR * 0.76f
                drawCircle(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF131B36), Color(0xFF0A0F1F))
                    ),
                    radius = portraitR,
                    center = Offset(cx, cy)
                )

                // ===============================================================
                // 4. ANIME GIRL CHARACTER ILLUSTRATION ("ZIA")
                // ===============================================================
                val u = portraitR / 100f // normalized unit inside portrait

                // Back flowing dark navy-violet anime hair
                val backHairPath = Path().apply {
                    moveTo(cx - 64 * u, cy - 18 * u)
                    cubicTo(cx - 76 * u, cy + 35 * u, cx - 68 * u, cy + 82 * u, cx - 42 * u, cy + 94 * u)
                    lineTo(cx + 42 * u, cy + 94 * u)
                    cubicTo(cx + 68 * u, cy + 82 * u, cx + 76 * u, cy + 35 * u, cx + 64 * u, cy - 18 * u)
                    cubicTo(cx + 55 * u, cy - 75 * u, cx - 55 * u, cy - 75 * u, cx - 64 * u, cy - 18 * u)
                    close()
                }
                drawPath(
                    path = backHairPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF282054), Color(0xFF141132))
                    )
                )

                // Futuristic Cyber Collar & Shoulders
                val shoulderPath = Path().apply {
                    moveTo(cx - 56 * u, cy + 92 * u)
                    quadraticBezierTo(cx - 42 * u, cy + 62 * u, cx - 18 * u, cy + 58 * u)
                    lineTo(cx + 18 * u, cy + 58 * u)
                    quadraticBezierTo(cx + 42 * u, cy + 62 * u, cx + 56 * u, cy + 92 * u)
                    close()
                }
                drawPath(
                    path = shoulderPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E294B), Color(0xFF0F162B))
                    )
                )

                // Glowing V-Neck Cyber Core Accent on Collar
                val collarCorePath = Path().apply {
                    moveTo(cx - 16 * u, cy + 62 * u)
                    lineTo(cx, cy + 82 * u)
                    lineTo(cx + 16 * u, cy + 62 * u)
                }
                drawPath(
                    path = collarCorePath,
                    color = stateAccentColor,
                    style = Stroke(width = 3.2f * u, cap = StrokeCap.Round)
                )

                // Neck & soft shadow
                drawRoundRect(
                    color = Color(0xFFF6D5C9),
                    topLeft = Offset(cx - 11 * u, cy + 38 * u),
                    size = Size(22 * u, 25 * u),
                    cornerRadius = CornerRadius(8 * u, 8 * u)
                )

                // Anime Face Contour (soft oval with delicate chin)
                val facePath = Path().apply {
                    moveTo(cx - 38 * u, cy - 22 * u)
                    cubicTo(cx - 40 * u, cy + 12 * u, cx - 26 * u, cy + 38 * u, cx, cy + 45 * u)
                    cubicTo(cx + 26 * u, cy + 38 * u, cx + 40 * u, cy + 12 * u, cx + 38 * u, cy - 22 * u)
                    cubicTo(cx + 36 * u, cy - 54 * u, cx - 36 * u, cy - 54 * u, cx - 38 * u, cy - 22 * u)
                    close()
                }
                drawPath(
                    path = facePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFE6DC), Color(0xFFF7D4C6))
                    )
                )

                // Soft Anime Cheek Blush
                drawOval(
                    color = Color(0xFFFF8DA1).copy(alpha = 0.32f),
                    topLeft = Offset(cx - 31 * u, cy + 11 * u),
                    size = Size(14 * u, 7 * u)
                )
                drawOval(
                    color = Color(0xFFFF8DA1).copy(alpha = 0.32f),
                    topLeft = Offset(cx + 17 * u, cy + 11 * u),
                    size = Size(14 * u, 7 * u)
                )

                // ===========================================================
                // Expressive Anime Eyes (with blinking & sapphire catchlights)
                // ===========================================================
                val isBlinking = blinkCycle > 0.94f
                val eyeY = cy - 2 * u
                val leftEyeX = cx - 18 * u
                val rightEyeX = cx + 18 * u

                if (isBlinking) {
                    // Closed smiling anime eye arcs
                    drawArc(
                        color = Color(0xFF2B2347),
                        startAngle = 15f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(leftEyeX - 10 * u, eyeY - 4 * u),
                        size = Size(20 * u, 10 * u),
                        style = Stroke(width = 2.8f * u, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFF2B2347),
                        startAngle = 15f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(rightEyeX - 10 * u, eyeY - 4 * u),
                        size = Size(20 * u, 10 * u),
                        style = Stroke(width = 2.8f * u, cap = StrokeCap.Round)
                    )
                } else {
                    for (eyeCenterX in listOf(leftEyeX, rightEyeX)) {
                        // Sclera (white base)
                        drawOval(
                            color = Color.White,
                            topLeft = Offset(eyeCenterX - 10.5f * u, eyeY - 9 * u),
                            size = Size(21 * u, 18 * u)
                        )
                        // Outer iris (deep sapphire-violet gradient)
                        drawOval(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF253B80), stateAccentColor)
                            ),
                            topLeft = Offset(eyeCenterX - 7.5f * u, eyeY - 8 * u),
                            size = Size(15 * u, 16 * u)
                        )
                        // Inner pupil
                        drawCircle(
                            color = Color(0xFF0E1329),
                            radius = 3.8f * u,
                            center = Offset(eyeCenterX, eyeY)
                        )
                        // Top-left large anime catchlight
                        drawCircle(
                            color = Color.White,
                            radius = 2.8f * u,
                            center = Offset(eyeCenterX - 3.2f * u, eyeY - 3.5f * u)
                        )
                        // Bottom-right secondary sparkle
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = 1.4f * u,
                            center = Offset(eyeCenterX + 3.0f * u, eyeY + 3.2f * u)
                        )
                        // Bold upper anime eyelash
                        drawArc(
                            color = Color(0xFF1B1533),
                            startAngle = 190f,
                            sweepAngle = 160f,
                            useCenter = false,
                            topLeft = Offset(eyeCenterX - 11.5f * u, eyeY - 10.5f * u),
                            size = Size(23 * u, 16 * u),
                            style = Stroke(width = 3.2f * u, cap = StrokeCap.Round)
                        )
                    }
                }

                // Delicate eyebrows
                drawArc(
                    color = Color(0xFF32285A),
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(leftEyeX - 10 * u, eyeY - 18 * u),
                    size = Size(20 * u, 8 * u),
                    style = Stroke(width = 2.0f * u, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color(0xFF32285A),
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(rightEyeX - 10 * u, eyeY - 18 * u),
                    size = Size(20 * u, 8 * u),
                    style = Stroke(width = 2.0f * u, cap = StrokeCap.Round)
                )

                // Subtle anime nose highlight
                drawLine(
                    color = Color(0xFFD6A99A),
                    start = Offset(cx, cy + 8 * u),
                    end = Offset(cx - 1.5f * u, cy + 13 * u),
                    strokeWidth = 1.8f * u,
                    cap = StrokeCap.Round
                )

                // ===========================================================
                // State-Synced Anime Mouth Expression
                // ===========================================================
                val mouthY = cy + 25 * u
                when (avatarState) {
                    AvatarState.SPEAKING -> {
                        val openH = (4f + 8f * speakMouthWave) * u
                        drawRoundRect(
                            color = Color(0xFFD95B72),
                            topLeft = Offset(cx - 7.5f * u, mouthY - openH / 2f),
                            size = Size(15 * u, openH),
                            cornerRadius = CornerRadius(6 * u, 6 * u)
                        )
                    }
                    AvatarState.LISTENING -> {
                        // Attentive soft smile + subtle open listening cue
                        drawArc(
                            color = Color(0xFFC2596E),
                            startAngle = 15f,
                            sweepAngle = 150f,
                            useCenter = false,
                            topLeft = Offset(cx - 8 * u, mouthY - 4 * u),
                            size = Size(16 * u, 8 * u),
                            style = Stroke(width = 2.3f * u, cap = StrokeCap.Round)
                        )
                    }
                    AvatarState.THINKING -> {
                        // Thoughtful small oval mouth
                        drawOval(
                            color = Color(0xFFC2596E),
                            topLeft = Offset(cx - 3.5f * u, mouthY - 2.5f * u),
                            size = Size(7 * u, 5 * u)
                        )
                    }
                    else -> {
                        // Warm welcoming anime smile
                        drawArc(
                            color = Color(0xFFC2596E),
                            startAngle = 15f,
                            sweepAngle = 150f,
                            useCenter = false,
                            topLeft = Offset(cx - 9 * u, mouthY - 5 * u),
                            size = Size(18 * u, 9 * u),
                            style = Stroke(width = 2.4f * u, cap = StrokeCap.Round)
                        )
                    }
                }

                // ===========================================================
                // Front Anime Hair Bangs & Glowing Cyber Highlights
                // ===========================================================
                val bangsPath = Path().apply {
                    moveTo(cx - 46 * u, cy - 12 * u)
                    cubicTo(cx - 48 * u, cy - 58 * u, cx + 48 * u, cy - 58 * u, cx + 46 * u, cy - 12 * u)
                    // Right side bang sweep
                    quadraticBezierTo(cx + 32 * u, cy - 24 * u, cx + 22 * u, cy - 6 * u)
                    quadraticBezierTo(cx + 14 * u, cy - 26 * u, cx + 3 * u, cy - 8 * u)
                    // Center parted locks
                    quadraticBezierTo(cx - 6 * u, cy - 28 * u, cx - 16 * u, cy - 7 * u)
                    quadraticBezierTo(cx - 28 * u, cy - 25 * u, cx - 46 * u, cy - 12 * u)
                    close()
                }
                drawPath(
                    path = bangsPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF392E75), Color(0xFF1E1842))
                    )
                )

                // Glowing Cyan/Sapphire Hair Halo Highlight Arc
                drawArc(
                    color = stateAccentColor.copy(alpha = 0.55f),
                    startAngle = 205f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(cx - 34 * u, cy - 42 * u),
                    size = Size(68 * u, 26 * u),
                    style = Stroke(width = 2.4f * u, cap = StrokeCap.Round)
                )

                // ===========================================================
                // Futuristic Anime Cyber Headphones & Cheek Mic Boom
                // ===========================================================
                // Headband arc
                drawArc(
                    color = Color(0xFFD8E4FF),
                    startAngle = 192f,
                    sweepAngle = 156f,
                    useCenter = false,
                    topLeft = Offset(cx - 50 * u, cy - 62 * u),
                    size = Size(100 * u, 92 * u),
                    style = Stroke(width = 4.5f * u, cap = StrokeCap.Round)
                )

                // Left & Right Cyber Ear-Cups
                val earCupPositions = listOf(cx - 46 * u, cx + 46 * u)
                for (cupX in earCupPositions) {
                    drawRoundRect(
                        color = Color(0xFF192342),
                        topLeft = Offset(cupX - 7 * u, cy - 14 * u),
                        size = Size(14 * u, 28 * u),
                        cornerRadius = CornerRadius(6 * u, 6 * u)
                    )
                    // Glowing LED ring on ear-cup
                    drawRoundRect(
                        color = stateAccentColor,
                        topLeft = Offset(cupX - 4 * u, cy - 9 * u),
                        size = Size(8 * u, 18 * u),
                        cornerRadius = CornerRadius(4 * u, 4 * u),
                        style = Stroke(width = 2.2f * u)
                    )
                }

                // Translucent Acoustic Microphone Boom extending to left cheek
                drawLine(
                    color = stateAccentColor.copy(alpha = 0.9f),
                    start = Offset(cx - 42 * u, cy + 8 * u),
                    end = Offset(cx - 18 * u, cy + 24 * u),
                    strokeWidth = 2.4f * u,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = stateAccentColor,
                    radius = 3.5f * u,
                    center = Offset(cx - 18 * u, cy + 24 * u)
                )

                // Outer metallic portrait rim
                drawCircle(
                    color = stateAccentColor.copy(alpha = 0.65f),
                    radius = portraitR,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.2.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // State Pill Indicator
        Surface(
            color = ZiaCardSurface,
            shape = CircleShape,
            modifier = Modifier
                .border(1.dp, stateAccentColor.copy(alpha = 0.6f), CircleShape)
                .testTag("avatar_state_badge")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val stateIcon = when (avatarState) {
                    AvatarState.IDLE -> Icons.Default.GraphicEq
                    AvatarState.LISTENING -> Icons.Default.Mic
                    AvatarState.THINKING -> Icons.Default.Psychology
                    AvatarState.SPEAKING -> Icons.Default.VolumeUp
                    AvatarState.UNAVAILABLE -> Icons.Default.MicOff
                }
                Icon(
                    imageVector = stateIcon,
                    contentDescription = avatarState.label,
                    tint = stateAccentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = avatarState.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = stateAccentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = avatarState.hinglishStatus,
            style = MaterialTheme.typography.bodyMedium,
            color = ZiaTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Mandatory Transparency Disclaimer
        Text(
            text = stringResource(id = R.string.avatar_disclaimer),
            style = MaterialTheme.typography.labelSmall,
            color = ZiaTextMuted,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Confirmation Preview Card shown before executing any consequential phone action
 * (Calls, SMS, Alarms, Camera, or Wiping Memory).
 */
@Composable
fun ActionConfirmationModalCard(
    pendingAction: PendingAction,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, ZiaAmberWarning.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
            .testTag("confirmation_preview_card"),
        color = Color(0xFF1F1B2C)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(ZiaAmberWarning)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CONFIRMATION PREVIEW · ${pendingAction.actionTitle.uppercase()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = ZiaAmberWarning,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pendingAction.summaryHinglish,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Security Note: ${pendingAction.permissionNote}",
                style = MaterialTheme.typography.labelSmall,
                color = ZiaTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cancel_pending_action_button")
                ) {
                    Text("Cancel")
                }
                androidx.compose.material3.Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("confirm_pending_action_button")
                ) {
                    Text("Confirm & Execute")
                }
            }
        }
    }
}
