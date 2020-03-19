/*
 * Copyright (c) 2008, 2009, 2011 Oracle, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.  The Eclipse Public License is available
 * at http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution License
 * is available at http://www.eclipse.org/org/documents/edl-v10.php.
 */
package javax.persistence;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The Convert annotation is used to specify the conversion of a Basic field or
 * property. It is not necessary to use the Basic annotation or corresponding XML
 * element to specify the basic type.
 *
 * @since Java Persistence 2.1
 */
@Target({METHOD, FIELD, TYPE})
@Retention(RUNTIME)
public @interface Convert {
    /**
     * Specifies the converter to be applied. A value for this
     * element must be specified if multiple converters would
     * otherwise apply.
     * @return The converter class
     */
    Class converter() default void.class;

    /**
     * The attributeName must be specified unless the Convert annotation
     * is on an attribute of basic type or on an element collection of
     * basic type. In these cases, attributeName must not be
     * specified.
     * @return The attribute name
     */
    String attributeName() default "";

    /**
     * Used to disable an auto-apply or inherited converter.
     * If disableConversion is true, the converter element should
     * not be specified.
     * @return Whether to disable auto-apply or inherited converter
     */
    boolean disableConversion() default false;
}
