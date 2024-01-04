/*
 * Copyright 2014 - 2024 Blazebit.
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
package com.blazebit.persistence.testsuite.hibernate6;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.hibernate6.entity.PropertyHolder;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;

public class AnyMappingTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            PropertyHolder.class
        };
    }

    @Test
    public void anyMapping_doesNotFailCriteriaBuilderFactoryCreation() {
        CriteriaBuilder<PropertyHolder> cb = cbf.create(em, PropertyHolder.class, "p");
        assertTrue(true);
    }
}
