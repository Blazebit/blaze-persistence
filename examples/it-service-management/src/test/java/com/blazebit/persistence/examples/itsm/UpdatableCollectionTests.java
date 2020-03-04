/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.examples.itsm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import com.blazebit.persistence.examples.itsm.model.common.entity.User;
import com.blazebit.persistence.examples.itsm.model.common.repository.GroupRepository;
import com.blazebit.persistence.examples.itsm.model.common.repository.RoleRepository;
import com.blazebit.persistence.examples.itsm.model.common.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;

import com.blazebit.persistence.view.EntityViewManager;

import com.blazebit.persistence.examples.itsm.model.common.view.GroupDetail;
import com.blazebit.persistence.examples.itsm.model.common.view.RoleDetail;
import com.blazebit.persistence.examples.itsm.model.common.view.UserDetail;

import javax.persistence.EntityManager;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@DataJpaTest(showSql = false)
@ContextConfiguration(classes = BlazeConfiguration.class)
public class UpdatableCollectionTests {

    @Autowired
    private TestEntityManager em;

    @BeforeEach
    public void init() {
        User user1 = new User();
        user1.setName("one");
        this.em.persist(user1);

        User user2 = new User();
        user2.setName("two");
        this.em.persist(user2);

        User user3 = new User();
        user3.setName("three");
        this.em.persist(user3);

        this.em.flush();
        this.em.clear();
    }

    @Test
    public void testMapCollectionSubquery(
            @Autowired UserRepository repository) {
        repository.findByEmailAddress("foo@bar.baz");
    }

    @Test
    public void testAddSingle(@Autowired EntityViewManager evm,
            @Autowired UserRepository userRepository,
            @Autowired GroupRepository groupRepository) {
        EntityManager em = this.em.getEntityManager();
        UserDetail user = userRepository.findByName("one");
        GroupDetail group = evm.create(GroupDetail.class);
        group.getUsers().add(user);
        evm.save(em, group);
        em.flush();
        em.clear();

        int size = groupRepository.findById(group.getId()).get().getUsers()
            .size();
        assertEquals(1, size);

        group = evm.find(em, GroupDetail.class, group.getId());
        group.getUsers().remove(user);
        evm.save(em, group);
        em.flush();
        em.clear();

        size = groupRepository.findById(group.getId()).get().getUsers().size();
        assertEquals(0, size);
    }

    @Test
    public void testAddMultiple(@Autowired EntityViewManager evm,
            @Autowired UserRepository userRepository,
            @Autowired GroupRepository groupRepository) {
        EntityManager em = this.em.getEntityManager();
        UserDetail user1 = userRepository.findByName("one");
        UserDetail user2 = userRepository.findByName("two");
        GroupDetail group = evm.create(GroupDetail.class);
        group.getUsers().addAll(Arrays.asList(user1, user2));
        evm.save(em, group);
        em.flush();
        em.clear();

        int size = groupRepository.findById(group.getId()).get().getUsers()
            .size();
        assertEquals(2, size);

        group = evm.find(em, GroupDetail.class, group.getId());
        group.getUsers().retainAll(Collections.emptyList());
        evm.save(em, group);
        em.flush();
        em.clear();

        size = groupRepository.findById(group.getId()).get().getUsers().size();
        assertEquals(0, size);
    }

    @Test
    public void testInverseAddSingle(@Autowired EntityViewManager evm,
            @Autowired UserRepository userRepository,
            @Autowired RoleRepository roleRepository) {
        EntityManager em = this.em.getEntityManager();
        UserDetail user = userRepository.findByName("one");
        RoleDetail role = evm.create(RoleDetail.class);
        role.getUsers().add(user);
        evm.save(em, role);
        em.flush();
        em.clear();

        int size = roleRepository.findById(role.getId()).get().getUsers()
            .size();
        assertEquals(1, size);

        role = evm.find(em, RoleDetail.class, role.getId());
        role.getUsers().remove(user);
        evm.save(em, role);
        em.flush();
        em.clear();

        size = roleRepository.findById(role.getId()).get().getUsers().size();
        assertEquals(0, size);
    }

    @Test
    public void testInverseAddMultiple(@Autowired EntityViewManager evm,
            @Autowired UserRepository userRepository,
            @Autowired RoleRepository roleRepository) {
        EntityManager em = this.em.getEntityManager();
        UserDetail user1 = userRepository.findByName("one");
        UserDetail user2 = userRepository.findByName("two");
        RoleDetail role = evm.create(RoleDetail.class);
        role.getUsers().addAll(Arrays.asList(user1, user2));
        evm.save(em, role);
        em.flush();
        em.clear();

        int size = roleRepository.findById(role.getId()).get().getUsers()
            .size();
        assertEquals(2, size);

        role = evm.find(em, RoleDetail.class, role.getId());
        role.getUsers().retainAll(Collections.emptyList());
        evm.save(em, role);
        em.flush();
        em.clear();

        size = roleRepository.findById(role.getId()).get().getUsers().size();
        assertEquals(0, size);
    }

    @Test
    public void testInverseAddAll(@Autowired EntityViewManager evm,
            @Autowired UserRepository userRepository,
            @Autowired RoleRepository roleRepository) {
        RoleDetail role = evm.create(RoleDetail.class);
        long id = roleRepository.saveAndFlush(role).getId();

        Set<UserDetail> users = roleRepository.getOne(id).getUsers();

        UserDetail user = evm.create(UserDetail.class);
        userRepository.saveAndFlush(user);

        users.addAll(Set.of(user));
        roleRepository.saveAndFlush(role);

        long size = roleRepository.getOne(id).getUsers().size();
        assertEquals(1, size);
    }

}
