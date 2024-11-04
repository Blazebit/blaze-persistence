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
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a callback method for the corresponding lifecycle event.
 * This annotation may be applied to methods of an entity class, a
 * mapped superclass, or a callback listener class.
 *
 * <p>A generated primary key value is available when this callback
 * occurs only for {@link GenerationType#UUID UUID},
 * {@link GenerationType#TABLE TABLE}, or
 * {@link GenerationType#SEQUENCE SEQUENCE}
 * primary key generation. For {@link GenerationType#IDENTITY IDENTITY}
 * primary key generation, the generated primary key is not available
 * when this callback occurs.
 *
 * @since 1.0
 */
@Target({METHOD}) 
@Retention(RUNTIME)
public @interface PrePersist {}
