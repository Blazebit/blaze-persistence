package com.blazebit.persistence.impl.expression;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 12.07.2016.
 */
public enum NumericType {

    // NOTE: order matters!

    BYTE(Byte.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class);

    private Class<?> javaType;

    NumericType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public Class<?> getJavaType() {
        return javaType;
    }
}
