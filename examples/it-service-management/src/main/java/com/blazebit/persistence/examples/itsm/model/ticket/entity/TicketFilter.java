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

package com.blazebit.persistence.examples.itsm.model.ticket.entity;

import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.examples.itsm.model.common.entity.User_;
import com.blazebit.persistence.examples.itsm.model.customer.entity.AbstractCustomer;
import com.blazebit.persistence.examples.itsm.model.customer.entity.AbstractCustomer_;
import com.blazebit.persistence.examples.itsm.model.customer.entity.Customer_;
import com.blazebit.persistence.examples.itsm.model.customer.entity.ShippingAddress;
import com.blazebit.persistence.examples.itsm.model.customer.entity.ShippingAddress_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public class TicketFilter implements Specification<Ticket>, Serializable {

    private String number;

    private String content;

    private boolean onlySubject;

    private boolean includeComments;

    private boolean onlyActive;

    private boolean unseenOnly;

    private String customer;

    private boolean includeShippingAddresses;

    private String businessUnit;

    private String status;

    private String queue;

    private String category;

    private Instant createdFrom;

    private Instant createdUntil;

    private Long author;

    private Long reporter;

    private Long assignee;

    private String authoringMediumReference;

    private String reportingMediumReference;

    private String customerTicketReference;

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isOnlySubject() {
        return this.onlySubject;
    }

    public void setOnlySubject(boolean onlySubject) {
        this.onlySubject = onlySubject;
    }

    public boolean isIncludeComments() {
        return this.includeComments;
    }

    public void setIncludeComments(boolean includeComments) {
        this.includeComments = includeComments;
    }

    public boolean isOnlyActive() {
        return this.onlyActive;
    }

    public void setOnlyActive(boolean onlyActive) {
        this.onlyActive = onlyActive;
    }

    public boolean isUnseenOnly() {
        return this.unseenOnly;
    }

    public void setUnseenOnly(boolean unseenOnly) {
        this.unseenOnly = unseenOnly;
    }

    public String getCustomer() {
        return this.customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public boolean isIncludeShippingAddresses() {
        return this.includeShippingAddresses;
    }

    public void setIncludeShippingAddresses(boolean includeShippingAddresses) {
        this.includeShippingAddresses = includeShippingAddresses;
    }

    public String getBusinessUnit() {
        return this.businessUnit;
    }

    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQueue() {
        return this.queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Instant getCreatedFrom() {
        return this.createdFrom;
    }

    public void setCreatedFrom(Instant createdFrom) {
        this.createdFrom = createdFrom;
    }

    public Instant getCreatedUntil() {
        return this.createdUntil;
    }

    public void setCreatedUntil(Instant createdUntil) {
        this.createdUntil = createdUntil;
    }

    public Long getAuthor() {
        return this.author;
    }

    public void setAuthor(Long author) {
        this.author = author;
    }

    public Long getReporter() {
        return this.reporter;
    }

    public void setReporter(Long reporter) {
        this.reporter = reporter;
    }

    public Long getAssignee() {
        return this.assignee;
    }

    public void setAssignee(Long assignee) {
        this.assignee = assignee;
    }

    public String getAuthoringMediumReference() {
        return this.authoringMediumReference;
    }

    public void setAuthoringMediumReference(String authoringMediumReference) {
        this.authoringMediumReference = authoringMediumReference;
    }

    public String getReportingMediumReference() {
        return this.reportingMediumReference;
    }

    public void setReportingMediumReference(String reportingMediumReference) {
        this.reportingMediumReference = reportingMediumReference;
    }

    public String getCustomerTicketReference() {
        return this.customerTicketReference;
    }

    public void setCustomerTicketReference(String customerTicketReference) {
        this.customerTicketReference = customerTicketReference;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.assignee, this.author,
                this.authoringMediumReference, this.businessUnit, this.category,
                this.content, this.createdFrom, this.createdUntil,
                this.customer, this.customerTicketReference,
                this.includeComments, this.includeShippingAddresses,
                this.number, this.onlyActive, this.onlySubject, this.queue,
                this.reporter, this.reportingMediumReference, this.status,
                this.unseenOnly);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TicketFilter)) {
            return false;
        }
        TicketFilter other = (TicketFilter) obj;
        return Objects.equals(this.assignee, other.assignee)
                && Objects.equals(this.author, other.author)
                && Objects.equals(this.authoringMediumReference,
                        other.authoringMediumReference)
                && Objects.equals(this.businessUnit, other.businessUnit)
                && Objects.equals(this.category, other.category)
                && Objects.equals(this.content, other.content)
                && Objects.equals(this.createdFrom, other.createdFrom)
                && Objects.equals(this.createdUntil, other.createdUntil)
                && Objects.equals(this.customer, other.customer)
                && Objects.equals(this.customerTicketReference,
                        other.customerTicketReference)
                && this.includeComments == other.includeComments
                && this.includeShippingAddresses == other.includeShippingAddresses
                && Objects.equals(this.number, other.number)
                && this.onlyActive == other.onlyActive
                && this.onlySubject == other.onlySubject
                && Objects.equals(this.queue, other.queue)
                && Objects.equals(this.reporter, other.reporter)
                && Objects.equals(this.reportingMediumReference,
                        other.reportingMediumReference)
                && Objects.equals(this.status, other.status)
                && this.unseenOnly == other.unseenOnly;
    }

    @Override
    public Predicate toPredicate(Root<Ticket> root, CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (this.onlyActive) {
            predicates.add(criteriaBuilder.isTrue(root.get(Ticket_.open)));
        }

        if (!StringUtils.isEmpty(this.customer)) {
            Path<AbstractCustomer> customerPath = root.get(Ticket_.customer);
            Path<Long> customerIdPath = customerPath.get(AbstractCustomer_.id);
            Predicate customerPredicate = criteriaBuilder.equal(customerIdPath,
                    this.customer);
            if (this.includeShippingAddresses) {
                Expression<Class<? extends Ticket>> customerType = root.type();
                Path<Long> addressCustomerIdPath = criteriaBuilder
                    .treat(customerPath, ShippingAddress.class)
                    .get(ShippingAddress_.customer).get(Customer_.id);
                customerPredicate = criteriaBuilder.or(customerPredicate,
                        criteriaBuilder.and(
                                criteriaBuilder.equal(customerType,
                                        ShippingAddress.class),
                                criteriaBuilder.equal(addressCustomerIdPath,
                                        this.customer)));
            }
            predicates.add(customerPredicate);
        }

        if (this.createdFrom != null) {
            Path<Instant> path = root.get(Ticket_.creationInstant);
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(path,
                    this.createdFrom));
        }

        if (this.createdUntil != null) {
            Path<Instant> path = root.get(Ticket_.creationInstant);
            predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(path, this.createdUntil));
        }

        if (this.author != null) {
            Path<Long> path = root.get(Ticket_.author).get(User_.id);
            predicates.add(criteriaBuilder.equal(path, this.author));
        }

        if (this.assignee != null) {
            Path<Long> path = root.get(Ticket_.assignee).get(User_.id);
            predicates.add(criteriaBuilder.equal(path, this.assignee));
        }

        return predicates.stream().reduce(criteriaBuilder::and)
            .orElseGet(criteriaBuilder::conjunction);
    }

}
