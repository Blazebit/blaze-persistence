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
public class AssertUpdateStatement extends AbstractAssertStatement {

    public AssertUpdateStatement(List<String> tables) {
        super(tables);
    }

    public void validate(String query) {
        query = query.toLowerCase();
        if (!query.startsWith("update ")) {
            if (!query.startsWith("merge into ")) {
                int updateIndex;
                if (!query.startsWith("with ") || (updateIndex = query.indexOf(") update ")) == -1) {
                    query = stripReturningClause(query);
                    updateIndex = -2;
                    if (!query.startsWith("update ")) {
                        Assert.fail("Query is not an update statement: " + query);
                        return;
                    }
                }

                // TODO: validate with clauses?
                query = query.substring(updateIndex + 2);
            }
        }
        // Strip returning because the parser can't handle it
        // TODO: validate returning clause?
        query = stripReturningClause(query);
        validateTables(query);
    }

}
