package com.catpuppyapp.puppygit.codeeditor

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.SettingsUtil

object PLFont {
    val codeFont = FontFamily(Font(R.font.jb_mono_regular))

    fun editorCodeFont() = if(SettingsUtil.isEditorUseSystemFonts()) null else codeFont

}
