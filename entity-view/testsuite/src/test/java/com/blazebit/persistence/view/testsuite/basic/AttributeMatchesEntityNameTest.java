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

package com.blazebit.persistence.view.testsuite.basic;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.DocumentHolder;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public class AttributeMatchesEntityNameTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Override
    protected Class<?>[] getEntityClasses() {
        return concat(super.getEntityClasses(), DocumentHolder.class);
    }

    @Before
    public void initEvm() {
        evm = build(
            DocumentHolderView.class,
            DocumentIdView.class
        );
    }

    @EntityView(DocumentHolder.class)
    public interface DocumentHolderView {
        @IdMapping
        Long getId();
        @Mapping("Document")
        DocumentIdView getDocument();
    }

    @EntityView(Document.class)
    public interface DocumentIdView {
        @IdMapping
        Long getId();
    }

    // Test for https://github.com/Blazebit/blaze-persistence/issues/1294
    @Test
    public void testFetch() {
        evm.find(em, DocumentHolderView.class, 1L);
    }
}
