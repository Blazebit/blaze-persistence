/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.basic.model.IdHolderView;
import org.junit.Assert;
import org.junit.Test;

import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.DocumentValidationView;
import com.blazebit.persistence.view.testsuite.basic.model.PersonDuplicateCollectionUsageValidationView;
import com.blazebit.persistence.view.testsuite.basic.model.PersonInvalidMappingValidationView;
import com.blazebit.persistence.view.testsuite.basic.model.PersonValidationView;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ValidationTest extends AbstractEntityViewTest {

    @Test
    public void testValidation() {
        build(
                DocumentValidationView.class,
                PersonValidationView.class
        );
    }

    @Test
    public void testValidationDuplicateCollection() {
        try {
            build(PersonDuplicateCollectionUsageValidationView.class);
            Assert.fail("Expected validation exception!");
        } catch (IllegalArgumentException ex) {
            if (!ex.getMessage().contains("'ownedDocuments'")) {
                throw ex;
            }
        }
    }

    @Test
    public void testValidationInvalidMapping() {
        try {
            build(PersonInvalidMappingValidationView.class);
            Assert.fail("Expected validation exception!");
        } catch (IllegalArgumentException ex) {
            if (!ex.getMessage().contains(PersonInvalidMappingValidationView.class.getSimpleName() + ".getName") || !ex.getMessage().contains("'defaultContact'")) {
                throw ex;
            }
        }
    }

    @Test
    public void testValidationInvalidCaseMapping() {
        try {
            build(PersonInvalidCaseMappingValidationView.class);
            Assert.fail("Expected validation exception!");
        } catch (IllegalArgumentException ex) {
            if (!ex.getMessage().contains(PersonInvalidCaseMappingValidationView.class.getSimpleName() + ".getValid") || !ex.getMessage().contains("'invalid'")) {
                throw ex;
            }
        }
    }

    @EntityView(Person.class)
    public interface PersonInvalidCaseMappingValidationView extends IdHolderView<Long> {

        @Mapping("CASE WHEN partnerDocument.invalid = 1 THEN true ELSE false END")
        public boolean getValid();
    }

    // Test for issue #1700
    @Test
    public void testValidationNullIf() {
        build(PersonNullIfView.class);
    }

    @EntityView(Person.class)
    public interface PersonNullIfView extends IdHolderView<Long> {

        @Mapping("NULLIF(name,'')")
        public String getName();
    }
}
