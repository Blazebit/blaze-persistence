/*
 * Copyright (c) 2011, 2023 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Declares that the annotated class is a converter and specifies
 * whether the converter is {@linkplain #autoApply automatically
 * applied}.
 *
 * <p>Every converter class must implement {@link AttributeConverter}
 * and must be annotated with the {@code Converter} annotation or
 * declared as a converter in the object/relational mapping descriptor.
 * The target type for a converter is determined by the actual type
 * argument of the first type parameter of {@code AttributeConverter}.
 *
 * <p>If {@link #autoApply autoApply = true}, the persistence provider
 * must automatically apply the converter to every mapped attribute of
 * the specified target type belonging to any entity in the persistence
 * unit, except for attributes for which conversion is overridden by
 * means of the {@link Convert} annotation (or XML equivalent). The
 * {@link Convert} annotation may be used to override or disable
 * auto-apply conversion on a per-attribute basis.
 * <ul>
 * <li>In determining whether a converter applies to an attribute,
 *     the provider must treat primitive types and wrapper types as
 *     equivalent.
 * <li>A converter never applies to {@linkplain Id id attributes},
 *     {@linkplain Version version attributes}, relationship attributes,
 *     or to attributes explicitly annotated {@link Enumerated} or
 *     {@link Temporal} (or designated as such via XML).
 * <li>A converter never applies to any attribute annotated
 *     {@link Convert#disableConversion @Convert(disableConversion=true)},
 *     or to an attribute for which the {@link Convert} annotation
 *     explicitly specifies a different {@linkplain Convert#converter
 *     converter}.
 * </ul>
 *
 * <p>If {@code autoApply = false}, the converter applies only to
 * attributes of the target type for which conversion is explicitly
 * enabled via the {@link Convert} annotation (or corresponding XML
 * element).
 *
 * <p>If there is more than one converter defined for the same target
 * type, {@link Convert#converter} must be used to explicitly specify
 * which converter applies.
 *
 * @see AttributeConverter
 * @see Convert
 *
 * @since 2.1
 */
@Target({TYPE}) @Retention(RUNTIME)
public @interface Converter {
     /**
      * Specifies whether the annotated converter should be
      * automatically applied to attributes of the target
      * type.
      */
     boolean autoApply() default false;
}
