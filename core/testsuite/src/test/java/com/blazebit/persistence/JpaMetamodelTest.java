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
package com.blazebit.persistence;

import static com.googlecode.catchexception.CatchException.verifyException;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Set;

import javax.persistence.Tuple;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Document_;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.function.ZeroFunction;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import com.blazebit.persistence.testsuite.base.category.NoDB2;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class JpaMetamodelTest extends AbstractCoreTest {
    
    @Test
    @Ignore("Still work in progress")
    public void testSimpleSelect() {
        javax.persistence.criteria.CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class).from(Document.class, "d");
//        criteria.select(Document_.id);
//        criteria.select("d", Document_.name);
//        
//        criteria.select(Document_.id);
//        criteria.select("d", Document_.name);

        assertEquals("SELECT d.id, d.name FROM Document d", criteria.getQueryString());
        criteria.getResultList();
    }
}
