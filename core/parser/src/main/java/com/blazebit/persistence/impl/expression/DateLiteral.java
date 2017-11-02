/*
 * Copyright 2014 - 2017 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.util.TypeUtils;

import java.util.Date;

/**
 *
 * @author Moritz Becker
 * @since 1.2
 */
public class DateLiteral extends TemporalLiteral {

    public DateLiteral(Date value) {
        super(value);
    }

    @Override
    public Expression clone(boolean resolved) {
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
