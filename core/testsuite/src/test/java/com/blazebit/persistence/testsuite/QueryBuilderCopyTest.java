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

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.testsuite.entity.Document;
import org.junit.Test;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
public class QueryBuilderCopyTest extends AbstractCoreTest {

    @Test
    // Test for issue #602
    public void testQueryCopying() {
        // The key to reproducing the bug is having the parameter in the select clause multiple times
        cbf.create(em, Long.class)
                .from(Document.class)
                .select("COALESCE(id,:param3)")
                .select("COALESCE(id,:param3)")
                .copy(String.class)
                .select("name");
    }
}
