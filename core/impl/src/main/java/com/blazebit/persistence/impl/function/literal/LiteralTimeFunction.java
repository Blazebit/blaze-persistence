/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.impl.function.literal;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public class LiteralTimeFunction extends LiteralFunction {

    public static final String FUNCTION_NAME = "LITERAL_TIME";

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return java.sql.Time.class;
    }

    @Override
    protected String getFunctionName() {
        return FUNCTION_NAME;
    }

}
