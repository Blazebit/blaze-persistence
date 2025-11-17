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
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Declares a type whose instances are stored as an intrinsic
 * part of an owning entity, sharing the identity of the entity.
 * A single embeddable type may be used as the type of multiple
 * persistent fields or properties, across several entities,
 * and so distinct instances of an embeddable type might have
 * owning entities of completely unrelated entity types.
 *
 * <p>The annotated type must:
 * <ul>
 * <li>be a non-{@code abstract}, non-{@code final} top-level
 *     class or static inner class, or a Java record type,
 * <li>have a {@code public} or {@code protected} constructor
 *     with no parameters, unless it is a record type, and
 * <li>have no {@code final} methods or persistent instance
 *     variables.
 * </ul>
 * <p>An enum or interface may not be designated as an embeddable
 * type.
 *
 * <p>An embeddable class does not have its own table. Instead,
 * the state of an instance is stored in the table or tables
 * mapped by the owning entity.
 *
 * <p>The persistent fields and properties of an embeddable
 * class are mapped using the same mapping annotations used to
 * map {@linkplain Entity entity classes}, and may themselves
 * hold instances of embeddable types. An embeddable class may
 * even declare an association from its owning entity to another
 * entity.
 *
 * <p>However, an embeddable class may not have a field or
 * property annotated {@link Id} or {@link EmbeddedId}.
 *
 * <p>Fields or properties of an embeddable class are persistent
 * by default. The {@link Transient} annotation or the Java
 * {@code transient} keyword must be used to explicitly declare
 * any field or property of an embeddable class which is
 * <em>not</em> persistent.
 *
 * <p>Example 1:
 * {@snippet :
 * @Embeddable
 * public class EmploymentPeriod {
 *     @Temporal(DATE) java.util.Date startDate;
 *     @Temporal(DATE) java.util.Date endDate;
 *     ...
 * }
 * }
 *
 * <p>Example 2:
 * {@snippet :
 * @Embeddable
 * public class PhoneNumber {
 *     protected String areaCode;
 *     protected String localNumber;
 *     @ManyToOne
 *     protected PhoneServiceProvider provider;
 *     ...
 * }
 *
 * @Entity
 * public class PhoneServiceProvider {
 *     @Id
 *     protected String name;
 *     ...
 * }
 * }
 *
 * <p>Example 3:
 * {@snippet :
 * @Embeddable
 * public class Address {
 *     protected String street;
 *     protected String city;
 *     protected String state;
 *     @Embedded
 *     protected Zipcode zipcode;
 * }
 *
 * @Embeddable
 * public class Zipcode {
 *     protected String zip;
 *     protected String plusFour;
 * }
 * }
 *
 * @see Embedded
 * @see EmbeddedId
 *
 * @since 1.0
 */
@Documented
@Target({TYPE}) 
@Retention(RUNTIME)
public @interface Embeddable {
}
