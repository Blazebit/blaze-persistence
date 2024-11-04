/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import static org.junit.Assert.assertEquals;

import jakarta.persistence.Tuple;

import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Workflow;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class FunctionTest extends AbstractCoreTest {
    
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Workflow.class
        };
    }
    
    @Test
    public void testCustomFunctionNoArgs() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class)
            .select("FUNCTION('zero')");
        String expectedQuery = "SELECT " + function("zero") + " FROM Workflow workflow";
        assertEquals(expectedQuery, cb.getQueryString());
    }
    
    @Test
    public void testCustomFunctionSingleArg() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class)
            .select("FUNCTION('zero', id)");
        String expectedQuery = "SELECT " + function("zero", "workflow.id") + " FROM Workflow workflow";
        assertEquals(expectedQuery, cb.getQueryString());
    }
    
    @Test
    public void testCustomFunctionMultipleArgs() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class)
            .select("FUNCTION('zero', id, id)");
        String expectedQuery = "SELECT " + function("zero", "workflow.id", "workflow.id") + " FROM Workflow workflow";
        assertEquals(expectedQuery, cb.getQueryString());
    }
}
