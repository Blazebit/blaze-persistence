/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.examples.itsm.model.customer.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SecondaryTable;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
@SecondaryTable(name = ServiceContract.DETAIL)
public class ServiceContract {

    static final String DETAIL = "service_contract_detail";

    /**
     * The Enum Status.
     */
    public enum Status {
        NONE, SIGNED, CANCELED;
    }

    /**
     * The Enum ChangeStatus.
     */
    public enum ChangeStatus {
        OPEN, LOCKED;
    }

    @Id
    private String id;

    private String description;

    private Status status;

    private ChangeStatus changeStatus;

    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    private Customer billingCustomer;

    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    private AbstractCustomer customer;

    @ManyToMany
    @JoinTable(name = "service_contract_abstract_customer",
            joinColumns = @JoinColumn(name = "service_contract_id"),
            inverseJoinColumns = @JoinColumn(name = "addresses_id"),
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private List<ShippingAddress> addresses = new ArrayList<>();

    private LocalDate startingDate;

    private LocalDate endingDate;

    private LocalDate firstServiceDate;

    @ManyToMany
    private Set<ServiceItem> serviceItems;

    @Lob
    @Type(type = "text")
    @Column(table = DETAIL)
    private String note;

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public AbstractCustomer getCustomer() {
        return this.customer;
    }

    public void setCustomer(AbstractCustomer customer) {
        this.customer = customer;
    }

    public LocalDate getEndingDate() {
        return this.endingDate;
    }

    public void setEndingDate(LocalDate endingDate) {
        this.endingDate = endingDate;
    }

    public String getId() {
        return this.id;
    }

    public List<ShippingAddress> getAddresses() {
        return this.addresses;
    }

}
