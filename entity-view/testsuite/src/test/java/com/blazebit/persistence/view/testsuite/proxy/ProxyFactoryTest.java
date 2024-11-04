/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.proxy;

import com.blazebit.persistence.spi.PackageOpener;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.SerializableEntityViewManager;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.proxy.ObjectInstantiator;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.impl.proxy.TupleConstructorReflectionInstantiator;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.proxy.model.DocumentClassView;
import com.blazebit.persistence.view.testsuite.proxy.model.DocumentCreateView;
import com.blazebit.persistence.view.testsuite.proxy.model.DocumentCreateViewWithPrimitiveArray;
import com.blazebit.persistence.view.testsuite.proxy.model.DocumentInterfaceView;
import com.blazebit.persistence.view.testsuite.proxy.model.NameObjectView;
import com.blazebit.persistence.view.testsuite.proxy.model.UnsafeDocumentClassView;
import com.blazebit.reflection.ReflectionUtils;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ProxyFactoryTest extends AbstractEntityViewTest {

    private final ProxyFactory proxyFactory = new ProxyFactory(false, false, PackageOpener.NOOP);
    private final EntityViewConfiguration entityViewConfiguration;

    {
        entityViewConfiguration = EntityViews.createDefaultConfiguration();
        entityViewConfiguration.setProperty(ConfigurationProperties.PROXY_EAGER_LOADING, "true");
        entityViewConfiguration.setProperty(ConfigurationProperties.UPDATER_EAGER_LOADING, "true");
    }

    private ViewMetamodel getViewMetamodel() {
        return build(
                entityViewConfiguration,
                DocumentInterfaceView.class,
                DocumentClassView.class,
                UnsafeDocumentClassView.class,
                DocumentCreateView.class,
                NameObjectView.class
        ).getMetamodel();
    }

    @Test
    public void testUnsafeClassProxy() throws Exception {
        ViewType<UnsafeDocumentClassView> viewType = getViewMetamodel().view(UnsafeDocumentClassView.class);

        // The parameter order is _id, contacts, firstContactPerson, id, name
        Class<?>[] parameterTypes = new Class[]{ Long.class, Map.class, Person.class, Person.class, String.class, Long.class, Integer.class};
        ObjectInstantiator<UnsafeDocumentClassView> instantiator = new TupleConstructorReflectionInstantiator<>((MappingConstructorImpl<UnsafeDocumentClassView>) viewType.getConstructor(Long.class, Integer.class), proxyFactory, (ManagedViewTypeImplementor<UnsafeDocumentClassView>) viewType, parameterTypes, evm, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        Map<Integer, Person> expectedContacts = new HashMap<Integer, Person>();
        Person expectedFirstContactPerson = new Person("pers");
        Long expectedId = 1L;
        String expectedName = "doc";
        long expectedAge = 10;
        Person expectedMyContactPerson = new Person("my-pers");
        Integer expectedContactPersonNumber = 2;

        UnsafeDocumentClassView instance = instantiator.newInstance(new Object[] {expectedId, expectedContacts, expectedFirstContactPerson,
                expectedMyContactPerson, expectedName, expectedAge, expectedContactPersonNumber});

        assertTrue(expectedContacts == instance.getContacts());
        assertTrue(expectedFirstContactPerson == instance.getFirstContactPerson());
        assertTrue(expectedId == instance.getId());
        assertTrue(expectedMyContactPerson == instance.getMyContactPerson());
        assertTrue(expectedName == instance.getName());
        assertTrue(expectedName == instance.getUnsafeName());
        assertTrue(expectedAge == instance.getAge());
        assertTrue(expectedContactPersonNumber == instance.getContactPersonNumber());

        expectedContacts = new HashMap<Integer, Person>();
        expectedId = 2L;

        instance.setContacts(expectedContacts);
        instance.setId(expectedId);

        assertTrue(expectedContacts == instance.getContacts());
        assertTrue(expectedId == instance.getId());
    }

    @Test
    public void testInterfaceProxy() throws Exception {
        ViewType<DocumentInterfaceView> viewType = getViewMetamodel().view(DocumentInterfaceView.class);
        Class<? extends DocumentInterfaceView> proxyClass = proxyFactory.getProxy(evm, (ManagedViewTypeImplementor<DocumentInterfaceView>) viewType);

        // The parameter order is _id, contacts, firstContactPerson, id, name
        Constructor<? extends DocumentInterfaceView> constructor = proxyClass.getConstructor(Long.class, Map.class,
                                                                                             Person.class, Person.class, String.class);

        Map<Integer, Person> expectedContacts = new HashMap<Integer, Person>();
        Person expectedFirstContactPerson = new Person("pers");
        Long expectedId = 1L;
        Person expectedMyContactPerson = new Person("my-pers");
        String expectedName = "doc";

        DocumentInterfaceView instance = constructor.newInstance(expectedId, expectedContacts, expectedFirstContactPerson,
                                                                 expectedMyContactPerson, expectedName);

        assertTrue(expectedContacts == instance.getContacts());
        assertTrue(expectedFirstContactPerson == instance.getFirstContactPerson());
        assertTrue(expectedId == instance.getId());
        assertTrue(expectedMyContactPerson == instance.getMyContactPerson());
        assertTrue(expectedName == instance.getName());

        expectedContacts = new HashMap<Integer, Person>();
        expectedId = 2L;

        instance.setContacts(expectedContacts);
        instance.setId(expectedId);

        assertTrue(expectedContacts == instance.getContacts());
        assertTrue(expectedId == instance.getId());
    }

    @Test
    public void testClassProxy() throws Exception {
        ViewType<DocumentClassView> viewType = getViewMetamodel().view(DocumentClassView.class);
        Class<? extends DocumentClassView> proxyClass = proxyFactory.getProxy(evm, (ManagedViewTypeImplementor<DocumentClassView>) viewType);

        // The parameter order is _id, contacts, firstContactPerson, id, name
        Constructor<? extends DocumentClassView> constructor = proxyClass.getConstructor(Long.class, Map.class, Person.class,
                                                                                         Person.class, String.class,
                                                                                         Long.class, Integer.class);

        Map<Integer, Person> expectedContacts = new HashMap<Integer, Person>();
        Person expectedFirstContactPerson = new Person("pers");
        Long expectedId = 1L;
        String expectedName = "doc";
        long expectedAge = 10;
        Person expectedMyContactPerson = new Person("my-pers");
        Integer expectedContactPersonNumber = 2;

        DocumentClassView instance = constructor.newInstance(expectedId, expectedContacts, expectedFirstContactPerson,
                                                             expectedMyContactPerson, expectedName, expectedAge,
                                                             expectedContactPersonNumber);

        assertTrue(expectedContacts == instance.getContacts());
        assertTrue(expectedFirstContactPerson == instance.getFirstContactPerson());
        assertTrue(expectedId == instance.getId());
        assertTrue(expectedMyContactPerson == instance.getMyContactPerson());
        assertTrue(expectedName == instance.getName());
        assertTrue(expectedAge == instance.getAge());
        assertTrue(expectedContactPersonNumber == instance.getContactPersonNumber());

        expectedContacts = new HashMap<Integer, Person>();
        expectedId = 2L;

        instance.setContacts(expectedContacts);
        instance.setId(expectedId);

        assertTrue(expectedContacts == instance.getContacts());
        assertTrue(expectedId == instance.getId());
    }

    @Test
    public void testInterfaceEqualsHashCode() throws Exception {
        ViewType<DocumentInterfaceView> viewType = getViewMetamodel().view(DocumentInterfaceView.class);
        Class<? extends DocumentInterfaceView> proxyClass = proxyFactory.getProxy(evm, (ManagedViewTypeImplementor<DocumentInterfaceView>) viewType);

        // The parameter order is _id, contacts, firstContactPerson, id, name
        Constructor<? extends DocumentInterfaceView> constructor = proxyClass.getConstructor(Long.class, Map.class,
                                                                                             Person.class, Person.class, String.class);

        Map<Integer, Person> expectedContacts = new HashMap<Integer, Person>();
        Person expectedFirstContactPerson = new Person("pers");
        Long expectedId = 1L;
        Person expectedMyContactPerson = new Person("my-pers");
        String expectedName = "doc";

        DocumentInterfaceView instance1 = constructor
            .newInstance(expectedId, expectedContacts, expectedFirstContactPerson, expectedMyContactPerson, expectedName);
        DocumentInterfaceView instance2 = constructor
            .newInstance(expectedId, expectedContacts, expectedFirstContactPerson, expectedMyContactPerson, expectedName);
        assertEquals(instance1, instance2);
        assertEquals(instance1.hashCode(), instance2.hashCode());
    }

    @Test
    public void testClassEqualsHashCode() throws Exception {
        ViewType<DocumentClassView> viewType = getViewMetamodel().view(DocumentClassView.class);
        Class<? extends DocumentClassView> proxyClass = proxyFactory.getProxy(evm, (ManagedViewTypeImplementor<DocumentClassView>) viewType);

        // The parameter order is _id, contacts, firstContactPerson, id, name
        Constructor<? extends DocumentClassView> constructor = proxyClass.getConstructor(Long.class, Map.class, Person.class,
                                                                                         Person.class, String.class,
                                                                                         Long.class, Integer.class);

        Map<Integer, Person> expectedContacts = new HashMap<Integer, Person>();
        Person expectedFirstContactPerson = new Person("pers");
        Long expectedId = 1L;
        String expectedName = "doc";
        long expectedAge = 10;
        Person expectedMyContactPerson = new Person("my-pers");
        Integer expectedContactPersonNumber = 2;

        DocumentClassView instance1 = constructor.newInstance(expectedId, expectedContacts, expectedFirstContactPerson,
                                                              expectedMyContactPerson, expectedName, expectedAge,
                                                              expectedContactPersonNumber);
        DocumentClassView instance2 = constructor.newInstance(expectedId, expectedContacts, expectedFirstContactPerson,
                                                              expectedMyContactPerson, expectedName, expectedAge,
                                                              expectedContactPersonNumber);

        assertEquals(instance1, instance2);
        assertEquals(instance1.hashCode(), instance2.hashCode());
    }

    @Test
    public void testInterfaceProxyStructure() throws Exception {
        ViewType<DocumentInterfaceView> viewType = getViewMetamodel().view(DocumentInterfaceView.class);
        Class<? extends DocumentInterfaceView> proxyClass = proxyFactory.getProxy(evm, (ManagedViewTypeImplementor<DocumentInterfaceView>) viewType);

        // Full constructor + 2 tuple constructors, id constructor and create constructor
        assertEquals(5, proxyClass.getDeclaredConstructors().length);
        assertNotNull(proxyClass.getDeclaredConstructor(Long.class, Map.class, Person.class, Person.class,
                                                        String.class));
        assertNotNull(proxyClass.getDeclaredConstructor(Long.class));
        assertNotNull(proxyClass.getDeclaredConstructor(proxyClass, Map.class));

        // 5 Fields, 2 static field for EntityViewManager, 1 field for EntityViewProxy kind
        assertEquals(8, proxyClass.getDeclaredFields().length);
        // 5 Getters, 2 Setter, 1 Bridge-Getter, 1 Bridge-Setter, 1 Equals, 1 HashCode, 1 ToString, 9 EntityViewProxy methods
        assertEquals(21, proxyClass.getDeclaredMethods().length);
        assertAttribute(proxyClass, "contacts", Modifier.PRIVATE, Map.class, Integer.class, Person.class);
        assertAttribute(proxyClass, "myContactPerson", Modifier.PRIVATE | Modifier.FINAL, Person.class);
        assertAttribute(proxyClass, "firstContactPerson", Modifier.PRIVATE | Modifier.FINAL, Person.class);
        assertAttribute(proxyClass, "id", Modifier.PRIVATE, Long.class);
        assertAttribute(proxyClass, "name", Modifier.PRIVATE | Modifier.FINAL, String.class);
    }

    @Test
    public void testClassProxyStructure() throws Exception {
        ViewType<DocumentClassView> viewType = getViewMetamodel().view(DocumentClassView.class);
        Class<? extends DocumentClassView> proxyClass = proxyFactory.getProxy(evm, (ManagedViewTypeImplementor<DocumentClassView>) viewType);

        // Full constructor + 2 tuple constructors
        assertEquals(3, proxyClass.getDeclaredConstructors().length);
        assertNotNull(proxyClass.getDeclaredConstructor(Long.class, Map.class, Person.class, Person.class,
                                                        String.class, Long.class, Integer.class));

        // 5 Fields, 2 static field for EntityViewManager, 1 field for EntityViewProxy kind
        assertEquals(8, proxyClass.getDeclaredFields().length);
        // 5 Getters, 2 Setter, 1 Bridge-Getter, 1 Bridge-Setter, 1 Equals, 1 HashCode, 1 ToString, 9 EntityViewProxy methods
        assertEquals(21, proxyClass.getDeclaredMethods().length);
        assertAttribute(proxyClass, "contacts", Modifier.PRIVATE, Map.class, Integer.class, Person.class);
        assertAttribute(proxyClass, "myContactPerson", Modifier.PRIVATE | Modifier.FINAL, Person.class);
        assertAttribute(proxyClass, "firstContactPerson", Modifier.PRIVATE | Modifier.FINAL, Person.class);
        assertAttribute(proxyClass, "id", Modifier.PRIVATE, Long.class);
        assertAttribute(proxyClass, "name", Modifier.PRIVATE | Modifier.FINAL, String.class);
    }

    @Test
    public void testProxyCreateInitialization() throws Exception {
        ViewType<DocumentCreateView> viewType = getViewMetamodel().view(DocumentCreateView.class);
        Class<? extends DocumentCreateView> proxyClass = proxyFactory.getProxy(evm, (ManagedViewTypeImplementor<DocumentCreateView>) viewType);

        DocumentCreateView instance = proxyClass.getConstructor(proxyClass, Map.class).newInstance(null, Collections.emptyMap());

        assertTrue(instance.isPostCreated());
        assertNotNull(instance.getContacts());
    }

    @Test
    public void testProxyToStringEmpty() throws Exception {
        ViewType<DocumentCreateView> viewType = getViewMetamodel().view(DocumentCreateView.class);
        Class<? extends DocumentCreateView> proxyClass = proxyFactory.getProxy(evm, (ManagedViewTypeImplementor<DocumentCreateView>) viewType);

        DocumentCreateView instance = proxyClass.getConstructor(proxyClass, Map.class).newInstance(null, Collections.emptyMap());

        assertEquals(DocumentCreateView.class.getSimpleName() + "(id = null)", instance.toString());
    }

    @Test
    public void testProxyToStringWithId() throws Exception {
        ViewType<DocumentCreateView> viewType = getViewMetamodel().view(DocumentCreateView.class);
        Class<? extends DocumentCreateView> proxyClass = proxyFactory.getProxy(evm, (ManagedViewTypeImplementor<DocumentCreateView>) viewType);

        DocumentCreateView instance = proxyClass.getConstructor(Long.class).newInstance(1L);

        assertEquals(DocumentCreateView.class.getSimpleName() + "(id = 1)", instance.toString());
    }

    @Test
    public void testProxyToStringFlatViewEmpty() throws Exception {
        FlatViewType<NameObjectView> viewType = getViewMetamodel().flatView(NameObjectView.class);
        Class<? extends NameObjectView> proxyClass = proxyFactory.getProxy(evm, (ManagedViewTypeImplementor<NameObjectView>) viewType);

        NameObjectView instance = proxyClass.getConstructor(proxyClass, Map.class).newInstance(null, Collections.emptyMap());

        assertEquals(NameObjectView.class.getSimpleName() + "(primaryName = null)", instance.toString());
    }

    @Test
    public void testProxyToStringFlatView() throws Exception {
        FlatViewType<NameObjectView> viewType = getViewMetamodel().flatView(NameObjectView.class);
        Class<? extends NameObjectView> proxyClass = proxyFactory.getProxy(evm, (ManagedViewTypeImplementor<NameObjectView>) viewType);

        NameObjectView instance = proxyClass.getConstructor(String.class).newInstance("test");

        assertEquals(NameObjectView.class.getSimpleName() + "(primaryName = test)", instance.toString());
    }

    @Test
    public void testMutableProxyWithPrimitiveArray() {
        ManagedViewType<DocumentCreateViewWithPrimitiveArray> viewType = build(entityViewConfiguration, DocumentCreateViewWithPrimitiveArray.class).getMetamodel().managedView(DocumentCreateViewWithPrimitiveArray.class);
        proxyFactory.getProxy(evm, (ManagedViewTypeImplementor<DocumentCreateViewWithPrimitiveArray>) viewType);
    }

    @Test
    public void close() throws NoSuchFieldException, IllegalAccessException {
        // Given
        EntityViewManager evm = build(entityViewConfiguration, DocumentCreateViewWithPrimitiveArray.class,
            DocumentInterfaceView.class);
        ManagedViewType<DocumentCreateViewWithPrimitiveArray> documentCreateViewWithPrimitiveArrayType = evm.getMetamodel()
            .managedView(DocumentCreateViewWithPrimitiveArray.class);
        ManagedViewType<DocumentInterfaceView> documentInterfaceViewType = evm.getMetamodel()
            .managedView(DocumentInterfaceView.class);
        Class<? extends DocumentCreateViewWithPrimitiveArray> documentCreateViewWithPrimitiveArrayProxyClass = proxyFactory.getProxy(
            AbstractEntityViewTest.evm, (ManagedViewTypeImplementor<DocumentCreateViewWithPrimitiveArray>) documentCreateViewWithPrimitiveArrayType);
        Class<? extends DocumentInterfaceView> documentInterfaceViewProxyClass = proxyFactory.getProxy(
            AbstractEntityViewTest.evm, (ManagedViewTypeImplementor<DocumentInterfaceView>) documentInterfaceViewType);

        // When
        proxyFactory.clear();

        // Then
        assertProxyEvmReferencesNull(documentCreateViewWithPrimitiveArrayProxyClass);
        assertProxyEvmReferencesNull(documentInterfaceViewProxyClass);
        Map<Class<?>, Class<?>> baseClasses = getDeclaredFieldValue(proxyFactory, "unsafeProxyClasses", Map.class);
        assertTrue(baseClasses.isEmpty());
        Map<Class<?>, Class<?>> proxyClasses = getDeclaredFieldValue(proxyFactory, "unsafeProxyClasses", Map.class);
        assertTrue(proxyClasses.isEmpty());
        Map<Class<?>, Class<?>> unsafeProxyClasses = getDeclaredFieldValue(proxyFactory, "unsafeProxyClasses", Map.class);
        assertTrue(unsafeProxyClasses.isEmpty());
        Map<Class<?>, Class<?>> proxyClassesToViewClasses = getDeclaredFieldValue(proxyFactory, "proxyClassesToViewClasses", Map.class);
        assertTrue(proxyClassesToViewClasses.isEmpty());
    }

    private void assertProxyEvmReferencesNull(Class<?> proxy) throws NoSuchFieldException, IllegalAccessException {
        EntityViewManager proxyEvm = (EntityViewManager) proxy.getField(SerializableEntityViewManager.EVM_FIELD_NAME).get(null);
        SerializableEntityViewManager serializableEvm =
            (SerializableEntityViewManager) proxy.getField(
                SerializableEntityViewManager.SERIALIZABLE_EVM_FIELD_NAME).get(null);
        Field serializableEvmDelegateField = SerializableEntityViewManager.class.getDeclaredField(
            SerializableEntityViewManager.SERIALIZABLE_EVM_DELEGATE_FIELD_NAME);
        serializableEvmDelegateField.setAccessible(true);
        EntityViewManager serializableEvmDelegate = (EntityViewManager) serializableEvmDelegateField.get(serializableEvm);
        assertNull(proxyEvm);
        assertNull(serializableEvmDelegate);
    }

    private <T> T getDeclaredFieldValue(Object obj, String fieldName, Class<T> resultType) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertAttribute(Class<?> proxyClass, String fieldName, int modifiers, Class<?> type, Class<?>... typeArguments) throws Exception {
        assertField(proxyClass, fieldName, modifiers, type, typeArguments);
        assertGetter(proxyClass, fieldName, type, typeArguments);
        if ((modifiers & Modifier.FINAL) == 0) {
            assertSetter(proxyClass, fieldName, type, typeArguments);
        }
    }

    private void assertField(Class<?> proxyClass, String fieldName, int modifiers, Class<?> type, Class<?>... typeArguments) throws Exception {
        Field field = proxyClass.getDeclaredField(fieldName);
        assertNotNull(field);
        assertEquals(modifiers, field.getModifiers());
        assertEquals(type, ReflectionUtils.getResolvedFieldType(proxyClass, field));
        assertArrayEquals(typeArguments, ReflectionUtils.getResolvedFieldTypeArguments(proxyClass, field));
    }

    private void assertGetter(Class<?> proxyClass, String attributeName, Class<?> type, Class<?>... typeArguments) throws Exception {
        Method method = ReflectionUtils.getGetter(proxyClass, attributeName);
        assertNotNull(method);
        assertEquals("Getter modifiers: " + attributeName, Modifier.PUBLIC, method.getModifiers());
        assertEquals("Getter return type of: " + attributeName, type, ReflectionUtils.getResolvedMethodReturnType(proxyClass,
                                                                                                                  method));
        assertArrayEquals("Getter return type arguments of: " + attributeName, typeArguments,
                          ReflectionUtils.getResolvedMethodReturnTypeArguments(proxyClass, method));
    }

    private void assertSetter(Class<?> proxyClass, String attributeName, Class<?> type, Class<?>... typeArguments) throws Exception {
        Method method = ReflectionUtils.getSetter(proxyClass, attributeName);
        assertNotNull(method);
        assertEquals("Setter modifiers: " + attributeName, Modifier.PUBLIC, method.getModifiers());
        assertEquals("Setter parameter type of: " + attributeName, type, ReflectionUtils.getResolvedMethodParameterTypes(
                     proxyClass, method)[0]);
        assertArrayEquals("Setter parameter type arguments of: " + attributeName, typeArguments,
                          ReflectionUtils.getResolvedMethodParameterTypesArguments(proxyClass, method)[0]);
    }
}
