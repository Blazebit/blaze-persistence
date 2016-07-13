package com.blazebit.persistence.impl.expression;

import java.util.Arrays;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 12.07.2016.
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
