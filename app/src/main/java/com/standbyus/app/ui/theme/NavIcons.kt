package com.standbyus.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** 4 custom bottom-nav icons — filled (面型) style matching preview.html. */
object NavIcons {

    val Home: ImageVector = ImageVector.Builder(
        name = "NavHome",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Filled house silhouette: pentagon roof + walls
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 10f)
            lineTo(12f, 2f)
            lineTo(21f, 10f)
            lineTo(21f, 21f)
            lineTo(3f, 21f)
            close()
        }
    }.build()

    val Checkin: ImageVector = ImageVector.Builder(
        name = "NavCheckin",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Filled calendar body
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 4f)
            lineTo(21f, 4f)
            lineTo(21f, 22f)
            lineTo(3f, 22f)
            close()
        }
        // Top hooks (stroke visible on transparent bg above body)
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(8f, 2f)
            lineTo(8f, 6f)
        }
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(16f, 2f)
            lineTo(16f, 6f)
        }
    }.build()

    val Album: ImageVector = ImageVector.Builder(
        name = "NavAlbum",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Frame border via EvenOdd: outer rect filled, inner cutout
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(2f, 2f)
            lineTo(22f, 2f)
            lineTo(22f, 22f)
            lineTo(2f, 22f)
            close()
            moveTo(5f, 5f)
            lineTo(19f, 5f)
            lineTo(19f, 19f)
            lineTo(5f, 19f)
            close()
        }
        // Filled mountain triangle
        path(fill = SolidColor(Color.Black)) {
            moveTo(16f, 11f)
            lineTo(8f, 19f)
            lineTo(19f, 19f)
            close()
        }
        // Filled circle (approximated as diamond)
        path(fill = SolidColor(Color.Black)) {
            moveTo(8.5f, 7f)
            lineTo(10f, 8.5f)
            lineTo(8.5f, 10f)
            lineTo(7f, 8.5f)
            close()
        }
    }.build()

    val Timeline: ImageVector = ImageVector.Builder(
        name = "NavTimeline",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Filled octagon body
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2f)
            lineTo(19f, 5f)
            lineTo(22f, 12f)
            lineTo(19f, 19f)
            lineTo(12f, 22f)
            lineTo(5f, 19f)
            lineTo(2f, 12f)
            lineTo(5f, 5f)
            close()
        }
        // Clock hand (thin filled rectangle pointing up)
        path(fill = SolidColor(Color.Black)) {
            moveTo(11f, 7f)
            lineTo(13f, 7f)
            lineTo(13f, 12f)
            lineTo(11f, 12f)
            close()
        }
    }.build()
}
