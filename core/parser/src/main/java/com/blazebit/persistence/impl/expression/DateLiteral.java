package com.blazebit.persistence.impl.expression;

import java.util.Date;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 04.08.2016.
 */
public class DateLiteral extends TemporalLiteral {

    public DateLiteral(Date value) {
        super(value);
    }

    @Override
    public Expression clone() {
        return new DateLiteral(value);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
