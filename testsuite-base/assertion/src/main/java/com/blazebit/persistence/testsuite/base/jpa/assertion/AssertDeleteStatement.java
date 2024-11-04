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
public class AssertDeleteStatement extends AbstractAssertStatement {

    public AssertDeleteStatement(List<String> tables) {
        super(tables);
    }

    public void validate(String query) {
        query = query.toLowerCase();
        if (query.startsWith("delete ")) {
            // The parser can't handle the PostgreSQL USING join style, so we need to replace this with DELETE x FROM syntax
            int usingIndex = query.indexOf(" using ");
            if (usingIndex != -1) {
                query = query.replace(" using ", " from ");
                if (query.startsWith("delete from ")) {
                    query = "delete " + query.substring("delete from ".length());
                }
            }
        } else {
            int deleteIndex;
            if (!query.startsWith("with ") || (deleteIndex = query.indexOf(") delete ")) == -1) {
                query = stripReturningClause(query);
                deleteIndex = -2;
                if (!query.startsWith("delete ")) {
                    Assert.fail("Query is not a delete statement: " + query);
                    return;
                }
            }

            // TODO: validate with clauses?
            query = query.substring(deleteIndex + 2);
        }
        // Strip returning because the parser can't handle it
        // TODO: validate returning clause?
        query = stripReturningClause(query);
        validateTables(query);
    }

}
