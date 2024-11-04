/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.subview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewDocumentCollectionsView;
import org.junit.Assert;

import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewPersonForCollectionsView;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SubviewAssert {

    public static void assertSubviewEquals(Map<Integer, PersonForCollections> persons, Map<Integer, SubviewPersonForCollectionsView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Map.Entry<Integer, PersonForCollections> personEntry : persons.entrySet()) {
            PersonForCollections p = personEntry.getValue();
            SubviewPersonForCollectionsView pSub = personSubviews.get(personEntry.getKey());
            assertEquals(p.getName(), pSub.getName());
        }
    }

    public static void assertSubviewEquals(List<PersonForCollections> persons, List<SubviewPersonForCollectionsView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (int i = 0; i < persons.size(); i++) {
            PersonForCollections p = persons.get(i);
            SubviewPersonForCollectionsView pSub = personSubviews.get(i);
            assertEquals(p.getName(), pSub.getName());
        }
    }

    public static void assertSubviewEquals(Set<PersonForCollections> persons, Set<SubviewPersonForCollectionsView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (PersonForCollections p : persons) {
            boolean found = false;
            for (SubviewPersonForCollectionsView pSub : personSubviews) {
                if (p.getName().equals(pSub.getName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a person subview instance with the name: " + p.getName());
            }
        }
    }
}
