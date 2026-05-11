package com.standbyus.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbyus.app.data.model.Feeling

@Composable
fun FeelingPicker(
    feelings: List<Feeling> = Feeling.entries,
    selectedFeeling: Feeling,
    onFeelingSelected: (Feeling) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("我现在的心情", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(feelings) { feeling ->
                val isSelected = feeling == selectedFeeling
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 1f,
                    animationSpec = tween(200)
                )
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(feeling.color)
                        .then(
                            if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                            else Modifier
                        )
                        .clickable { onFeelingSelected(feeling) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(feeling.emoji, fontSize = 24.sp)
                }
            }
        }
    }
}
