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
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the ordering of the elements of a collection-valued
 * association or element collection at the point when the association
 * or collection is retrieved.
 * 
 * <p> The syntax of the {@code value} ordering element is an 
 * {@code orderby_list}, as follows:
 * 
 * {@snippet :
 *     orderby_list ::= orderby_item [, orderby_item]*
 *     orderby_item ::= [property_or_field_name] [ASC | DESC]
 * }
 * 
 * <p>If {@code ASC} or {@code DESC} is not specified, {@code ASC}
 * (ascending order) is assumed.
 *
 * <p>If the ordering element is not specified for an entity association,
 * ordering by the primary key of the associated entity is assumed.
 *
 * <p>The property or field name must correspond to that of a persistent
 * property or field of the associated class or embedded class within it.
 * The properties or fields used in the ordering must correspond to columns
 * for which comparison operators are supported.
 *
 * <p>The dot ({@code .}) notation is used to refer to an attribute within
 * an embedded attribute.  The value of each identifier used with the dot
 * notation is the name of the respective embedded field or property.
 *
 * <p>The {@code OrderBy} annotation may be applied to an element
 * collection. When {@code OrderBy} is applied to an element collection
 * of basic type, the ordering is by value of the basic objects and the
 * property or field name is not used. When specifying an ordering over
 * an element collection of embeddable type, the dot notation must be
 * used to specify the attribute or attributes that determine the ordering.
 *
 * <p> The {@code OrderBy} annotation is not used when an order column is
 * specified using {@link OrderColumn}.
 *
 *
 * <p>Example 1:
 * {@snippet :
 * @Entity
 * public class Course {
 *     ...
 *     @ManyToMany
 *     @OrderBy("lastname ASC")
 *     public List<Student> getStudents() { ... }
 *     ...
 * }
 * }
 *
 * <p>Example 2:
 * {@snippet :
 * @Entity
 * public class Student {
 *     ...
 *     @ManyToMany(mappedBy = "students")
 *     @OrderBy // ordering by primary key is assumed
 *     public List<Course> getCourses() { ... }
 *     ...
 * }
 * }
 *
 * <p>Example 3:
 * {@snippet :
 * @Entity 
 * public class Person {
 *     ...
 *     @ElementCollection
 *     @OrderBy("zipcode.zip, zipcode.plusFour")
 *     public Set<Address> getResidences() { ... }
 *     ...
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
 * public class Zipcode {
 *     protected String zip;
 *     protected String plusFour;
 * }
 * }
 *
 * @see OrderColumn
 *
 * @since 1.0
 */
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface OrderBy {

   /**
    * An {@code orderby_list}. Specified as follows:
    *
    * {@snippet :
    *    orderby_list::= orderby_item [,orderby_item]*
    *    orderby_item::= [property_or_field_name] [ASC | DESC]
    * }
    *
    * <p> If {@code ASC} or {@code DESC} is not specified,
    * {@code ASC} (ascending order) is assumed.
    *
    * <p> If the ordering element is not specified, ordering by
    * the primary key of the associated entity is assumed.
    */
    String value() default "";
}
