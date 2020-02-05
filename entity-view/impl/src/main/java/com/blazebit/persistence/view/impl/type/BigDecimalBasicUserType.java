/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

import java.math.BigDecimal;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class BigDecimalBasicUserType extends ImmutableBasicUserType<BigDecimal> {

    public static final BasicUserType<BigDecimal> INSTANCE = new BigDecimalBasicUserType();

    @Override
    public BigDecimal fromString(CharSequence sequence) {
        return new BigDecimal(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
