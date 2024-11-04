/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.customer.entity;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public class ServiceContractFilter
        implements Serializable, Specification<ServiceContract> {

    private String id;

    private String customerId;

    private String customerName;

    private String customerStreet;

    private String customerCity;

    private String serviceItemSerialNumber;

    private boolean onlyActive;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return this.customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerStreet() {
        return this.customerStreet;
    }

    public void setCustomerStreet(String customerStreet) {
        this.customerStreet = customerStreet;
    }

    public String getCustomerCity() {
        return this.customerCity;
    }

    public void setCustomerCity(String customerCity) {
        this.customerCity = customerCity;
    }

    public String getServiceItemSerialNumber() {
        return this.serviceItemSerialNumber;
    }

    public void setServiceItemSerialNumber(String serviceItemSerialNumber) {
        this.serviceItemSerialNumber = serviceItemSerialNumber;
    }

    public boolean isOnlyActive() {
        return this.onlyActive;
    }

    public void setOnlyActive(boolean onlyActive) {
        this.onlyActive = onlyActive;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.customerId, this.customerName,
                this.customerStreet, this.customerCity,
                this.serviceItemSerialNumber, this.onlyActive);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceContractFilter) {
            ServiceContractFilter other = (ServiceContractFilter) obj;
            return Objects.equals(this.id, other.id)
                    && Objects.equals(this.customerId, other.customerId)
                    && Objects.equals(this.customerName, other.customerName)
                    && Objects.equals(this.customerStreet, other.customerStreet)
                    && Objects.equals(this.customerCity, other.customerCity)
                    && Objects.equals(this.serviceItemSerialNumber,
                            other.serviceItemSerialNumber)
                    && Objects.equals(this.onlyActive, other.onlyActive);
        } else {
            return false;
        }
    }

    @Override
    public Predicate toPredicate(Root<ServiceContract> root,
            CriteriaQuery<?> query, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();
        query.distinct(true);
        Path<AbstractCustomer> customer = root.get(ServiceContract_.customer);
        ListJoin<ServiceContract, ShippingAddress> addresses = root.join(ServiceContract_.addresses, JoinType.LEFT);
        if (this.customerCity != null && !"".equals(this.customerCity)) {
            Predicate customerPredicate = builder.equal(
                    customer.get(AbstractCustomer_.city), this.customerCity);
            Predicate addressPredicate = builder.equal(
                    addresses.get(ShippingAddress_.city), this.customerCity);
            predicates.add(builder.or(customerPredicate, addressPredicate));
        }
        return predicates.stream().reduce(builder::and)
                .orElseGet(builder::conjunction);
    }

}
