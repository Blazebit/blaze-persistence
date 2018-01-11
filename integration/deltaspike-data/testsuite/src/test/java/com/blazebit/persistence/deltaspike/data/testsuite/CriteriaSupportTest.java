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
import com.blazebit.persistence.deltaspike.data.testsuite.repository.PersonViewCriteriaRepository;
import com.blazebit.persistence.deltaspike.data.testsuite.view.PersonView;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class CriteriaSupportTest extends AbstractEntityViewRepositoryTest {

    @Inject
    private PersonViewCriteriaRepository personViewRepository;

    @Test
    public void testGetPersonViewsByComplexCondition() {
        List<PersonView> expected = Arrays.asList(
                fetch(PersonView.class, persons[1].getId()),
                fetch(PersonView.class, persons[2].getId()),
                fetch(PersonView.class, persons[3].getId()),
                fetch(PersonView.class, persons[4].getId())
        );
        List<PersonView> result = personViewRepository.getPersonViewsByComplexCondition();
        assertEquals(expected, result);
    }

    @Test
    public void testGetPersonsByComplexCondition() {
        List<Person> expected = Arrays.asList(
                persons[1],
                persons[2],
                persons[3],
                persons[4]
        );
        List<Person> result = personViewRepository.getPersonsByComplexCondition();
        assertEquals(expected, result);
    }
}