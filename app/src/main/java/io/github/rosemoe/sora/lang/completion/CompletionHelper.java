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
package io.github.rosemoe.sora.lang.completion;

import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;

/**
 * Helper class for completion
 *
 * @author Rosemoe
 */
public class CompletionHelper {

    /**
     * Searches backward on the line, with the given checker to check chars.
     * Returns the longest text that matches the requirement
     */
    public static String computePrefix(ContentReference ref, CharPosition pos, PrefixChecker checker) {
        int begin = pos.column;
        var line = ref.getLine(pos.line);
        for (; begin > 0; begin--) {
            if (!checker.check(line.charAt(begin - 1))) {
                break;
            }
        }
        return line.substring(begin, pos.column);
    }

    /**
     * Check whether the thread is abandoned by editor.
     * Return true if it is cancelled by editor.
     */
    public static boolean checkCancelled() {
            return true;
    }

    public interface PrefixChecker {

        boolean check(char ch);

    }

}
