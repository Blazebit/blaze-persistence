package com.blazebit.persistence.impl.expression;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 12.07.2016.
 */
public enum NumericType {

    // NOTE: order matters!

    INTEGER(Integer.class),
    LONG(Long.class),
    BIG_INTEGER(BigInteger.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    BIG_DECIMAL(BigDecimal.class);

    private Class<?> javaType;

    NumericType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public Class<?> getJavaType() {
        return javaType;
    }
}
