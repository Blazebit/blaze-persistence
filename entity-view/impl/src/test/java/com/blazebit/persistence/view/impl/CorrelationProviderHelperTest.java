/*
 * Copyright 2014 - 2019 Blazebit.
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
