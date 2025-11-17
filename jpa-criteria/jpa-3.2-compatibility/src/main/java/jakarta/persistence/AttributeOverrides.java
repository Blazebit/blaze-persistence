/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
 * Used to override mappings of multiple properties or fields.
 *
 * <p>Example:
 * {@snippet :
 * @Embedded
 * @AttributeOverrides({
 *     @AttributeOverride(name = "startDate",
 *                        column = @Column(name = "EMP_START")),
 *     @AttributeOverride(name="endDate",
 *                        column = @Column(name = "EMP_END"))})
 * public EmploymentPeriod getEmploymentPeriod() { ... }
 * }
 *
 * @see AttributeOverride
 * @since 1.0
 */
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)

public @interface AttributeOverrides {

    /**
     * (Required) One or more field or property mapping overrides.
     */
    AttributeOverride[] value();
}
