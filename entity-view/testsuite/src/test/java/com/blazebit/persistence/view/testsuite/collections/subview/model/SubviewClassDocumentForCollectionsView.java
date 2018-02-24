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

package com.blazebit.persistence.view.testsuite.collections.subview.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.ViewConstructor;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(DocumentForCollections.class)
public abstract class SubviewClassDocumentForCollectionsView {
    
    public static final String STRING_MAP_SET_LIST_CONSTRUCTOR = "STRING_MAP_SET_LIST_CONSTRUCTOR";
    public static final String STRING_MAP_LIST_SET_CONSTRUCTOR = "STRING_MAP_LIST_SET_CONSTRUCTOR";
    public static final String STRING_SET_MAP_LIST_CONSTRUCTOR = "STRING_SET_MAP_LIST_CONSTRUCTOR";
    public static final String STRING_SET_LIST_MAP_CONSTRUCTOR = "STRING_SET_LIST_MAP_CONSTRUCTOR";
    public static final String STRING_LIST_SET_MAP_CONSTRUCTOR = "STRING_LIST_SET_MAP_CONSTRUCTOR";
    public static final String STRING_LIST_MAP_SET_CONSTRUCTOR = "STRING_LIST_MAP_SET_CONSTRUCTOR";
    
    public static final String MAP_SET_STRING_LIST_CONSTRUCTOR = "MAP_SET_STRING_LIST_CONSTRUCTOR";
    public static final String MAP_SET_LIST_STRING_CONSTRUCTOR = "MAP_SET_LIST_STRING_CONSTRUCTOR";
    public static final String MAP_LIST_SET_STRING_CONSTRUCTOR = "MAP_LIST_SET_STRING_CONSTRUCTOR";
    public static final String MAP_LIST_STRING_SET_CONSTRUCTOR = "MAP_LIST_STRING_SET_CONSTRUCTOR";
    public static final String MAP_STRING_SET_LIST_CONSTRUCTOR = "MAP_STRING_SET_LIST_CONSTRUCTOR";
    public static final String MAP_STRING_LIST_SET_CONSTRUCTOR = "MAP_STRING_LIST_SET_CONSTRUCTOR";
    
    public static final String LIST_MAP_STRING_SET_CONSTRUCTOR = "LIST_MAP_STRING_SET_CONSTRUCTOR";
    public static final String LIST_MAP_SET_STRING_CONSTRUCTOR = "LIST_MAP_SET_STRING_CONSTRUCTOR";
    public static final String LIST_SET_MAP_STRING_CONSTRUCTOR = "LIST_SET_MAP_STRING_CONSTRUCTOR";
    public static final String LIST_SET_STRING_MAP_CONSTRUCTOR = "LIST_SET_STRING_MAP_CONSTRUCTOR";
    public static final String LIST_STRING_SET_MAP_CONSTRUCTOR = "LIST_STRING_SET_MAP_CONSTRUCTOR";
    public static final String LIST_STRING_MAP_SET_CONSTRUCTOR = "LIST_STRING_MAP_SET_CONSTRUCTOR";
    
    public static final String SET_MAP_LIST_STRING_CONSTRUCTOR = "SET_MAP_LIST_STRING_CONSTRUCTOR";
    public static final String SET_MAP_STRING_LIST_CONSTRUCTOR = "SET_MAP_STRING_LIST_CONSTRUCTOR";
    public static final String SET_LIST_MAP_STRING_CONSTRUCTOR = "SET_LIST_MAP_STRING_CONSTRUCTOR";
    public static final String SET_LIST_STRING_MAP_CONSTRUCTOR = "SET_LIST_STRING_MAP_CONSTRUCTOR";
    public static final String SET_STRING_LIST_MAP_CONSTRUCTOR = "SET_STRING_LIST_MAP_CONSTRUCTOR";
    public static final String SET_STRING_MAP_LIST_CONSTRUCTOR = "SET_STRING_MAP_LIST_CONSTRUCTOR";
    
    private final String name;
    private final Map<Integer, SubviewPersonForCollectionsView> contacts;
    private final Set<SubviewPersonForCollectionsView> partners;
    private final List<SubviewPersonForCollectionsView> personList;
    
    @ViewConstructor(STRING_MAP_SET_LIST_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("name") String name,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(STRING_MAP_LIST_SET_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("name") String name,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(STRING_SET_MAP_LIST_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("name") String name,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(STRING_SET_LIST_MAP_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("name") String name,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(STRING_LIST_SET_MAP_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("name") String name,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(STRING_LIST_MAP_SET_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("name") String name,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(MAP_SET_STRING_LIST_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("name") String name,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(MAP_SET_LIST_STRING_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("name") String name
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(MAP_LIST_SET_STRING_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("name") String name
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(MAP_LIST_STRING_SET_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("name") String name,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(MAP_STRING_SET_LIST_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("name") String name,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(MAP_STRING_LIST_SET_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("name") String name,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(LIST_MAP_STRING_SET_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("name") String name,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(LIST_MAP_SET_STRING_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("name") String name
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(LIST_SET_MAP_STRING_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("name") String name
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(LIST_SET_STRING_MAP_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("name") String name,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(LIST_STRING_SET_MAP_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("name") String name,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(LIST_STRING_MAP_SET_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("name") String name,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(SET_MAP_LIST_STRING_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("name") String name
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(SET_MAP_STRING_LIST_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("name") String name,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(SET_LIST_MAP_STRING_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("name") String name
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(SET_LIST_STRING_MAP_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("name") String name,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(SET_STRING_LIST_MAP_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("name") String name,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @ViewConstructor(SET_STRING_MAP_LIST_CONSTRUCTOR)
    public SubviewClassDocumentForCollectionsView(
        @Mapping("partners") Set<SubviewPersonForCollectionsView> partners,
        @Mapping("name") String name,
        @Mapping("contacts") Map<Integer, SubviewPersonForCollectionsView> contacts,
        @Mapping("personList") List<SubviewPersonForCollectionsView> personList
    ) {
        this.name = name;
        this.contacts = contacts;
        this.partners = partners;
        this.personList = personList;
    }
    
    @IdMapping
    public abstract Long getId();

    public String getName() {
        return name;
    }

    public Map<Integer, SubviewPersonForCollectionsView> getContacts() {
        return contacts;
    }

    public Set<SubviewPersonForCollectionsView> getPartners() {
        return partners;
    }

    public List<SubviewPersonForCollectionsView> getPersonList() {
        return personList;
    }
}
