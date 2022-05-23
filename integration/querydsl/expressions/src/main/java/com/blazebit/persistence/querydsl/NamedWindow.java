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

package com.blazebit.persistence.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;

import java.util.Objects;

/**
 * A named window. Combines a {@link WindowDefinition} and an alias.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public class NamedWindow extends WindowDefinition<NamedWindow, Void> {

    private static final long serialVersionUID = -7435742875703132533L;

    private final String alias;

    /**
     * Create a new named window.
     *
     * @param alias Alias for the window
     */
    public NamedWindow(String alias) {
        super(Void.class);
        this.alias = alias;
    }

    /**
     * Get the alias for the window
     *
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    @Override
    public Expression<Void> getValue() {
        return Expressions.template(super.getType(), alias);
    }

    public Expression<Void> getWindowDefinition() {
        return Expressions.operation(getType(), JPQLNextOps.WINDOW_NAME, Expressions.constant(alias), super.getValue());
    }

    @Override
    @SuppressWarnings("EqualsHashCode") // Hashcode is declared final in MutableExpressionBase, the implementation there is safe with this equals implementation
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof NamedWindow)) {
            return false;
        } else if (!super.equals(o)) {
            return false;
        }
        NamedWindow that = (NamedWindow) o;
        return Objects.equals(alias, that.alias) && super.equals(o);
    }

}
