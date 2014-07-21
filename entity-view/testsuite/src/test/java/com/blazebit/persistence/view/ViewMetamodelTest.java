/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence.view;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.model.DocumentViewInterface;
import com.blazebit.persistence.view.model.DocumentViewAbstractClass;
import com.blazebit.persistence.view.model.IdHolderView;
import com.blazebit.persistence.view.model.PersonView1;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author cpbec
 */
public class ViewMetamodelTest extends AbstractEntityViewPersistenceTest {
    
    private ViewMetamodel getViewMetamodel() {
        EntityViewConfiguration cfg = new EntityViewConfiguration();
        cfg.addEntityView(DocumentViewInterface.class);
        cfg.addEntityView(DocumentViewAbstractClass.class);
        cfg.addEntityView(PersonView1.class);
        return cfg.createEntityViewManagerFactory().getMetamodel();
    }
    
    @Test
    public void testGetViewsContainsViews() {
        EntityViewConfiguration cfg = new EntityViewConfiguration();
        cfg.addEntityView(DocumentViewInterface.class);
        cfg.addEntityView(DocumentViewAbstractClass.class);
        cfg.addEntityView(PersonView1.class);
        ViewMetamodel viewMetamodel = cfg.createEntityViewManagerFactory().getMetamodel();
        
        assertEquals(3, viewMetamodel.getViews().size());
        assertTrue(viewMetamodel.getViews().contains(viewMetamodel.view(DocumentViewInterface.class)));
        assertTrue(viewMetamodel.getViews().contains(viewMetamodel.view(DocumentViewAbstractClass.class)));
        assertTrue(viewMetamodel.getViews().contains(viewMetamodel.view(PersonView1.class)));
    }
    
    @Test
    public void testViewReturnsViewTypes() {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        
        assertNotNull(viewMetamodel.view(DocumentViewInterface.class));
        assertNotNull(viewMetamodel.view(DocumentViewAbstractClass.class));
        assertNotNull(viewMetamodel.view(PersonView1.class));
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
        Class<?> expectedViewClass = PersonView1.class;
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
        Set<MethodAttribute<? super DocumentViewInterface, ?>> attributes = viewMetamodel.view(DocumentViewInterface.class).getAttributes();
        assertEquals(3, attributes.size());
    }
    
    @Test
    public void testMappingAttributesClassView() {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        Set<MethodAttribute<? super DocumentViewAbstractClass, ?>> attributes = viewMetamodel.view(DocumentViewAbstractClass.class).getAttributes();
        assertEquals(3, attributes.size());
    }
    
    @Test
    public void testMappingAttributeInterfaceInheritedInterfaceView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewInterface.class);
        MethodAttribute<?, ?> attribute = viewType.getAttribute("id");
        assertNotNull(attribute);
        assertEquals("id", attribute.getName());
        assertEquals("id", attribute.getMapping());
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
        assertEquals("id", attribute.getMapping());
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
        assertEquals("name", attribute.getMapping());
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
        assertEquals("name", attribute.getMapping());
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
        assertEquals("contacts[1]", attribute.getMapping());
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
        assertEquals("contacts[1]", attribute.getMapping());
        assertEquals(Person.class, attribute.getJavaType());
        assertEquals(DocumentViewInterface.class.getMethod("getFirstContactPerson"), attribute.getJavaMethod());
        assertEquals(viewType, attribute.getDeclaringType());
    }
    
    @Test
    public void testGetConstructorsInterfaceView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewInterface.class);
        Set<MappingConstructor<?>> constructors = (Set<MappingConstructor<?>>) viewType.getConstructors();
        assertEquals(0, constructors.size());
    }
    
    @Test
    public void testGetConstructorsClassView() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewAbstractClass.class);
        Set<MappingConstructor<?>> constructors = (Set<MappingConstructor<?>>) viewType.getConstructors();
        assertEquals(1, constructors.size());
        assertNotNull(viewType.getConstructor(long.class));
        assertTrue(constructors.contains(viewType.getConstructor(long.class)));
    }
    
    @Test
    public void testMappingConstructor() throws Exception {
        ViewMetamodel viewMetamodel = getViewMetamodel();
        ViewType<?> viewType = viewMetamodel.view(DocumentViewAbstractClass.class);
        Set<MappingConstructor<?>> constructors = (Set<MappingConstructor<?>>) viewType.getConstructors();
        MappingConstructor<?> constructor = constructors.iterator().next();
        assertNotNull(constructor);
        assertEquals(1, constructor.getParameterAttributes().size());
        assertEquals(long.class, constructor.getParameterAttributes().get(0).getJavaType());
        assertEquals(constructor, constructor.getParameterAttributes().get(0).getDeclaringConstructor());
        assertEquals(viewType, constructor.getParameterAttributes().get(0).getDeclaringType());
        assertEquals(0, constructor.getParameterAttributes().get(0).getIndex());
        assertEquals(DocumentViewAbstractClass.class.getConstructor(long.class), constructor.getJavaConstructor());
        assertEquals(viewType, constructor.getDeclaringType());
    }
}
