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

package com.blazebit.persistence.view.filter;

import com.blazebit.persistence.Filterable;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.view.Filter;
import com.blazebit.persistence.view.impl.filter.ContainsFilterImpl;
import com.blazebit.persistence.view.impl.filter.ContainsIgnoreCaseFilterImpl;
import com.blazebit.persistence.view.impl.filter.EndsWithFilterImpl;
import com.blazebit.persistence.view.impl.filter.EndsWithIgnoreCaseFilterImpl;
import com.blazebit.persistence.view.impl.filter.ExactFilterImpl;
import com.blazebit.persistence.view.impl.filter.StartsWithFilterImpl;
import com.blazebit.persistence.view.impl.filter.StartsWithIgnoreCaseFilterImpl;
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
        Filter filter = new ContainsFilterImpl(value);
        verifyFilter(filter, expression).like("%" + value + "%");
    }
    
    @Test
    public void testContainsIgnoreCase() {
        Filter filter = new ContainsIgnoreCaseFilterImpl(value);
        verifyFilter(filter, expression).like("%" + value + "%", false);
    }
    
    @Test
    public void testEndsWith() {
        Filter filter = new EndsWithFilterImpl(value);
        verifyFilter(filter, expression).like("%" + value);
    }
    
    @Test
    public void testEndsWithIgnoreCase() {
        Filter filter = new EndsWithIgnoreCaseFilterImpl(value);
        verifyFilter(filter, expression).like("%" + value, false);
    }
    
    @Test
    public void testStartsWith() {
        Filter filter = new StartsWithFilterImpl(value);
        verifyFilter(filter, expression).like(value + "%");
    }
    
    @Test
    public void testStartsWithIgnoreCase() {
        Filter filter = new StartsWithIgnoreCaseFilterImpl(value);
        verifyFilter(filter, expression).like(value + "%", false);
    }
    
    @Test
    public void testExact() {
        Filter filter = new ExactFilterImpl(String.class, value);
        verifyFilter(filter, expression).eq(value);
    }
    
    public RestrictionBuilder<?> verifyFilter(Filter filter, String expression) {
        RestrictionBuilder<?> restrictionBuilder = Mockito.mock(RestrictionBuilder.class);
        filter.apply(restrictionBuilder);
        return Mockito.verify(restrictionBuilder);
    }
}
