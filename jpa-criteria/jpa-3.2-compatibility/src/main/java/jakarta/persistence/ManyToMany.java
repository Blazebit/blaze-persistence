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
 * Specifies a many-valued association with many-to-many multiplicity,
 * mapping to an intermediate table called the <em>join table</em>.
 *
 * <p>Every many-to-many association has two sides, the owning side
 * and the non-owning, or inverse, side. The join table is specified
 * on the owning side. If the association is bidirectional, either
 * side may be designated as the owning side, and the non-owning side
 * must use the {@link #mappedBy} element of the {@code ManyToMany}
 * annotation to specify the relationship field or property of the
 * owning side.
 *
 * <p>The join table for the relationship, if not defaulted, is
 * specified on the owning side. The {@link JoinTable} annotation
 * specifies a mapping to a database table.
 *
 * <p>The {@code ManyToMany} annotation may be used within an
 * embeddable class contained within an entity class to specify a
 * relationship to a collection of entities. If the relationship is
 * bidirectional and the entity containing the embeddable class is
 * the owner of the relationship, the non-owning side must use the
 * {@link #mappedBy} element of the {@code ManyToMany} annotation to
 * specify the relationship field or property of the embeddable class.
 * The dot ({@code .}) notation syntax must be used in the
 * {@code mappedBy} element to indicate the relationship attribute
 * within the embedded attribute. The value of each identifier used
 * with the dot notation is the name of the respective embedded field
 * or property.
 *
 * <p>Example 1:
 * {@snippet :
 * // In Customer class:
 *
 * @ManyToMany
 * @JoinTable(name = "CUST_PHONES")
 * public Set<PhoneNumber> getPhones() { return phones; }
 *
 * // In PhoneNumber class:
 *
 * @ManyToMany(mappedBy = "phones")
 * public Set<Customer> getCustomers() { return customers; }
 * }
 *
 * <p>Example 2:
 * {@snippet :
 * // In Customer class:
 *
 * @ManyToMany(targetEntity = com.acme.PhoneNumber.class)
 * public Set getPhones() { return phones; }
 *
 * // In PhoneNumber class:
 *
 * @ManyToMany(targetEntity = com.acme.Customer.class, mappedBy = "phones")
 * public Set getCustomers() { return customers; }
 * }
 *
 * <p>Example 3:
 * {@snippet :
 * // In Customer class:
 *
 * @ManyToMany
 * @JoinTable(name = "CUST_PHONE",
 *     joinColumns = @JoinColumn(name = "CUST_ID", referencedColumnName = "ID"),
 *     inverseJoinColumns = @JoinColumn(name = "PHONE_ID", referencedColumnName = "ID"))
 * public Set<PhoneNumber> getPhones() { return phones; }
 *
 * // In PhoneNumberClass:
 *
 * @ManyToMany(mappedBy = "phones")
 * public Set<Customer> getCustomers() { return customers; }
 * }
 *
 * @see JoinTable
 *
 * @since 1.0
 */
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface ManyToMany {

    /**
     * (Optional) The entity class that is the target of the
     * association. Optional only if the collection-valued
     * relationship property is defined using Java generics.
     * Must be specified otherwise.
     *
     * <p> Defaults to the parameterized type of
     * the collection when defined using generics.
     */
    Class<?> targetEntity() default void.class;

    /** 
     * (Optional) The operations that must be cascaded to the
     * target of the association.
     *
     * <p> When the target collection is a {@link java.util.Map},
     * the {@code cascade} element applies to the map value.
     *
     * <p> Defaults to no operations being cascaded.
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
     * <p>If not specified, defaults to {@code LAZY}.
     */
    FetchType fetch() default FetchType.LAZY;

    /** 
     * The field that owns the relationship. Required unless 
     * the relationship is unidirectional.
     */
    String mappedBy() default "";
}
