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
//     Petros Splinakis - 2.2
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to override the mapping of a {@code Basic} (whether explicit
 * or default) property or field or {@code Id} property or field.
 *
 * <p> May be applied to an entity that extends a mapped superclass
 * or to an embedded field or property to override a basic mapping or
 * id mapping defined by the mapped superclass or embeddable class
 * (or embeddable class of one of its attributes).

 * <p> May be applied to an element collection containing instances
 * of an embeddable class or to a map collection whose key and/or
 * value is an embeddable class. When {@code AttributeOverride} is
 * applied to a map, "{@code key.}" or "{@code value.}" must be used
 * to prefix the name of the attribute that is being overridden in
 * order to specify it as part of the map key or map value.
 *
 * <p> To override mappings at multiple levels of embedding, a dot
 * ({@code .}) notation form must be used in the {@code name} element
 * to indicate an attribute within an embedded attribute. The value
 * of each identifier used with the dot notation is the name of the
 * respective embedded field or property.
 *
 * <p> If {@code AttributeOverride} is not specified, the column is
 * mapped the same as in the original mapping.
 *
 * <p>Example 1:
 * {@snippet :
 * @MappedSuperclass
 * public class Employee {
 *     @Id
 *     protected Integer id;
 *     @Version
 *     protected Integer version;
 *     protected String address;
 *     public Integer getId() { ... }
 *     public void setId(Integer id) { ... }
 *     public String getAddress() { ... }
 *     public void setAddress(String address) { ... }
 * }
 *
 * @Entity
 * @AttributeOverride(name = "address", column = @Column(name = "ADDR"))
 * public class PartTimeEmployee extends Employee {
 *     // address field mapping overridden to ADDR
 *     protected Float wage();
 *     public Float getHourlyWage() { ... }
 *     public void setHourlyWage(Float wage) { ... }
 * }
 * }
 *
 * <p>Example 2:
 * {@snippet :
 * @Embeddable
 * public class Address {
 *     protected String street;
 *     protected String city;
 *     protected String state;
 *     @Embedded
 *     protected Zipcode zipcode;
 * }
 *
 * @Embeddable
 * public class Zipcode {
 *     protected String zip;
 *     protected String plusFour;
 * }
 *
 * @Entity
 * public class Customer {
 *     @Id
 *     protected Integer id;
 *     protected String name;
 *     @AttributeOverride(name = "state",
 *                        column = @Column(name = "ADDR_STATE"))
 *     @AttributeOverride(name = "zipcode.zip",
 *                        column = @Column(name = "ADDR_ZIP"))
 *     @Embedded
 *     protected Address address;
 *     ...
 * }
 * }
 *
 * <p>Example 3:
 * {@snippet :
 * @Entity
 * public class PropertyRecord {
 *     @EmbeddedId
 *     protected PropertyOwner owner;
 *     @AttributeOverride(name = "key.street",
 *                        column = @Column(name = "STREET_NAME"))
 *     @AttributeOverride(name = "value.size",
 *                        column = @Column(name = "SQUARE_FEET"))
 *     @AttributeOverride(name = "value.tax",
 *                        column = @Column(name = "ASSESSMENT"))
 *     @ElementCollection
 *     protected Map<Address, PropertyInfo> parcels;
 * }
 *
 * @Embeddable
 * public class Address {
 *     protected String street;
 *     protected String city;
 *     protected String state;
 *     @Embedded
 *     protected Zipcode zipcode;
 * }
 *
 * @Embeddable
 * public class PropertyInfo {
 *     Integer parcelNumber;
 *     Integer size;
 *     BigDecimal tax;
 * }
 * }
 *
 * @see Embedded
 * @see Embeddable
 * @see MappedSuperclass
 * @see AssociationOverride
 *
 * @since 1.0
 */
@Repeatable(AttributeOverrides.class)
@Target({TYPE, METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface AttributeOverride {

    /**
     * (Required) The name of the property whose mapping is being 
     * overridden if property-based access is being used, or the 
     * name of the field if field-based access is used.
     */
    String name();

    /**
     * (Required) The column that is being mapped to the persistent 
     * attribute. The mapping type will remain the same as is 
     * defined in the embeddable class or mapped superclass.
     */
    Column column();
}
