/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.view.testsuite.timeentity;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 *
 * @author Christian Beikov
 * @since 1.6.11
 */
@Entity
@Table(name = "doc_multiset")
public class DocumentForMultisetFetch implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Instant theInstant;
    private LocalDate theLocalDate;
    private LocalDateTime theLocalDateTime;
    private LocalTime theLocalTime;
    private OffsetDateTime theOffsetDateTime;
    private OffsetTime theOffsetTime;
    private ZonedDateTime theZonedDateTime;
    private Date theDate;
    private Time theTime;
    private Timestamp theTimestamp;
    private PersonForMultisetFetch owner;
    private Set<PersonForMultisetFetch> partners = new HashSet<PersonForMultisetFetch>();
    private Map<Integer, PersonForMultisetFetch> contacts = new HashMap<Integer, PersonForMultisetFetch>();
    private List<PersonForMultisetFetch> personList = new ArrayList<PersonForMultisetFetch>();

    public DocumentForMultisetFetch() {
    }

    public DocumentForMultisetFetch(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    public PersonForMultisetFetch getOwner() {
        return owner;
    }

    public void setOwner(PersonForMultisetFetch owner) {
        this.owner = owner;
    }

    @OneToMany(mappedBy = "partnerDocument")
    public Set<PersonForMultisetFetch> getPartners() {
        return partners;
    }

    public void setPartners(Set<PersonForMultisetFetch> partners) {
        this.partners = partners;
    }

    @OneToMany
    @MapKeyColumn(name = "doc_coll_contacts_key", nullable = false)
    @JoinTable(name = "doc_coll_contacts", joinColumns = @JoinColumn(name = "id"), inverseJoinColumns = @JoinColumn(name = "person_id"))
    public Map<Integer, PersonForMultisetFetch> getContacts() {
        return contacts;
    }

    public void setContacts(Map<Integer, PersonForMultisetFetch> contacts) {
        this.contacts = contacts;
    }

    @OneToMany
    @OrderColumn(name = "position", nullable = false)
    @JoinTable(name = "personlist", joinColumns = @JoinColumn(name = "id"), inverseJoinColumns = @JoinColumn(name = "person_id"))
    public List<PersonForMultisetFetch> getPersonList() {
        return personList;
    }

    public void setPersonList(List<PersonForMultisetFetch> personList) {
        this.personList = personList;
    }

    public Instant getTheInstant() {
        return theInstant;
    }

    public void setTheInstant(Instant theInstant) {
        this.theInstant = theInstant;
    }

    public LocalDate getTheLocalDate() {
        return theLocalDate;
    }

    public void setTheLocalDate(LocalDate dateCollected) {
        this.theLocalDate = dateCollected;
    }

    public LocalDateTime getTheLocalDateTime() {
        return theLocalDateTime;
    }

    public void setTheLocalDateTime(LocalDateTime theLocalDateTime) {
        this.theLocalDateTime = theLocalDateTime;
    }

    public LocalTime getTheLocalTime() {
        return theLocalTime;
    }

    public void setTheLocalTime(LocalTime theLocalTime) {
        this.theLocalTime = theLocalTime;
    }

    public OffsetDateTime getTheOffsetDateTime() {
        return theOffsetDateTime;
    }

    public void setTheOffsetDateTime(OffsetDateTime theOffsetDateTime) {
        this.theOffsetDateTime = theOffsetDateTime;
    }

    public OffsetTime getTheOffsetTime() {
        return theOffsetTime;
    }

    public void setTheOffsetTime(OffsetTime theOffsetTime) {
        this.theOffsetTime = theOffsetTime;
    }

    public ZonedDateTime getTheZonedDateTime() {
        return theZonedDateTime;
    }

    public void setTheZonedDateTime(ZonedDateTime theZonedDateTime) {
        this.theZonedDateTime = theZonedDateTime;
    }

    public Date getTheDate() {
        return theDate;
    }

    public void setTheDate(Date theDate) {
        this.theDate = theDate;
    }

    public Time getTheTime() {
        return theTime;
    }

    public void setTheTime(Time theTime) {
        this.theTime = theTime;
    }

    public Timestamp getTheTimestamp() {
        return theTimestamp;
    }

    public void setTheTimestamp(Timestamp theTimestamp) {
        this.theTimestamp = theTimestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DocumentForMultisetFetch other = (DocumentForMultisetFetch) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
