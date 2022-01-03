/*
 * Copyright 2014 - 2022 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
