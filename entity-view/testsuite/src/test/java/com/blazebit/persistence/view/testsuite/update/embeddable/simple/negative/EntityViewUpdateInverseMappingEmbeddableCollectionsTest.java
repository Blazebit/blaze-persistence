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

package com.blazebit.persistence.view.testsuite.update.embeddable.simple.negative;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.MappingInverse;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateInverseMappingEmbeddableCollectionsTest extends AbstractEntityViewTest {

    @Test
    public void testValidateInvalidConfiguration1() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(UpdatableDocumentEmbeddableWithCollectionsViewBase1.class);
        try {
            evm = cfg.createEntityViewManager(cbf);
            fail("Expected failure because of invalid attribute definition!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Found use of @MappingInverse on attribute that isn't an inverse relationship"));
        }
    }

    @UpdatableEntityView
    @EntityView(Document.class)
    public interface UpdatableDocumentEmbeddableWithCollectionsViewBase1 {

        @IdMapping
        public Long getId();

        @MappingInverse
        public List<NameObject> getNames();
        public void setNames(List<NameObject> names);

    }

    @Test
    public void testValidateInvalidConfiguration2() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(NameObjectView.class);
        cfg.addEntityView(UpdatableDocumentEmbeddableWithCollectionsViewBase2.class);
        try {
            evm = cfg.createEntityViewManager(cbf);
            fail("Expected failure because of invalid attribute definition!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Found use of @MappingInverse on attribute that isn't an inverse relationship"));
        }
    }

    @UpdatableEntityView
    @EntityView(Document.class)
    public interface UpdatableDocumentEmbeddableWithCollectionsViewBase2 {

        @IdMapping
        public Long getId();

        @MappingInverse
        public List<NameObjectView> getNames();
        public void setNames(List<NameObjectView> names);

    }

    @UpdatableEntityView
    @EntityView(NameObject.class)
    public interface NameObjectView {

        public String getPrimaryName();
        public void setPrimaryName(String primaryName);

    }
}
