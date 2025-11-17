/*
 * Copyright (c) 2008, 2024 Oracle and/or its affiliates. All rights reserved.
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
 * Designates a {@link ManyToOne} or {@link OneToOne} relationship
 * attribute that provides the mapping for an {@link EmbeddedId}
 * primary key, an attribute within an {@code EmbeddedId} primary
 * key, or a simple primary key of the parent entity.
 *
 * <p>The {@link #value} element specifies the attribute within a
 * composite key to which the relationship attribute corresponds.
 * If the primary key of the entity is of the same Java type as
 * the primary key of the entity referenced by the relationship,
 * the {@code value} attribute is not specified.
 *
 * <p>In this example, the parent entity has simple primary key:
 * {@snippet :
 * @Entity
 * public class Employee {
 *     @Id
 *     long empId;
 *     String name;
 *     ...
 * }
 * }
 *
 * <p>And then the dependent entity uses {@link EmbeddedId} to
 * declare its composite primary key:
 * {@snippet :
 * @Embeddable
 * public class DependentId {
 *     String name;
 *     long empid;  // corresponds to primary key type of Employee
 * }
 *
 * @Entity
 * public class Dependent {
 *     @EmbeddedId
 *     DependentId id;
 *     ...
 *     @MapsId("empid")  // maps the empid attribute of embedded id
 *     @ManyToOne
 *     Employee emp;
 * }
 * }
 *
 * <p>
 * If a {@link ManyToOne} or {@link OneToOne} relationship declared by a
 * dependent entity is annotated {@link MapsId}, an instance of the entity
 * cannot be made persistent until the relationship has been assigned a
 * reference to an instance of the parent entity, since the identity of
 * the dependent entity declaring the relationship is derived from the
 * referenced parent entity.
 *
 * @since 2.0
 */
@Target( { METHOD, FIELD })
@Retention(RUNTIME)
public @interface MapsId {

    /**
     * (Optional) The name of the attribute within the composite
     * key to which the relationship attribute corresponds. If
     * not explicitly specified, the relationship maps the primary
     * key of the entity.
     */
   String value() default ""; }
