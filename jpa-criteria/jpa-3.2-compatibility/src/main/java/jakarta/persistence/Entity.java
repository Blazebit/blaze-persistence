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
 * Declares that the annotated class is an entity. The annotated entity
 * class must:
 * <ul>
 * <li>be a non-{@code final} top-level class or static inner class,
 * <li>have a {@code public} or {@code protected} constructor with no
 *     parameters, and
 * <li>have no {@code final} methods or persistent instance variables.
 * </ul>
 * <p>An enum, record, or interface may not be designated as an entity.
 *
 * <p>An entity has a primary table, mapped using the {@link Table}
 * annotation, and may have one or more secondary tables, mapped using
 * the {@link SecondaryTable} annotation.
 *
 * <p>An entity class holds state, represented as persistent fields and
 * properties:
 * <ul>
 * <li>a field or property of {@linkplain Basic basic type} maps to a
 *     single {@linkplain Column column} in one of the tables mapped
 *     by the entity,
 * <li>a field of property of {@linkplain Embedded embeddable type}
 *     has nested mappings to multiple columns in one of the tables
 *     mapped by the entity,
 * <li>an {@linkplain ElementCollection element collection} usually
 *     maps to a separate {@linkplain CollectionTable collection table},
 * <li>a {@linkplain ManyToOne many-to-one} association usually maps
 *     to a {@linkplain JoinColumn foreign key column} or columns in
 *     one of the tables mapped by the entity,
 * <li>a {@linkplain OneToOne one-to-one} association usually maps
 *     to a unique foreign key relationship (sometimes using a shared
 *     primary key),
 * <li>a {@linkplain OneToMany one-to-many} association usually maps
 *     to a foreign key column or columns in one of the tables mapped
 *     by the <em>associated entity</em>, and
 * <li>a {@linkplain ManyToMany many-to-many} association usually maps
 *     to a {@linkplain JoinTable join table}.
 * </ul>
 *
 * <p>Every entity class must have at least one field or property
 * annotated {@link Id} or {@link EmbeddedId} holding the primary key
 * of the entity. An entity class may optionally have a field or
 * property annotated {@link Version}, which holds a value used to
 * detect optimistic lock failure.
 *
 * <p>Fields or properties of an entity class are persistent by default.
 * The {@link Transient} annotation or the Java {@code transient} keyword
 * must be used to explicitly declare any field or property of an entity
 * which is <em>not</em> persistent.
 *
 * <p>The entity {@linkplain AccessType access type} determines whether
 * the persistence provider accesses the state of the entity using getter
 * and setter methods, or via direct field access. It is almost never
 * necessary to explicitly specify an {@link AccessType}, since the
 * default access type for an entity is determined by the placement of
 * mapping annotations on the entity class.
 *
 * <p>Apart from its persistent fields and properties, an entity class
 * may declare callback methods using {@link PrePersist}, {@link PreUpdate},
 * {@link PreRemove}, {@link PostPersist}, {@link PostUpdate}, and/or
 * {@link PostRemove}.
 *
 * @since 1.0
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface Entity {

	/**
	 * (Optional) The entity name. Defaults to the unqualified
	 * name of the entity class. This name is used to refer to the
	 * entity in queries. The name must not be a reserved literal
	 * in the Jakarta Persistence query language.
	 */
	String name() default "";
}
