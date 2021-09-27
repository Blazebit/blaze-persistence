/*
 * Copyright 2014 - 2021 Blazebit.
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
import com.blazebit.persistence.view.processor.ViewFilter;

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
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
    private final ImportContext relationImportContext;
    private final ImportContext multiRelationImportContext;
    private final ImportContext implementationImportContext;
    private final ImportContext builderImportContext;
    private final TypeElement element;
    private final Element[] originatingElements;
    private final String entityClass;
    private final String jpaManagedBaseClass;
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
    private final Map<String, TypeElement> optionalParameters;
    private final Map<String, ViewFilter> viewFilters;
    private final boolean updatable;
    private final boolean creatable;
    private final boolean allSupportDirtyTracking;
    private final int mutableAttributeCount;
    private final int defaultDirtyMask;
    private final boolean hasEmptyConstructor;
    private final boolean hasSelfConstructor;
    private final boolean hasSubviews;
    private final boolean valid;
    private final Context context;
    private final Set<String> addedAccessors = new HashSet<>();

    public AnnotationMetaEntityView(TypeElement element, Context context) {
        this.element = element;
        this.context = context;
        this.metamodelImportContext = new ImportContextImpl(getPackageName());
        this.relationImportContext = new ImportContextImpl(getPackageName());
        this.multiRelationImportContext = new ImportContextImpl(getPackageName());
        this.implementationImportContext = new ImportContextImpl(getPackageName());
        this.builderImportContext = new ImportContextImpl(getPackageName());
        getContext().logMessage(Diagnostic.Kind.OTHER, "Initializing type " + getQualifiedName() + ".");

        String entityClass = null;
        boolean updatable = false;
        boolean creatable = false;
        boolean allSupportDirtyTracking = true;
        Map<String, ViewFilter> viewFilters = new HashMap<>();
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            switch (annotationMirror.getAnnotationType().toString()) {
                case Constants.ENTITY_VIEW:
                    entityClass = TypeUtils.getAnnotationValue(annotationMirror, "value").toString();
                    break;
                case Constants.UPDATABLE_ENTITY_VIEW:
                    updatable = true;
                    break;
                case Constants.CREATABLE_ENTITY_VIEW:
                    creatable = true;
                    break;
                case Constants.VIEW_FILTER:
                    addViewFilter(viewFilters, annotationMirror, context);
                    break;
                case Constants.VIEW_FILTERS:
                    for (AnnotationMirror value : TypeUtils.<List<AnnotationMirror>>getAnnotationValue(annotationMirror, "value")) {
                        addViewFilter(viewFilters, value, context);
                    }
                    break;
                default:
                    break;
            }
        }

        Element entityVersionAttribute = null;
        String jpaManagedBaseClass = null;
        TypeElement entityTypeElement = context.getTypeElement(entityClass);
        for (Element member : context.getAllMembers(entityTypeElement)) {
            if (TypeUtils.containsAnnotation(member, Constants.VERSION)) {
                entityVersionAttribute = member;
                break;
            }
        }

        if (isEntity(entityTypeElement)) {
            TypeElement entityElement = entityTypeElement;
            do {
                entityTypeElement = context.getTypeElement(TypeUtils.extractClosestRealTypeAsString(entityElement.getSuperclass(), context));
                if (isEntity(entityTypeElement)) {
                    entityElement = entityTypeElement;
                } else {
                    break;
                }
            } while (true);

            jpaManagedBaseClass = entityElement.getQualifiedName().toString();
        }
        if (jpaManagedBaseClass == null) {
            jpaManagedBaseClass = entityTypeElement.getQualifiedName().toString();
        }
        this.entityVersionAttribute = entityVersionAttribute;
        this.entityClass = entityClass;
        this.jpaManagedBaseClass = jpaManagedBaseClass;
        this.updatable = updatable;
        this.creatable = creatable;
        this.viewFilters = viewFilters;

        Iterable<? extends Element> allMembers = context.getAllMembers(element);
        MetaAttribute idMember = null;
        MetaAttribute versionMember = null;
        Map<String, MetaAttribute> members = new TreeMap<>();
        Map<String, ExecutableElement> specialMembers = new TreeMap<>();
        List<MetaConstructor> constructors = new ArrayList<>();
        MetaAttributeGenerationVisitor visitor = new MetaAttributeGenerationVisitor(this, context);
        Map<String, TypeElement> optionalParameters = new HashMap<>();
        boolean valid = true;
        boolean hasEmptyConstructor = false;
        boolean hasSelfConstructor = false;
        boolean hasSubviews = false;
        ExecutableElement postCreate = null;
        ExecutableElement postLoad = null;
        Set<Element> originatingElements = new HashSet<>();
        originatingElements.add(element);
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
                            for (Map.Entry<String, TypeElement> entry : result.getOptionalParameters().entrySet()) {
                                TypeElement typeElement = entry.getValue();
                                TypeElement existingTypeElement = optionalParameters.get(entry.getKey());
                                if (existingTypeElement == null || context.getTypeUtils().isAssignable(typeElement.asType(), existingTypeElement.asType())) {
                                    optionalParameters.put(entry.getKey(), entry.getValue());
                                }
                            }

                            if (result.isSubview()) {
                                hasSubviews = true;
                                originatingElements.add(result.getSubviewElement());
                            }
                        }
                    } else if (!modifiers.contains(Modifier.PRIVATE) && memberOfClass.getKind() == ElementKind.CONSTRUCTOR) {
                        AnnotationMetaConstructor constructor = new AnnotationMetaConstructor(this, executableElement, visitor, context);
                        hasEmptyConstructor = hasEmptyConstructor || constructor.getParameters().isEmpty();
                        hasSelfConstructor = hasSelfConstructor || constructor.hasSelfParameter();
                        constructors.add(constructor);
                        for (Map.Entry<String, TypeElement> entry : constructor.getOptionalParameters().entrySet()) {
                            TypeElement existingTypeElement = optionalParameters.get(entry.getKey());
                            TypeElement typeElement = entry.getValue();
                            if (existingTypeElement != null && context.getTypeUtils().isAssignable(typeElement.asType(), existingTypeElement.asType())) {
                                optionalParameters.put(entry.getKey(), entry.getValue());
                            }
                        }
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
                        defaultDirtyMask |= 1 << dirtyStateIndex;
                    }
                    dirtyStateIndex++;
                }
                index++;
            }
        }

        this.hasEmptyConstructor = hasEmptyConstructor || constructors.isEmpty();
        this.hasSelfConstructor = hasSelfConstructor;
        this.hasSubviews = hasSubviews;
        this.valid = valid;
        this.allSupportDirtyTracking = allSupportDirtyTracking;
        this.mutableAttributeCount = dirtyStateIndex;
        this.defaultDirtyMask = defaultDirtyMask;

        if (constructors.isEmpty()) {
            constructors.add(new AnnotationMetaConstructor(this));
        } else {
            constructors.sort(MetaConstructor.NAME_COMPARATOR);
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
                        foreignPackageSuperTypeVariables.add(TypeUtils.asMemberOf(context, (DeclaredType) element.asType(), typeParameter));
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
        this.optionalParameters = optionalParameters;
        this.postCreate = postCreate;
        this.postLoad = postLoad;
        this.originatingElements = originatingElements.toArray(new Element[0]);
    }

    private static boolean isEntity(TypeElement typeElement) {
        for (AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().toString().equals(Constants.ENTITY)) {
                return true;
            }
        }

        return false;
    }

    private static void addViewFilter(Map<String, ViewFilter> filters, AnnotationMirror mirror, Context context) {
        String name = "";
        TypeMirror type = null;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
            switch (entry.getKey().getSimpleName().toString()) {
                case "name":
                    name = (String) entry.getValue().getValue();
                    break;
                case "value":
                    type = (TypeMirror) entry.getValue().getValue();
                    break;
                default:
                    break;
            }
        }
        filters.put(name, new ViewFilter(name, (TypeElement) ((DeclaredType) type).asElement(), context));
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
    public boolean hasSubviews() {
        return hasSubviews;
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
    public String getJpaManagedBaseClass() {
        return jpaManagedBaseClass;
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
        return packageOf.getQualifiedName().toString();
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
    public ImportContext getRelationImportContext() {
        return relationImportContext;
    }

    @Override
    public ImportContext getMultiRelationImportContext() {
        return multiRelationImportContext;
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
        relationImportContext.importType(fqcn);
        multiRelationImportContext.importType(fqcn);
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
    public String relationImportType(String fqcn) {
        return relationImportContext.importType(fqcn);
    }

    @Override
    public String multiRelationImportType(String fqcn) {
        return multiRelationImportContext.importType(fqcn);
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

    @Override
    public Element[] getOriginatingElements() {
        return originatingElements;
    }

    @Override
    public Map<String, TypeElement> getOptionalParameters() {
        return optionalParameters;
    }

    @Override
    public Map<String, ViewFilter> getViewFilters() {
        return viewFilters;
    }

    @Override
    public String getSafeTypeVariable(String typeVariable) {
        List<TypeVariable> typeArguments = (List<TypeVariable>) ((DeclaredType) element.asType()).getTypeArguments();
        if (typeArguments.isEmpty()) {
            return typeVariable;
        }
        String originalTypeVariable = typeVariable;
        int suffix = 0;
        OUTER: do {
            for (TypeVariable variable : typeArguments) {
                if (typeVariable.equals(variable.asElement().getSimpleName().toString())) {
                    typeVariable = originalTypeVariable + (suffix++);
                    continue OUTER;
                }
            }

            return typeVariable;
        } while (true);
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
