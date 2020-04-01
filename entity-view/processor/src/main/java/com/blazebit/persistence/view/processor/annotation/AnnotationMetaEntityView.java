/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.processor.annotation;

import com.blazebit.persistence.view.processor.Constants;
import com.blazebit.persistence.view.processor.Context;
import com.blazebit.persistence.view.processor.ImportContext;
import com.blazebit.persistence.view.processor.ImportContextImpl;
import com.blazebit.persistence.view.processor.MetaAttribute;
import com.blazebit.persistence.view.processor.MetaConstructor;
import com.blazebit.persistence.view.processor.MetaEntityView;
import com.blazebit.persistence.view.processor.TypeUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class AnnotationMetaEntityView implements MetaEntityView {

    private final ImportContext metamodelImportContext;
    private final ImportContext implementationImportContext;
    private final ImportContext builderImportContext;
    private final TypeElement element;
    private final MetaAttribute idMember;
    private final Map<String, MetaAttribute> members;
    private final Map<String, MetaConstructor> constructors;
    private final Map<String, ExecutableElement> specialMembers;
    private final Map<String, TypeElement> foreignPackageSuperTypes;
    private final boolean hasEmptyConstructor;
    private final boolean valid;
    private final boolean needsEntityViewManager;
    private final Context context;

    public AnnotationMetaEntityView(TypeElement element, Context context) {
        this.element = element;
        this.context = context;
        this.metamodelImportContext = new ImportContextImpl(getPackageName());
        this.implementationImportContext = new ImportContextImpl(getPackageName());
        this.builderImportContext = new ImportContextImpl(getPackageName());
        getContext().logMessage(Diagnostic.Kind.OTHER, "Initializing type " + getQualifiedName() + ".");

        Collection<Element> allMembers = getAllMembers(element, context);
        boolean needsEntityViewManager = false;
        MetaAttribute idMember = null;
        Map<String, MetaAttribute> members = new TreeMap<>();
        Map<String, ExecutableElement> specialMembers = new TreeMap<>();
        Map<String, MetaConstructor> constructors = new TreeMap<>();
        MetaAttributeGenerationVisitor visitor = new MetaAttributeGenerationVisitor(this, context);
        boolean valid = true;
        boolean hasEmptyConstructor = false;
        for (Element memberOfClass : allMembers) {
            if (memberOfClass instanceof ExecutableElement) {
                ExecutableElement executableElement = (ExecutableElement) memberOfClass;
                if (Constants.SPECIAL.contains(executableElement.getReturnType().toString())) {
                    specialMembers.put(memberOfClass.getSimpleName().toString(), executableElement);
                    if (Constants.ENTITY_VIEW_MANAGER.equals(executableElement.getReturnType().toString())) {
                        needsEntityViewManager = true;
                    }
                } else if (isGetterOrSetter(memberOfClass)) {
                    // Entity view attributes are always abstract methods
                    if (!memberOfClass.getModifiers().contains(Modifier.ABSTRACT)) {
                        continue;
                    }

                    AnnotationMetaAttribute result = memberOfClass.asType().accept(visitor, memberOfClass);
                    if (result != null) {
                        if (result.isIdMember()) {
                            idMember = result;
                        }
                        members.put(result.getPropertyName(), result);
                    }
                } else if (memberOfClass.getKind() == ElementKind.CONSTRUCTOR) {
                    AnnotationMetaConstructor constructor = new AnnotationMetaConstructor(this, executableElement, visitor);
                    hasEmptyConstructor = constructor.getParameters().isEmpty();
                    constructors.put(constructor.getName(), constructor);
                }
            }
        }
        this.hasEmptyConstructor = hasEmptyConstructor || constructors.isEmpty();
        this.valid = valid;
        this.needsEntityViewManager = needsEntityViewManager;

        if (constructors.isEmpty()) {
            constructors.put("init", new AnnotationMetaConstructor(this));
        }

        Map<String, TypeElement> foreignPackageSuperTypes = new LinkedHashMap<>();
        TypeMirror superClass = element.getSuperclass();
        PackageElement elementPackage = context.getElementUtils().getPackageOf(element);
        while (superClass.getKind() == TypeKind.DECLARED) {
            final Element superClassElement = ((DeclaredType) superClass).asElement();
            PackageElement superClassPackage;
            if (superClassElement.getModifiers().contains(Modifier.ABSTRACT) && !elementPackage.equals(superClassPackage = context.getElementUtils().getPackageOf(superClassElement))) {
                String packageName = superClassPackage.getQualifiedName().toString();
                if (!foreignPackageSuperTypes.containsKey(packageName)) {
                    foreignPackageSuperTypes.put(packageName, (TypeElement) superClassElement);
                }
            }
            superClass = ((TypeElement) superClassElement).getSuperclass();
        }

        this.idMember = idMember;
        this.members = members;
        this.constructors = constructors;
        this.specialMembers = specialMembers;
        this.foreignPackageSuperTypes = foreignPackageSuperTypes;
    }

    public final Context getContext() {
        return context;
    }

    @Override
    public Map<String, TypeElement> getForeignPackageSuperTypes() {
        return foreignPackageSuperTypes;
    }

    @Override
    public boolean hasEmptyConstructor() {
        return hasEmptyConstructor;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean needsEntityViewManager() {
        return needsEntityViewManager;
    }

    @Override
    public String getBaseSuperclass() {
        Iterator<TypeElement> iterator = foreignPackageSuperTypes.values().iterator();
        if (iterator.hasNext()) {
            return iterator.next().getQualifiedName().toString() + "_" + getQualifiedName().replace('.', '_');
        }
        return getQualifiedName();
    }

    @Override
    public final String getSimpleName() {
        return TypeUtils.getSimpleTypeName(element);
    }

    @Override
    public final String getQualifiedName() {
        return element.getQualifiedName().toString();
    }

    @Override
    public final String getPackageName() {
        PackageElement packageOf = context.getElementUtils().getPackageOf(element);
        return context.getElementUtils().getName(packageOf.getQualifiedName()).toString();
    }

    @Override
    public MetaAttribute getIdMember() {
        return idMember;
    }

    @Override
    public MetaConstructor getConstructor(String name) {
        return constructors.get(name);
    }

    @Override
    public Collection<MetaConstructor> getConstructors() {
        return constructors.values();
    }

    @Override
    public Collection<MetaAttribute> getMembers() {
        return members.values();
    }

    @Override
    public Collection<ExecutableElement> getSpecialMembers() {
        return specialMembers.values();
    }

    @Override
    public ImportContext getMetamodelImportContext() {
        return metamodelImportContext;
    }

    @Override
    public ImportContext getImplementationImportContext() {
        return implementationImportContext;
    }

    @Override
    public ImportContext getBuilderImportContext() {
        return builderImportContext;
    }

    @Override
    public final String importType(String fqcn) {
        implementationImportContext.importType(fqcn);
        metamodelImportContext.importType(fqcn);
        return builderImportContext.importType(fqcn);
    }

    @Override
    public String metamodelImportType(String fqcn) {
        return metamodelImportContext.importType(fqcn);
    }

    @Override
    public String implementationImportType(String fqcn) {
        return implementationImportContext.importType(fqcn);
    }

    @Override
    public String builderImportType(String fqcn) {
        return builderImportContext.importType(fqcn);
    }

    @Override
    public final TypeElement getTypeElement() {
        return element;
    }

    private static Collection<Element> getAllMembers(TypeElement element, Context context) {
        List<TypeMirror> superClasses = new ArrayList<>();
        superClasses.add(element.asType());
        Map<String, Element> members = new TreeMap<>();
        for (int i = 0; i < superClasses.size(); i++) {
            TypeMirror superClass = superClasses.get(i);
            final Element superClassElement = ((DeclaredType) superClass).asElement();
            for (Element enclosedElement : superClassElement.getEnclosedElements()) {
                String name = enclosedElement.getSimpleName().toString();
                Element old = members.put(name, enclosedElement);
                if (old != null) {
                    members.put(name, old);
                }
            }

            superClass = ((TypeElement) superClassElement).getSuperclass();
            if (superClass.getKind() == TypeKind.DECLARED) {
                superClasses.add(superClass);
            }
            superClasses.addAll(((TypeElement) superClassElement).getInterfaces());
        }
        return members.values();
    }

    private boolean isGetterOrSetter(Element methodOfClass) {
        if (methodOfClass instanceof ExecutableElement) {
            ExecutableType methodType = (ExecutableType) methodOfClass.asType();
            String methodSimpleName = methodOfClass.getSimpleName().toString();
            List<? extends TypeMirror> methodParameterTypes = methodType.getParameterTypes();
            TypeMirror returnType = methodType.getReturnType();

            if (methodSimpleName.startsWith("set") && methodParameterTypes.size() == 1 && "void".equalsIgnoreCase(returnType.toString())) {
                return true;
            } else if ((methodSimpleName.startsWith("get") || methodSimpleName.startsWith("is")) && methodParameterTypes.isEmpty() && !"void".equalsIgnoreCase(returnType.toString())) {
                return true;
            }
        }
        return false;
    }
}
