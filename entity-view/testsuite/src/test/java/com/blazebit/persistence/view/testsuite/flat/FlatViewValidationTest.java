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

package com.blazebit.persistence.view.testsuite.flat;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class FlatViewValidationTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    /* **************************** *
     * Collections not allowed in flat views when used as view root
     * **************************** */

    @EntityView(Document.class)
    interface FlatViewWithCollection {
        Set<Person> getPartners();
    }

    @Test
    public void flatViewWithCollectionQueryingFails() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(FlatViewWithCollection.class);
        evm = cfg.createEntityViewManager(cbf);
        try {
            applySetting(evm, FlatViewWithCollection.class, cbf.create(em, Document.class));
            fail("Expected querying to fail!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("flat view"));
            assertTrue(ex.getMessage().contains("collection"));
            assertTrue(ex.getMessage().contains(FlatViewWithCollection.class.getSimpleName()));
        }
    }

    /* **************************** *
     * Collections not allowed in flat views when embedded in view root that is also a flat view
     * **************************** */

    @EntityView(Document.class)
    interface FlatViewWithCollectionHolder {
        @Mapping("this")
        FlatViewWithCollection getEmbedded();
    }

    @Test
    public void nestedFlatViewWithCollectionQueryingFails() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(FlatViewWithCollection.class);
        cfg.addEntityView(FlatViewWithCollectionHolder.class);
        evm = cfg.createEntityViewManager(cbf);
        try {
            applySetting(evm, FlatViewWithCollectionHolder.class, cbf.create(em, Document.class));
            fail("Expected querying to fail!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("flat view"));
            assertTrue(ex.getMessage().contains("collection"));
            assertTrue(ex.getMessage().contains(FlatViewWithCollection.class.getSimpleName()));
        }
    }

    /* **************************** *
     * Collections with fetch = JOIN not allowed in flat views when used as subview in non-indexed collections
     * **************************** */

    @EntityView(Person.class)
    interface FlatViewWithCollectionParent {
        Set<FlatViewWithCollection> getOwnedDocuments();
    }

    @Test
    public void flatViewWithCollectionAsNonIndexedCollectionSubviewBuildingFails() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(FlatViewWithCollection.class);
        cfg.addEntityView(FlatViewWithCollectionParent.class);
        try {
            evm = cfg.createEntityViewManager(cbf);
            fail("Expected building to fail!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("flat view"));
            assertTrue(ex.getMessage().contains("collection"));
            assertTrue(ex.getMessage().contains(FlatViewWithCollection.class.getSimpleName()));
        }
    }

    /* **************************** *
     * Collections with fetch != JOIN are allowed
     * **************************** */

    @EntityView(Person.class)
    interface FlatViewWithCollectionSelectParent {
        Set<FlatViewWithCollectionSelect> getOwnedDocuments();
    }

    @EntityView(Document.class)
    interface FlatViewWithCollectionSelect {
        @Mapping(fetch = FetchStrategy.SELECT)
        Set<Person> getPartners();
    }

    @Test
    public void flatViewWithCollectionSelectAsNonIndexedCollectionSubviewBuilds() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(FlatViewWithCollectionSelect.class);
        cfg.addEntityView(FlatViewWithCollectionSelectParent.class);
        evm = cfg.createEntityViewManager(cbf);
    }

    /* **************************** *
     * Correlation with fetch = JOIN not allowed in flat views when used as subview in non-indexed collections
     * **************************** */

    @EntityView(Person.class)
    interface FlatViewWithCorrelationParent {
        Set<FlatViewWithCorrelation> getOwnedDocuments();
    }

    @EntityView(Document.class)
    interface FlatViewWithCorrelation {
        @MappingCorrelatedSimple(correlated = Person.class, correlationBasis = "owner", correlationExpression = "this IN correlationKey", fetch = FetchStrategy.JOIN)
        Set<Person> getPartners();
    }

    @Test
    public void flatViewWithCorrelationAsNonIndexedCollectionSubviewBuildingFails() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(FlatViewWithCorrelation.class);
        cfg.addEntityView(FlatViewWithCorrelationParent.class);
        try {
            evm = cfg.createEntityViewManager(cbf);
            fail("Expected building to fail!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("flat view"));
            assertTrue(ex.getMessage().contains("collection"));
            assertTrue(ex.getMessage().contains(FlatViewWithCorrelation.class.getSimpleName()));
        }
    }

    /* **************************** *
     * Correlations with fetch != JOIN are allowed
     * **************************** */

    @EntityView(Person.class)
    interface FlatViewWithCorrelationSelectParent {
        Set<FlatViewWithCorrelationSelect> getOwnedDocuments();
    }

    @EntityView(Document.class)
    interface FlatViewWithCorrelationSelect {
        @MappingCorrelatedSimple(correlated = Person.class, correlationBasis = "owner", correlationExpression = "this IN correlationKey", fetch = FetchStrategy.SELECT)
        Set<Person> getPartners();
    }

    @Test
    public void flatViewWithCorrelationSelectAsNonIndexedCollectionSubviewBuilds() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(FlatViewWithCorrelationSelect.class);
        cfg.addEntityView(FlatViewWithCorrelationSelectParent.class);
        evm = cfg.createEntityViewManager(cbf);
    }

}
