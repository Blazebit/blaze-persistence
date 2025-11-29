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
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the value of the discriminator column for the annotated
 * entity type.
 *
 * <p>The {@code DiscriminatorValue} annotation can only be specified
 * on a concrete entity class.
 *
 * <p> If the {@code DiscriminatorValue} annotation is not specified,
 * and a discriminator column is used, a provider-specific function
 * is used to generate a value representing the entity type. If the
 * {@link DiscriminatorType} is {@link DiscriminatorType#STRING
 * STRING}, the discriminator value default is the entity name.
 *
 * <p>The inheritance strategy and the discriminator column are only
 * specified for the root of an entity class hierarchy or subhierarchy
 * in which a different inheritance strategy is applied. But the
 * discriminator value, if not defaulted, should be specified for each
 * concrete entity class in the hierarchy.
 *
 * <p>Example:
 * {@snippet :
 * @Entity
 * @Table(name = "CUST")
 * @Inheritance(strategy = SINGLE_TABLE)
 * @DiscriminatorColumn(name = "DISC", discriminatorType = STRING, length = 20)
 * @DiscriminatorValue("CUSTOMER")
 * public class Customer { ... }
 *
 * @Entity
 * @DiscriminatorValue("VCUSTOMER")
 * public class ValuedCustomer extends Customer { ... }
 * }
 *
 * @see DiscriminatorColumn
 *
 * @since 1.0
 */
@Target({TYPE}) 
@Retention(RUNTIME)
public @interface DiscriminatorValue {

    /**
     * (Optional) The value that indicates that the row is an entity of
     * the annotated entity type.
     *
     * <p> If the {@code DiscriminatorValue} annotation is not specified
     * and a discriminator column is used, a provider-specific function
     * is used to generate a value representing the entity type. If the
     * {@link DiscriminatorType} is {@link DiscriminatorType#STRING
     * STRING}, the discriminator value default is the entity name.
     */
    String value();
}
