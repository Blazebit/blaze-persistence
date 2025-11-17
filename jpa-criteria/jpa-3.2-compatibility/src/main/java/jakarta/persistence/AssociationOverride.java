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
//     Petros Splinakis - 2.2
//     Linda DeMichiel - 2.0 - Version 2.0 (October 1 - 2013)
//     Specification available from https://projects.eclipse.org/projects/ee4j.jpa

package jakarta.persistence;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static jakarta.persistence.ConstraintMode.PROVIDER_DEFAULT;

/**
 * Used to override a mapping for an entity relationship.
 *
 * <p>May be applied to an entity that extends a mapped superclass
 * to override a relationship mapping defined by the mapped superclass.
 * If not specified, the association is mapped the same as in the
 * original mapping. When used to override a mapping defined by a
 * mapped superclass, {@code AssociationOverride} is applied to the
 * entity class.
 *
 * <p> May be used to override a relationship mapping from an
 * embeddable within an entity to another entity when the embeddable
 * is on the owning side of the relationship. When used to override a
 * relationship mapping defined by an embeddable class (including an
 * embeddable class embedded within another embeddable class),
 * {@code AssociationOverride} is applied to the field or property
 * containing the embeddable.
 *
 * <p> When {@code AssociationOverride} is used to override a
 * relationship mapping from an embeddable class, the {@link #name}
 * element specifies the referencing relationship field or property
 * within the embeddable class. To override mappings at multiple
 * levels of embedding, a dot ({@code .}) notation syntax must be used
 * in the {@code name} element to indicate an attribute within an
 * embedded attribute. The value of each identifier used with the dot
 * notation is the name of the respective embedded field or property.
 * 
 * <p>When {@code AssociationOverride} is applied to override the
 * mappings of an embeddable class used as a map value, "{@code value.}"
 * must be used to prefix the name of the attribute within the embeddable
 * class that is being overridden in order to specify it as part of the
 * map value.
 *
 * <p>If the relationship mapping is a foreign key mapping, the
 * {@link #joinColumns} element is used. If the relationship mapping
 * uses a join table, the {@link #joinTable} element must be specified
 * to override the mapping of the join table and/or its join columns.
 *
 * <p>Example 1: Overriding the mapping of a relationship defined by a
 * mapped superclass
 * {@snippet :
 * @MappedSuperclass
 * public class Employee {
 *     ...
 *     @ManyToOne
 *     protected Address address;
 *     ...
 * }
 *    
 * @Entity
 * @AssociationOverride(name = "address",
 *                      joinColumns = @JoinColumn(name = "ADDR_ID"))
 * // address field mapping overridden to ADDR_ID foreign key
 * public class PartTimeEmployee extends Employee {
 *     ...
 * }
 * }
 *
 * <p>Example 2: Overriding the mapping for {@code phoneNumbers} defined
 * in the {@code ContactInfo} class
 * {@snippet :
 * @Entity
 * public class Employee {
 *     @Id int id;
 *     @AssociationOverride(
 *         name = "phoneNumbers",
 *         joinTable = @JoinTable(name = "EMPPHONES",
 *                       joinColumns = @JoinColumn(name = "EMP"),
 *                       inverseJoinColumns = @JoinColumn(name = "PHONE")))
 *     @Embedded
 *     ContactInfo contactInfo;
 *     ...
 * }
 * 
 * @Embeddable
 * public class ContactInfo {
 *     @ManyToOne
 *     Address address; // Unidirectional
 *     @ManyToMany(targetEntity = PhoneNumber.class)
 *     List phoneNumbers;
 * }
 *
 * @Entity
 * public class PhoneNumber {
 *     @Id
 *     int number;
 *     @ManyToMany(mappedBy = "contactInfo.phoneNumbers")
 *     Collection<Employee> employees;
 * }
 * }
 *
 * @see Embedded
 * @see Embeddable
 * @see MappedSuperclass
 * @see AttributeOverride
 *
 * @since 1.0 
 */
@Repeatable(AssociationOverrides.class)
@Target({TYPE, METHOD, FIELD}) 
@Retention(RUNTIME)

public @interface AssociationOverride {

    /**
     * (Required) The name of the relationship property whose mapping
     * is being overridden if property-based access is being used,
     * or the name of the relationship field if field-based access is
     * used.
     */
    String name();

    /**
     * The join column(s) being mapped to the persistent attribute(s).
     * The {@code joinColumns} elements must be specified if a foreign
     * key mapping is used in the overriding of the mapping of the
     * relationship. The {@code joinColumns} element must not be
     * specified if a join table is used in the overriding of the
     * mapping of the relationship.
     */
    JoinColumn[] joinColumns() default {};

    /**
     * (Optional) Used to specify or control the generation of a
     * foreign key constraint for the columns corresponding to the
     * {@code joinColumns} element when table generation is in effect.
     * If both this element and the {@code foreignKey} element of any
     * of the {@code joinColumns} elements are specified, the behavior
     * is undefined. If no foreign key annotation element is specified
     * in either location, the persistence provider's default foreign
     * key strategy will apply.
     *
     * @since 2.1
     */
    ForeignKey foreignKey() default @ForeignKey(PROVIDER_DEFAULT);

    /**
     * The join table that maps the relationship.
     * The {@code joinTable} element must be specified if a join table 
     * is used in the overriding of the mapping of the relationship.
     * The {@code joinTable} element must not be specified if a foreign
     * key mapping is used in the overriding of the relationship.
     *
     * @since 2.0
     */
    JoinTable joinTable() default @JoinTable;
}
