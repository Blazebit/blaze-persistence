/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.controller;

import com.blazebit.persistence.spring.data.testsuite.webmvc.repository.PersonRepository;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.PersonUpdateView;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.PersonView;
import com.blazebit.persistence.spring.data.webmvc.EntityViewId;
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

    public PersonController(PersonRepository personRepository) {
        this.personRepository = personRepository;
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
        personRepository.updatePerson(personUpdate);
        PersonView personView = personRepository.findOne(personUpdate.getId().toString());
        return ResponseEntity.ok(personView);
    }
}
