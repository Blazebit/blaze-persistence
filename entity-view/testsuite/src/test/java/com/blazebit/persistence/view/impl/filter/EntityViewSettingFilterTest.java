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

package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.AbstractPersistenceTest;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.spi.Criteria;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Filter;
import com.blazebit.persistence.view.Sorter;
import com.blazebit.persistence.view.Sorters;
import com.blazebit.persistence.view.impl.EntityViewConfigurationImpl;
import com.blazebit.persistence.view.impl.filter.model.FilteredDocument;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityTransaction;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 */
public class EntityViewSettingFilterTest extends AbstractPersistenceTest {
    
    
    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Document doc1 = new Document("MyTest");
            Document doc2 = new Document("YourTest");
            
            Person o1 = new Person("pers1");
            Person o2 = new Person("pers2");
            o1.getLocalized().put(1, "localized1");
            o2.getLocalized().put(1, "localized2");
            
            doc1.setOwner(o1);
            doc2.setOwner(o2);
            
            doc1.getContacts().put(1, o1);
            doc2.getContacts().put(1, o2);
            
            doc1.getContacts2().put(2, o1);
            doc2.getContacts2().put(2, o2);
            
            em.persist(o1);
            em.persist(o2);
            
            em.persist(doc1);
            em.persist(doc2);
            
            em.flush();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            tx.rollback();
        }
    }
    
    @Test
    public void testEntityViewSettingFilter() {
        EntityViewConfigurationImpl cfg = new EntityViewConfigurationImpl();
        cfg.addEntityView(FilteredDocument.class);
        EntityViewManager evm = cfg.createEntityViewManager();
        
        // Base setting
        EntityViewSetting<FilteredDocument> setting = new EntityViewSetting<FilteredDocument>(FilteredDocument.class, 0, 1);
        
        // Query
        CriteriaBuilder<Document> cb = Criteria.from(em, Document.class);
        // Filters
        Map<String, String> attributeFilters = new HashMap<String, String>();
        attributeFilters.put("name", "Test");
        // Sorters
        Map<String, Boolean> attributeSorters = new HashMap<String, Boolean>();
        attributeSorters.put("contactName", Boolean.FALSE);
        // Parameters
        Map<String, Object> optionalParameters = new HashMap<String, Object>();
        optionalParameters.put("index", 1);
        
        prepareSetting(evm.getMetamodel().view(FilteredDocument.class), setting, attributeFilters, attributeSorters, optionalParameters);
        PaginatedCriteriaBuilder<FilteredDocument> paginatedCb = setting.apply(evm, cb);
        PagedList<FilteredDocument> result = paginatedCb.getResultList();
        
        assertEquals(1, result.size());
        assertEquals(2, result.totalSize());
        assertEquals("YourTest", result.get(0).getName());
        assertEquals("pers2", result.get(0).getContactName());
    }
    
    private <T> void prepareSetting(ViewType<T> viewType, EntityViewSetting<T> setting, Map<String, String> attributeFilters, Map<String, Boolean> attributeSorters, Map<String, Object> optionalParameters) {
        Map<String, Filter> filters = new HashMap<String, Filter>();
        Map<String, Sorter> sorters = new HashMap<String, Sorter>();
        
        for (Map.Entry<String, String> attributeFilterEntry : attributeFilters.entrySet()) {
            String attributeName = attributeFilterEntry.getKey();
            String filterValue = attributeFilterEntry.getValue();
            Class<? extends Filter> filterClass = viewType.getAttribute(attributeName).getFilterMapping();
            if (filterClass == null) {
                throw new IllegalArgumentException("No filter mapping given for the attribute '" + attributeName + "' in the entity view type '" + viewType.getJavaType().getName() + "'");
            }
            
            Filter filter = null;
            
            try {
                Constructor<?>[] constructors = filterClass.getDeclaredConstructors();
                Constructor<? extends Filter> filterConstructor = findConstructor(constructors, String.class);

                if (filterConstructor != null) {
                    filter = filterConstructor.newInstance(filterValue);
                } else {
                    filterConstructor = findConstructor(constructors, Object.class);

                    if  (filterConstructor != null) {
                        filter = filterConstructor.newInstance((Object) filterValue);
                    } else {
                        filterConstructor = findConstructor(constructors);

                        if (filterConstructor == null) {
                            throw new IllegalArgumentException("No suitable constructor found for filter class '" + filterClass.getName() + "'");
                        }

                        filter = filterConstructor.newInstance();
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException("Could not create an instance of the filter class '" + filterClass.getName() + "'", ex);
            }
            
            filters.put(viewType.getAttribute(attributeName).getMapping(), filter);
        }
        for (Map.Entry<String, Boolean> attributeSorterEntry : attributeSorters.entrySet()) {
            String attributeName = attributeSorterEntry.getKey();
            Sorter sorter = attributeSorterEntry.getValue() ? Sorters.ascending(false) : Sorters.descending(false);
            sorters.put(viewType.getAttribute(attributeName).getMapping(), sorter);
        }
        
        setting.addFilters(filters);
        setting.addSorters(sorters);
        
        for (Map.Entry<String, Object> entry : optionalParameters.entrySet()) {
            setting.addOptionalParameter(entry.getKey(), entry.getValue());
        }
    }

    private Constructor<? extends Filter> findConstructor(Constructor<?>[] constructors, Class<?>... classes) {
        for (int i = 0; i < constructors.length; i++) {
            if (Arrays.equals(constructors[i].getParameterTypes(), classes)) {
                return (Constructor<? extends Filter>) constructors[i];
            }
        }
        
        return null;
    }
    
}
