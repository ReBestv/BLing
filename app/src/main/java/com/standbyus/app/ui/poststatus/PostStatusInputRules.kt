package com.standbyus.app.ui.poststatus

object PostStatusInputRules {
    const val MaxDetailTextLength = 30

    fun sanitizeDetailInput(value: String): String =
        value.take(MaxDetailTextLength)

    fun publishNote(value: String): String =
        sanitizeDetailInput(value).trim()
}
