/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

import com.blazebit.persistence.parser.util.TypeUtils;
import org.junit.Test;

import java.lang.annotation.RetentionPolicy;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 */
public class TypeUtilsTest {

    @Test
    public void testEnumAsLiteral() {
        assertEquals(RetentionPolicy.class.getName() + "." + RetentionPolicy.RUNTIME.name(), TypeUtils.asLiteral(RetentionPolicy.RUNTIME, (Set<String>) null));
    }

}
