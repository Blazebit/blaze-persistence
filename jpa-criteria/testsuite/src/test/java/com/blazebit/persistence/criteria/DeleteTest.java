package com.blazebit.persistence.criteria;


import com.blazebit.persistence.criteria.impl.BlazeCriteria;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Document_;
import org.junit.Test;

import javax.persistence.criteria.Root;

import static org.junit.Assert.assertEquals;

public class DeleteTest extends AbstractCoreTest {

    @Test
    public void simpleWhere() {
        BlazeCriteriaBuilder cb = BlazeCriteria.get(em, cbf);
        BlazeCriteriaDelete<Document> query = cb.createCriteriaDelete(Document.class, "d");
        Root<Document> root = query.getRoot();

        query.where(cb.equal(root.get(Document_.name), "abc"));
        assertEquals("DELETE FROM Document d WHERE d.name = :generated_param_0", query.getQueryString());
        query.getQuery();
    }
}
