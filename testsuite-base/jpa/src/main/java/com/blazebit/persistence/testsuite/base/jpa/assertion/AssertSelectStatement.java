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
            if (!query.startsWith("with ") || (selectIndex = query.indexOf(")\nselect ")) == -1) {
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
