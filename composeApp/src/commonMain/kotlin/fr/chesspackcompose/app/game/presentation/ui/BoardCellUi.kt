package fr.chesspackcompose.app.game.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import fr.chesspackcompose.app.core.ui.conditional
import fr.chesspackcompose.app.game.domain.PiecePosition
import fr.chesspackcompose.app.game.presentation.CellUIModel
import fr.chesspackcompose.app.game.presentation.CoordinateUI
import fr.chesspackcompose.app.game.presentation.GameUiEvent
import org.jetbrains.compose.resources.painterResource

private val moveIndicatorColor = Color.White.copy(alpha = 0.75F)

@Composable
fun BoardCellUi(
    cell: CellUIModel,
    size: Dp,
    rotation: Float,
    onEvent: (GameUiEvent) -> Unit
) {
    val xLocation = remember(size) { size.times(cell.position.x) }
    val yLocation = remember(size) { size.times(cell.position.y) }
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var startReleaseAnimation by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val sizeInPixel = remember(size) { size.value * density.density }

    LaunchedEffect(startReleaseAnimation) {
        if (startReleaseAnimation) {
            Animatable(initialValue = 1F).animateTo(
                targetValue = 0F,
                animationSpec = tween()
            ) {
                dragOffset *= value
            }
            dragOffset = Offset.Zero
            startReleaseAnimation = false
        }
    }
    Box(
        modifier = Modifier
            .offset(x = xLocation, y = yLocation)
            .size(width = size, height = size)
            .background(color = cell.backgroundColor)
            .drawBehind {
                if (cell.markAsLegalMove) {
                    if (cell.pieceInfo == null) {
                        drawCircle(
                            color = moveIndicatorColor,
                            radius = sizeInPixel / 8
                        )
                    } else {
                        drawCircle(
                            color = moveIndicatorColor,
                            radius = sizeInPixel / 2 - sizeInPixel / 8 + sizeInPixel / 16,
                            style = Stroke(width = sizeInPixel / 8)
                        )
                    }
                }
            }
    ) {
        if (cell.coordinateUI != null) {
            CoordinatesUI(coordinateUI = cell.coordinateUI, rotation = rotation)
        }
    }
    if (cell.pieceInfo != null) {
        var dragX by remember { mutableIntStateOf(0) }
        var dragY by remember { mutableIntStateOf(0) }

        val dragPosition by remember(dragX, dragY) {
            mutableStateOf(PiecePosition(x = dragX, y = dragY))
        }

        LaunchedEffect(dragPosition, isDragging) {
            if (isDragging) {
                onEvent(GameUiEvent.PieceDragging(at = dragPosition))
            }
        }
        Image(
            modifier = Modifier
                .offset(x = xLocation, y = yLocation)
                .size(width = size, height = size)
                .zIndex(if (isDragging) 2F else 1F)
                .padding(3.dp)
                .graphicsLayer {
                    translationY = dragOffset.y
                    translationX = dragOffset.x
                }
                .conditional(cell.moveEnabled) {
                    pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                onEvent(GameUiEvent.PiecePicked(cell = cell))
                            },
                            onDrag = { _, dragAmount ->
                                dragOffset += dragAmount
                                dragX =
                                    ((((dragOffset.x + sizeInPixel / 2) / sizeInPixel)) + cell.position.x).toInt()
                                dragY =
                                    ((((dragOffset.y + sizeInPixel / 2) / sizeInPixel)) + cell.position.y).toInt()
                            },
                            onDragEnd = {
                                isDragging = false
                                val position = PiecePosition(
                                    ((((dragOffset.x + sizeInPixel / 2) / sizeInPixel)) + cell.position.x).toInt(),
                                    ((((dragOffset.y + sizeInPixel / 2) / sizeInPixel)) + cell.position.y).toInt()
                                )

                                if (cell.pieceInfo.legalMoves.contains(position)) {
                                    onEvent(
                                        GameUiEvent.PieceDropped(cell = cell, at = position)
                                    )
                                    dragOffset = Offset.Zero
                                } else {
                                    onEvent(GameUiEvent.DragCanceled)
                                    startReleaseAnimation = true
                                }
                            }
                        )
                    }
                }
                .graphicsLayer { rotationZ = rotation },
            painter = painterResource(cell.pieceInfo.drawableResource),
            contentDescription = cell.contentDescription
        )
    }
}

@Composable
fun BoxScope.CoordinatesUI(
    coordinateUI: CoordinateUI,
    rotation: Float
) {
    val yAlign = if (rotation == 0F) Alignment.BottomEnd else Alignment.TopStart
    val xAlign = if (rotation == 0F) Alignment.TopStart else Alignment.BottomEnd

    if (coordinateUI.y != null) {
        CoordinatesUILabel(
            modifier = Modifier
                .align(yAlign)
                .rotate(rotation),
            text = coordinateUI.y,
            color = coordinateUI.color,
        )
    }
    if (coordinateUI.x != null) {
        CoordinatesUILabel(
            modifier = Modifier
                .align(xAlign)
                .rotate(rotation),
            text = coordinateUI.x,
            color = coordinateUI.color
        )
    }
}

@Composable
fun CoordinatesUILabel(modifier: Modifier = Modifier, text: String, color: Color) {
    Text(
        modifier = modifier.padding(1.dp).zIndex(2F),
        text = text,
        color = color,
        fontWeight = FontWeight.Bold,
        lineHeight = 9.sp
    )
}