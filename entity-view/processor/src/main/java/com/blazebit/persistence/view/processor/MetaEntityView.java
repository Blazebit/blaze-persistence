/*
 * Copyright 2014 - 2019 Blazebit.
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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface MetaEntityView {

    boolean isValid();

    boolean needsEntityViewManager();

    String getSimpleName();

    String getQualifiedName();

    String getPackageName();

    String getBaseSuperclass();

    Map<String, TypeElement> getForeignPackageSuperTypes();

    MetaAttribute getIdMember();

    Collection<MetaAttribute> getMembers();

    Collection<ExecutableElement> getSpecialMembers();

    ImportContext getMetamodelImportContext();

    ImportContext getImplementationImportContext();

    String importType(String fqcn);

    String metamodelImportType(String fqcn);

    String implementationImportType(String fqcn);

    TypeElement getTypeElement();
}
