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

/**
 * Interface implemented by custom attribute <em>converters</em>. A
 * converter is a class whose methods convert between:
 * <ul>
 * <li>the <em>target type</em> of the converter, an arbitrary Java
 *     type which may be used as the type of a persistent field or
 *     property, and
 * <li>a {@linkplain Basic basic type} used as an intermediate step
 *     in mapping to the database representation.
 * </ul>
 *
 * <p>A converted field or property is considered {@link Basic}, since,
 * with the aid of the converter, its values can be represented as
 * instances of a basic type.
 *
 * <p>A converter class must be annotated {@link Converter} or declared
 * as a converter in the object/relational mapping descriptor. The value
 * of {@link Converter#autoApply autoApply} determines if the converter
 * is automatically applied to persistent fields and properties of the
 * target type. The {@link Convert} annotation may be used to apply a
 * converter which is declared {@code autoApply=false}, to explicitly
 * {@linkplain Convert#disableConversion disable conversion}, or to
 * resolve ambiguities when multiple converters would otherwise apply.
 *
 * <p>Note that the target type {@code X} and the converted basic type
 * {@code Y} may be the same Java type.
 *
 * @param <X> the target type, that is, the type of the entity attribute
 * @param <Y> a basic type representing the type of the database column
 *
 * @see Converter
 * @see Convert#converter
 */
public interface AttributeConverter<X,Y> {

    /**
     * Converts the value stored in the entity attribute into the 
     * data representation to be stored in the database.
     *
     * @param attribute  the entity attribute value to be converted
     * @return  the converted data to be stored in the database column
     */
    Y convertToDatabaseColumn(X attribute);

    /**
     * Converts the data stored in the database column into the value
     * to be stored in the entity attribute.
     *
     * <p>Note that it is the responsibility of the converter writer
     * to specify the correct {@code dbData} type for the corresponding
     * column for use by the JDBC driver: i.e., persistence providers
     * are not expected to do such type conversion.
     *
     * @param dbData  the data from the database column to be converted
     * @return  the converted value to be stored in the entity attribute
     */
    X convertToEntityAttribute(Y dbData);
}
