/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.3.3
 */
public class CorrelationProviderHelperTest {

    @Test
    public void replaceAlias() {
        assertEquals("id IN __WF", CorrelationProviderHelper.temporaryReplace("id IN correlationKey", "correlationKey", "__WF"));
        assertEquals("__WF IN __correlationAlias", CorrelationProviderHelper.temporaryReplace("workflow IN __correlationAlias", "workflow", "__WF"));
        assertEquals(" __WF IN __correlationAlias", CorrelationProviderHelper.temporaryReplace(" workflow IN __correlationAlias", "workflow", "__WF"));
        assertEquals("(__WF IN __correlationAlias)", CorrelationProviderHelper.temporaryReplace("(workflow IN __correlationAlias)", "workflow", "__WF"));
        assertEquals("s.workflow IN __correlationAlias", CorrelationProviderHelper.temporaryReplace("s.workflow IN __correlationAlias", "workflow", "__WF"));
        assertEquals("sworkflow IN __correlationAlias", CorrelationProviderHelper.temporaryReplace("sworkflow IN __correlationAlias", "workflow", "__WF"));
        assertEquals("workflows IN __correlationAlias", CorrelationProviderHelper.temporaryReplace("workflows IN __correlationAlias", "workflow", "__WF"));
    }

}
