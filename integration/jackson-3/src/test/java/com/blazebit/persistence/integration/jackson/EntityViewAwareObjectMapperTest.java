/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jackson;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import org.junit.BeforeClass;
import org.junit.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntityViewAwareObjectMapperTest {

    static EntityManagerFactory emf;
    static CriteriaBuilderFactory cbf;

    @BeforeClass
    public static void prepare() {
        emf = Persistence.createEntityManagerFactory("Test");
        cbf = Criteria.getDefault().createCriteriaBuilderFactory(emf);
    }

    private static EntityViewAwareObjectMapper mapper(EntityViewIdValueAccessor idValueAccessor, Class<?>... classes) {
        EntityViewConfiguration configuration = EntityViews.createDefaultConfiguration();
        for (Class<?> clazz : classes) {
            configuration.addEntityView(clazz);
        }
        return new EntityViewAwareObjectMapper(configuration.createEntityViewManager(cbf), new ObjectMapper(), idValueAccessor);
    }

    @Test
    public void deserializesUpdatableEntityView() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(null, UpdateView.class);
        ObjectReader objectReader = mapper.readerFor(UpdateView.class);

        UpdateView view = objectReader.readValue("{\"id\": 1, \"name\": \"test\"}");

        assertFalse(((EntityViewProxy) view).$$_isNew());
        assertEquals(1L, view.getId());
        assertEquals("test", view.getName());
    }

    @Test
    public void createsCreatableEntityViewWithoutId() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(null, CreateView.class);
        ObjectReader objectReader = mapper.readerFor(CreateView.class);

        CreateView view = objectReader.readValue("{\"name\": \"test\"}");

        assertTrue(((EntityViewProxy) view).$$_isNew());
        assertEquals("test", view.getName());
    }

    @Test
    public void usesIdValueAccessorForRootEntityView() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(new EntityViewIdValueAccessor() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getValue(tools.jackson.core.JsonParser jsonParser, Class<T> idType) {
                return (T) (Object) 1L;
            }
        }, UpdateView.class);

        UpdateView view = mapper.readerFor(UpdateView.class).readValue("{\"name\": \"test\"}");

        assertEquals(1L, view.getId());
        assertEquals("test", view.getName());
    }

    @EntityView(SomeEntity.class)
    @UpdatableEntityView
    interface UpdateView {
        @IdMapping
        long getId();
        String getName();
        void setName(String name);
    }

    @EntityView(SomeEntity.class)
    @CreatableEntityView
    interface CreateView {
        @IdMapping
        long getId();
        String getName();
        void setName(String name);
    }
}
