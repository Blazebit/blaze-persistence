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
            if (!query.startsWith("with ") || (insertIndex = query.indexOf(")\ninsert ")) == -1) {
                query = stripReturningClause(query);
                insertIndex = -2;
                if (!query.startsWith("delete ")) {
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
