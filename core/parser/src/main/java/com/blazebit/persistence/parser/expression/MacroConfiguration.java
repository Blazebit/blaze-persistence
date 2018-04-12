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

package com.blazebit.persistence.parser.expression;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class MacroConfiguration {

    final Map<String, MacroFunction> macros;
    final int hash;

    private MacroConfiguration(Map<String, MacroFunction> macros) {
        this.macros = macros;
        this.hash = macros == null ? 0 : macros.hashCode();
    }

    public static MacroConfiguration of(Map<String, MacroFunction> macros) {
        Map<String, MacroFunction> map = new HashMap<>(macros.size());
        for (Map.Entry<String, MacroFunction> entry : macros.entrySet()) {
            map.put(entry.getKey().toUpperCase(), entry.getValue());
        }
        return new MacroConfiguration(map);
    }

    public MacroFunction get(String name) {
        return macros.get(name);
    }

    public MacroConfiguration with(Map<String, MacroFunction> newMacros) {
        Map<String, MacroFunction> map = new HashMap<>(this.macros.size() + newMacros.size());
        map.putAll(this.macros);
        for (Map.Entry<String, MacroFunction> entry : newMacros.entrySet()) {
            map.put(entry.getKey().toUpperCase(), entry.getValue());
        }
        return new MacroConfiguration(map);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MacroConfiguration)) {
            return false;
        }

        MacroConfiguration that = (MacroConfiguration) o;
        return hash == that.hash && macros.equals(that.macros);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
