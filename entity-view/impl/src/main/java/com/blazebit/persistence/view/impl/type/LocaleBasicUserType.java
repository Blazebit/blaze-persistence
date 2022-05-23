/*
 * Copyright 2014 - 2022 Blazebit.
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

import com.blazebit.i18n.LocaleUtils;
import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

import java.util.Locale;

/**
 *
 * @author Christian Beikov
 * @since 1.6.3
 */
public class LocaleBasicUserType extends ImmutableBasicUserType<Locale> {

    public static final BasicUserType<Locale> INSTANCE = new LocaleBasicUserType();

    @Override
    public Locale fromString(CharSequence sequence) {
        return LocaleUtils.getLocale(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
