/*
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2024  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 */
package com.catpuppyapp.puppygit.utils.programlang;

import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState;import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable;import java.lang.ref.WeakReference;

import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager;
import io.github.rosemoe.sora.lang.analysis.StyleReceiver;
import io.github.rosemoe.sora.lang.analysis.StyleUpdateRange;
import io.github.rosemoe.sora.lang.brackets.BracketsProvider;
import io.github.rosemoe.sora.lang.brackets.PairedBracket;
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticsContainer;
import io.github.rosemoe.sora.lang.styling.Styles;

public class EditorStyleDelegate implements StyleReceiver {

    private final WeakReference<CustomStateSaveable<TextEditorState>> editorRef;
    private PairedBracket foundPair;
    private BracketsProvider bracketsProvider;

    public EditorStyleDelegate(@NonNull CustomStateSaveable<TextEditorState> editor) {
        editorRef = new WeakReference<>(editor);
    }

    void onTextChange() {
        //  Should we do this?
        //bracketsProvider = null;
        //foundPair = null;
    }



    @Nullable
    public PairedBracket getFoundBracketPair() {
        return foundPair;
    }

    void reset() {
        foundPair = null;
        bracketsProvider = null;
    }

    private void runOnUiThread(Runnable operation) {
        var editor = editorRef.get();
        if (editor == null) {
            return;
        }
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            operation.run();
        } else {
            editor.getValue().postInLifecycle(operation);
        }
    }

    @Override
    public void setStyles(@NonNull AnalyzeManager sourceManager, @Nullable Styles styles) {
        setStyles(sourceManager, styles, null);
    }

    @Override
    public void setStyles(@NonNull AnalyzeManager sourceManager, @Nullable Styles styles, @Nullable Runnable action) {
        var editor = editorRef.get();
        if (editor != null && sourceManager == editor.getValue().getEditorLanguage().getAnalyzeManager()) {
            runOnUiThread(() -> {
                if (action != null) {
                    action.run();
                }
                editor.getValue().setStyles(styles);
            });
        }
    }

    @Override
    public void setDiagnostics(@NonNull AnalyzeManager sourceManager, @Nullable DiagnosticsContainer diagnostics) {

    }

    @Override
    public void updateBracketProvider(@NonNull AnalyzeManager sourceManager, @Nullable BracketsProvider provider) {

    }

    @Override
    public void updateStyles(@NonNull AnalyzeManager sourceManager, @NonNull Styles styles, @NonNull StyleUpdateRange range) {
        var editor = editorRef.get().getValue();
        if (editor != null && sourceManager == editor.getEditorLanguage().getAnalyzeManager()) {
            runOnUiThread(() -> editor.updateStyles(styles, range));
        }
    }

    public void clearFoundBracketPair() {
        this.foundPair = null;
    }
}
