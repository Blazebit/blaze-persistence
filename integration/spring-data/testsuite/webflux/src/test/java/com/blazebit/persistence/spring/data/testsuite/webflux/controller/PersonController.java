/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webflux.controller;

import com.blazebit.persistence.spring.data.testsuite.webflux.repository.ModificationPersonRepository;
import com.blazebit.persistence.spring.data.testsuite.webflux.repository.PersonRepository;
import com.blazebit.persistence.spring.data.testsuite.webflux.view.PersonUpdateView;
import com.blazebit.persistence.spring.data.testsuite.webflux.view.PersonView;
import com.blazebit.persistence.spring.data.webflux.EntityViewId;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@RestController
public class PersonController {

    private final PersonRepository personRepository;
    private final ModificationPersonRepository modificationPersonRepository;

    public PersonController(PersonRepository personRepository, ModificationPersonRepository modificationPersonRepository) {
        this.personRepository = personRepository;
        this.modificationPersonRepository = modificationPersonRepository;
    }

    @PutMapping(
            value = "/persons/{id1}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PersonView> updatePerson(@EntityViewId("id1") @RequestBody PersonUpdateView personUpdate) {
        return updatePerson0(personUpdate);
    }

    private ResponseEntity<PersonView> updatePerson0(PersonUpdateView personUpdate) {
        modificationPersonRepository.updatePerson(personUpdate);
        PersonView personView = personRepository.findOne(personUpdate.getId().toString());
        return ResponseEntity.ok(personView);
    }
}
