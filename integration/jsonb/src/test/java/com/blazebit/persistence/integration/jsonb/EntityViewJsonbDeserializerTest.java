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

package com.blazebit.persistence.integration.jsonb;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingSingular;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.stream.JsonParser;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Christian Beikov
 * @since 1.6.3
 */
public class EntityViewJsonbDeserializerTest {

    static EntityManagerFactory emf;
    static CriteriaBuilderFactory cbf;

    @BeforeClass
    public static void prepare() {
        emf = Persistence.createEntityManagerFactory("Test");
        cbf = Criteria.getDefault().createCriteriaBuilderFactory(emf);
    }

    static EntityViewManager evm(Class<?>... classes) {
        EntityViewConfiguration configuration = EntityViews.createDefaultConfiguration();
        for (Class<?> clazz : classes) {
            configuration.addEntityView(clazz);
        }

        return configuration.createEntityViewManager(cbf);
    }

    static Jsonb mapper(EntityViewManager evm) {
        return mapper(evm, null);
    }

    static Jsonb mapper(EntityViewManager evm, EntityViewIdValueAccessor idValueAccessor) {
        try {
            JsonbConfig jsonbConfig = new JsonbConfig()
                    .withFormatting(true)
                    .withNullValues(true)
                    .setProperty("jsonb.fail-on-unknown-properties", true);
            EntityViewJsonbDeserializer.integrate(jsonbConfig, evm, EntityViewJsonbDeserializer.createDeserializers(evm, idValueAccessor));
            return JsonbBuilder.create(jsonbConfig);
        } catch (Exception ex) {
            throw new RuntimeException("Could register jsonb objects for deserialization!", ex);
        }
    }

    @Test
    public void testReadView() throws Exception {
        Jsonb mapper = mapper(evm(NameView.class));
        NameView view = mapper.fromJson("{\"id\": 1}", NameView.class);
        assertEquals(1L, view.getId());
        Assert.assertNull(view.getName());
    }

    @Test
    public void testReadViewList() throws Exception {
        Jsonb mapper = mapper(evm(NameView.class));
        ParameterizedType parameterizedType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{NameView.class};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        List<NameView> views = mapper.fromJson("[{\"id\": 1}, {\"id\": 2}]", parameterizedType);
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
        Jsonb mapper = mapper(evm(ReadViewWithSetters.class, NameView.class));
        ReadViewWithSetters view = mapper.fromJson("{\"id\": 1, \"name\": \"test\", \"parent\": {\"id\": 2}}", ReadViewWithSetters.class);
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
        Jsonb mapper = mapper(evm(UpdateViewWithSetters.class, NameView.class));
        UpdateViewWithSetters view = mapper.fromJson("{\"id\": 1, \"name\": \"test\", \"parent\": {\"id\": 2}}", UpdateViewWithSetters.class);
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
        Jsonb mapper = mapper(evm(CreatableViewWithSetters.class, NameView.class));
        CreatableViewWithSetters view = mapper.fromJson("{\"name\": \"test\", \"parent\": {\"id\": 2}}", CreatableViewWithSetters.class);
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
        Jsonb mapper = mapper(evm(CreatableAndUpdatableViewWithSetters.class, NameView.class));
        CreatableAndUpdatableViewWithSetters view = mapper.fromJson("{\"id\": 1, \"name\": \"test\", \"parent\": {\"id\": 2}}", CreatableAndUpdatableViewWithSetters.class);
        assertFalse(((EntityViewProxy) view).$$_isNew());
        assertEquals(1L, view.getId());
        assertEquals("test", view.getName());
        assertEquals(2L, view.getParent().getId());
    }

    @Test
    public void testCreatableAndUpdatableViewWithoutId() throws Exception {
        Jsonb mapper = mapper(evm(CreatableAndUpdatableViewWithSetters.class, NameView.class));
        CreatableAndUpdatableViewWithSetters view = mapper.fromJson("{\"name\": \"test\", \"parent\": {\"id\": 2}}", CreatableAndUpdatableViewWithSetters.class);
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
        Jsonb mapper = mapper(evm(CreatableAndUpdatableViewWithNested.class, CreatableAndUpdatableViewWithSetters.class, NameView.class));
        CreatableAndUpdatableViewWithNested view = mapper.fromJson("{\"name\": \"test\", \"parent\": {\"id\": 2, \"name\": \"parent\"}}", CreatableAndUpdatableViewWithNested.class);
        Assert.assertTrue(((EntityViewProxy) view).$$_isNew());
        assertEquals("test", view.getName());
        assertEquals(2L, view.getParent().getId());
        assertEquals("parent", view.getParent().getName());
    }

    @Test
    public void testCreatableAndUpdatableViewWithoutNestedUpdatableViewWithoutId() throws Exception {
        Jsonb mapper = mapper(evm(CreatableAndUpdatableViewWithNested.class, CreatableAndUpdatableViewWithSetters.class, NameView.class));
        CreatableAndUpdatableViewWithNested view = mapper.fromJson("{\"name\": \"test\", \"parent\": {\"name\": \"parent\"}}", CreatableAndUpdatableViewWithNested.class);
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
        EntityViewManager evm = evm(CreatableWithCollection.class, CreatableAndUpdatableViewWithSetters.class, NameView.class);
        Jsonb mapper = mapper(evm);
        CreatableWithCollection view = mapper.fromJson("{\"name\": \"test\", \"children\": [{\"name\": \"parent\"}]}", CreatableWithCollection.class);
        Assert.assertTrue(((EntityViewProxy) view).$$_isNew());
        assertEquals("test", view.getName());
        assertEquals(evm.getChangeModel(view).get("children").getInitialState(), view.getChildren());
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
        EntityViewManager evm = evm(CreatableWithCollectionWithSetter.class, CreatableAndUpdatableViewWithSetters.class, NameView.class);
        Jsonb mapper = mapper(evm);
        CreatableWithCollectionWithSetter view = mapper.fromJson("{\"name\": \"test\", \"children\": [{\"name\": \"parent\"}]}", CreatableWithCollectionWithSetter.class);
        Assert.assertTrue(((EntityViewProxy) view).$$_isNew());
        assertEquals("test", view.getName());
        assertEquals(evm.getChangeModel(view).get("children").getInitialState(), view.getChildren());
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
        Jsonb mapper = mapper(evm(CreatableWithIgnoreId.class));
        try {
            mapper.fromJson("{\"id\": 1, \"name\": \"test\"}", CreatableWithIgnoreId.class);
            Assert.fail("Expected failure");
        } catch (Exception ex) {
            assertEquals("Unknown attribute [id]", ex.getCause().getCause().getMessage());
        }
    }

    @EntityView(SomeEntity.class)
    @CreatableEntityView
    interface CreatableWithIgnoreId {
        @IdMapping
        @JsonbTransient
        long getId();
        String getName();
        void setName(String name);
    }

    @Test
    public void testUpdatableWithCollectionWithSetter() throws Exception {
        Jsonb mapper = mapper(evm(UpdatableWithCollectionWithSetter.class, NameView.class));
        UpdatableWithCollectionWithSetter view = mapper.fromJson("{\"id\":1, \"name\": \"test\", \"children\": [{\"id\": 1}]}", UpdatableWithCollectionWithSetter.class);
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
            public <T> T getValue(JsonParser jsonParser, DeserializationContext deserializationContext, Class<T> idType) {
                return (T) (Object) 1L;
            }
        };
        Jsonb mapper = mapper(evm(UpdatableWithCollectionForIdAccessor.class, NameViewForIdAccessor.class), accessor);
        UpdatableWithCollectionForIdAccessor view = mapper.fromJson("{\"name\": \"test\", \"children\": [{\"name\": \"The child\"}]}", UpdatableWithCollectionForIdAccessor.class);
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
        EntityViewManager evm = evm(ViewWithJsonIgnore.class);
        Jsonb mapper = mapper(evm);
        ViewWithJsonIgnore view = evm.create(ViewWithJsonIgnore.class);
        view.setId(1L);
        view.setName("Joe");
        JsonObject viewAsJsonTree = mapper.fromJson(mapper.toJson(view, ViewWithJsonIgnore.class), JsonObject.class);
        assertEquals(1L, viewAsJsonTree.getJsonNumber("id").longValue());
        assertNull(viewAsJsonTree.get("name"));
    }

    @EntityView(SomeEntity.class)
    @CreatableEntityView
    static abstract class ViewWithJsonIgnore {
        @IdMapping
        public abstract long getId();
        public abstract void setId(long id);
        @JsonbTransient
        public abstract String getName();
        public abstract void setName(String name);
    }

    @Test
    public void testSingularCollection() throws Exception {
        Jsonb mapper = mapper(evm(ViewWithSingularCollection.class));
        ViewWithSingularCollection view = mapper.fromJson("{\"name\": \"Joe\", \"tags\": [\"t1\", \"t2\"]}", ViewWithSingularCollection.class);
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
