/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.testsuite;

import com.blazebit.persistence.deltaspike.data.testsuite.entity.Person;
import com.blazebit.persistence.deltaspike.data.testsuite.repository.InvalidRestrictedPersonViewRepository;
import com.blazebit.persistence.deltaspike.data.testsuite.repository.PersonRepository;
import com.blazebit.persistence.deltaspike.data.testsuite.repository.RestrictedPersonViewRepository;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class RestrictedPersonViewRepositoryTest extends AbstractEntityViewRepositoryTest {

    @Inject
    private RestrictedPersonViewRepository personViewRepository;
    @Inject
    private InvalidRestrictedPersonViewRepository invalidRestrictedPersonViewRepository;
    @Inject
    private PersonRepository personRepository;

    @Test
    public void testFindAll() {
        assertEquals(persons.length, personRepository.findAll().size());
        assertEquals(persons.length, personViewRepository.findAll().size());
    }

    @Test
    public void testInvalid() {
        List result = invalidRestrictedPersonViewRepository.findAll();
        assertTrue(result.get(0) instanceof Person);
    }
}