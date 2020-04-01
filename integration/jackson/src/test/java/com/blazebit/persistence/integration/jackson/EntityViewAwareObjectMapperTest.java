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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class EntityViewAwareObjectMapperTest {

    static EntityManagerFactory emf;
    static CriteriaBuilderFactory cbf;

    @BeforeClass
    public static void prepare() {
        emf = Persistence.createEntityManagerFactory("Test");
        cbf = Criteria.getDefault().createCriteriaBuilderFactory(emf);
    }

    static EntityViewAwareObjectMapper mapper(Class<?>... classes) {
        EntityViewConfiguration configuration = EntityViews.createDefaultConfiguration();
        for (Class<?> clazz : classes) {
            configuration.addEntityView(clazz);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return new EntityViewAwareObjectMapper(configuration.createEntityViewManager(cbf), objectMapper, null);
    }

    @Test
    public void testReadView() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(NameView.class));
        NameView view = objectReader.readValue("{\"id\": 1}");
        Assert.assertEquals(1L, view.getId());
        Assert.assertNull(view.getName());
    }

    @Test
    public void testReadViewList() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{ NameView.class };
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        }));
        List<NameView> views = objectReader.readValue("[{\"id\": 1}, {\"id\": 2}]");
        Assert.assertEquals(2, views.size());
        Assert.assertEquals(1L, views.get(0).getId());
        Assert.assertNull(views.get(0).getName());
        Assert.assertEquals(2L, views.get(1).getId());
        Assert.assertNull(views.get(0).getName());
    }

    @EntityView(SomeEntity.class)
    interface NameView {
        @IdMapping
        long getId();
        String getName();
    }

    @Test
    public void testReadViewWithSetters() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(ReadViewWithSetters.class, NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(ReadViewWithSetters.class));
        ReadViewWithSetters view = objectReader.readValue("{\"id\": 1, \"name\": \"test\", \"parent\": {\"id\": 2}}");
        Assert.assertEquals(1L, view.getId());
        Assert.assertEquals("test", view.getName());
        Assert.assertEquals(2L, view.getParent().getId());
    }

    @EntityView(SomeEntity.class)
    interface ReadViewWithSetters {
        @IdMapping
        long getId();
        String getName();
        void setName(String name);
        NameView getParent();
        void setParent(NameView parent);
    }

    @Test
    public void testUpdatableView() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(UpdateViewWithSetters.class, NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(UpdateViewWithSetters.class));
        UpdateViewWithSetters view = objectReader.readValue("{\"id\": 1, \"name\": \"test\", \"parent\": {\"id\": 2}}");
        Assert.assertEquals(1L, view.getId());
        Assert.assertEquals("test", view.getName());
        Assert.assertEquals(2L, view.getParent().getId());
    }

    @EntityView(SomeEntity.class)
    @UpdatableEntityView
    interface UpdateViewWithSetters {
        @IdMapping
        long getId();
        String getName();
        void setName(String name);
        NameView getParent();
        void setParent(NameView parent);
    }

    @Test
    public void testCreatableView() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(CreatableViewWithSetters.class, NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(CreatableViewWithSetters.class));
        CreatableViewWithSetters view = objectReader.readValue("{\"name\": \"test\", \"parent\": {\"id\": 2}}");
        Assert.assertTrue(((EntityViewProxy) view).$$_isNew());
        Assert.assertEquals("test", view.getName());
        Assert.assertEquals(2L, view.getParent().getId());
    }

    @EntityView(SomeEntity.class)
    @CreatableEntityView
    interface CreatableViewWithSetters {
        @IdMapping
        long getId();
        String getName();
        void setName(String name);
        NameView getParent();
        void setParent(NameView parent);
    }

    @Test
    public void testCreatableAndUpdatableView() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(CreatableAndUpdatableViewWithSetters.class, NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(CreatableAndUpdatableViewWithSetters.class));
        CreatableAndUpdatableViewWithSetters view = objectReader.readValue("{\"id\": 1, \"name\": \"test\", \"parent\": {\"id\": 2}}");
        Assert.assertFalse(((EntityViewProxy) view).$$_isNew());
        Assert.assertEquals(1L, view.getId());
        Assert.assertEquals("test", view.getName());
        Assert.assertEquals(2L, view.getParent().getId());
    }

    @Test
    public void testCreatableAndUpdatableViewWithoutId() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(CreatableAndUpdatableViewWithSetters.class, NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(CreatableAndUpdatableViewWithSetters.class));
        CreatableAndUpdatableViewWithSetters view = objectReader.readValue("{\"name\": \"test\", \"parent\": {\"id\": 2}}");
        Assert.assertTrue(((EntityViewProxy) view).$$_isNew());
        Assert.assertEquals("test", view.getName());
        Assert.assertEquals(2L, view.getParent().getId());
    }

    @EntityView(SomeEntity.class)
    @CreatableEntityView
    @UpdatableEntityView
    interface CreatableAndUpdatableViewWithSetters {
        @IdMapping
        long getId();
        String getName();
        void setName(String name);
        NameView getParent();
        void setParent(NameView parent);
    }

    @Test
    public void testCreatableAndUpdatableViewWithoutNestedUpdatableView() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(CreatableAndUpdatableViewWithNested.class, CreatableAndUpdatableViewWithSetters.class, NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(CreatableAndUpdatableViewWithNested.class));
        CreatableAndUpdatableViewWithNested view = objectReader.readValue("{\"name\": \"test\", \"parent\": {\"id\": 2, \"name\": \"parent\"}}");
        Assert.assertTrue(((EntityViewProxy) view).$$_isNew());
        Assert.assertEquals("test", view.getName());
        Assert.assertEquals(2L, view.getParent().getId());
        Assert.assertEquals("parent", view.getParent().getName());
    }

    @Test
    public void testCreatableAndUpdatableViewWithoutNestedUpdatableViewWithoutId() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(CreatableAndUpdatableViewWithNested.class, CreatableAndUpdatableViewWithSetters.class, NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(CreatableAndUpdatableViewWithNested.class));
        CreatableAndUpdatableViewWithNested view = objectReader.readValue("{\"name\": \"test\", \"parent\": {\"name\": \"parent\"}}");
        Assert.assertTrue(((EntityViewProxy) view).$$_isNew());
        Assert.assertEquals("test", view.getName());
        Assert.assertTrue(((EntityViewProxy) view.getParent()).$$_isNew());
        Assert.assertEquals("parent", view.getParent().getName());
    }

    @EntityView(SomeEntity.class)
    @CreatableEntityView
    @UpdatableEntityView
    interface CreatableAndUpdatableViewWithNested {
        @IdMapping
        long getId();
        String getName();
        void setName(String name);
        CreatableAndUpdatableViewWithSetters getParent();
        void setParent(CreatableAndUpdatableViewWithSetters parent);
    }
}
