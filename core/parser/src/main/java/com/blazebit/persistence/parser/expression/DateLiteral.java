/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

import com.blazebit.persistence.parser.util.TypeUtils;

import java.util.Date;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class DateLiteral extends TemporalLiteral {

    public DateLiteral(Date value) {
        super(value);
    }

    @Override
    public Expression copy(ExpressionCopyContext copyContext) {
        return new DateLiteral((Date) value.clone());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return TypeUtils.DATE_AS_DATE_CONVERTER.toString(value);
    }
}
