/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
