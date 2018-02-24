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

package com.blazebit.persistence.view.testsuite.basic;

import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.CatchException.verifyException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import org.junit.Test;

import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.CircularDocument;
import com.blazebit.persistence.view.testsuite.basic.model.CircularPerson;
import com.blazebit.persistence.view.testsuite.basic.model.CountSubqueryProvider;
import com.blazebit.persistence.view.testsuite.basic.model.DocumentViewAbstractClass;
import com.blazebit.persistence.view.testsuite.basic.model.DocumentViewInterface;
import com.blazebit.persistence.view.testsuite.basic.model.IdHolderView;
import com.blazebit.persistence.view.testsuite.basic.model.PersonView;
import com.blazebit.persistence.view.testsuite.basic.model.PersonViewWithSingularMapping;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ViewMetamodelTest extends AbstractEntityViewTest {

    private ViewMetamodel getViewMetamodel() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentViewInterface.class);
        cfg.addEntityView(DocumentViewAbstractClass.class);
        cfg.addEntityView(PersonView.class);
        return cfg.createEntityViewManager(cbf).getMetamodel();
    }

    @Test
    public void testCircularViews() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(CircularDocument.class);
        cfg.addEntityView(CircularPerson.class);
        verifyException(cfg, IllegalArgumentException.class).createEntityViewManager(cbf);
    }

    @Test
    public void testGetViewsContainsViews() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentViewInterface.class);
        cfg.addEntityView(DocumentViewAbstractClass.class);
        cfg.addEntityView(PersonView.class);
        ViewMetamodel viewMetamodel = cfg.createEntityViewManager(cbf).getMetamodel();

        assertEquals(3, viewMetamodel.getViews().size());
        assertTrue(viewMetamodel.getViews().contains(viewMetamodel.view(DocumentViewInterface.class)));
        assertTrue(viewMetamodel.getViews().contains(viewMetamodel.view(DocumentViewAbstractClass.class)));
        assertTrue(viewMetamodel.getViews().contains(viewMetamodel.view(PersonView.class)));
    }

    @Test
    public void testMappingSingularView() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(PersonViewWithSingularMapping.class);
        ViewMetamodel viewMetamodel = cfg.createEntityViewManager(cbf).getMetamodel();

        ViewType<?> viewType = viewMetamodel.view(PersonViewWithSingularMapping.class);
        assertNotNull(viewType);
        MethodAttribute<?, ?> attribute = viewType.getAttribute("ownedDocuments");
        assertNotNull(attribute);
        assertFalse(attribute.isCollection());
        assertTrue(attribute instanceof SingularAttribute);
    }

    @Test
    public void testViewReturnsViewTypes() {
        ViewMetamodel viewMetamodel = getViewMetamodel();

        assertNotNull(viewMetamodel.view(DocumentViewInterface.class));
        assertNotNull(viewMetamodel.view(DocumentViewAbstractClass.class));
        assertNotNull(viewMetamodel.view(PersonView.class));
        assertNull(viewMetamodel.view(IdHolderView.class));
    }

    @Test
    public void testViewTypeDefaults() {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        Class<?> expectedViewClass = DocumentViewInterface.class;
        Class<?> expectedEntityClass = Document.class;
        String expectedViewName = expectedViewClass.getSimpleName();
        ViewType<?> docView = viewMetamodel.view(expectedViewClass);

        assertEquals(expectedViewClass, docView.getJavaType());
        assertEquals(expectedViewName, docView.getName());
        assertEquals(expectedEntityClass, docView.getEntityClass());
    }

    @Test
    public void testViewTypeOverrides() {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        Class<?> expectedViewClass = PersonView.class;
        Class<?> expectedEntityClass = Person.class;
        String expectedViewName = "PersView";
        ViewType<?> docView = viewMetamodel.view(expectedViewClass);

        assertEquals(expectedViewClass, docView.getJavaType());
        assertEquals(expectedViewName, docView.getName());
        assertEquals(expectedEntityClass, docView.getEntityClass());
    }

    @Test
    public void testMappingAttributesInterfaceView() {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        Set<MethodAttribute<? super DocumentViewInterface, ?>> attributes = viewMetamodel.view(DocumentViewInterface.class)
            .getAttributes();
        assertEquals(6, attributes.size());
    }

    @Test
    public void testMappingAttributesClassView() {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        Set<MethodAttribute<? super DocumentViewAbstractClass, ?>> attributes = viewMetamodel.view(
            DocumentViewAbstractClass.class).getAttributes();
        assertEquals(7, attributes.size());
    }

    @Test
    public void testMappingAttributeInterfaceInheritedInterfaceView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewInterface.class);
        MethodAttribute<?, ?> attribute = viewType.getAttribute("id");
        assertNotNull(attribute);
        assertEquals("id", attribute.getName());
        assertFalse(attribute.isSubquery());
        assertEquals("id", ((MappingAttribute<?, ?>) attribute).getMapping());
        assertFalse(attribute.isCollection());
        assertFalse(((SingularAttribute<?, ?>) attribute).isQueryParameter());
        assertEquals(Long.class, attribute.getJavaType());
        assertEquals(IdHolderView.class.getMethod("getId"), attribute.getJavaMethod());
        assertEquals(viewType, attribute.getDeclaringType());
    }

    @Test
    public void testMappingAttributeInterfaceInheritedClassView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewAbstractClass.class);
        MethodAttribute<?, ?> attribute = viewType.getAttribute("id");
        assertNotNull(attribute);
        assertEquals("id", attribute.getName());
        assertFalse(attribute.isSubquery());
        assertEquals("id", ((MappingAttribute<?, ?>) attribute).getMapping());
        assertFalse(attribute.isCollection());
        assertFalse(((SingularAttribute<?, ?>) attribute).isQueryParameter());
        assertEquals(Long.class, attribute.getJavaType());
        assertEquals(IdHolderView.class.getMethod("getId"), attribute.getJavaMethod());
        assertEquals(viewType, attribute.getDeclaringType());
    }

    @Test
    public void testMappingAttributeImplicitAttributeInterfaceView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewInterface.class);
        MethodAttribute<?, ?> attribute = viewType.getAttribute("name");
        assertNotNull(attribute);
        assertEquals("name", attribute.getName());
        assertFalse(attribute.isSubquery());
        assertEquals("name", ((MappingAttribute<?, ?>) attribute).getMapping());
        assertFalse(attribute.isCollection());
        assertFalse(((SingularAttribute<?, ?>) attribute).isQueryParameter());
        assertEquals(String.class, attribute.getJavaType());
        assertEquals(DocumentViewInterface.class.getMethod("getName"), attribute.getJavaMethod());
        assertEquals(viewType, attribute.getDeclaringType());
    }

    @Test
    public void testMappingAttributeImplicitAttributeClassView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewAbstractClass.class);
        MethodAttribute<?, ?> attribute = viewType.getAttribute("name");
        assertNotNull(attribute);
        assertEquals("name", attribute.getName());
        assertFalse(attribute.isSubquery());
        assertEquals("name", ((MappingAttribute<?, ?>) attribute).getMapping());
        assertFalse(attribute.isCollection());
        assertFalse(((SingularAttribute<?, ?>) attribute).isQueryParameter());
        assertEquals(String.class, attribute.getJavaType());
        assertEquals(DocumentViewInterface.class.getMethod("getName"), attribute.getJavaMethod());
        assertEquals(viewType, attribute.getDeclaringType());
    }

    @Test
    public void testMappingAttributeExplicitAttributeInterfaceView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewInterface.class);
        MethodAttribute<?, ?> attribute = viewType.getAttribute("firstContactPerson");
        assertNotNull(attribute);
        assertEquals("firstContactPerson", attribute.getName());
        assertFalse(attribute.isSubquery());
        assertEquals("contacts[1]", ((MappingAttribute<?, ?>) attribute).getMapping());
        assertFalse(attribute.isCollection());
        assertFalse(((SingularAttribute<?, ?>) attribute).isQueryParameter());
        assertEquals(Person.class, attribute.getJavaType());
        assertEquals(DocumentViewInterface.class.getMethod("getFirstContactPerson"), attribute.getJavaMethod());
        assertEquals(viewType, attribute.getDeclaringType());
    }

    @Test
    public void testMappingAttributeExplicitAttributeClassView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewAbstractClass.class);
        MethodAttribute<?, ?> attribute = viewType.getAttribute("firstContactPerson");
        assertNotNull(attribute);
        assertEquals("firstContactPerson", attribute.getName());
        assertFalse(attribute.isSubquery());
        assertEquals("contacts[1]", ((MappingAttribute<?, ?>) attribute).getMapping());
        assertFalse(attribute.isCollection());
        assertFalse(((SingularAttribute<?, ?>) attribute).isQueryParameter());
        assertEquals(Person.class, attribute.getJavaType());
        assertEquals(DocumentViewInterface.class.getMethod("getFirstContactPerson"), attribute.getJavaMethod());
        assertEquals(viewType, attribute.getDeclaringType());
    }

    @Test
    public void testMappingAttributeWithParameterInterfaceView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewInterface.class);
        MethodAttribute<?, ?> attribute = viewType.getAttribute("myContactPerson");
        assertNotNull(attribute);
        assertEquals("myContactPerson", attribute.getName());
        assertFalse(attribute.isSubquery());
        assertEquals("contacts2[:contactPersonNumber]", ((MappingAttribute<?, ?>) attribute).getMapping());
        assertFalse(attribute.isCollection());
        assertFalse(((SingularAttribute<?, ?>) attribute).isQueryParameter());
        assertEquals(Person.class, attribute.getJavaType());
        assertEquals(DocumentViewInterface.class.getMethod("getMyContactPerson"), attribute.getJavaMethod());
        assertEquals(viewType, attribute.getDeclaringType());
    }

    @Test
    public void testMappingAttributeWithParameterClassView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewAbstractClass.class);
        MethodAttribute<?, ?> attribute = viewType.getAttribute("myContactPerson");
        assertNotNull(attribute);
        assertEquals("myContactPerson", attribute.getName());
        assertFalse(attribute.isSubquery());
        assertEquals("contacts2[:contactPersonNumber]", ((MappingAttribute<?, ?>) attribute).getMapping());
        assertFalse(attribute.isCollection());
        assertFalse(((SingularAttribute<?, ?>) attribute).isQueryParameter());
        assertEquals(Person.class, attribute.getJavaType());
        assertEquals(DocumentViewInterface.class.getMethod("getMyContactPerson"), attribute.getJavaMethod());
        assertEquals(viewType, attribute.getDeclaringType());
    }

    @Test
    public void testMappingParameterInterfaceView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewInterface.class);
        MethodAttribute<?, ?> attribute = viewType.getAttribute("contactPersonNumber2");
        assertNotNull(attribute);
        assertEquals("contactPersonNumber2", attribute.getName());
        assertFalse(attribute.isSubquery());
        assertEquals("contactPersonNumber", ((MappingAttribute<?, ?>) attribute).getMapping());
        assertFalse(attribute.isCollection());
        assertTrue(((SingularAttribute<?, ?>) attribute).isQueryParameter());
        assertEquals(Integer.class, attribute.getJavaType());
        assertEquals(DocumentViewInterface.class.getMethod("getContactPersonNumber2"), attribute.getJavaMethod());
        assertEquals(viewType, attribute.getDeclaringType());
    }

    @Test
    public void testMappingParameterClassView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewAbstractClass.class);
        MethodAttribute<?, ?> attribute = viewType.getAttribute("contactPersonNumber2");
        assertNotNull(attribute);
        assertEquals("contactPersonNumber2", attribute.getName());
        assertFalse(attribute.isSubquery());
        assertEquals("contactPersonNumber", ((MappingAttribute<?, ?>) attribute).getMapping());
        assertFalse(attribute.isCollection());
        assertTrue(((SingularAttribute<?, ?>) attribute).isQueryParameter());
        assertEquals(Integer.class, attribute.getJavaType());
        assertEquals(DocumentViewInterface.class.getMethod("getContactPersonNumber2"), attribute.getJavaMethod());
        assertEquals(viewType, attribute.getDeclaringType());
    }

    @Test
    public void testMappingSubqueryInterfaceView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewInterface.class);
        MethodAttribute<?, ?> attribute = viewType.getAttribute("contactCount");
        assertNotNull(attribute);
        assertEquals("contactCount", attribute.getName());
        assertTrue(attribute.isSubquery());
        assertEquals(CountSubqueryProvider.class, ((SubqueryAttribute<?, ?>) attribute).getSubqueryProvider());
        assertFalse(attribute.isCollection());
        assertFalse(((SingularAttribute<?, ?>) attribute).isQueryParameter());
        assertEquals(Long.class, attribute.getJavaType());
        assertEquals(DocumentViewInterface.class.getMethod("getContactCount"), attribute.getJavaMethod());
        assertEquals(viewType, attribute.getDeclaringType());
    }

    @Test
    public void testMappingSubqueryClassView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewAbstractClass.class);
        MethodAttribute<?, ?> attribute = viewType.getAttribute("contactCount");
        assertNotNull(attribute);
        assertEquals("contactCount", attribute.getName());
        assertTrue(attribute.isSubquery());
        assertEquals(CountSubqueryProvider.class, ((SubqueryAttribute<?, ?>) attribute).getSubqueryProvider());
        assertFalse(attribute.isCollection());
        assertFalse(((SingularAttribute<?, ?>) attribute).isQueryParameter());
        assertEquals(Long.class, attribute.getJavaType());
        assertEquals(DocumentViewInterface.class.getMethod("getContactCount"), attribute.getJavaMethod());
        assertEquals(viewType, attribute.getDeclaringType());
    }

    @Test
    public void testGetConstructorsInterfaceView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<DocumentViewInterface> viewType = viewMetamodel.view(DocumentViewInterface.class);
        Set<MappingConstructor<DocumentViewInterface>> constructors = viewType.getConstructors();
        assertEquals(0, constructors.size());
    }

    @Test
    public void testGetConstructorsClassView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<DocumentViewAbstractClass> viewType = viewMetamodel.view(DocumentViewAbstractClass.class);
        Set<MappingConstructor<DocumentViewAbstractClass>> constructors = viewType.getConstructors();
        assertEquals(1, constructors.size());
        assertNotNull(viewType.getConstructor(Long.class, Integer.class, String.class));
        assertTrue(constructors.contains(viewType.getConstructor(Long.class, Integer.class, String.class)));
    }

    @Test
    public void testMappingConstructor() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<DocumentViewAbstractClass> viewType = viewMetamodel.view(DocumentViewAbstractClass.class);
        Set<MappingConstructor<DocumentViewAbstractClass>> constructors = viewType.getConstructors();
        MappingConstructor<DocumentViewAbstractClass> constructor = constructors.iterator().next();
        assertNotNull(constructor);
        assertEquals(3, constructor.getParameterAttributes().size());

        assertEquals(Long.class, constructor.getParameterAttributes().get(0).getJavaType());
        assertEquals(constructor, constructor.getParameterAttributes().get(0).getDeclaringConstructor());
        assertEquals(viewType, constructor.getParameterAttributes().get(0).getDeclaringType());
        assertEquals(0, constructor.getParameterAttributes().get(0).getIndex());
        assertFalse(constructor.getParameterAttributes().get(0).isSubquery());
        assertEquals("age + 1", ((MappingAttribute<?, ?>) constructor.getParameterAttributes().get(0)).getMapping());
        assertFalse(constructor.getParameterAttributes().get(0).isCollection());
        assertFalse(((SingularAttribute<?, ?>) constructor.getParameterAttributes().get(0)).isQueryParameter());

        assertEquals(Integer.class, constructor.getParameterAttributes().get(1).getJavaType());
        assertEquals(constructor, constructor.getParameterAttributes().get(1).getDeclaringConstructor());
        assertEquals(viewType, constructor.getParameterAttributes().get(1).getDeclaringType());
        assertEquals(1, constructor.getParameterAttributes().get(1).getIndex());
        assertFalse(constructor.getParameterAttributes().get(1).isSubquery());
        assertEquals("contactPersonNumber", ((MappingAttribute<?, ?>) constructor.getParameterAttributes().get(1)).getMapping());
        assertFalse(constructor.getParameterAttributes().get(1).isCollection());
        assertTrue(((SingularAttribute<?, ?>) constructor.getParameterAttributes().get(1)).isQueryParameter());

        assertEquals(String.class, constructor.getParameterAttributes().get(2).getJavaType());
        assertEquals(constructor, constructor.getParameterAttributes().get(2).getDeclaringConstructor());
        assertEquals(viewType, constructor.getParameterAttributes().get(2).getDeclaringType());
        assertEquals(2, constructor.getParameterAttributes().get(2).getIndex());
        assertFalse(constructor.getParameterAttributes().get(2).isSubquery());
        assertEquals("optionalParameter", ((MappingAttribute<?, ?>) constructor.getParameterAttributes().get(2)).getMapping());
        assertFalse(constructor.getParameterAttributes().get(2).isCollection());
        assertTrue(((SingularAttribute<?, ?>) constructor.getParameterAttributes().get(2)).isQueryParameter());

        assertEquals(DocumentViewAbstractClass.class.getConstructor(Long.class, Integer.class, String.class), constructor.getJavaConstructor());
        assertEquals(viewType, constructor.getDeclaringType());
    }

    @Test
    public void testConflictingMapping() throws Exception {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(ConflictingDoc.class);
        verifyException(cfg, IllegalArgumentException.class).createEntityViewManager(cbf);
        Throwable t = caughtException();
        assertTrue(t.getMessage().contains("'name'"));
        assertTrue(t.getMessage().contains(ConflictingDoc1.class.getName() + ".getName"));
        assertTrue(t.getMessage().contains(ConflictingDoc2.class.getName() + ".getName"));
    }

    public static interface ConflictingDoc1 extends IdHolderView<Long> {
        @Mapping("name")
        public String getName();
    }

    public static interface ConflictingDoc2 extends IdHolderView<Long> {
        @Mapping("COALESCE(name, '')")
        public String getName();
    }

    @EntityView(Document.class)
    public static interface ConflictingDoc extends ConflictingDoc1, ConflictingDoc2 {
    }

    @Test
    public void testResolveConflictingMapping() throws Exception {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(ResolveConflictingDoc.class);
        ViewMetamodel metamodel = cfg.createEntityViewManager(cbf).getMetamodel();
        MappingAttribute<?, ?> mappingAttribute = (MappingAttribute<?, ?>) metamodel.view(ResolveConflictingDoc.class).getAttribute("name");
        assertEquals("UPPER(name)", mappingAttribute.getMapping());
    }

    public static interface ResolveConflictingDoc1 extends IdHolderView<Long> {
        @Mapping("name")
        public String getName();
    }

    public static interface ResolveConflictingDoc2 extends IdHolderView<Long> {
        @Mapping("COALESCE(name, '')")
        public String getName();
    }

    @EntityView(Document.class)
    public static interface ResolveConflictingDoc extends ResolveConflictingDoc1, ResolveConflictingDoc2 {
        @Mapping("UPPER(name)")
        public String getName();
    }

    @Test
    public void testInheritancePrecedenceNonConflictingMapping() throws Exception {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(InheritancePrecedenceNonConflictingDoc.class);
        ViewMetamodel metamodel = cfg.createEntityViewManager(cbf).getMetamodel();
        MappingAttribute<?, ?> mappingAttribute = (MappingAttribute<?, ?>) metamodel.view(InheritancePrecedenceNonConflictingDoc.class).getAttribute("name");
        assertEquals("COALESCE(name, '')", mappingAttribute.getMapping());
    }

    public static interface InheritancePrecedenceNonConflictingDoc1 extends IdHolderView<Long> {
        @Mapping("name")
        public String getName();
    }

    public static interface InheritancePrecedenceNonConflictingDoc2 extends InheritancePrecedenceNonConflictingDoc1 {
        @Mapping("COALESCE(name, '')")
        public String getName();
    }

    @EntityView(Document.class)
    public static interface InheritancePrecedenceNonConflictingDoc extends InheritancePrecedenceNonConflictingDoc2 {
    }

    @Test
    public void testNoMappingNonConflictingMapping() throws Exception {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(NoMappingNonConflictingDoc.class);
        ViewMetamodel metamodel = cfg.createEntityViewManager(cbf).getMetamodel();
        MappingAttribute<?, ?> mappingAttribute = (MappingAttribute<?, ?>) metamodel.view(NoMappingNonConflictingDoc.class).getAttribute("name");
        assertEquals("COALESCE(name, '')", mappingAttribute.getMapping());
    }

    public static interface NoMappingNonConflictingDoc1 extends IdHolderView<Long> {
        public String getName();
    }

    public static interface NoMappingNonConflictingDoc2 extends IdHolderView<Long> {
        @Mapping("COALESCE(name, '')")
        public String getName();
    }

    @EntityView(Document.class)
    public static interface NoMappingNonConflictingDoc extends NoMappingNonConflictingDoc1, NoMappingNonConflictingDoc2 {
    }

    
    // TODO: Test filter mapping
}
