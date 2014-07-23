/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.Filterable;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.view.Filter;
import com.blazebit.persistence.view.filter.ContainsFilter;
import com.blazebit.persistence.view.filter.EndsWithFilter;
import com.blazebit.persistence.view.filter.ExactFilter;
import com.blazebit.persistence.view.filter.StartsWithFilter;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author Christian Beikov
 */
public class FilterTest {
    
    private final String expression = "name";
    private final String value = "test";
    
    @Test
    public void testContains() {
        Filter filter = new ContainsFilter(value);
        verifyFilter(filter, expression).like("%" + value + "%");
    }
    
    @Test
    public void testEndsWith() {
        Filter filter = new EndsWithFilter(value);
        verifyFilter(filter, expression).like("%" + value);
    }
    
    @Test
    public void testStartsWith() {
        Filter filter = new StartsWithFilter(value);
        verifyFilter(filter, expression).like(value + "%");
    }
    
    @Test
    public void testExact() {
        Filter filter = new ExactFilter(value);
        verifyFilter(filter, expression).eq(value);
    }
    
    public RestrictionBuilder<?> verifyFilter(Filter filter, String expression) {
        Filterable<?> filterable = Mockito.mock(Filterable.class);
        RestrictionBuilder<?> restrictionBuilder = Mockito.mock(RestrictionBuilder.class);
        Mockito.when(filterable.where(expression)).thenReturn((RestrictionBuilder) restrictionBuilder);
        filter.apply((Filterable) filterable, expression);
        return Mockito.verify(filterable.where(expression));
    }
}
