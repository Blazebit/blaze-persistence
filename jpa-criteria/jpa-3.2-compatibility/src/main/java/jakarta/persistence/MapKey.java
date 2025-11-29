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
 * Specifies the map key for associations of type {@link java.util.Map}
 * when the map key is itself the primary key or a persistent field or
 * property of the entity that is the value of the map.
 * 
 * <p> If a persistent field or property other than the primary key is
 * used as a map key then it is expected to have a uniqueness constraint
 * associated with it.
 *
 * <p> The {@link MapKeyClass} annotation is not used when {@code MapKey}
 * is specified and vice versa.
 *
 * <p>Example 1:
 * {@snippet :
 * @Entity
 * public class Department {
 *     ...
 *     @OneToMany(mappedBy = "department")
 *     @MapKey  // map key is primary key
 *     public Map<Integer, Employee> getEmployees() {... }
 *     ...
 * }
 *
 * @Entity
 * public class Employee {
 *     ...
 *     @Id
 *     Integer getEmpId() { ... }
 *     @ManyToOne
 *     @JoinColumn(name = "dept_id")
 *     public Department getDepartment() { ... }
 *     ...
 * }
 * }
 *
 * <p>Example 2:
 * {@snippet :
 * @Entity
 * public class Department {
 *     ...
 *     @OneToMany(mappedBy = "department")
 *     @MapKey(name = "name")
 *     public Map<String, Employee> getEmployees() {... }
 *     ...
 * }
 *
 * @Entity
 * public class Employee {
 *     @Id
 *     public Integer getEmpId() { ... }
 *     ...
 *     @ManyToOne
 *     @JoinColumn(name = "dept_id")
 *     public Department getDepartment() { ... }
 *     ...
 * }
 * }
 *
 * @since 1.0
 */
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface MapKey {

    /**
     * (Optional) The name of the persistent field or property of
     * the associated entity that is used as the map key.
     * <p> Default: If the {@code name} element is not specified,
     * the primary key of the associated entity is used as the map
     * key. If the primary key is a composite primary key and is
     * mapped as {@link IdClass}, an instance of the primary key
     * class is used as the key.
     */
    String name() default "";
}
