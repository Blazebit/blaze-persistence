package com.blazebit.persistence.impl.expression;

import java.util.Date;

/**
 *
 * @author Moritz Becker
 * @since 1.2
 */
public abstract class TemporalLiteral extends AbstractExpression {

    protected final Date value;

    public TemporalLiteral(Date value) {
        this.value = value;
    }

    public Date getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemporalLiteral)) return false;

        TemporalLiteral that = (TemporalLiteral) o;

        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
