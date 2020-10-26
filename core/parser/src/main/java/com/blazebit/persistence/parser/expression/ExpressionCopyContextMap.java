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

package com.blazebit.persistence.parser.expression;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
public class ExpressionCopyContextMap implements ExpressionCopyContext {

    private final Map<String, String> parameterMapping;
    private final boolean copyResolved;

    public ExpressionCopyContextMap(Map<String, String> parameterMapping, boolean copyResolved) {
        this.parameterMapping = parameterMapping;
        this.copyResolved = copyResolved;
    }

    @Override
    public String getNewParameterName(String oldParameterName) {
        String newParameterName = parameterMapping.get(oldParameterName);
        if (newParameterName == null) {
            return oldParameterName;
        }
        return newParameterName;
    }

    @Override
    public boolean isCopyResolved() {
        return copyResolved;
    }
}
