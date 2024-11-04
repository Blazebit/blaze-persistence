/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite;

import com.blazebit.persistence.testsuite.base.jpa.BlazePersistenceForkedTestsuite;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

/**
 *
 * @author Moritz Becker
 * @since 1.5.0
 */
@RunWith(AllTests.class)
public class EntityViewTestsuite1 extends BlazePersistenceForkedTestsuite {

    public static TestSuite suite() {
        return suite0(1);
    }
}
