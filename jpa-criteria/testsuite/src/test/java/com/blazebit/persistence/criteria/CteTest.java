/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.criteria;

import com.blazebit.persistence.ConfigurationProperties;
import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Document_;
import com.blazebit.persistence.testsuite.entity.NameObjectContainer2_;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CteTest extends AbstractCoreTest {

    private CriteriaBuilderFactory cbfUnoptimized;

    @Before
    public void initNonOptimized() {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        config.getProperties().setProperty(ConfigurationProperties.EXPRESSION_OPTIMIZATION, "false");
        cbfUnoptimized = config.createCriteriaBuilderFactory(emf);
    }

    @Test
    public void singularAttributeWithLiterals() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);

        BlazeCTECriteria<Document> documentCte = cq.with(Document.class);
        documentCte.bind(Document_.name, "");
        documentCte.bind(documentCte.get(Document_.nameContainer).get(NameObjectContainer2_.name), "");


        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        Long longValue = 999999999L;
        Path<Double> doublePath = root.get(Document_.someValue);
        Path<Integer> integerPath = root.get(Document_.idx);

        cq.select(root.get(Document_.id));
        cq.where(cb.and(
                cb.equal(root.get(Document_.id), 1L),
                cb.greaterThan(root.get(Document_.creationDate), Calendar.getInstance()),
                cb.notEqual(root.get(Document_.lastModified), new Date()),
                cb.equal(cb.lower(cb.literal("ABC")), "abc"),
                cb.ge(
                        cb.quot( integerPath, doublePath ),
                        longValue
                )
        ));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document WHERE document.id = 1L AND document.creationDate > :generated_param_0 " +
                "AND document.lastModified <> :generated_param_1 AND LOWER(:generated_param_2) = :generated_param_3 AND document.idx / document.someValue >= 999999999L", criteriaBuilder.getQueryString());
        assertEquals(GregorianCalendar.class, criteriaBuilder.getParameter("generated_param_0").getParameterType());
        assertEquals(Date.class, criteriaBuilder.getParameter("generated_param_1").getParameterType());
        assertEquals(String.class, criteriaBuilder.getParameter("generated_param_2").getParameterType());
    }

}
