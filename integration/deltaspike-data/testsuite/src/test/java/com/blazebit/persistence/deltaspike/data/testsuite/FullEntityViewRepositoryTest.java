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

import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.deltaspike.data.KeysetAwarePage;
import com.blazebit.persistence.deltaspike.data.KeysetPageRequest;
import com.blazebit.persistence.deltaspike.data.KeysetPageable;
import com.blazebit.persistence.deltaspike.data.Page;
import com.blazebit.persistence.deltaspike.data.PageRequest;
import com.blazebit.persistence.deltaspike.data.Slice;
import com.blazebit.persistence.deltaspike.data.Sort;
import com.blazebit.persistence.deltaspike.data.testsuite.entity.Person;
import com.blazebit.persistence.deltaspike.data.testsuite.entity.Person_;
import com.blazebit.persistence.deltaspike.data.testsuite.repository.FullPersonViewRepository;
import com.blazebit.persistence.deltaspike.data.testsuite.repository.PersonRepository;
import com.blazebit.persistence.deltaspike.data.testsuite.view.PersonView;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class FullEntityViewRepositoryTest extends AbstractEntityViewRepositoryTest {

    @Inject
    private FullPersonViewRepository personViewRepository;
    @Inject
    private PersonRepository personRepository;

    @Test
    public void testFindAll() {
        assertEquals(persons.length, personRepository.findAll().size());
        assertEquals(persons.length, personViewRepository.findAll().size());
    }

    @Test
    public void testFindAllRange() {
        int start = 1;
        int offset = 2;
        List<PersonView> expected = new ArrayList<>();
        for (int i = start; i < start + offset; i++) {
            expected.add(fetch(PersonView.class, persons[i].getId()));
        }
        // we do not test DeltaSpike's findAll(int, int) method here because its results are non-deterministic
        List<PersonView> actual = personViewRepository.findAll(start, offset);
        assertEquals(expected, actual);
        for (int i = 0; i < actual.size(); i++) {
            assertEquals(expected.get(i).getChildren(), actual.get(i).getChildren());
        }
    }

    @Test
    public void testFindByNamePaginated() {
        // we do not test DeltaSpike's findAll(int, int) method here because its results are non-deterministic
        List<PersonView> actual = personViewRepository.findByNameLike("John %", new PageRequest(0, 1, Sort.Direction.ASC, "id"));
        assertTrue(actual instanceof PagedList<?>);
        assertEquals(1, actual.size());
        assertEquals("John Doe", actual.get(0).getName());

        actual = personViewRepository.findByNameLike("John %", new PageRequest(1, 1, Sort.Direction.ASC, "id"));
        assertEquals(1, actual.size());
        assertEquals("John Smith", actual.get(0).getName());
    }

    @Test
    public void testFindSlice() {
        Slice<PersonView> actual = personViewRepository.findByIdIsNotNull(new PageRequest(0, 1, Sort.Direction.ASC, "id"));
        assertEquals(1, actual.getSize());
        assertEquals("Mother", actual.getContent().get(0).getName());

        actual = personViewRepository.findByIdIsNotNull(new PageRequest(1, 1, Sort.Direction.ASC, "id"));
        assertEquals(1, actual.getSize());
        assertEquals("John Doe", actual.getContent().get(0).getName());
    }

    @Test
    public void testFindByNameKeysetPaginated() {
        // we do not test DeltaSpike's findAll(int, int) method here because its results are non-deterministic
        Page<PersonView> actual = personViewRepository.findByNameLike("John %", new KeysetPageRequest(null, new PageRequest(0, 1, Sort.Direction.ASC, "id")));
        assertTrue(actual instanceof KeysetAwarePage<?>);
        assertEquals(1, actual.getNumberOfElements());
        assertEquals("John Doe", actual.getContent().get(0).getName());

        actual = personViewRepository.findByNameLike("John %", (KeysetPageable) actual.nextPageable());
        assertEquals(1, actual.getNumberOfElements());
        assertEquals("John Smith", actual.getContent().get(0).getName());
    }

    @Test
    public void testFindSliceKeyset() {
        Slice<PersonView> actual = personViewRepository.findByIdIsNotNull(new KeysetPageRequest(null, new PageRequest(0, 1, Sort.Direction.ASC, "id")));
        assertEquals(1, actual.getSize());
        assertEquals("Mother", actual.getContent().get(0).getName());

        actual = personViewRepository.findByIdIsNotNull(actual.nextPageable());
        assertEquals(1, actual.getSize());
        assertEquals("John Doe", actual.getContent().get(0).getName());
    }

    @Test
    public void testFindByNamePaginatedEntity() {
        // we do not test DeltaSpike's findAll(int, int) method here because its results are non-deterministic
        List<Person> actual = personRepository.findByNameLike("John %", new PageRequest(0, 1, Sort.Direction.ASC, "id"));
        assertTrue(actual instanceof PagedList<?>);
        assertEquals(1, actual.size());
        assertEquals("John Doe", actual.get(0).getName());

        actual = personRepository.findByNameLike("John %", new PageRequest(1, 1, Sort.Direction.ASC, "id"));
        assertEquals(1, actual.size());
        assertEquals("John Smith", actual.get(0).getName());
    }

    @Test
    public void testFindSliceEntity() {
        Slice<Person> actual = personRepository.findByIdIsNotNull(new PageRequest(0, 1, Sort.Direction.ASC, "id"));
        assertEquals(1, actual.getSize());
        assertEquals("Mother", actual.getContent().get(0).getName());

        actual = personRepository.findByIdIsNotNull(new PageRequest(1, 1, Sort.Direction.ASC, "id"));
        assertEquals(1, actual.getSize());
        assertEquals("John Doe", actual.getContent().get(0).getName());
    }

    @Test
    public void testFindByNameKeysetPaginatedEntity() {
        // we do not test DeltaSpike's findAll(int, int) method here because its results are non-deterministic
        Page<Person> actual = personRepository.findByNameLike("John %", new KeysetPageRequest(null, new PageRequest(0, 1, Sort.Direction.ASC, "id")));
        assertTrue(actual instanceof KeysetAwarePage<?>);
        assertEquals(1, actual.getNumberOfElements());
        assertEquals("John Doe", actual.getContent().get(0).getName());

        actual = personRepository.findByNameLike("John %", (KeysetPageable) actual.nextPageable());
        assertEquals(1, actual.getNumberOfElements());
        assertEquals("John Smith", actual.getContent().get(0).getName());
    }

    @Test
    public void testFindSliceKeysetEntity() {
        Slice<Person> actual = personRepository.findByIdIsNotNull(new KeysetPageRequest(null, new PageRequest(0, 1, Sort.Direction.ASC, "id")));
        assertEquals(1, actual.getSize());
        assertEquals("Mother", actual.getContent().get(0).getName());

        actual = personRepository.findByIdIsNotNull(actual.nextPageable());
        assertEquals(1, actual.getSize());
        assertEquals("John Doe", actual.getContent().get(0).getName());
    }

    @Test
    public void testFindBy() {
        List<PersonView> expected = Arrays.asList(
                fetch(PersonView.class, persons[2].getId()),
                fetch(PersonView.class, persons[4].getId())
        );
        Person example = new Person();
        example.setPosition(4);

        // we do not test DeltaSpike's findBy(E, SingularAttribute<E,?>...) method here because its results are non-deterministic
        assertEquals(expected, personViewRepository.findBy(example, Person_.position));
    }

    @Test
    public void testFindByPaginated() {
        Person example = new Person();
        example.setPosition(4);

        // we do not test DeltaSpike's findBy(E, int, int, SingularAttribute<E,?>...) method here because its results are non-deterministic
        List<PersonView> page2 = personViewRepository.findBy(example, 1, 1, Person_.position);
        assertEquals(1, page2.size());
        assertEquals(persons[4].getId(), page2.get(0).getId());

        List<PersonView> page1 = personViewRepository.findBy(example, 0, 1, Person_.position);
        assertEquals(1, page1.size());
        assertEquals(persons[2].getId(), page1.get(0).getId());
    }

    @Test
    public void testFindByLike() {
        List<PersonView> expected = Arrays.asList(
                fetch(PersonView.class, persons[1].getId()),
                fetch(PersonView.class, persons[4].getId())
        );
        Person example = new Person();
        example.setName("John %");

        // we do not test DeltaSpike's findByLike(E, SingularAttribute<E,?>...) method here because its results are non-deterministic
        assertEquals(expected, personViewRepository.findByLike(example, Person_.name));
    }

    @Test
    public void testFindByLikePaginated() {
        Person example = new Person();
        example.setName("John %");

        // we do not test DeltaSpike's findByLike(E, int, int, SingularAttribute<E,?>...) method here because its results are non-deterministic
        List<PersonView> page2 = personViewRepository.findByLike(example, 1, 1, Person_.name);
        assertEquals(1, page2.size());
        assertEquals(persons[4].getId(), page2.get(0).getId());

        List<PersonView> page1 = personViewRepository.findByLike(example, 0, 1, Person_.name);
        assertEquals(1, page1.size());
        assertEquals(persons[1].getId(), page1.get(0).getId());
    }
}