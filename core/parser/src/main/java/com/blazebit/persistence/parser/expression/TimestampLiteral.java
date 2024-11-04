/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

import com.blazebit.persistence.parser.util.TypeUtils;

import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class TimestampLiteral extends TemporalLiteral {

    public TimestampLiteral(Date value) {
        super(value);
    }

    @Override
    public Expression copy(ExpressionCopyContext copyContext) {
        return new TimestampLiteral((Date) value.clone());
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
        if (value instanceof java.sql.Timestamp) {
            return TypeUtils.TIMESTAMP_CONVERTER.toString((Timestamp) value);
        } else {
            return TypeUtils.DATE_TIMESTAMP_CONVERTER.toString(value);
        }
    }
}
