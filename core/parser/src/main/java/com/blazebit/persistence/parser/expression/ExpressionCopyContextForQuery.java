/*
 * Copyright 2014 - 2023 Blazebit.
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
 * @since 1.6.9
 */
public class ExpressionCopyContextForQuery implements ExpressionCopyContext {

    private final ExpressionCopyContext parent;
    private final Map<String, Expression> aliasedExpressions;

    public ExpressionCopyContextForQuery(ExpressionCopyContext parent, Map<String, Expression> aliasedExpressions) {
        this.parent = parent;
        this.aliasedExpressions = aliasedExpressions;
    }

    @Override
    public String getNewParameterName(String oldParameterName) {
        return parent.getNewParameterName(oldParameterName);
    }

    @Override
    public Expression getExpressionForAlias(String alias) {
        return aliasedExpressions.get(alias);
    }

    @Override
    public boolean isCopyResolved() {
        return false;
    }
}
