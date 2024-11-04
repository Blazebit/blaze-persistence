/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AbstractAttributeTest {

    @Test
    public void stripThisFromMappingTests() {
        assertEquals("", AbstractAttribute.stripThisFromMapping("this"));
        assertEquals("this[]", AbstractAttribute.stripThisFromMapping("this[]"));
        assertEquals("this()", AbstractAttribute.stripThisFromMapping("this()"));
        assertEquals("this", AbstractAttribute.stripThisFromMapping("this.this"));
        assertEquals("this.this", AbstractAttribute.stripThisFromMapping("this.this.this"));
        assertEquals("", AbstractAttribute.stripThisFromMapping(" this"));
        assertEquals("thisa", AbstractAttribute.stripThisFromMapping("thisa"));

        assertEquals("this MEMBER OF abc", AbstractAttribute.stripThisFromMapping("this MEMBER OF abc"));
        assertEquals("CASE WHEN this = owner THEN true ELSE false END", AbstractAttribute.stripThisFromMapping("CASE WHEN this = owner THEN true ELSE false END"));
        assertEquals("TYPE(this)", AbstractAttribute.stripThisFromMapping("TYPE(this)"));
        assertEquals("TREAT(this AS Subtype)", AbstractAttribute.stripThisFromMapping("TREAT(this AS Subtype)"));
    }

}
