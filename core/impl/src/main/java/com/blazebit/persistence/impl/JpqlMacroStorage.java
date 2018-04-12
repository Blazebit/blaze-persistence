/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.MacroConfiguration;
import com.blazebit.persistence.spi.JpqlMacro;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class JpqlMacroStorage {

    private final ExpressionFactory expressionFactory;

    private boolean dirty;
    private Map<String, JpqlMacro> scopedMacros;
    private MacroConfiguration macroConfiguration;

    public JpqlMacroStorage(ExpressionFactory expressionFactory, MacroConfiguration macroConfiguration) {
        this.expressionFactory = expressionFactory;
        this.macroConfiguration = macroConfiguration;
    }

    public MacroConfiguration getMacroConfiguration() {
        if (dirty) {
            macroConfiguration = macroConfiguration.with(JpqlMacroAdapter.createMacros(scopedMacros, expressionFactory));
            scopedMacros.clear();
            dirty = false;
        }

        return macroConfiguration;
    }

    public void registerMacro(String macroName, JpqlMacro jpqlMacro) {
        if (scopedMacros == null) {
            scopedMacros = new HashMap<>();
        }
        scopedMacros.put(macroName.toUpperCase(), jpqlMacro);
        dirty = true;
    }
}
