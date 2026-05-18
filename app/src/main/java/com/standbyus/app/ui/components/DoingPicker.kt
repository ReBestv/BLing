package com.standbyus.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.ui.theme.StandByUsLightColors

@Composable
fun DoingPicker(
    selectedFeeling: Feeling,
    customDoing: String,
    onCustomDoingChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        Text(
            "此刻状态（可选）",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = customDoing,
            onValueChange = { if (it.length <= 20) onCustomDoingChanged(it) },
            placeholder = {
                Text(
                    "布宁布宁在干嘛…",
                    fontSize = 15.sp,
                    color = StandByUsLightColors.placeholder
                )
            },
            textStyle = TextStyle(fontSize = 15.sp),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StandByUsLightColors.accent,
                unfocusedBorderColor = StandByUsLightColors.border,
                cursorColor = StandByUsLightColors.accent
            )
        )
    }
}
