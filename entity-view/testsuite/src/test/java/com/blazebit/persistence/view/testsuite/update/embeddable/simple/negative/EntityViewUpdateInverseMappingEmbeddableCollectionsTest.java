/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.simple.negative;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.MappingInverse;
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
@Category({ NoEclipselink.class})
public class EntityViewUpdateInverseMappingEmbeddableCollectionsTest extends AbstractEntityViewTest {

    @Test
    public void testValidateInvalidConfiguration1() {
        try {
            evm = build(UpdatableDocumentEmbeddableWithCollectionsViewBase1.class);
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
        try {
            evm = build(
                    NameObjectView.class,
                    UpdatableDocumentEmbeddableWithCollectionsViewBase2.class
            );
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
