/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base.jpa.assertion;

import org.junit.Assert;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AssertInsertStatement extends AbstractAssertStatement {

    public AssertInsertStatement(List<String> tables) {
        super(tables);
    }

    public void validate(String query) {
        query = query.toLowerCase();
        if (!query.startsWith("insert ")) {
            int insertIndex;
            if (!query.startsWith("with ") || (insertIndex = query.indexOf(") insert ")) == -1) {
                query = stripReturningClause(query);
                insertIndex = -2;
                if (!query.startsWith("insert ")) {
                    Assert.fail("Query is not an insert statement: " + query);
                    return;
                }
            }

            // TODO: validate with clauses?
            query = query.substring(insertIndex + 2);
        }
        // Strip returning because the parser can't handle it
        // TODO: validate returning clause?
        query = stripReturningClause(query);
        validateTables(query);
    }

}
