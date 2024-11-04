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
 * Specifies the type of the map key for associations of type
 * {@link java.util.Map}.  The map key can be a basic type, an
 * embeddable class, or an entity. If the map is specified using Java
 * generics, the {@code MapKeyClass} annotation and associated
 * type need not be specified; otherwise they must be specified.
 * 
 * <p> The {@code MapKeyClass} annotation is used in conjunction
 * with {@link ElementCollection} or one of the collection-valued
 * relationship annotations ({@link OneToMany} or {@link ManyToMany}).
 * The {@link MapKey} annotation is not used when {@code MapKeyClass}
 * is specified and vice versa.
 *
 * <p>Example 1:
 * {@snippet :
 * @Entity
 * public class Item {
 *     @Id
 *     int id;
 *     ...
 *     @ElementCollection(targetClass = String.class)
 *     @MapKeyClass(String.class)
 *     Map images;  // map from image name to image filename
 *     ...
 * }
 * }
 *
 * <p>Example 2:
 * {@snippet :
 * // MapKeyClass and target type of relationship can be defaulted
 *
 * @Entity
 * public class Item {
 *     @Id
 *     int id;
 *     ...
 *     @ElementCollection
 *     Map<String, String> images;
 *     ...
 * }
 * }
 *
 * <p>Example 3:
 * {@snippet :
 * @Entity
 * public class Company {
 *     @Id
 *     int id;
 *     ...
 *     @OneToMany(targetEntity = com.example.VicePresident.class)
 *     @MapKeyClass(com.example.Division.class)
 *     Map organization;
 * }
 * }
 *
 * <p>Example 4:
 * {@snippet :
 * // MapKeyClass and target type of relationship are defaulted
 *
 * @Entity
 * public class Company {
 *     @Id
 *     int id;
 *     ...
 *     @OneToMany
 *     Map<Division, VicePresident> organization;
 * }
 * }
 *
 * @see ElementCollection 
 * @see OneToMany
 * @see ManyToMany
 * @since 2.0
 */

@Target( { METHOD, FIELD })
@Retention(RUNTIME)
public @interface MapKeyClass {
	/**
	 * (Required) The type of the map key.
	 */
	Class<?> value();
}
