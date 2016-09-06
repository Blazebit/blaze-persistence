package com.blazebit.persistence.impl.expression;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 * @author Moritz Becker
 * @since 1.2
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
