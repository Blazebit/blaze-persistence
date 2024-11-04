/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.customer.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SecondaryTable;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.type.SqlTypes;

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

    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
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
