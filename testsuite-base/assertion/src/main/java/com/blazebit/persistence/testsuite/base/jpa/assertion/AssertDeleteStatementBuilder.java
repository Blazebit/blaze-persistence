/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base.jpa.assertion;

import com.blazebit.persistence.testsuite.base.jpa.RelationalModelAccessor;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AssertDeleteStatementBuilder extends AbstractAssertStatementBuilder<AssertDeleteStatementBuilder> {

    public AssertDeleteStatementBuilder(AssertStatementBuilder parentBuilder, RelationalModelAccessor relationalModelAccessor) {
        super(parentBuilder, relationalModelAccessor);
    }

    @Override
    protected AssertStatement build() {
        return new AssertDeleteStatement(tables);
    }
}
