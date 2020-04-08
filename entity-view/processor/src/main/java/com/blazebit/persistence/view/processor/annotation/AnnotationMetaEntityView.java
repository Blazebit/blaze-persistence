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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final String entityClass;
    private final Element entityVersionAttribute;
    private final ExecutableElement postCreate;
    private final ExecutableElement postLoad;
    private final MetaAttribute idMember;
    private final MetaAttribute versionMember;
    private final Map<String, MetaAttribute> members;
    private final List<MetaConstructor> constructors;
    private final Map<String, ExecutableElement> specialMembers;
    private final Map<String, TypeElement> foreignPackageSuperTypes;
    private final List<TypeMirror> foreignPackageSuperTypeVariables;
    private final boolean updatable;
    private final boolean creatable;
    private final boolean allSupportDirtyTracking;
    private final int mutableAttributeCount;
    private final int defaultDirtyMask;
    private final boolean hasEmptyConstructor;
    private final boolean hasSelfConstructor;
    private final boolean valid;
    private final Context context;
    private final Set<String> addedAccessors = new HashSet<>();

    public AnnotationMetaEntityView(TypeElement element, Context context) {
        this.element = element;
        this.context = context;
        this.metamodelImportContext = new ImportContextImpl(getPackageName());
        this.implementationImportContext = new ImportContextImpl(getPackageName());
        this.builderImportContext = new ImportContextImpl(getPackageName());
        getContext().logMessage(Diagnostic.Kind.OTHER, "Initializing type " + getQualifiedName() + ".");

        String entityClass = null;
        boolean updatable = false;
        boolean creatable = false;
        boolean allSupportDirtyTracking = true;
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            String annotationFqcn = annotationMirror.getAnnotationType().toString();
            if (annotationFqcn.equals(Constants.ENTITY_VIEW)) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
                    if ("value".equals(entry.getKey().getSimpleName().toString())) {
                        entityClass = entry.getValue().getValue().toString();
                        break;
                    }
                }
            } else if (annotationFqcn.equals(Constants.UPDATABLE_ENTITY_VIEW)) {
                updatable = true;
            } else if (annotationFqcn.equals(Constants.CREATABLE_ENTITY_VIEW)) {
                creatable = true;
            }
        }

        Element entityVersionAttribute = null;
        for (Element member : TypeUtils.getAllMembers(context.getElementUtils().getTypeElement(entityClass), context)) {
            if (TypeUtils.containsAnnotation(member, Constants.VERSION)) {
                entityVersionAttribute = member;
                break;
            }
        }
        this.entityVersionAttribute = entityVersionAttribute;
        this.entityClass = entityClass;
        this.updatable = updatable;
        this.creatable = creatable;

        Collection<Element> allMembers = TypeUtils.getAllMembers(element, context);
        MetaAttribute idMember = null;
        MetaAttribute versionMember = null;
        Map<String, MetaAttribute> members = new TreeMap<>();
        Map<String, ExecutableElement> specialMembers = new TreeMap<>();
        List<MetaConstructor> constructors = new ArrayList<>();
        MetaAttributeGenerationVisitor visitor = new MetaAttributeGenerationVisitor(this, context);
        boolean valid = true;
        boolean hasEmptyConstructor = false;
        boolean hasSelfConstructor = false;
        ExecutableElement postCreate = null;
        ExecutableElement postLoad = null;
        for (Element memberOfClass : allMembers) {
            if (memberOfClass instanceof ExecutableElement) {
                ExecutableElement executableElement = (ExecutableElement) memberOfClass;
                Set<Modifier> modifiers = memberOfClass.getModifiers();
                if (!modifiers.contains(Modifier.STATIC)) {
                    if (Constants.SPECIAL.contains(executableElement.getReturnType().toString())) {
                        specialMembers.put(memberOfClass.getSimpleName().toString(), executableElement);
                    } else if (modifiers.contains(Modifier.ABSTRACT) && isGetterOrSetter(memberOfClass)) {
                        AnnotationMetaAttribute result = memberOfClass.asType().accept(visitor, memberOfClass);
                        if (result != null) {
                            if (result.isIdMember()) {
                                idMember = result;
                            } else if (result.isVersion()) {
                                versionMember = result;
                            }
                            members.put(result.getPropertyName(), result);
                        }
                    } else if (!modifiers.contains(Modifier.PRIVATE) && memberOfClass.getKind() == ElementKind.CONSTRUCTOR) {
                        AnnotationMetaConstructor constructor = new AnnotationMetaConstructor(this, executableElement, visitor);
                        hasEmptyConstructor = hasEmptyConstructor || constructor.getParameters().isEmpty();
                        hasSelfConstructor = hasSelfConstructor || constructor.hasSelfParameter();
                        constructors.add(constructor);
                    } else if (TypeUtils.containsAnnotation(executableElement, Constants.POST_CREATE, Constants.POST_LOAD)) {
                        if (TypeUtils.containsAnnotation(executableElement, Constants.POST_CREATE)) {
                            if (postCreate == null) {
                                postCreate = executableElement;
                            } else {
                                if (context.getTypeUtils().isAssignable(executableElement.getEnclosingElement().asType(), postCreate.getEnclosingElement().asType())) {
                                    postCreate = executableElement;
                                }
                            }
                        }
                        if (TypeUtils.containsAnnotation(executableElement, Constants.POST_LOAD)) {
                            if (postLoad == null) {
                                postLoad = executableElement;
                            } else {
                                if (context.getTypeUtils().isAssignable(executableElement.getEnclosingElement().asType(), postLoad.getEnclosingElement().asType())) {
                                    postLoad = executableElement;
                                }
                            }
                        }
                    }
                }
            }
        }

        int dirtyStateIndex = 0;
        int defaultDirtyMask = 0;
        int index = 0;
        if (idMember != null) {
            idMember.setAttributeIndex(index);
            index++;
        }
        if (versionMember == null && updatable && entityVersionAttribute != null) {
            versionMember = new AnnotationMetaVersionAttribute(this, context);
            members.put(versionMember.getPropertyName(), versionMember);
        }
        for (MetaAttribute value : members.values()) {
            if (value.getAttributeIndex() == -1) {
                value.setAttributeIndex(index);
                if ((creatable || updatable) && value.isMutable() && value != versionMember) {
                    value.setDirtyStateIndex(dirtyStateIndex);
                    if (!value.supportsDirtyTracking()) {
                        allSupportDirtyTracking = false;
                    }
                    dirtyStateIndex++;
                }
                index++;
            }
        }

        this.hasEmptyConstructor = hasEmptyConstructor || constructors.isEmpty();
        this.hasSelfConstructor = hasSelfConstructor;
        this.valid = valid;
        this.allSupportDirtyTracking = allSupportDirtyTracking;
        this.mutableAttributeCount = dirtyStateIndex;
        this.defaultDirtyMask = defaultDirtyMask;

        if (constructors.isEmpty()) {
            constructors.add(new AnnotationMetaConstructor(this));
        }

        Map<String, TypeElement> foreignPackageSuperTypes = new LinkedHashMap<>();
        List<TypeMirror> foreignPackageSuperTypeVariables = new ArrayList<>();
        TypeMirror superClass = element.getSuperclass();
        PackageElement elementPackage = context.getElementUtils().getPackageOf(element);
        while (superClass.getKind() == TypeKind.DECLARED) {
            final TypeElement superClassElement = (TypeElement) ((DeclaredType) superClass).asElement();
            PackageElement superClassPackage;
            if (superClassElement.getModifiers().contains(Modifier.ABSTRACT) && !elementPackage.equals(superClassPackage = context.getElementUtils().getPackageOf(superClassElement))) {
                String packageName = superClassPackage.getQualifiedName().toString();
                if (!foreignPackageSuperTypes.containsKey(packageName)) {
                    foreignPackageSuperTypes.put(packageName, superClassElement);
                    for (TypeParameterElement typeParameter : superClassElement.getTypeParameters()) {
                        foreignPackageSuperTypeVariables.add(context.getTypeUtils().asMemberOf((DeclaredType) element.asType(), typeParameter));
                    }
                }
            }
            superClass = superClassElement.getSuperclass();
        }

        this.idMember = idMember;
        this.versionMember = versionMember;
        this.members = members;
        this.constructors = constructors;
        this.specialMembers = specialMembers;
        this.foreignPackageSuperTypes = foreignPackageSuperTypes;
        this.foreignPackageSuperTypeVariables = foreignPackageSuperTypeVariables;
        this.postCreate = postCreate;
        this.postLoad = postLoad;
    }

    public final Context getContext() {
        return context;
    }

    @Override
    public Map<String, TypeElement> getForeignPackageSuperTypes() {
        return foreignPackageSuperTypes;
    }

    @Override
    public List<TypeMirror> getForeignPackageSuperTypeVariables() {
        return foreignPackageSuperTypeVariables;
    }

    @Override
    public boolean isUpdatable() {
        return updatable;
    }

    @Override
    public boolean isCreatable() {
        return creatable;
    }

    @Override
    public boolean isAllSupportDirtyTracking() {
        return allSupportDirtyTracking;
    }

    @Override
    public int getMutableAttributeCount() {
        return mutableAttributeCount;
    }

    @Override
    public boolean hasEmptyConstructor() {
        return hasEmptyConstructor;
    }

    @Override
    public boolean hasSelfConstructor() {
        return hasSelfConstructor;
    }

    @Override
    public boolean isValid() {
        return valid;
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
    public String getEntityClass() {
        return entityClass;
    }

    @Override
    public Element getEntityVersionAttribute() {
        return entityVersionAttribute;
    }

    @Override
    public ExecutableElement getPostCreate() {
        return postCreate;
    }

    @Override
    public ExecutableElement getPostLoad() {
        return postLoad;
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
    public MetaAttribute getVersionMember() {
        return versionMember;
    }

    @Override
    public int getDefaultDirtyMask() {
        return defaultDirtyMask;
    }

    @Override
    public Collection<MetaConstructor> getConstructors() {
        return constructors;
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
    public String importTypeExceptMetamodel(String fqcn) {
        implementationImportContext.importType(fqcn);
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

    public boolean addAccessorForType(String realType) {
        return addedAccessors.add(realType);
    }
}
