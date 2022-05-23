/*
 * Copyright 2014 - 2022 Blazebit.
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
public class EntityViewTestsuite2 extends BlazePersistenceForkedTestsuite {

    public static TestSuite suite() {
        return suite0(2);
    }
}
