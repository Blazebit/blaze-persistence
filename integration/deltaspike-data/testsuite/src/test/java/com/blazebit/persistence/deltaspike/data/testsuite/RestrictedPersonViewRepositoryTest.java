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