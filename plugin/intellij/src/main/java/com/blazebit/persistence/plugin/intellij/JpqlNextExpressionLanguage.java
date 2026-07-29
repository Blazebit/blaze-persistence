/*
 * Copyright 2014 - 2019 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence.plugin.intellij;

import com.intellij.lang.InjectableLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.Nullable;

public class JpqlNextExpressionLanguage extends Language implements InjectableLanguage {

    public static final JpqlNextExpressionLanguage INSTANCE = new JpqlNextExpressionLanguage();

    public JpqlNextExpressionLanguage() {
        super("JPQL.Next-Expression");
    }

    @Nullable
    @Override
    public LanguageFileType getAssociatedFileType() {
        return JpqlNextExpressionFileType.INSTANCE;
    }
}
