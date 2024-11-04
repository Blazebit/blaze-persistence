/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
