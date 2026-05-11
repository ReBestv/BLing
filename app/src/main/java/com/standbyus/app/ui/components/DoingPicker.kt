package com.standbyus.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbyus.app.data.model.Doing

@Composable
fun DoingPicker(
    presets: List<String> = Doing.entries.filter { it != Doing.CUSTOM }.map { it.displayName },
    selectedDoing: String,
    onDoingSelected: (String) -> Unit,
    customDoing: String,
    onCustomDoingChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("你在做什么？", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(presets) { preset ->
                val isSelected = preset == selectedDoing
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFFFF9F43) else Color(0xFFFFF0E0),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onDoingSelected(preset) }
                ) {
                    Text(
                        text = preset,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = if (isSelected) Color.White else Color(0xFF2D3436),
                        fontSize = 14.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = customDoing,
            onValueChange = { if (it.length <= 20) onCustomDoingChanged(it) },
            placeholder = { Text("自定义状态…") },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
    }
}
