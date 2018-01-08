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

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@Entity
public class DocumentWithNullableName extends Ownable implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Person> people = new ArrayList<Person>();

    public DocumentWithNullableName() {
    }

    public DocumentWithNullableName(Long id) {
        super(id);
    }

    public DocumentWithNullableName(String name) {
        this.name = name;
    }

    public DocumentWithNullableName(String name, Person owner) {
        this(name);
        this.setOwner(owner);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany
    @OrderColumn(name = "people_idx", nullable = false)
    @JoinTable(name = "documentnullablename_people")
    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }
}
