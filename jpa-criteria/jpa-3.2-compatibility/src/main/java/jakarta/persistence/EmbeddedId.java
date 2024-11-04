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
 * Specifies that the annotated persistent field or property
 * of an entity class or mapped superclass is the composite
 * primary key of the entity. The type of the annotated field
 * or property must be an {@linkplain Embeddable embeddable}
 * type, and must be explicitly annotated {@link Embeddable}.
 *
 * <p>If a field or property of an entity class is annotated
 * {@code EmbeddedId}, then no other field or property of the
 * entity may be annotated {@link Id} or {@code EmbeddedId},
 * and the entity class must not declare an {@link IdClass}.
 *
 * <p>The embedded primary key type must implement
 * {@link #equals} and {@link #hashCode}, defining value
 * equality consistently with equality of the mapped primary
 * key of the database table.
 *
 * <p>The {@link AttributeOverride} annotation may be used to
 * override the column mappings declared within the embeddable
 * class.
 * 
 * <p>The {@link MapsId} annotation may be used in conjunction
 * with the {@code EmbeddedId} annotation to declare a derived
 * primary key.
 *
 * <p>If the entity has a derived primary key, the
 * {@link AttributeOverride} annotation may only be used to
 * override those attributes of the embedded id that do not
 * correspond to the relationship to the parent entity.
 *
 * <p>Relationship mappings defined within an embedded primary
 * key type are not supported.
 *
 * <p>Example 1:
 * {@snippet :
 * @Entity
 * public class Employee {
 *     @EmbeddedId
 *     protected EmployeePK empPK;
 * ...
 * }
 *
 * public record EmployeePK(String empName, Date birthDay) {}
 * }
 *
 * <p>Example 2:
 * {@snippet :
 * @Embeddable
 * public class DependentId {
 *     String name;
 *     EmployeeId empPK;   // corresponds to primary key type of Employee
 * }
 *
 * @Entity
 * public class Dependent {
 *     // default column name for "name" attribute is overridden
 *     @AttributeOverride(name = "name", column = @Column(name = "dep_name"))
 *     @EmbeddedId
 *     DependentId id;
 *     ...
 *     @MapsId("empPK")
 *     @ManyToOne
 *     Employee emp;
 * }
 * }
 *
 * @see Embeddable
 * @see MapsId
 * @see IdClass
 *
 * @since 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface EmbeddedId {}
