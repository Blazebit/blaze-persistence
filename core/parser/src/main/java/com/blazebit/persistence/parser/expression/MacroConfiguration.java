/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

import java.util.Collections;
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
        this.macros = Collections.unmodifiableMap(macros);
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
            if (entry.getValue() == null) {
                map.remove(entry.getKey().toUpperCase());
            } else {
                map.put(entry.getKey().toUpperCase(), entry.getValue());
            }
        }
        return new MacroConfiguration(map);
    }

    public Map<String, MacroFunction> getMacros() {
        return macros;
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
