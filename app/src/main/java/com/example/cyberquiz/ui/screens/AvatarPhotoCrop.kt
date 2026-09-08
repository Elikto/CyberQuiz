package com.example.cyberquiz.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

private const val MAX_IMPORT_SIDE = 2048
private const val SAVED_AVATAR_SIDE = 512

internal fun loadAvatarBitmap(context: Context, uri: Uri): Bitmap? {
    val decoded = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
        }
    }.getOrNull() ?: return null

    val largestSide = max(decoded.width, decoded.height)
    if (largestSide <= MAX_IMPORT_SIDE) return decoded

    val ratio = MAX_IMPORT_SIDE.toFloat() / largestSide.toFloat()
    return Bitmap.createScaledBitmap(
        decoded,
        (decoded.width * ratio).roundToInt().coerceAtLeast(1),
        (decoded.height * ratio).roundToInt().coerceAtLeast(1),
        true
    )
}

internal data class AvatarCropTransform(
    val viewportSidePx: Int,
    val zoom: Float,
    val offsetX: Float,
    val offsetY: Float
)

internal fun clampAvatarCropOffset(
    bitmapWidth: Int,
    bitmapHeight: Int,
    viewportSidePx: Int,
    zoom: Float,
    requested: Offset
): Offset {
    if (bitmapWidth <= 0 || bitmapHeight <= 0 || viewportSidePx <= 0) return Offset.Zero
    val baseScale = max(
        viewportSidePx.toFloat() / bitmapWidth.toFloat(),
        viewportSidePx.toFloat() / bitmapHeight.toFloat()
    )
    val displayScale = baseScale * zoom.coerceAtLeast(1f)
    val displayedWidth = bitmapWidth * displayScale
    val displayedHeight = bitmapHeight * displayScale
    val maxX = ((displayedWidth - viewportSidePx) / 2f).coerceAtLeast(0f)
    val maxY = ((displayedHeight - viewportSidePx) / 2f).coerceAtLeast(0f)
    return Offset(
        requested.x.coerceIn(-maxX, maxX),
        requested.y.coerceIn(-maxY, maxY)
    )
}

internal fun cropAvatarBitmap(source: Bitmap, transform: AvatarCropTransform): Bitmap {
    val viewport = transform.viewportSidePx.coerceAtLeast(1)
    val baseScale = max(
        viewport.toFloat() / source.width.toFloat(),
        viewport.toFloat() / source.height.toFloat()
    )
    val displayScale = baseScale * transform.zoom.coerceAtLeast(1f)
    val displayedWidth = source.width * displayScale
    val displayedHeight = source.height * displayScale
    val topLeftX = (viewport - displayedWidth) / 2f + transform.offsetX
    val topLeftY = (viewport - displayedHeight) / 2f + transform.offsetY

    val sourceLeft = ((-topLeftX) / displayScale)
        .roundToInt()
        .coerceIn(0, (source.width - 1).coerceAtLeast(0))
    val sourceTop = ((-topLeftY) / displayScale)
        .roundToInt()
        .coerceIn(0, (source.height - 1).coerceAtLeast(0))
    val desiredSide = (viewport / displayScale).roundToInt().coerceAtLeast(1)
    val cropSide = minOf(
        desiredSide,
        source.width - sourceLeft,
        source.height - sourceTop
    ).coerceAtLeast(1)

    val cropped = Bitmap.createBitmap(source, sourceLeft, sourceTop, cropSide, cropSide)
    return Bitmap.createScaledBitmap(cropped, SAVED_AVATAR_SIDE, SAVED_AVATAR_SIDE, true)
}

internal fun saveCustomPlayerAvatar(context: Context, bitmap: Bitmap): String {
    val file = File(context.filesDir, PLAYER_CUSTOM_AVATAR_FILE)
    FileOutputStream(file, false).use { output ->
        check(bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)) {
            "Impossible d'enregistrer l'avatar."
        }
    }
    playerCosmeticsPreferences(context)
        .edit()
        .putString(PLAYER_CUSTOM_AVATAR_PATH_KEY, file.absolutePath)
        .putString(PLAYER_SELECTED_AVATAR_KEY, PlayerAvatarStyle.CUSTOM.storageKey)
        .apply()
    return file.absolutePath
}

@Composable
internal fun AvatarPhotoCropScreen(
    bitmap: Bitmap,
    saving: Boolean,
    onCancel: () -> Unit,
    onSave: (AvatarCropTransform) -> Unit
) {
    BackHandler(enabled = !saving, onBack = onCancel)
    val image = remember(bitmap) { bitmap.asImageBitmap() }
    var viewportSide by remember { mutableStateOf(0) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF081126), Color(0xFF030712))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel, enabled = !saving) {
                Text("‹", color = Color(0xFFF5F7FF), fontSize = 31.sp)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "Recadrer l'avatar",
                    color = Color(0xFFF5F7FF),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "ZOOM & CADRAGE",
                    color = Color(0xFF9FAED3),
                    fontSize = 8.sp,
                    letterSpacing = 1.4.sp
                )
            }
        }

        Text(
            "Pince avec deux doigts pour zoomer, puis glisse l'image pour choisir le cadrage.",
            color = Color(0xFFB9C5E8),
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(maxHeight = 520.dp)
                    .aspectRatio(1f)
                    .background(Color(0xFF050B16), RoundedCornerShape(24.dp))
                    .border(2.dp, Color(0xFF19F2E5), RoundedCornerShape(24.dp))
                    .onSizeChanged { size ->
                        viewportSide = minOf(size.width, size.height)
                        offset = clampAvatarCropOffset(
                            bitmap.width,
                            bitmap.height,
                            viewportSide,
                            zoom,
                            offset
                        )
                    }
                    .pointerInput(bitmap, viewportSide) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            val newZoom = (zoom * gestureZoom).coerceIn(1f, 5f)
                            val proposed = offset + pan
                            zoom = newZoom
                            offset = clampAvatarCropOffset(
                                bitmap.width,
                                bitmap.height,
                                viewportSide,
                                newZoom,
                                proposed
                            )
                        }
                    }
            ) {
                val viewport = minOf(size.width, size.height)
                if (viewport <= 0f) return@Canvas
                val baseScale = max(
                    viewport / bitmap.width.toFloat(),
                    viewport / bitmap.height.toFloat()
                )
                val displayScale = baseScale * zoom
                val displayedWidth = bitmap.width * displayScale
                val displayedHeight = bitmap.height * displayScale
                val topLeft = Offset(
                    (size.width - displayedWidth) / 2f + offset.x,
                    (size.height - displayedHeight) / 2f + offset.y
                )

                drawImage(
                    image = image,
                    dstOffset = IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()),
                    dstSize = IntSize(
                        displayedWidth.roundToInt().coerceAtLeast(1),
                        displayedHeight.roundToInt().coerceAtLeast(1)
                    )
                )

                val gridColor = Color.White.copy(alpha = .42f)
                drawLine(gridColor, Offset(size.width / 3f, 0f), Offset(size.width / 3f, size.height), 1.2f)
                drawLine(gridColor, Offset(size.width * 2f / 3f, 0f), Offset(size.width * 2f / 3f, size.height), 1.2f)
                drawLine(gridColor, Offset(0f, size.height / 3f), Offset(size.width, size.height / 3f), 1.2f)
                drawLine(gridColor, Offset(0f, size.height * 2f / 3f), Offset(size.width, size.height * 2f / 3f), 1.2f)
            }
        }

        Text(
            "Zoom × ${"%.1f".format(zoom)}",
            color = Color(0xFF19F2E5),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(
                enabled = !saving,
                onClick = {
                    zoom = 1f
                    offset = Offset.Zero
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("RÉINITIALISER", color = Color(0xFF9FAED3), fontWeight = FontWeight.Bold)
            }
            Button(
                enabled = viewportSide > 0 && !saving,
                onClick = {
                    onSave(
                        AvatarCropTransform(
                            viewportSidePx = viewportSide,
                            zoom = zoom,
                            offsetX = offset.x,
                            offsetY = offset.y
                        )
                    )
                },
                modifier = Modifier.weight(1.4f).height(48.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD652FF),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF26304A)
                )
            ) {
                Text(
                    if (saving) "ENREGISTREMENT…" else "UTILISER LA PHOTO",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}
