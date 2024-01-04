/*
 * Copyright 2014 - 2024 Blazebit.
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
