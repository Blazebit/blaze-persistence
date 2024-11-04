/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker annotation for annotating the "self"-instance which can be injected into a constructor to access state in a safe way.
 *
 * Usually, one accesses the state of an entity view in an unsafe way in a constructor through getters like:
 *
 * <code>
 * &#064;EntityView(Cat.class)
 * public abstract class CatView {
 *     public CatView() {
 *         System.out.println(getName());
 *     }
 *     public abstract String getName();
 * }
 * </code>
 *
 * With @{@linkplain Self} it is possible to inject the self instance and get the state from it which is safe.
 *
 * <code>
 * &#064;EntityView(Cat.class)
 * public abstract class CatView {
 *     public CatView(@Self CatView self) {
 *         System.out.println(self.getName());
 *     }
 *     public abstract String getName();
 * }
 * </code>
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Self {
}
