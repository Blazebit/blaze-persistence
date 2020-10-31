/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.testsuite.update.metamodel;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewMetamodelFlatViewTest extends AbstractEntityViewTest {

    private final EntityViewConfiguration entityViewConfiguration;

    {
        entityViewConfiguration = EntityViews.createDefaultConfiguration();
        entityViewConfiguration.setProperty(ConfigurationProperties.PROXY_EAGER_LOADING, "true");
        entityViewConfiguration.setProperty(ConfigurationProperties.UPDATER_EAGER_LOADING, "true");
    }

    @UpdatableEntityView
    @EntityView(Document.class)
    public static interface DocumentBaseView {
        @IdMapping
        Long getId();
    }

    @EntityView(NameObject.class)
    public static interface NameObjectView {
        String getPrimaryName();
    }

    @UpdatableEntityView
    public static interface NameObjectUpdateView extends NameObjectView {
        void setPrimaryName(String name);
    }

    @CreatableEntityView
    public static interface NameObjectCreateAndUpdateView extends NameObjectUpdateView {

    }

    @CreatableEntityView
    public static interface NameObjectCreateView extends NameObjectView {
        void setPrimaryName(String name);
    }

    public static interface DocumentViewWithReadOnlyFlatView extends DocumentBaseView {
        NameObjectView getNameObject();
    }

    @Test
    public void nonUpdatableReadOnlyFlatView() {
        ViewMetamodel metamodel = build(entityViewConfiguration, DocumentViewWithReadOnlyFlatView.class, NameObjectView.class).getMetamodel();
        ManagedViewType<?> docViewType = metamodel.managedView(DocumentViewWithReadOnlyFlatView.class);
        assertFalse(docViewType.getAttribute("nameObject").isUpdatable());

        assertFalse(docViewType.getAttribute("nameObject").isPersistCascaded());
        assertFalse(docViewType.getAttribute("nameObject").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("nameObject").getPersistCascadeAllowedSubtypes().isEmpty());
        assertTrue(docViewType.getAttribute("nameObject").getUpdateCascadeAllowedSubtypes().isEmpty());

        assertFalse(docViewType.getAttribute("nameObject").isMutable());
    }

    public static interface DocumentViewWithUpdatableReadOnlyFlatView extends DocumentBaseView {
        NameObjectView getNameObject();
        void setNameObject(NameObjectView nameObject);
    }

    @Test
    public void updatableReadOnlyFlatView() {
        ViewMetamodel metamodel = build(entityViewConfiguration, DocumentViewWithUpdatableReadOnlyFlatView.class, NameObjectView.class).getMetamodel();
        ManagedViewType<?> docViewType = metamodel.managedView(DocumentViewWithUpdatableReadOnlyFlatView.class);
        assertTrue(docViewType.getAttribute("nameObject").isUpdatable());

        assertFalse(docViewType.getAttribute("nameObject").isPersistCascaded());
        assertFalse(docViewType.getAttribute("nameObject").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("nameObject").getPersistCascadeAllowedSubtypes().isEmpty());
        assertTrue(docViewType.getAttribute("nameObject").getUpdateCascadeAllowedSubtypes().isEmpty());

        assertTrue(docViewType.getAttribute("nameObject").isMutable());
    }

    /*
     * Use of NameObjectUpdateView
     */

    public static interface DocumentViewWithNonUpdatableMutableFlatView extends DocumentBaseView {
        NameObjectUpdateView getNameObject();
    }

    @Test
    public void nonUpdatableMutableFlatView() {
        ViewMetamodel metamodel = build(entityViewConfiguration, DocumentViewWithNonUpdatableMutableFlatView.class, NameObjectView.class, NameObjectUpdateView.class).getMetamodel();
        ManagedViewType<?> nameObjectViewType = metamodel.managedView(NameObjectUpdateView.class);
        ManagedViewType<?> docViewType = metamodel.managedView(DocumentViewWithNonUpdatableMutableFlatView.class);
        assertTrue(docViewType.getAttribute("nameObject").isUpdatable());

        assertTrue(docViewType.getAttribute("nameObject").isPersistCascaded());
        assertTrue(docViewType.getAttribute("nameObject").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("nameObject").getPersistCascadeAllowedSubtypes().contains(nameObjectViewType));
        assertTrue(docViewType.getAttribute("nameObject").getUpdateCascadeAllowedSubtypes().contains(nameObjectViewType));

        assertTrue(docViewType.getAttribute("nameObject").isMutable());
    }

    public static interface DocumentViewWithUpdatableMutableFlatView extends DocumentBaseView {
        NameObjectUpdateView getNameObject();
        void setNameObject(NameObjectUpdateView nameObject);
    }

    @Test
    public void updatableMutableFlatView() {
        ViewMetamodel metamodel = build(entityViewConfiguration, DocumentViewWithUpdatableMutableFlatView.class, NameObjectView.class, NameObjectUpdateView.class).getMetamodel();
        ManagedViewType<?> nameObjectViewType = metamodel.managedView(NameObjectUpdateView.class);
        ManagedViewType<?> docViewType = metamodel.managedView(DocumentViewWithUpdatableMutableFlatView.class);
        assertTrue(docViewType.getAttribute("nameObject").isUpdatable());

        assertTrue(docViewType.getAttribute("nameObject").isPersistCascaded());
        assertTrue(docViewType.getAttribute("nameObject").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("nameObject").getPersistCascadeAllowedSubtypes().contains(nameObjectViewType));
        assertTrue(docViewType.getAttribute("nameObject").getUpdateCascadeAllowedSubtypes().contains(nameObjectViewType));

        assertTrue(docViewType.getAttribute("nameObject").isMutable());
    }

    /*
     * Use of NameObjectCreateAndUpdateView
     */

    public static interface DocumentViewWithNonUpdatableCreateMutableFlatView extends DocumentBaseView {
        NameObjectCreateAndUpdateView getNameObject();
    }

    @Test
    public void nonUpdatableCreateMutableFlatView() {
        ViewMetamodel metamodel = build(entityViewConfiguration, DocumentViewWithNonUpdatableCreateMutableFlatView.class, NameObjectView.class, NameObjectCreateAndUpdateView.class).getMetamodel();
        ManagedViewType<?> nameObjectViewType = metamodel.managedView(NameObjectCreateAndUpdateView.class);
        ManagedViewType<?> docViewType = metamodel.managedView(DocumentViewWithNonUpdatableCreateMutableFlatView.class);
        assertTrue(docViewType.getAttribute("nameObject").isUpdatable());

        assertTrue(docViewType.getAttribute("nameObject").isPersistCascaded());
        assertTrue(docViewType.getAttribute("nameObject").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("nameObject").getPersistCascadeAllowedSubtypes().contains(nameObjectViewType));
        assertTrue(docViewType.getAttribute("nameObject").getUpdateCascadeAllowedSubtypes().contains(nameObjectViewType));

        assertTrue(docViewType.getAttribute("nameObject").isMutable());
    }

    public static interface DocumentViewWithUpdatableCreateMutableFlatView extends DocumentBaseView {
        NameObjectCreateAndUpdateView getNameObject();
        void setNameObject(NameObjectCreateAndUpdateView nameObject);
    }

    @Test
    public void updatableCreateMutableFlatView() {
        ViewMetamodel metamodel = build(entityViewConfiguration, DocumentViewWithUpdatableCreateMutableFlatView.class, NameObjectView.class, NameObjectCreateAndUpdateView.class).getMetamodel();
        ManagedViewType<?> nameObjectViewType = metamodel.managedView(NameObjectCreateAndUpdateView.class);
        ManagedViewType<?> docViewType = metamodel.managedView(DocumentViewWithUpdatableCreateMutableFlatView.class);
        assertTrue(docViewType.getAttribute("nameObject").isUpdatable());

        assertTrue(docViewType.getAttribute("nameObject").isPersistCascaded());
        assertTrue(docViewType.getAttribute("nameObject").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("nameObject").getPersistCascadeAllowedSubtypes().contains(nameObjectViewType));
        assertTrue(docViewType.getAttribute("nameObject").getUpdateCascadeAllowedSubtypes().contains(nameObjectViewType));

        assertTrue(docViewType.getAttribute("nameObject").isMutable());
    }

    /*
     * Use of NameObjectCreateView
     */

    public static interface DocumentViewWithNonUpdatableCreateFlatView extends DocumentBaseView {
        NameObjectCreateView getNameObject();
    }

    @Test
    public void nonUpdatableCreateFlatView() {
        try {
            build(entityViewConfiguration, DocumentViewWithNonUpdatableCreateFlatView.class, NameObjectView.class, NameObjectCreateView.class);
            fail("Expected to fail validation!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Illegal creatable-only mapping"));
        }
    }

    public static interface DocumentViewWithUpdatableCreateFlatView extends DocumentBaseView {
        NameObjectCreateView getNameObject();
        void setNameObject(NameObjectCreateView nameObject);
    }

    @Test
    public void updatableCreateFlatView() {
        try {
        build(entityViewConfiguration, DocumentViewWithUpdatableCreateFlatView.class, NameObjectView.class, NameObjectCreateView.class);
            fail("Expected to fail validation!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Illegal creatable-only mapping"));
        }
    }
}
