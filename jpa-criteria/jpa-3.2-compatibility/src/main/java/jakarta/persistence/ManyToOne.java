/*
 * Copyright (c) 2008, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a single-valued association to another entity class that
 * has many-to-one multiplicity. It is not usually necessary to specify
 * the target entity explicitly since it can usually be inferred from
 * the type of the object being referenced.
 *
 * <p>If the relationship is bidirectional, the non-owning
 * {@link OneToMany} entity side must use the {@link OneToMany#mappedBy
 * mappedBy} element to specify the relationship field or property of the
 * entity that is the owner of the relationship.
 *
 * <p>A {@code ManyToOne} association usually maps a foreign key column
 * or columns. This mapping may be specified using the {@link JoinColumn}
 * annotation. Alternatively, an optional {@code OneToOne} association is
 * sometimes mapped to a join table using the {@link JoinTable} annotation.
 *
 * <p>The {@code ManyToOne} annotation may be used within an embeddable
 * class to specify a relationship from the embeddable class to an entity
 * class. If the relationship is bidirectional, the non-owning
 * {@link OneToMany} entity side must use the {@code mappedBy} element of
 * the {@code OneToMany} annotation to specify the relationship field or
 * property of the embeddable field or property on the owning side of the
 * relationship. The dot ({@code .}) notation syntax must be used in the
 * {@code mappedBy} element to indicate the relationship attribute within
 * the embedded attribute. The value of each identifier used with the dot
 * notation is the name of the respective embedded field or property.
 *
 * <p>Example 1:
 * {@snippet :
 * @ManyToOne(optional = false)
 * @JoinColumn(name = "CUST_ID", nullable = false, updatable = false)
 * public Customer getCustomer() { return customer; }
 * }
 *
 * <p>Example 2:
 * {@snippet :
 * @Entity
 * public class Employee {
 *     @Id
 *     int id;
 *     @Embedded
 *     JobInfo jobInfo;
 *     ...
 * }
 *
 * @Embeddable
 * public class JobInfo {
 *     String jobDescription;
 *     @ManyToOne
 *     ProgramManager pm; // Bidirectional
 * }
 *
 * @Entity
 * public class ProgramManager {
 *     @Id
 *     int id;
 *     @OneToMany(mappedBy = "jobInfo.pm")
 *     Collection<Employee> manages;
 * }
 * }
 *
 * @since 1.0
 */
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface ManyToOne {

    /** 
     * (Optional) The entity class that is the target of 
     * the association. 
     *
     * <p>Defaults to the type of the field or property
     * that stores the association. 
     */
    Class<?> targetEntity() default void.class;

    /**
     * (Optional) The operations that must be cascaded to 
     * the target of the association.
     *
     * <p>By default no operations are cascaded.
     */
    CascadeType[] cascade() default {};

    /** 
     * (Optional) Whether the association should be lazily 
     * loaded or must be eagerly fetched.
     * <ul>
     * <li>The {@link FetchType#EAGER EAGER} strategy is a
     *     requirement on the persistence provider runtime
     *     that the associated entity must be eagerly fetched.
     * <li>The {@link FetchType#LAZY LAZY} strategy is a hint
     *     to the persistence provider runtime.
     * </ul>
     *
     * <p>If not specified, defaults to {@code EAGER}.
     */
    FetchType fetch() default FetchType.EAGER;

    /** 
     * (Optional) Whether the association is optional. If set 
     * to false then a non-null relationship must always exist.
     *
     * <p>May be used in schema generation to infer that the
     * mapped foreign key column is {@link JoinColumn#nullable
     * not null}.
     */
    boolean optional() default true;
}
