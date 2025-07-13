package com.catpuppyapp.puppygit.codeeditor

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.SettingsUtil

object PLFont {
    // nl 是 no-ligature，非连体字
    // nl is no-ligature
    val codeFont = FontFamily(Font(R.font.jb_mono_nl_regular))

    fun editorCodeFont() = if(SettingsUtil.isEditorUseSystemFonts()) null else codeFont

}
