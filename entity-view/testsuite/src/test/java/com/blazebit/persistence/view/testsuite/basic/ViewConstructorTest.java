/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic;

import org.junit.Assert;
import org.junit.Test;

import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.DocumentViewWithMissingMappingParameter;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ViewConstructorTest extends AbstractEntityViewTest {

    @Test
    public void testAbstractClass() {
        try {
            build(DocumentViewWithMissingMappingParameter.class);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
    }
}
