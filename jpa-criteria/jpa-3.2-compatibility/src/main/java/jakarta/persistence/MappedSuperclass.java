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
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Declares a class which is not itself an entity, but whose
 * mappings are inherited by the entities which extend it.
 *
 * <p>A mapped superclass is not a persistent type, and is
 * not mapped to a database table.
 *
 * <p>The persistent fields and properties of a mapped
 * superclass are declared and mapped using the same mapping
 * annotations used to map {@linkplain Entity entity classes}.
 * However, these mappings are interpreted in the context of
 * each entity class which inherits the mapped superclass,
 * since the mapped superclass itself has no table to map.
 *
 * <p>Mapping information may be overridden in each such
 * subclass using the {@link AttributeOverride} and
 * {@link AssociationOverride} annotations or corresponding
 * XML elements.
 *
 * <p>Example: Concrete class as a mapped superclass
 * {@snippet :
 * @MappedSuperclass
 * public class Employee {
 *    
 *     @Id
 *     protected Integer empId;
 *     @Version
 *     protected Integer version;
 *     @ManyToOne
 *     @JoinColumn(name = "ADDR")
 *     protected Address address;
 *    
 *     public Integer getEmpId() { ... }
 *     public void setEmpId(Integer id) { ... }
 *     public Address getAddress() { ... }
 *     public void setAddress(Address addr) { ... }
 * }
 *    
 * // Default table is FTEMPLOYEE table
 * @Entity
 * public class FTEmployee extends Employee {
 *    
 *     // Inherited empId field mapped to FTEMPLOYEE.EMPID
 *     // Inherited version field mapped to FTEMPLOYEE.VERSION
 *     // Inherited address field mapped to FTEMPLOYEE.ADDR fk
 *    
 *     // Defaults to FTEMPLOYEE.SALARY
 *     protected Integer salary;
 *    
 *     public FTEmployee() {}
 *    
 *     public Integer getSalary() { ... }
 *     public void setSalary(Integer salary) { ... }
 * }
 *    
 * @Entity @Table(name = "PT_EMP")
 * @AssociationOverride(
 *     name = "address",
 *     joinColumns = @JoinColumn(name = "ADDR_ID"))
 * public class PartTimeEmployee extends Employee {
 *    
 *     // Inherited empId field mapped to PT_EMP.EMPID
 *     // Inherited version field mapped to PT_EMP.VERSION
 *     // address field mapping overridden to PT_EMP.ADDR_ID fk

 *     @Column(name = "WAGE")
 *     protected Float hourlyWage;
 *    
 *     public PartTimeEmployee() {}
 *    
 *     public Float getHourlyWage() { ... }
 *     public void setHourlyWage(Float wage) { ... }
 * }
 * }
 *
 * @see AttributeOverride 
 * @see AssociationOverride
 * @since 1.0
 */
@Documented
@Target({TYPE})
@Retention(RUNTIME)
public @interface MappedSuperclass {
}
