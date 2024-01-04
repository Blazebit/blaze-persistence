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

package com.blazebit.persistence.examples.itsm.model.customer.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.SecondaryTable;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
@DiscriminatorColumn
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@SecondaryTable(name = AbstractCustomer.SERVICE_DETAIL)
public abstract class AbstractCustomer extends Company {

    static final String SERVICE_DETAIL = "abstract_customer_service_detail";

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = ServiceContract_.CUSTOMER)
    private List<ServiceContract> serviceContracts = new ArrayList<>();

    @Embedded
    // @formatter:off
    @AttributeOverride(name = "serviceHours", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "serviceNote", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "installationDate", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "switchboard", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "switchboardRelease", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "switchboardVersion", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "number", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "password", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "cpuCode", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "greetingSystem", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "switchboardNote", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "vpnType", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "vpnAddress", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "vpnUsername", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "vpnPassword", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "vpnNote", column = @Column(table = SERVICE_DETAIL))
    @AttributeOverride(name = "technician", column = @Column(table = SERVICE_DETAIL))
    // @formatter:on
    private ServiceDetail serviceDetail = new ServiceDetail();

    public Long getId() {
        return this.id;
    }

    public List<ServiceContract> getServiceContracts() {
        return this.serviceContracts;
    }

    public ServiceDetail getServiceDetail() {
        return this.serviceDetail;
    }

}
