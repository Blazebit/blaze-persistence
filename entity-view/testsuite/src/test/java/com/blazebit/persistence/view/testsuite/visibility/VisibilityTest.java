/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.view.testsuite.visibility;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.visibility.model1.IdHolderView;
import com.blazebit.persistence.view.testsuite.visibility.model2.DocumentView;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class VisibilityTest extends AbstractEntityViewTest {

    @Test
    public void testVisibilityModifierAndDifferentPackages() {
        EntityViewManager evm = build(
                IdHolderView.class,
                DocumentView.class
        );

        // Requires a class in the package where IdHolderView resides that overrides with public modifiers
        // Should also print a warning that equals can't support user types
        DocumentView documentView = evm.getReference(DocumentView.class, 0L);
        Assert.assertEquals(Long.MIN_VALUE, documentView.id());
    }
}
