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
public class AssertInsertStatementBuilder extends AbstractAssertStatementBuilder<AssertInsertStatementBuilder> {

    public AssertInsertStatementBuilder(AssertStatementBuilder parentBuilder, RelationalModelAccessor relationalModelAccessor) {
        super(parentBuilder, relationalModelAccessor);
    }

    @Override
    protected AssertStatement build() {
        return new AssertInsertStatement(tables);
    }
}
