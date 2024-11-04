/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public enum ArithmeticOperator {

    ADDITION("+"),
    SUBTRACTION("-"),
    DIVISION("/"),
    MULTIPLICATION("*");

    private final String symbol;

    ArithmeticOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isAddOrSubtract() {
        return this == ADDITION || this == SUBTRACTION;
    }

    public static ArithmeticOperator fromSymbol(String symbol) {
        for (ArithmeticOperator op : ArithmeticOperator.values()) {
            if (op.getSymbol().equals(symbol)) {
                return op;
            }
        }
        return null;
    }
}
