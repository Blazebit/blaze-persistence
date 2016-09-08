package com.blazebit.persistence.impl;

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.MacroConfiguration;
import com.blazebit.persistence.impl.expression.MacroFunction;
import com.blazebit.persistence.spi.JpqlMacro;

import java.util.HashMap;
import java.util.List;
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
            scopedMacros = new HashMap<String, JpqlMacro>();
        }
        scopedMacros.put(macroName.toUpperCase(), jpqlMacro);
        dirty = true;
    }
}
