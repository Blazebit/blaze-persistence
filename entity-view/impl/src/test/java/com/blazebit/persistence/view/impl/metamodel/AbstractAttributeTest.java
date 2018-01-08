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
