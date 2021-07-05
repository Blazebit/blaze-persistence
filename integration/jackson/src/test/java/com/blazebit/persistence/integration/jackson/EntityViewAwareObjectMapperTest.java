/*
 * Copyright 2014 - 2021 Blazebit.
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
import com.blazebit.persistence.view.MappingSingular;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.IgnoredPropertyException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

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
        return mapper(null, classes);
    }

    static EntityViewAwareObjectMapper mapper(EntityViewIdValueAccessor idValueAccessor, Class<?>... classes) {
        EntityViewConfiguration configuration = EntityViews.createDefaultConfiguration();
        for (Class<?> clazz : classes) {
            configuration.addEntityView(clazz);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return new EntityViewAwareObjectMapper(configuration.createEntityViewManager(cbf), objectMapper, idValueAccessor);
    }

    @Test
    public void testReadView() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(NameView.class));
        NameView view = objectReader.readValue("{\"id\": 1}");
        assertEquals(1L, view.getId());
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
        assertEquals(2, views.size());
        assertEquals(1L, views.get(0).getId());
        Assert.assertNull(views.get(0).getName());
        assertEquals(2L, views.get(1).getId());
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
        assertEquals(1L, view.getId());
        assertEquals("test", view.getName());
        assertEquals(2L, view.getParent().getId());
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
        assertEquals(1L, view.getId());
        assertEquals("test", view.getName());
        assertEquals(2L, view.getParent().getId());
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
        assertEquals("test", view.getName());
        assertEquals(2L, view.getParent().getId());
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
        assertFalse(((EntityViewProxy) view).$$_isNew());
        assertEquals(1L, view.getId());
        assertEquals("test", view.getName());
        assertEquals(2L, view.getParent().getId());
    }

    @Test
    public void testCreatableAndUpdatableViewWithoutId() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(CreatableAndUpdatableViewWithSetters.class, NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(CreatableAndUpdatableViewWithSetters.class));
        CreatableAndUpdatableViewWithSetters view = objectReader.readValue("{\"name\": \"test\", \"parent\": {\"id\": 2}}");
        Assert.assertTrue(((EntityViewProxy) view).$$_isNew());
        assertEquals("test", view.getName());
        assertEquals(2L, view.getParent().getId());
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
        assertEquals("test", view.getName());
        assertEquals(2L, view.getParent().getId());
        assertEquals("parent", view.getParent().getName());
    }

    @Test
    public void testCreatableAndUpdatableViewWithoutNestedUpdatableViewWithoutId() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(CreatableAndUpdatableViewWithNested.class, CreatableAndUpdatableViewWithSetters.class, NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(CreatableAndUpdatableViewWithNested.class));
        CreatableAndUpdatableViewWithNested view = objectReader.readValue("{\"name\": \"test\", \"parent\": {\"name\": \"parent\"}}");
        Assert.assertTrue(((EntityViewProxy) view).$$_isNew());
        assertEquals("test", view.getName());
        Assert.assertTrue(((EntityViewProxy) view.getParent()).$$_isNew());
        assertEquals("parent", view.getParent().getName());
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

    @Test
    public void testCreatableWithCollection() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(CreatableWithCollection.class, CreatableAndUpdatableViewWithSetters.class, NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(CreatableWithCollection.class));
        CreatableWithCollection view = objectReader.readValue("{\"name\": \"test\", \"children\": [{\"name\": \"parent\"}]}");
        Assert.assertTrue(((EntityViewProxy) view).$$_isNew());
        assertEquals("test", view.getName());
        assertEquals(mapper.getEntityViewManager().getChangeModel(view).get("children").getInitialState(), view.getChildren());
        assertEquals(1, view.getChildren().size());
    }

    @EntityView(SomeEntity.class)
    @CreatableEntityView
    @UpdatableEntityView
    interface CreatableWithCollection {
        @IdMapping
        long getId();
        String getName();
        void setName(String name);
        Set<CreatableAndUpdatableViewWithSetters> getChildren();
    }

    @Test
    public void testCreatableWithCollectionWithSetter() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(CreatableWithCollectionWithSetter.class, CreatableAndUpdatableViewWithSetters.class, NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(CreatableWithCollectionWithSetter.class));
        CreatableWithCollectionWithSetter view = objectReader.readValue("{\"name\": \"test\", \"children\": [{\"name\": \"parent\"}]}");
        Assert.assertTrue(((EntityViewProxy) view).$$_isNew());
        assertEquals("test", view.getName());
        assertEquals(mapper.getEntityViewManager().getChangeModel(view).get("children").getInitialState(), view.getChildren());
        assertEquals(1, view.getChildren().size());
    }

    @EntityView(SomeEntity.class)
    @CreatableEntityView
    @UpdatableEntityView
    interface CreatableWithCollectionWithSetter {
        @IdMapping
        long getId();
        String getName();
        void setName(String name);
        Set<CreatableAndUpdatableViewWithSetters> getChildren();
        void setChildren(Set<CreatableAndUpdatableViewWithSetters> children);
    }

    @Test
    public void testCreatableWithIgnoreId() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(CreatableWithIgnoreId.class);
        mapper.getObjectMapper().configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(CreatableWithIgnoreId.class));
        try {
            objectReader.readValue("{\"id\": 1, \"name\": \"test\"}");
            Assert.fail("Expected failure");
        } catch (IgnoredPropertyException ex) {
            assertEquals("id", ex.getPropertyName());
        }
    }

    @EntityView(SomeEntity.class)
    @CreatableEntityView
    interface CreatableWithIgnoreId {
        @IdMapping
        @JsonIgnore
        long getId();
        String getName();
        void setName(String name);
    }

    @Test
    public void testUpdatableWithCollectionWithSetter() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(UpdatableWithCollectionWithSetter.class, NameView.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(UpdatableWithCollectionWithSetter.class));
        UpdatableWithCollectionWithSetter view = objectReader.readValue("{\"id\":1, \"name\": \"test\", \"children\": [{\"id\": 1}]}");
        assertFalse(((EntityViewProxy) view).$$_isNew());
        assertEquals(1L, view.getId());
        assertEquals("test", view.getName());
        assertEquals(1L, view.getChildren().iterator().next().getId());
        assertEquals(1, view.getChildren().size());
    }

    @EntityView(SomeEntity.class)
    @UpdatableEntityView
    interface UpdatableWithCollectionWithSetter {
        @IdMapping
        long getId();
        String getName();
        void setName(String name);
        Set<NameView> getChildren();
        void setChildren(Set<NameView> children);
    }

    @Test
    public void testIdValueAccessorWithCollection() throws Exception {
        EntityViewIdValueAccessor accessor = new EntityViewIdValueAccessor() {
            @Override
            public <T> T getValue(JsonParser jsonParser, Class<T> idType) {
                return (T) (Object) 1L;
            }
        };
        EntityViewAwareObjectMapper mapper = mapper(accessor, UpdatableWithCollectionForIdAccessor.class, NameViewForIdAccessor.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(UpdatableWithCollectionForIdAccessor.class));
        UpdatableWithCollectionForIdAccessor view = objectReader.readValue("{\"name\": \"test\", \"children\": [{\"name\": \"The child\"}]}");
        assertFalse(((EntityViewProxy) view).$$_isNew());
        assertEquals(1L, view.getId());
        assertEquals("test", view.getName());
        assertNull(view.getChildren().iterator().next().getId());
        assertEquals("The child", view.getChildren().iterator().next().getName());
        assertEquals(1, view.getChildren().size());
    }

    @EntityView(SomeEntity.class)
    @UpdatableEntityView
    interface UpdatableWithCollectionForIdAccessor {
        @IdMapping
        long getId();
        String getName();
        void setName(String name);
        Set<NameViewForIdAccessor> getChildren();
        void setChildren(Set<NameViewForIdAccessor> children);
    }

    @EntityView(SomeEntity.class)
    @CreatableEntityView
    interface NameViewForIdAccessor {
        @IdMapping
        Long getId();
        String getName();
        void setName(String name);
    }

    @Test
    public void testJsonIgnore() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(ViewWithJsonIgnore.class);
        ViewWithJsonIgnore view = mapper.getEntityViewManager().create(ViewWithJsonIgnore.class);
        view.setId(1L);
        view.setName("Joe");
        JsonNode viewAsJsonTree = mapper.getObjectMapper().readTree(mapper.getObjectMapper().writeValueAsString(view));
        assertEquals(1L, viewAsJsonTree.get("id").asLong());
        assertFalse(viewAsJsonTree.has("name"));
    }

    @EntityView(SomeEntity.class)
    @CreatableEntityView
    static abstract class ViewWithJsonIgnore {
        @IdMapping
        public abstract long getId();
        public abstract void setId(long id);
        @JsonIgnore
        public abstract String getName();
        public abstract void setName(String name);
    }

    @Test
    public void testSingularCollection() throws Exception {
        EntityViewAwareObjectMapper mapper = mapper(ViewWithSingularCollection.class);
        ObjectReader objectReader = mapper.readerFor(mapper.getObjectMapper().constructType(ViewWithSingularCollection.class));
        ViewWithSingularCollection view = objectReader.readValue("{\"name\": \"Joe\", \"tags\": [\"t1\", \"t2\"]}");
        Assert.assertTrue(((EntityViewProxy) view).$$_isNew());
        assertEquals("Joe", view.getName());
        assertEquals(2, view.getTags().size());
        assertEquals("t1", view.getTags().get(0));
        assertEquals("t2", view.getTags().get(1));
    }

    @EntityView(SomeEntity.class)
    @CreatableEntityView
    static interface ViewWithSingularCollection {
        @IdMapping
        long getId();
        String getName();
        void setName(String name);
        @MappingSingular
        List<String> getTags();
        void setTags(List<String> tags);
    }
}
