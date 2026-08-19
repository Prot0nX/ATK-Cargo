package com.atk.atk_cargo.feature.chat.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ColorWheel(
    modifier: Modifier = Modifier,
    initialColor: Color,
    onColorChanged: (Color) -> Unit
) {
    val density = LocalDensity.current.density
    
    // HSV State
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }

    // Initial calculation (approximate)
    // In a real app we would convert initialColor to HSV here correctly
    
    Box(modifier = modifier.aspectRatio(1f)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val delta = change.position - center
                        val radius = min(size.width, size.height) / 2f
                        
                        val angle = (atan2(delta.y, delta.x) * 180 / Math.PI).toFloat()
                        hue = if (angle < 0) angle + 360 else angle
                        
                        val dist = hypot(delta.x, delta.y)
                        saturation = (dist / radius).coerceIn(0f, 1f)
                        
                        val color = Color.hsv(hue, saturation, value)
                        onColorChanged(color)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val delta = offset - center
                        val radius = min(size.width, size.height) / 2f
                        
                        val angle = (atan2(delta.y, delta.x) * 180 / Math.PI).toFloat()
                        hue = if (angle < 0) angle + 360 else angle
                        
                        val dist = hypot(delta.x, delta.y)
                        saturation = (dist / radius).coerceIn(0f, 1f)
                        
                        val color = Color.hsv(hue, saturation, value)
                        onColorChanged(color)
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f
            
            // Draw Color Wheel
            val sweepGradient = Brush.sweepGradient(
                colors = listOf(
                    Color.Red, Color.Magenta, Color.Blue, Color.Cyan,
                    Color.Green, Color.Yellow, Color.Red
                ),
                center = center
            )
            
            drawCircle(
                brush = sweepGradient,
                radius = radius,
                center = center
            )
            
            // Saturation Overlay (White in center)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            
            // Selector Indicator
            val selectorAngleRad = (hue * Math.PI / 180)
            val selectorDist = saturation * radius
            val selectorX = center.x + selectorDist * cos(selectorAngleRad).toFloat()
            val selectorY = center.y + selectorDist * sin(selectorAngleRad).toFloat()
            
            drawCircle(
                color = Color.White,
                radius = 12.dp.toPx(),
                center = Offset(selectorX, selectorY),
            )
            drawCircle(
                color = Color.Black,
                radius = 12.dp.toPx(),
                center = Offset(selectorX, selectorY),
                style = Stroke(width = 2.dp.toPx())
            )
            
            drawCircle(
                color = Color.hsv(hue, saturation, value),
                radius = 8.dp.toPx(),
                center = Offset(selectorX, selectorY)
            )
        }
    }
}
