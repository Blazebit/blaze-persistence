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

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public final class EntityViewUtils {

    private EntityViewUtils() {
    }

    public static String getAttributeName(Element element) {
        if (element == null) {
            return null;
        }
        final String elementName = element.getSimpleName().toString();
        if (element.getKind() == ElementKind.METHOD) {
            if (elementName.startsWith("is") && elementName.length() > 2 && Character.isUpperCase(elementName.charAt(2))) {
                return Character.toLowerCase(elementName.charAt(2)) + elementName.substring(3);
            } else if (elementName.startsWith("get") && elementName.length() > 3 && Character.isUpperCase(elementName.charAt(3))) {
                return Character.toLowerCase(elementName.charAt(3)) + elementName.substring(4);
            }
        } else if (element.getKind() == ElementKind.FIELD) {
            return elementName;
        }
        return null;
    }

}
