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
import com.blazebit.persistence.deltaspike.data.testsuite.repository.FullPersonViewRepository;
import com.blazebit.persistence.deltaspike.data.testsuite.repository.SimplePersonViewRepository;
import com.blazebit.persistence.deltaspike.data.testsuite.view.PersonView;
import org.apache.deltaspike.data.api.QueryResult;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class ExtendedEntityViewRepositoryTest extends AbstractEntityViewRepositoryTest {

    @Inject
    private FullPersonViewRepository personViewRepository;
    @Inject
    private SimplePersonViewRepository simplePersonViewRepository;

    @Test
    public void testFindAnyByName() {
        String name = "John Smith";
        PersonView result1 = personViewRepository.findAnyByName(name);
        PersonView result2 = simplePersonViewRepository.findAnyByName(name);
        assertEquals(persons[4].getId(), result1.getId());
        assertEquals(result1, result2);
    }

    @Test
    public void testFindByQuery() {
        Person result1 = personViewRepository.getJohnDoe();
        assertEquals(persons[1].getId(), result1.getId());
    }

    @Test
    public void testFindByNameAndPosition() {
        List<PersonView> result = personViewRepository.findByNameAndPosition("Harry Norman", 1);
        assertEquals(1, result.size());
        assertEquals(persons[5].getId(), result.get(0).getId());
    }

    @Test
    public void testFindByNameAndPosition_noResult() {
        List<PersonView> result = personViewRepository.findByNameAndPosition("Harry Norman", 2);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByNameOrPosition() {
        List<PersonView> expected = Arrays.asList(
                fetch(PersonView.class, persons[1].getId()),
                fetch(PersonView.class, persons[5].getId()),
                fetch(PersonView.class, persons[6].getId())
        );
        List<PersonView> result = personViewRepository.findByNameOrPosition("Harry Norman", 2);
        assertUnorderedEquals(expected, result);
    }

    @Test
    public void testFindByPositionBetween() {
        List<PersonView> expected = Arrays.asList(
                fetch(PersonView.class, persons[1].getId()),
                fetch(PersonView.class, persons[6].getId())
        );
        List<PersonView> result = personViewRepository.findByPositionBetween(2, 3);
        assertUnorderedEquals(expected, result);
    }

    @Test
    public void testFindFirstByNameLikeOrderByIdAsc() {
        PersonView result1 = personViewRepository.findFirst1ByNameLikeOrderByIdAsc("John%");
        PersonView result2 = personViewRepository.findTop1ByNameLikeOrderByIdAsc("John%");
        assertEquals(persons[1].getId(), result1.getId());
        assertEquals(persons[1].getId(), result2.getId());
    }

    @Test
    public void testFindFirst2ByNameLikeOrderByIdAsc() {
        List<PersonView> expected = Arrays.asList(
                fetch(PersonView.class, persons[1].getId()),
                fetch(PersonView.class, persons[4].getId())
        );
        List<PersonView> result1 = personViewRepository.findFirst2ByNameLikeOrderByIdAsc("John%");
        List<PersonView> result2 = personViewRepository.findTop2ByNameLikeOrderByIdAsc("John%");
        assertEquals(expected, result1);
        assertEquals(expected, result2);
    }

    @Test
    public void testFindAnyByNameIsNullOrderByIdAsc() {
        assertNull(personViewRepository.findAnyByNameIsNullOrderByIdAsc());
        final Person newPerson = new Person(persons[persons.length - 1].getId() + 1, null, 0);
        transactional(new Runnable() {
            @Override
            public void run() {
                em.persist(newPerson);
            }
        });
        PersonView result = personViewRepository.findAnyByNameIsNullOrderByIdAsc();
        assertEquals(newPerson.getId(), result.getId());
    }

    @Test
    public void testFindAllOrderByNameDescIdAsc() {
        List<PersonView> expected = Arrays.asList(
                fetch(PersonView.class, persons[0].getId()),
                fetch(PersonView.class, persons[4].getId()),
                fetch(PersonView.class, persons[1].getId()),
                fetch(PersonView.class, persons[2].getId()),
                fetch(PersonView.class, persons[5].getId()),
                fetch(PersonView.class, persons[6].getId()),
                fetch(PersonView.class, persons[3].getId())
        );
        List<PersonView> result = personViewRepository.findAllOrderByNameDescIdAsc();
        assertEquals(expected, result);
    }

    @Test
    public void testFindByNameLikeOrderByIdAsc() {
        List<PersonView> expected1 = Arrays.asList(
                fetch(PersonView.class, persons[1].getId()),
                fetch(PersonView.class, persons[2].getId())
        );
        List<PersonView> expected2 = Arrays.asList(
                fetch(PersonView.class, persons[2].getId()),
                fetch(PersonView.class, persons[4].getId())
        );
        List<PersonView> result1 = personViewRepository.findByNameLikeOrderByIdAsc("J%", 0, 2);
        List<PersonView> result2 = personViewRepository.findByNameLikeOrderByIdAsc("J%", 1, 2);
        assertEquals(expected1, result1);
        assertEquals(expected2, result2);
    }

    @Test
    public void testFindAnyByAdress_streetLike() {
        PersonView result = personViewRepository.findAnyByAddress_streetLikeOrderByIdAsc("King%");
        assertEquals(persons[4].getId(), result.getId());
    }

    @Test
    public void testFindByPosition() {
        List<PersonView> expected = Arrays.asList(
                fetch(PersonView.class, persons[2].getId()),
                fetch(PersonView.class, persons[4].getId())
        );
        List<PersonView> expectedReversed = new ArrayList<>(expected);
        Collections.reverse(expectedReversed);
        QueryResult<PersonView> result = personViewRepository.findByPosition(4);
        assertEquals(expected, result.orderAsc("id").getResultList());
        assertEquals(expectedReversed, result.changeOrder("id").getResultList());
        result.clearOrder();

        // Always use max results as Hibernate has to do "rownumber < first + max" for databases like DB2 which don't support a limit clause.
        // Without the max results, we will run into an integer overflow which results in a negative number...
        assertEquals(persons[4].getId(), result.orderAsc("id").firstResult(1).maxResults(100).getSingleResult().getId());
        assertEquals(persons[2].getId(), result.firstResult(0).maxResults(1).getSingleResult().getId());

        assertEquals(persons[2].getId(), result.withPageSize(1).toPage(0).getSingleResult().getId());
        assertEquals(persons[4].getId(), result.nextPage().getSingleResult().getId());
        assertEquals(persons[2].getId(), result.previousPage().getSingleResult().getId());
    }
}