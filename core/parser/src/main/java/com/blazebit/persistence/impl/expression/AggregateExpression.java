/*
 * Copyright 2014 Blazebit.
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

import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author Moritz Becker
 */
public class AggregateExpression extends FunctionExpression {
    private final boolean distinct;

    public AggregateExpression(boolean distinct, String functionName, PathExpression expression) {
        super(functionName, Arrays.asList(new Expression[]{expression}));
        this.distinct = distinct;
    }
    
    /**
     * Constructor for COUNT(*)
     * @param distinct
     */
    public AggregateExpression() {
        super("COUNT", Collections.<Expression>emptyList());
        this.distinct = false;
    }

    public boolean isDistinct() {
        return distinct;
    }
}
