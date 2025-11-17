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
import static jakarta.persistence.ConstraintMode.PROVIDER_DEFAULT;

/**
 * Specifies the mapping for composite foreign keys. This annotation groups
 * {@link JoinColumn} annotations for the same relationship.
 *
 * <p>Each {@link JoinColumn} annotation must explicit specify both
 * {@link JoinColumn#name name} and {@link JoinColumn#referencedColumnName
 * referencedColumnName}.
 *
 * <p>Example:
 * {@snippet :
 * @ManyToOne
 * @JoinColumns({
 *     @JoinColumn(name = "ADDR_ID", referencedColumnName = "ID"),
 *     @JoinColumn(name = "ADDR_ZIP", referencedColumnName = "ZIP")})
 * public Address getAddress() { return address; }
 * }
 *
 * @see JoinColumn
 * @see ForeignKey
 *
 * @since 1.0
 */
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface JoinColumns {

    /**
     * The join columns that map the relationship.
     */
    JoinColumn[] value();

    /**
     * (Optional) Used to specify or control the generation of a
     * foreign key constraint when table generation is in effect.
     * If both this element and the {@code foreignKey} element of
     * any of the {@link JoinColumn} elements are specified, the
     * behavior is undefined. If no foreign key annotation element
     * is specified in either location, a default foreign key
     * strategy is selected by the persistence provider.
     *
     * @since 2.1
     */
    ForeignKey foreignKey() default @ForeignKey(PROVIDER_DEFAULT);
}
