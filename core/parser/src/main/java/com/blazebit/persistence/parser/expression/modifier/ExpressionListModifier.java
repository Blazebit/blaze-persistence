/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.parser.expression.modifier;

import com.blazebit.persistence.parser.expression.Expression;

import java.util.List;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ExpressionListModifier implements ExpressionModifier {

    protected final List<Expression> target;
    protected final int modificationIndex;

    public ExpressionListModifier(List<? extends Expression> target, int modificationIndex) {
        this.target = (List<Expression>) target;
        this.modificationIndex = modificationIndex;
    }

    public List<Expression> getTarget() {
        return target;
    }

    @Override
    public void set(Expression expression) {
        target.set(modificationIndex, expression);
    }

    @Override
    public Expression get() {
        return target.get(modificationIndex);
    }

}
