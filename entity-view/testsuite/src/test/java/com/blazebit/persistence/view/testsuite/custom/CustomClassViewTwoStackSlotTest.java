/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.custom;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
public class CustomClassViewTwoStackSlotTest extends AbstractEntityViewTest {

    @EntityView(Document.class)
    static class CustomDocumentView {

        private final long id;
        private final String name;

        public CustomDocumentView(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @IdMapping
        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    // For issue #1575
    @Test
    public void test() {
        build(
            CustomDocumentView.class
        );
    }

}
