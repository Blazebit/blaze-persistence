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

package com.blazebit.persistence.view.testsuite.filter;

import org.junit.Test;
import org.mockito.Mockito;

import com.blazebit.persistence.EscapeBuilder;
import com.blazebit.persistence.LikeBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.WhereBuilder;
import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.impl.filter.ContainsFilterImpl;
import com.blazebit.persistence.view.impl.filter.ContainsIgnoreCaseFilterImpl;
import com.blazebit.persistence.view.impl.filter.EndsWithFilterImpl;
import com.blazebit.persistence.view.impl.filter.EndsWithIgnoreCaseFilterImpl;
import com.blazebit.persistence.view.impl.filter.EqualFilterImpl;
import com.blazebit.persistence.view.impl.filter.StartsWithFilterImpl;
import com.blazebit.persistence.view.impl.filter.StartsWithIgnoreCaseFilterImpl;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class FilterTest {

    private final String expression = "name";
    private final String value = "test";

    @Test
    public void testContains() {
        AttributeFilterProvider filter = new ContainsFilterImpl(value);
        Mockito.verify(verifyLikeFilter(filter, expression).like()).value("%" + value + "%");
    }

    @Test
    public void testContainsIgnoreCase() {
        AttributeFilterProvider filter = new ContainsIgnoreCaseFilterImpl(value);
        Mockito.verify(verifyLikeFilter(filter, expression).like(false)).value("%" + value + "%");
    }

    @Test
    public void testEndsWith() {
        AttributeFilterProvider filter = new EndsWithFilterImpl(value);
        Mockito.verify(verifyLikeFilter(filter, expression).like()).value("%" + value);
    }

    @Test
    public void testEndsWithIgnoreCase() {
        AttributeFilterProvider filter = new EndsWithIgnoreCaseFilterImpl(value);
        Mockito.verify(verifyLikeFilter(filter, expression).like(false)).value("%" + value);
    }

    @Test
    public void testStartsWith() {
        AttributeFilterProvider filter = new StartsWithFilterImpl(value);
        Mockito.verify(verifyLikeFilter(filter, expression).like()).value(value + "%");
    }

    @Test
    public void testStartsWithIgnoreCase() {
        AttributeFilterProvider filter = new StartsWithIgnoreCaseFilterImpl(value);
        Mockito.verify(verifyLikeFilter(filter, expression).like(false)).value(value + "%");
    }

    @Test
    public void testExact() {
        AttributeFilterProvider filter = new EqualFilterImpl(String.class, value);
        verifyFilter(filter, expression).eq(value);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public RestrictionBuilder<?> verifyFilter(AttributeFilterProvider filter, String expression) {
        WhereBuilder whereBuilder = Mockito.mock(WhereBuilder.class);
        RestrictionBuilder<?> rb = Mockito.mock(RestrictionBuilder.class);
        Mockito.when(whereBuilder.where(expression)).thenReturn(rb);
        filter.apply(whereBuilder, expression);
        return Mockito.verify(rb);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public RestrictionBuilder<?> verifyLikeFilter(AttributeFilterProvider filter, String expression) {
        WhereBuilder whereBuilder = Mockito.mock(WhereBuilder.class);
        RestrictionBuilder rb = Mockito.mock(RestrictionBuilder.class, Mockito.RETURNS_DEEP_STUBS);
        EscapeBuilder eb = Mockito.mock(EscapeBuilder.class);
        LikeBuilder lb = Mockito.mock(LikeBuilder.class);
        Mockito.when(whereBuilder.where(expression)).thenReturn(rb);
        Mockito.when(rb.like()).thenReturn(lb);
        Mockito.when(rb.like(false)).thenReturn(lb);
        Mockito.when(lb.value("%" + value)).thenReturn(eb);
        Mockito.when(lb.value(value + "%")).thenReturn(eb);
        Mockito.when(lb.value("%" + value + "%")).thenReturn(eb);
        Mockito.when(eb.noEscape()).thenReturn(whereBuilder);
        filter.apply(whereBuilder, expression);
        return rb;
    }
}
