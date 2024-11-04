/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
public interface ExpressionCopyContext {

    public static final ExpressionCopyContext EMPTY = new ExpressionCopyContext() {
        @Override
        public String getNewParameterName(String oldParameterName) {
            return oldParameterName;
        }

        @Override
        public Expression getExpressionForAlias(String alias) {
            return null;
        }

        @Override
        public boolean isCopyResolved() {
            return false;
        }
    };

    public static final ExpressionCopyContext CLONE = new ExpressionCopyContext() {
        @Override
        public String getNewParameterName(String oldParameterName) {
            return oldParameterName;
        }

        @Override
        public Expression getExpressionForAlias(String alias) {
            return null;
        }

        @Override
        public boolean isCopyResolved() {
            return true;
        }
    };

    public String getNewParameterName(String oldParameterName);

    public Expression getExpressionForAlias(String alias);

    public boolean isCopyResolved();
}
