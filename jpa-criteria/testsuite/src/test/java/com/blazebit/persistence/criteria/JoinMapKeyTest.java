/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.DocumentForEntityKeyMaps;
import com.blazebit.persistence.testsuite.entity.DocumentForEntityKeyMaps_;
import com.blazebit.persistence.testsuite.entity.PersonForEntityKeyMaps;
import com.blazebit.persistence.testsuite.entity.PersonForEntityKeyMaps_;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JoinMapKeyTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[]{
                DocumentForEntityKeyMaps.class,
                PersonForEntityKeyMaps.class
        };
    }

    @Test
    @Ignore("Not yet implemented")
    public void joinMapKey() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<DocumentForEntityKeyMaps> root = cq.from(DocumentForEntityKeyMaps.class, "document");
        BlazeMapJoin<DocumentForEntityKeyMaps, PersonForEntityKeyMaps, DocumentForEntityKeyMaps> contactDocuments =
                root.join(DocumentForEntityKeyMaps_.contactDocuments, "contact");
        BlazeJoin<Map<PersonForEntityKeyMaps, DocumentForEntityKeyMaps>, PersonForEntityKeyMaps> keyJoin =
                (BlazeJoin<Map<PersonForEntityKeyMaps, DocumentForEntityKeyMaps>, PersonForEntityKeyMaps>) contactDocuments.key();
        BlazeJoin<PersonForEntityKeyMaps, DocumentForEntityKeyMaps> someDocumentJoin =  keyJoin.join(PersonForEntityKeyMaps_.someDocument, "someDoc");

        cq.select(someDocumentJoin.get(DocumentForEntityKeyMaps_.id));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT someDoc.id FROM DocumentForEntityKeyMaps document " +
                "JOIN document.contactDocuments contact " +
                "JOIN KEY(contact).someDocument someDoc", criteriaBuilder.getQueryString());
    }

}
