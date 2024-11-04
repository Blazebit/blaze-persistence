/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base.jpa.assertion;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AssertSelectStatement extends AbstractAssertStatement {

    private final List<String> fetchedTables;

    public AssertSelectStatement(List<String> tables, List<String> fetchedTables) {
        super(tables);
        this.fetchedTables = fetchedTables;
    }

    public void validate(String query) {
        query = query.toLowerCase();
        if (!query.startsWith("select ")) {
            int selectIndex;
            if (!query.startsWith("with ") || (selectIndex = query.indexOf(") select ")) == -1) {
                Assert.fail("Query is not a select statement: " + query);
                return;
            }

            // TODO: validate with clauses?
            query = query.substring(selectIndex + 2);
        }
        validateTables(query);

        if (!fetchedTables.isEmpty()) {
            List<String> fromElements = getFetchedFromElements(query);

            List<String> missingFromElements = new ArrayList<>(fetchedTables);
            ListIterator<String> iter = fromElements.listIterator();
            while (iter.hasNext()) {
                String element = iter.next();
                if (missingFromElements.remove(element)) {
                    iter.remove();
                }
            }

            Assert.assertTrue(
                    "Expected fetched from elements don't match. Missing " + missingFromElements + ", Unexpected " + fromElements + "\nQuery: " + query,
                    fromElements.isEmpty() && missingFromElements.isEmpty()
            );
        }
    }

}
