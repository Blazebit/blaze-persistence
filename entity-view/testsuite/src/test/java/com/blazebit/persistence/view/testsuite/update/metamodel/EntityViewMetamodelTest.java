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

package com.blazebit.persistence.view.testsuite.update.metamodel;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.testsuite.tx.TxWork;
import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.ViewConstructor;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewMetamodelTest extends AbstractEntityViewTest {

    @UpdatableEntityView
    @EntityView(Document.class)
    public static interface DocumentViewWithoutId {
        String getName();
        void setName(String name);
    }

    @Test
    public void updatableEntityViewRequiresId() {
        cleanDatabase();
        build(DocumentViewWithoutId.class);
        Long docId = transactional(new TxWork<Long>() {
            @Override
            public Long work(EntityManager em) {
                Person p1 = new Person("p1");
                Document d1 = new Document("doc1", p1);
                em.persist(p1);
                em.persist(d1);
                return d1.getId();
            }
        });

        final DocumentViewWithoutId view = evm.applySetting(
                EntityViewSetting.create(DocumentViewWithoutId.class),
                cbf.create(em, Document.class).where("id").eq(docId)
        ).getSingleResult();

        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                try {
                    evm.update(em, view);
                    fail("Expected to fail the update");
                } catch (Exception ex) {
                    assertTrue(ex instanceof IllegalArgumentException);
                    assertTrue(ex.getMessage().contains("no entity id is known"));
                }
            }
        });
    }

    @UpdatableEntityView
    @EntityView(Document.class)
    public static interface DocumentBaseView {
        @IdMapping
        Long getId();
    }

    @UpdatableEntityView
    public static abstract class DocumentUpdateView implements DocumentBaseView {
        public DocumentUpdateView(@Mapping("name") String name) {

        }
        @ViewConstructor("reference")
        DocumentUpdateView() {
        }
        public abstract String getName();
        abstract void setName(String name);
    }

    @EntityView(Person.class)
    public static interface PersonView {
        @IdMapping
        Long getId();
    }

    @UpdatableEntityView
    public static interface PersonUpdateView extends PersonView {
        String getName();
        void setName(String name);
    }

    @CreatableEntityView(validatePersistability = false)
    public static interface PersonCreateAndUpdateView extends PersonUpdateView {

    }

    @CreatableEntityView(validatePersistability = false)
    public static interface PersonCreateView extends PersonView {
        String getName();
        void setName(String name);
    }

    public static interface DocumentViewWithEntityTypes extends DocumentBaseView {
        Set<Person> getPartners();
        Person getOwner();
    }

    @Test
    public void getReferenceForLongId() {
        build(DocumentBaseView.class, PersonUpdateView.class, DocumentUpdateView.class);

        DocumentBaseView docView = evm.getReference(DocumentBaseView.class, 1L);
        assertNotNull(docView);
        assertEquals(Long.valueOf(1L), docView.getId());

        PersonUpdateView persView = evm.getReference(PersonUpdateView.class, 1L);
        assertNotNull(persView);
        assertEquals(Long.valueOf(1L), persView.getId());

        DocumentUpdateView docView2 = evm.getReference(DocumentUpdateView.class, 1L);
        assertNotNull(docView2);
        assertEquals(Long.valueOf(1L), docView2.getId());
    }

    @Test
    // Test for #613
    public void loadWithMultipleViewConstruct() {
        build(DocumentBaseView.class, PersonUpdateView.class, DocumentUpdateView.class);

        evm.find(em, DocumentUpdateView.class, 1L);
    }


    @Test
    public void nonUpdatableEntityAttributeDefaults() {
        ViewMetamodel metamodel = build(DocumentViewWithEntityTypes.class);
        ManagedViewType<?> docViewType = metamodel.managedView(DocumentViewWithEntityTypes.class);
        // By default, the collection relations are not updatable
        assertFalse(docViewType.getAttribute("partners").isUpdatable());
        assertFalse(docViewType.getAttribute("owner").isUpdatable());

        // Cascades require the update to be updatable, so they are all disabled
        assertFalse(docViewType.getAttribute("partners").isPersistCascaded());
        assertFalse(docViewType.getAttribute("owner").isPersistCascaded());
        assertTrue(docViewType.getAttribute("partners").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("owner").isUpdateCascaded());

        // Entity types are mutable
        assertTrue(docViewType.getAttribute("partners").isMutable());
        assertTrue(docViewType.getAttribute("owner").isMutable());
    }

    public static interface DocumentViewWithImmutableViewTypes extends DocumentBaseView {
        Set<PersonView> getPartners();
        PersonView getOwner();
    }

    @Test
    public void nonUpdatableViewAttributeDefaults() {
        ViewMetamodel metamodel = build(DocumentViewWithImmutableViewTypes.class, PersonView.class);
        ManagedViewType<?> docViewType = metamodel.managedView(DocumentViewWithImmutableViewTypes.class);
        // By default, the collection relations are not updatable
        assertFalse(docViewType.getAttribute("partners").isUpdatable());
        assertFalse(docViewType.getAttribute("owner").isUpdatable());

        // Non-updatable attributes with immutable types don't cascade
        assertFalse(docViewType.getAttribute("partners").isPersistCascaded());
        assertFalse(docViewType.getAttribute("owner").isPersistCascaded());
        assertFalse(docViewType.getAttribute("partners").isUpdateCascaded());
        assertFalse(docViewType.getAttribute("owner").isUpdateCascaded());

        // Immutable view types are not mutable
        assertFalse(docViewType.getAttribute("partners").isMutable());
        assertFalse(docViewType.getAttribute("owner").isMutable());
    }

    public static interface DocumentViewWithUpdatableImmutableViewTypes extends DocumentBaseView {
        @UpdatableMapping
        Set<PersonView> getPartners();

        PersonView getOwner();
        void setOwner(PersonView owner);

        List<PersonView> getPeople();
        void setPeople(List<PersonView> people);
    }

    @Test
    public void updatableViewAttributeDefaults() {
        ViewMetamodel metamodel = build(DocumentViewWithUpdatableImmutableViewTypes.class, PersonView.class);
        ManagedViewType<?> docViewType = metamodel.managedView(DocumentViewWithUpdatableImmutableViewTypes.class);
        // By default, the collection relations are not updatable
        assertTrue(docViewType.getAttribute("partners").isUpdatable());
        assertTrue(docViewType.getAttribute("owner").isUpdatable());
        assertTrue(docViewType.getAttribute("people").isUpdatable());

        // Even updatable attributes with immutable types don't cascade by default
        assertFalse(docViewType.getAttribute("partners").isPersistCascaded());
        assertFalse(docViewType.getAttribute("owner").isPersistCascaded());
        assertFalse(docViewType.getAttribute("people").isPersistCascaded());
        assertFalse(docViewType.getAttribute("partners").isUpdateCascaded());
        assertFalse(docViewType.getAttribute("owner").isUpdateCascaded());
        assertFalse(docViewType.getAttribute("people").isUpdateCascaded());

        // An attribute is mutable if it's updatable or the type is mutable
        assertTrue(docViewType.getAttribute("partners").isMutable());
        assertTrue(docViewType.getAttribute("owner").isMutable());
        assertTrue(docViewType.getAttribute("people").isMutable());
    }

    public static interface DocumentViewWithMutableViewTypes extends DocumentBaseView {
        Set<PersonUpdateView> getPartners();
        PersonUpdateView getOwner();
        List<PersonUpdateView> getPeople();
    }

    @Test
    public void nonUpdatableMutableViewAttributeDefaults() {
        ViewMetamodel metamodel = build(DocumentViewWithMutableViewTypes.class, PersonUpdateView.class);
        ManagedViewType<?> docViewType = metamodel.managedView(DocumentViewWithMutableViewTypes.class);
        // Collections are only considered being updatable if the element type is "persistable"
        assertFalse(docViewType.getAttribute("partners").isUpdatable());
        assertFalse(docViewType.getAttribute("owner").isUpdatable());
        assertFalse(docViewType.getAttribute("people").isUpdatable());

        // Non-updatable attributes with mutable types cascade only UPDATE by default
        assertFalse(docViewType.getAttribute("partners").isPersistCascaded());
        assertFalse(docViewType.getAttribute("owner").isPersistCascaded());
        assertFalse(docViewType.getAttribute("people").isPersistCascaded());
        assertTrue(docViewType.getAttribute("partners").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("owner").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("people").isUpdateCascaded());

        // An attribute is mutable if it's updatable or the type is mutable
        assertTrue(docViewType.getAttribute("partners").isMutable());
        assertTrue(docViewType.getAttribute("owner").isMutable());
        assertTrue(docViewType.getAttribute("people").isMutable());
    }

    public static interface DocumentViewWithUpdatableMutableViewTypes extends DocumentBaseView {
        @UpdatableMapping
        Set<PersonUpdateView> getPartners();

        PersonUpdateView getOwner();
        void setOwner(PersonUpdateView owner);

        List<PersonUpdateView> getPeople();
        void setPeople(List<PersonUpdateView> people);
    }

    @Test
    public void updatableMutableViewAttributeDefaults() {
        ViewMetamodel metamodel = build(DocumentViewWithUpdatableMutableViewTypes.class, PersonUpdateView.class);
        ManagedViewType<?> docViewType = metamodel.managedView(DocumentViewWithUpdatableMutableViewTypes.class);
        // By default, the collection relations are not updatable
        assertTrue(docViewType.getAttribute("partners").isUpdatable());
        assertTrue(docViewType.getAttribute("owner").isUpdatable());
        assertTrue(docViewType.getAttribute("people").isUpdatable());

        // Updatable attributes with mutable types cascade only UPDATE by default
        assertFalse(docViewType.getAttribute("partners").isPersistCascaded());
        assertFalse(docViewType.getAttribute("owner").isPersistCascaded());
        assertFalse(docViewType.getAttribute("people").isPersistCascaded());
        assertTrue(docViewType.getAttribute("partners").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("owner").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("people").isUpdateCascaded());

        // An attribute is mutable if it's updatable or the type is mutable
        assertTrue(docViewType.getAttribute("partners").isMutable());
        assertTrue(docViewType.getAttribute("owner").isMutable());
        assertTrue(docViewType.getAttribute("people").isMutable());
    }

    public static interface DocumentViewWithUpdatableMutableAndCreatableViewTypes extends DocumentBaseView {
        @UpdatableMapping
        Set<PersonCreateAndUpdateView> getPartners();

        PersonCreateAndUpdateView getOwner();
        void setOwner(PersonCreateAndUpdateView owner);

        List<PersonCreateAndUpdateView> getPeople();
        void setPeople(List<PersonCreateAndUpdateView> people);
    }

    @Test
    public void updatableMutableAndCreatableView() {
        ViewMetamodel metamodel = build(DocumentViewWithUpdatableMutableAndCreatableViewTypes.class, PersonCreateAndUpdateView.class);
        ManagedViewType<?> docViewType = metamodel.managedView(DocumentViewWithUpdatableMutableAndCreatableViewTypes.class);
        // By default, the collection relations are not updatable
        assertTrue(docViewType.getAttribute("partners").isUpdatable());
        assertTrue(docViewType.getAttribute("owner").isUpdatable());
        assertTrue(docViewType.getAttribute("people").isUpdatable());

        // Updatable attributes with mutable types cascade only UPDATE by default
        assertTrue(docViewType.getAttribute("partners").isPersistCascaded());
        assertTrue(docViewType.getAttribute("owner").isPersistCascaded());
        assertTrue(docViewType.getAttribute("people").isPersistCascaded());
        assertTrue(docViewType.getAttribute("partners").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("owner").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("people").isUpdateCascaded());

        // An attribute is mutable if it's updatable or the type is mutable
        assertTrue(docViewType.getAttribute("partners").isMutable());
        assertTrue(docViewType.getAttribute("owner").isMutable());
        assertTrue(docViewType.getAttribute("people").isMutable());
    }

    @CreatableEntityView
    @EntityView(Document.class)
    public static interface DocumentViewWithUpdatableCreatableViewTypes {
        @IdMapping
        Long getId();

        @UpdatableMapping(cascade = { CascadeType.PERSIST })
        Set<PersonView> getPartners();

        @UpdatableMapping(cascade = { CascadeType.PERSIST })
        PersonView getOwner();
        void setOwner(PersonView owner);

        @UpdatableMapping(cascade = { CascadeType.PERSIST })
        List<PersonView> getPeople();
        void setPeople(List<PersonView> people);
    }

    @Test
    public void updatableCreatableView() {
        ViewMetamodel metamodel = build(DocumentViewWithUpdatableCreatableViewTypes.class, PersonView.class, PersonCreateView.class);
        ManagedViewType<?> docViewType = metamodel.managedView(DocumentViewWithUpdatableCreatableViewTypes.class);
        // By default, the collection relations are not updatable
        assertTrue(docViewType.getAttribute("partners").isUpdatable());
        assertTrue(docViewType.getAttribute("owner").isUpdatable());
        assertTrue(docViewType.getAttribute("people").isUpdatable());

        // Updatable attributes with mutable types cascade only UPDATE by default
        assertTrue(docViewType.getAttribute("partners").isPersistCascaded());
        assertTrue(docViewType.getAttribute("owner").isPersistCascaded());
        assertTrue(docViewType.getAttribute("people").isPersistCascaded());
        assertFalse(docViewType.getAttribute("partners").isUpdateCascaded());
        assertFalse(docViewType.getAttribute("owner").isUpdateCascaded());
        assertFalse(docViewType.getAttribute("people").isUpdateCascaded());

        // An attribute is mutable if it's updatable or the type is mutable
        assertTrue(docViewType.getAttribute("partners").isMutable());
        assertTrue(docViewType.getAttribute("owner").isMutable());
        assertTrue(docViewType.getAttribute("people").isMutable());
    }

    public static interface DocumentViewWithUpdatableMutableViewTypesAndCreateCascade extends DocumentBaseView {
        @UpdatableMapping(cascade = { CascadeType.UPDATE, CascadeType.PERSIST })
        Set<PersonUpdateView> getPartners();

        @UpdatableMapping(cascade = { CascadeType.UPDATE, CascadeType.PERSIST })
        PersonUpdateView getOwner();
        void setOwner(PersonUpdateView owner);

        @UpdatableMapping(cascade = { CascadeType.UPDATE, CascadeType.PERSIST })
        List<PersonUpdateView> getPeople();
        void setPeople(List<PersonUpdateView> people);
    }

    @Test
    public void updatableMutableViewWithManualCascade() {
        ViewMetamodel metamodel = build(DocumentViewWithUpdatableMutableViewTypesAndCreateCascade.class, PersonUpdateView.class);
        ManagedViewType<?> docViewType = metamodel.managedView(DocumentViewWithUpdatableMutableViewTypesAndCreateCascade.class);
        // By default, the collection relations are not updatable
        assertTrue(docViewType.getAttribute("partners").isUpdatable());
        assertTrue(docViewType.getAttribute("owner").isUpdatable());
        assertTrue(docViewType.getAttribute("people").isUpdatable());

        // Updatable attributes with mutable types cascade only UPDATE by default
        assertTrue(docViewType.getAttribute("partners").isPersistCascaded());
        assertTrue(docViewType.getAttribute("owner").isPersistCascaded());
        assertTrue(docViewType.getAttribute("people").isPersistCascaded());
        assertTrue(docViewType.getAttribute("partners").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("owner").isUpdateCascaded());
        assertTrue(docViewType.getAttribute("people").isUpdateCascaded());

        // An attribute is mutable if it's updatable or the type is mutable
        assertTrue(docViewType.getAttribute("partners").isMutable());
        assertTrue(docViewType.getAttribute("owner").isMutable());
        assertTrue(docViewType.getAttribute("people").isMutable());
    }
}
