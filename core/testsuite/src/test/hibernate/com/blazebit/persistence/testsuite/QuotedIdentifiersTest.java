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

import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Beikov
 * @since 1.2.1
 */
public class QuotedIdentifiersTest extends AbstractCoreTest {

    @Override
    protected Properties applyProperties(Properties properties) {
        Properties p = super.applyProperties(properties);
        p.setProperty("hibernate.globally_quoted_identifiers", "true");
        return p;
    }

    // Test for issue #574
    @Test
    public void testQueryValues() {
        long count = cbf.create(em, Long.class)
                .fromValues(Integer.class, "intVal", Arrays.asList(1, 2))
                .select("COUNT(*)")
                .getSingleResult();
        assertEquals(2L, count);
    }

}
