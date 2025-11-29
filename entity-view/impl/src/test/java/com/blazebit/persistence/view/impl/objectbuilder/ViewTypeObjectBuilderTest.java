/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.view.impl.objectbuilder;

import java.util.TreeSet;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.6.11
 */
public class ViewTypeObjectBuilderTest {

    // Test for #1832
    @Test
    public void hasSubFetches() {
        TreeSet<String> fetches = new TreeSet<>();
        fetches.add( "id" );
        fetches.add( "bbbbbb" );
        fetches.add( "a.name" );
        assertTrue(ViewTypeObjectBuilder.hasSubFetches(fetches, "id"));
        assertTrue(ViewTypeObjectBuilder.hasSubFetches(fetches, "bbbbbb"));
        assertFalse(ViewTypeObjectBuilder.hasSubFetches(fetches, "aaa.id"));
        assertFalse(ViewTypeObjectBuilder.hasSubFetches(fetches, "ccc.id"));
        assertFalse(ViewTypeObjectBuilder.hasSubFetches(fetches, "bbbbb"));
        assertFalse(ViewTypeObjectBuilder.hasSubFetches(fetches, "bbbbbbb"));
        assertTrue(ViewTypeObjectBuilder.hasSubFetches(fetches, "a"));
        assertTrue(ViewTypeObjectBuilder.hasSubFetches(fetches, "a.name"));
        assertFalse(ViewTypeObjectBuilder.hasSubFetches(fetches, "a.id"));
    }
}
