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
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to override mappings of multiple relationship properties or fields.
 *
 * <p>Example:
 * {@snippet :
 * @MappedSuperclass
 * public class Employee {
 *    
 *     @Id
 *     protected Integer id;
 *     @Version
 *     protected Integer version;
 *     @ManyToOne
 *     protected Address address;
 *     @OneToOne
 *     protected Locker locker;
 *    
 *     public Integer getId() { ... }
 *     public void setId(Integer id) { ... }
 *     public Address getAddress() { ... }
 *     public void setAddress(Address address) { ... }
 *     public Locker getLocker() { ... }
 *     public void setLocker(Locker locker) { ... }
 *     ...
 * }
 *    
 * @Entity
 * @AssociationOverrides({
 *     @AssociationOverride(
 *                name = "address",
 *                joinColumns = @JoinColumn("ADDR_ID")),
 *     @AttributeOverride(
 *                name = "locker",
 *                joinColumns = @JoinColumn("LCKR_ID"))})
 * public PartTimeEmployee { ... }
 * }
 *
 *@see AssociationOverride
 *
 * @since 1.0
 */
@Target({TYPE, METHOD, FIELD}) 
@Retention(RUNTIME)

public @interface AssociationOverrides {

    /** 
     *(Required) The association override mappings that are to be 
     * applied to the relationship field or property.
     */
    AssociationOverride[] value();
}
