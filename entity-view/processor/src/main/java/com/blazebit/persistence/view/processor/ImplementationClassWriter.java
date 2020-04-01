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

package com.blazebit.persistence.view.processor;

import com.blazebit.persistence.view.processor.annotation.AnnotationMetaCollection;
import com.blazebit.persistence.view.processor.annotation.AnnotationMetaVersionAttribute;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class ImplementationClassWriter {

    public static final String IMPL_CLASS_NAME_SUFFIX = "Impl";
    // The following two must be aligned with com.blazebit.persistence.view.SerializableEntityViewManager
    public static final String EVM_FIELD_NAME = "ENTITY_VIEW_MANAGER";
    public static final String SERIALIZABLE_EVM_FIELD_NAME = "SERIALIZABLE_ENTITY_VIEW_MANAGER";
    private static final String NEW_LINE = System.lineSeparator();

    private ImplementationClassWriter() {
    }

    public static void writeFile(StringBuilder sb, MetaEntityView entity, Context context) {
        sb.setLength(0);
        generateBody(sb, entity, context);
        ClassWriterUtils.writeFile(sb, entity.getPackageName(), entity.getSimpleName() + IMPL_CLASS_NAME_SUFFIX, entity.getImplementationImportContext(), context);
    }

    private static void generateBody(StringBuilder sb, MetaEntityView entity, Context context) {
        if (context.addGeneratedAnnotation()) {
            ClassWriterUtils.writeGeneratedAnnotation(sb, entity.getImplementationImportContext(), context);
            sb.append(NEW_LINE);
        }
        if (context.isAddSuppressWarningsAnnotation()) {
            sb.append(ClassWriterUtils.writeSuppressWarnings());
            sb.append(NEW_LINE);
        }

        sb.append("@").append(entity.implementationImportType(Constants.STATIC_IMPLEMENTATION)).append("(").append(entity.metamodelImportType(entity.getQualifiedName())).append(".class)");
        sb.append(NEW_LINE);
        printClassDeclaration(sb, entity, context);

        sb.append(NEW_LINE);

        sb.append("    public static volatile ").append(entity.implementationImportType(Constants.ENTITY_VIEW_MANAGER)).append(" ").append(EVM_FIELD_NAME).append(";");
        sb.append(NEW_LINE);
        sb.append("    public static final ").append(entity.implementationImportType(Constants.SERIALIZABLE_ENTITY_VIEW_MANAGER)).append(" ").append(SERIALIZABLE_EVM_FIELD_NAME);
        sb.append(" = new ").append(entity.implementationImportType(Constants.SERIALIZABLE_ENTITY_VIEW_MANAGER)).append("(").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append(".class, ").append(EVM_FIELD_NAME).append(");");
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);

        Collection<MetaAttribute> members = entity.getMembers();
        for (MetaAttribute metaMember : members) {
            if (!(metaMember instanceof AnnotationMetaVersionAttribute)) {
                metaMember.appendImplementationAttributeDeclarationString(sb);
                sb.append(NEW_LINE);
            }
        }

        sb.append(NEW_LINE);

        printConstructors(sb, entity, context);

        sb.append(NEW_LINE);

        for (MetaAttribute metaMember : members) {
            if (!(metaMember instanceof AnnotationMetaVersionAttribute)) {
                metaMember.appendImplementationAttributeGetterAndSetterString(sb, context);
                sb.append(NEW_LINE);
            }
        }

        sb.append(NEW_LINE);

        for (ExecutableElement specialMember : entity.getSpecialMembers()) {
            if (Constants.ENTITY_VIEW_MANAGER.equals(specialMember.getReturnType().toString())) {
                sb.append("    @Override").append(NEW_LINE);
                sb.append("    public ").append(entity.implementationImportType(specialMember.getReturnType().toString())).append(" ").append(specialMember.getSimpleName().toString()).append("() {").append(NEW_LINE);
                sb.append("        return ").append(SERIALIZABLE_EVM_FIELD_NAME).append(";").append(NEW_LINE);
                sb.append("    }").append(NEW_LINE);
                sb.append(NEW_LINE);
            } else {
                context.logMessage(Diagnostic.Kind.ERROR, "Unsupported special member: " + specialMember);
            }
        }

        MetaAttribute version = entity.getVersionMember();
        sb.append(NEW_LINE);
        if (entity.isCreatable()) {
            sb.append("    private boolean $$_isNew;").append(NEW_LINE);
        }
        if (entity.isCreatable() || entity.isUpdatable()) {
            sb.append("    private final Object[] $$_initialState;").append(NEW_LINE);
            sb.append("    private final Object[] $$_mutableState;").append(NEW_LINE);
            sb.append("    private final boolean $$_initialized;").append(NEW_LINE);
            sb.append("    private ").append(entity.implementationImportType(Constants.LIST)).append("<Object> $$_readOnlyParents;").append(NEW_LINE);
            sb.append("    private ").append(entity.implementationImportType(Constants.DIRTY_TRACKER)).append(" $$_parent;").append(NEW_LINE);
            sb.append("    private int $$_parentIndex;").append(NEW_LINE);
            sb.append("    private long $$_dirty = ").append(entity.getDefaultDirtyMask()).append("L;").append(NEW_LINE);
        }
        if (version != null && version.getPropertyName().equals("$$_version")) {
            sb.append("    private ").append(version.getImplementationTypeString()).append(" ").append(version.getPropertyName()).append(";").append(NEW_LINE);
        }
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public Class<?> $$_getJpaManagedClass() {").append(NEW_LINE);
        sb.append("        return ").append(entity.implementationImportType(entity.getEntityClass())).append(".class;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public Class<?> $$_getJpaManagedBaseClass() {").append(NEW_LINE);
        sb.append("        return ").append(entity.implementationImportType(getJpaManagedBaseClass(entity.getEntityClass(), context))).append(".class;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        String entityViewClassName = entity.implementationImportType(entity.getQualifiedName());
        sb.append("    public Class<?> $$_getEntityViewClass() {").append(NEW_LINE);
        sb.append("        return ").append(entityViewClassName).append(".class;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public boolean $$_isNew() {").append(NEW_LINE);
        if (entity.isCreatable()) {
            sb.append("        return $$_isNew;").append(NEW_LINE);
        } else {
            sb.append("        return false;").append(NEW_LINE);
        }
        sb.append("    }").append(NEW_LINE);

        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public Object $$_getId() {").append(NEW_LINE);
        sb.append("        return ").append(entity.getIdMember() == null ? "null" : entity.getIdMember().getPropertyName()).append(";").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public Object $$_getVersion() {").append(NEW_LINE);
        if (version != null) {
            sb.append("        return ").append(version.getPropertyName()).append(";").append(NEW_LINE);
        } else {
            sb.append("        return null;").append(NEW_LINE);
        }
        sb.append("    }").append(NEW_LINE);

        if (entity.isCreatable() || entity.isUpdatable()) {
            // BasicDirtyTracker
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public boolean $$_isDirty() {").append(NEW_LINE);
            if (entity.isAllSupportDirtyTracking()) {
                sb.append("        return $$_dirty != 0L;").append(NEW_LINE);
            } else {
                sb.append("        return true;").append(NEW_LINE);
            }
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public void $$_markDirty(int attributeIndex) {").append(NEW_LINE);
            sb.append("        this.$$_dirty |= (1 << attributeIndex);").append(NEW_LINE);
            sb.append("        if (this.$$_parent != null) {").append(NEW_LINE);
            sb.append("            this.$$_parent.$$_markDirty(this.$$_parentIndex);").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public void $$_unmarkDirty() {").append(NEW_LINE);
            sb.append("        this.$$_dirty = ").append(entity.getDefaultDirtyMask()).append("L;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public void $$_setParent(").append(entity.implementationImportType(Constants.BASIC_DIRTY_TRACKER)).append(" parent, int parentIndex) {").append(NEW_LINE);
            sb.append("        if (this.$$_parent != null) {").append(NEW_LINE);
            sb.append("            throw new IllegalStateException(\"Parent object for \" + this.toString() + \" is already set to \" + this.$$_parent.toString() + \" and can't be set to: \" + parent.toString());").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);
            sb.append("        this.$$_parent = (").append(entity.implementationImportType(Constants.DIRTY_TRACKER)).append(") parent;").append(NEW_LINE);
            sb.append("        this.$$_parentIndex = parentIndex;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public boolean $$_hasParent() {").append(NEW_LINE);
            sb.append("        return this.$$_parent != null;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public void $$_unsetParent() {").append(NEW_LINE);
            sb.append("        if (this.$$_parent != null && this.$$_readOnlyParents != null && !this.$$_readOnlyParents.isEmpty()) {").append(NEW_LINE);
            sb.append("            throw new IllegalStateException(\"Can't unset writable parent \" + this.$$_parent.toString() + \" on object \" + this.toString() + \" because it is still connected to read only parents: \" + this.$$_readOnlyParents);").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);
            sb.append("        this.$$_parent = null;").append(NEW_LINE);
            sb.append("        this.$$_parentIndex = 0;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);

            // DirtyTracker
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public boolean $$_isDirty(int attributeIndex) {").append(NEW_LINE);
            if (!entity.isAllSupportDirtyTracking()) {
                sb.append("        switch (attributeIndex) {").append(NEW_LINE);
                for (MetaAttribute member : members) {
                    if (member.getDirtyStateIndex() != -1 && !member.supportsDirtyTracking()) {
                        sb.append("            case ").append(member.getDirtyStateIndex()).append(": return true;").append(NEW_LINE);
                    }
                }

                sb.append("        }").append(NEW_LINE);
            }
            sb.append("        return (this.$$_dirty & (1L << attributeIndex)) != 0;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public <T> boolean $$_copyDirty(T[] source, T[] target) {").append(NEW_LINE);
            sb.append("        if (this.$$_dirty == 0L) {").append(NEW_LINE);
            sb.append("            return false;").append(NEW_LINE);
            sb.append("        } else {").append(NEW_LINE);
            for (MetaAttribute member : members) {
                if ((member.getDirtyStateIndex() != -1)) {
                    if (member.supportsDirtyTracking()) {
                        long mask = 1 << member.getDirtyStateIndex();
                        sb.append("            target[").append(member.getDirtyStateIndex()).append("] = (this.$$_dirty & ").append(mask).append(") == 0 ? null : source[").append(member.getDirtyStateIndex()).append("];").append(NEW_LINE);
                    } else {
                        sb.append("            target[").append(member.getDirtyStateIndex()).append("] = source[").append(member.getDirtyStateIndex()).append("];").append(NEW_LINE);
                    }
                }
            }
            sb.append("            return true;").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public void $$_setDirty(long[] dirty) {").append(NEW_LINE);
            if (entity.getDefaultDirtyMask() == 0) {
                sb.append("        this.$$_dirty = dirty[0];").append(NEW_LINE);
            } else {
                sb.append("        this.$$_dirty = dirty[0] | ").append(entity.getDefaultDirtyMask()).append("L;").append(NEW_LINE);
            }
            sb.append("        if (this.$$_dirty != 0L && this.$$_parent != null) {").append(NEW_LINE);
            sb.append("            this.$$_parent.$$_markDirty(this.$$_parentIndex);").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public long[] $$_resetDirty() {").append(NEW_LINE);
            sb.append("        long[] dirty = new long[]{ this.$$_dirty };").append(NEW_LINE);
            sb.append("        this.$$_dirty = ").append(entity.getDefaultDirtyMask()).append("L;").append(NEW_LINE);
            sb.append("        return dirty;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public long[] $$_getDirty() {").append(NEW_LINE);
            sb.append("        return new long[]{ this.$$_dirty };").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public long $$_getSimpleDirty() {").append(NEW_LINE);
            sb.append("        return $$_dirty;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public void $$_replaceAttribute(Object oldObject, int attributeIndex, Object newObject) {").append(NEW_LINE);
            sb.append("        switch (attributeIndex) {").append(NEW_LINE);
            for (MetaAttribute member : members) {
                if (member.getDirtyStateIndex() != -1 && member.getSetter() != null) {
                    sb.append("            case ").append(member.getDirtyStateIndex()).append(": ");
                    sb.append(member.getSetter().getSimpleName().toString()).append('(');
                    if (member.isPrimitive()) {
                        appendUnwrap(sb, member.getRealType(), "newObject");
                    } else {
                        sb.append("(").append(member.getImplementationTypeString()).append(") newObject");
                    }
                    sb.append("); break;").append(NEW_LINE);
                }
            }
            sb.append("            default: throw new IllegalArgumentException(\"Invalid non-mutable attribute index: \" + attributeIndex);").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);

            // MutableStateTrackable
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public Object[] $$_getMutableState() {").append(NEW_LINE);
            sb.append("        return $$_mutableState;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public DirtyTracker $$_getParent() {").append(NEW_LINE);
            sb.append("        return $$_parent;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public List<Object> $$_getReadOnlyParents() {").append(NEW_LINE);
            sb.append("        return $$_readOnlyParents;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public void $$_addReadOnlyParent(DirtyTracker readOnlyParent, int parentIndex) {").append(NEW_LINE);
            if (context.isStrictCascadingCheck()) {
                sb.append("        if (this != readOnlyParent && this.$$_parent == null) {").append(NEW_LINE);
                sb.append("            throw new IllegalStateException(\"Can't set read only parent for object \" + this.toString() + \" util it doesn't have a writable parent! First add the object to an attribute with proper cascading. If you just want to reference it convert the object with EntityViewManager.getReference() or EntityViewManager.convert()!\");").append(NEW_LINE);
                sb.append("        }").append(NEW_LINE);
            }
            sb.append("        if (this.$$_readOnlyParents == null) {").append(NEW_LINE);
            sb.append("            this.$$_readOnlyParents = new ").append(entity.implementationImportType(Constants.ARRAY_LIST)).append("<>();").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);
            sb.append("        this.$$_readOnlyParents.add(readOnlyParent);").append(NEW_LINE);
            sb.append("        this.$$_readOnlyParents.add(parentIndex);").append(NEW_LINE);
            sb.append("        return;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public void $$_removeReadOnlyParent(DirtyTracker readOnlyParent, int parentIndex) {").append(NEW_LINE);
            sb.append("        if (this.$$_readOnlyParents != null) {").append(NEW_LINE);
            sb.append("            int size = this.$$_readOnlyParents.size();").append(NEW_LINE);
            sb.append("            for (int i = 0; i < size; i += 2) {").append(NEW_LINE);
            sb.append("                if (this.$$_readOnlyParents.get(i) == readOnlyParent && ((Integer) this.$$_readOnlyParents.get(i + 1)).intValue() == parentIndex) {").append(NEW_LINE);
            sb.append("                    this.$$_readOnlyParents.remove(i + 1);").append(NEW_LINE);
            sb.append("                    this.$$_readOnlyParents.remove(i);").append(NEW_LINE);
            sb.append("                    break;").append(NEW_LINE);
            sb.append("                }").append(NEW_LINE);
            sb.append("            }").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public int $$_getParentIndex() {").append(NEW_LINE);
            sb.append("        return $$_parentIndex;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public void $$_setIsNew(boolean isNew) {").append(NEW_LINE);
            if (entity.isCreatable()) {
                sb.append("        this.$$_isNew = isNew;").append(NEW_LINE);
            } else {
                sb.append("        // No-op").append(NEW_LINE);
            }
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public void $$_setId(Object id) {").append(NEW_LINE);
            if (entity.getIdMember() == null) {
                sb.append("        throw new UnsupportedOperationException(\"No id attribute available!\");").append(NEW_LINE);
            } else {
                sb.append("        this.").append(entity.getIdMember().getPropertyName()).append(" = (").append(entity.getIdMember().getImplementationTypeString()).append(") id;").append(NEW_LINE);
            }
            sb.append("    }").append(NEW_LINE);
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public void $$_setVersion(Object version) {").append(NEW_LINE);
            if (version == null) {
                sb.append("        throw new UnsupportedOperationException(\"No version attribute available!\");").append(NEW_LINE);
            } else {
                sb.append("        this.").append(version.getPropertyName()).append(" = ");
                if (version.isPrimitive()) {
                    appendUnwrap(sb, version.getRealType(), "version");
                } else {
                    sb.append("(").append(version.getImplementationTypeString()).append(") version");
                }
                sb.append(";").append(NEW_LINE);
            }
            sb.append("    }").append(NEW_LINE);

            // DirtyStateTrackable
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public Object[] $$_getInitialState() {").append(NEW_LINE);
            sb.append("        return $$_initialState;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);

        }

        appendEqualsHashCodeAndToString(sb, entity, context, members, entityViewClassName);

        sb.append("}");
        sb.append(NEW_LINE);
    }

    private static void appendUnwrap(StringBuilder sb, String type, String field) {
        if ("long".equals(type)) {
            sb.append("((Long) ").append(field).append(").longValue()");
        } else if ("float".equals(type)) {
            sb.append("((Float) ").append(field).append(").floatValue()");
        } else if ("double".equals(type)) {
            sb.append("((Double) ").append(field).append(").doubleValue()");
        } else if ("int".equals(type)) {
            sb.append("((Integer) ").append(field).append(").intValue()");
        } else if ("short".equals(type)) {
            sb.append("((Short) ").append(field).append(").shortValue()");
        } else if ("byte".equals(type)) {
            sb.append("((Byte) ").append(field).append(").byteValue()");
        } else if ("boolean".equals(type)) {
            sb.append("((Boolean) ").append(field).append(").booleanValue()");
        } else if ("char".equals(type)) {
            sb.append("((Character) ").append(field).append(").charValue()");
        } else {
            throw new UnsupportedOperationException("Unwrap not possible for type: " + type);
        }
    }

    private static void appendEqualsHashCodeAndToString(StringBuilder sb, MetaEntityView entity, Context context, Collection<MetaAttribute> members, String entityViewClassName) {
        boolean generateEqualsHashCode = !hasCustom(context, entity.getTypeElement(), "equals", "java.lang.Object") && !hasCustom(context, entity.getTypeElement(), "hashCode");
        if (generateEqualsHashCode) {
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public boolean equals(Object obj) {").append(NEW_LINE);
            sb.append("        if (this == obj) {").append(NEW_LINE);
            sb.append("            return true;").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);
            Collection<MetaAttribute> equalityMembers;
            if (entity.getIdMember() == null) {
                equalityMembers = members;
            } else {
                equalityMembers = Collections.singleton(entity.getIdMember());
                sb.append("        if (obj == null || this.$$_getId() == null) {").append(NEW_LINE);
                sb.append("            return false;").append(NEW_LINE);
                sb.append("        }").append(NEW_LINE);
                sb.append("        if (obj instanceof ").append(entity.implementationImportType(Constants.ENTITY_VIEW_PROXY)).append(") {").append(NEW_LINE);
                sb.append("            ").append(entity.implementationImportType(Constants.ENTITY_VIEW_PROXY)).append(" other = (").append(entity.implementationImportType(Constants.ENTITY_VIEW_PROXY)).append(") obj;").append(NEW_LINE);
                sb.append("            if (this.$$_getJpaManagedBaseClass() == other.$$_getJpaManagedBaseClass() && this.$$_getId().equals(other.$$_getId())) {").append(NEW_LINE);
                sb.append("                return true;").append(NEW_LINE);
                sb.append("            } else {").append(NEW_LINE);
                sb.append("                return false;").append(NEW_LINE);
                sb.append("            }").append(NEW_LINE);
                sb.append("        }").append(NEW_LINE);
            }
            String baseType = entity.implementationImportType(entity.getBaseSuperclass());
            if (!baseType.equals(entityViewClassName)) {
                boolean needsForeignPackageClass = false;
                for (MetaAttribute equalityMember : equalityMembers) {
                    Set<Modifier> modifiers = equalityMember.getElement().getModifiers();
                    boolean containsProtected = modifiers.contains(Modifier.PROTECTED);
                    if (!modifiers.contains(Modifier.PUBLIC) && !containsProtected || containsProtected && !TypeUtils.getPackageName(entity.getTypeElement()).equals(TypeUtils.getPackageName(equalityMember.getElement()))) {
                        needsForeignPackageClass = true;
                    }
                }
                if (!needsForeignPackageClass) {
                    baseType = entityViewClassName;
                }
            }
            sb.append("        if (obj instanceof ").append(baseType).append(") {").append(NEW_LINE);
            sb.append("            ").append(baseType).append(" other = (").append(baseType).append(") obj;").append(NEW_LINE);
            for (MetaAttribute member : equalityMembers) {
                if (!(member instanceof AnnotationMetaVersionAttribute)) {
                    if (member.isPrimitive()) {
                        sb.append("            if (this.").append(member.getPropertyName()).append(" != other.").append(member.getElement().toString()).append(") {").append(NEW_LINE);
                    } else {
                        sb.append("            if (!").append(entity.implementationImportType(Objects.class.getName())).append(".equals(this.").append(member.getPropertyName()).append(", other.").append(member.getElement().toString()).append(")) {").append(NEW_LINE);
                    }

                    sb.append("                return false;").append(NEW_LINE);
                    sb.append("            }").append(NEW_LINE);
                }
            }
            sb.append("            return true;").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);
            sb.append("        return false;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);

            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public int hashCode() {").append(NEW_LINE);
            sb.append("        long bits;").append(NEW_LINE);
            sb.append("        int hash = 3;").append(NEW_LINE);
            for (MetaAttribute member : equalityMembers) {
                if (!(member instanceof AnnotationMetaVersionAttribute)) {
                    String type = member.getRealType();
                    if ("double".equals(type)) {
                        sb.append("        bits = java.lang.Double.doubleToLongBits(this.").append(member.getPropertyName()).append(");").append(NEW_LINE);
                    }
                    sb.append("        hash = 83 * hash + ");
                    if (member.isPrimitive()) {
                        if ("boolean".equals(type)) {
                            sb.append("(this.").append(member.getPropertyName()).append(" ? 1231 : 1237)");
                        } else if ("byte".equals(type) || "short".equals(type) || "char".equals(type)) {
                            sb.append("(int) this.").append(member.getPropertyName());
                        } else if ("int".equals(type)) {
                            sb.append("this.").append(member.getPropertyName());
                        } else if ("long".equals(type)) {
                            sb.append("(int)(this.").append(member.getPropertyName()).append(" ^ (this.").append(member.getPropertyName()).append(" >>> 32))");
                        } else if ("float".equals(type)) {
                            sb.append("java.lang.Float.floatToIntBits(this.").append(member.getPropertyName()).append(")");
                        } else if ("double".equals(type)) {
                            sb.append("(int)(bits ^ (bits >>> 32))");
                        } else {
                            throw new IllegalArgumentException("Unsupported primitive type: " + type);
                        }
                    } else {
                        sb.append("(this.").append(member.getPropertyName()).append(" != null ? this.").append(member.getPropertyName()).append(".hashCode() : 0)");
                    }
                    sb.append(";").append(NEW_LINE);
                }
            }
            sb.append("        return hash;").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
        }
        boolean generateToString = !hasCustom(context, entity.getTypeElement(), "toString");
        if (generateToString) {
            sb.append("    @Override").append(NEW_LINE);
            sb.append("    public String toString() {").append(NEW_LINE);
            if (entity.getIdMember() == null) {
                int sizeEstimate = entityViewClassName.length() + 2;
                for (MetaAttribute member : members) {
                    if (!(member instanceof AnnotationMetaVersionAttribute)) {
                        // 5 is the amount of chars for the format
                        // 10 is the amount of chars that we assume is needed to represent a value on average
                        sizeEstimate += member.getPropertyName().length() + 5 + 10;
                    }
                }
                sb.append("        StringBuilder sb = new StringBuilder(").append(sizeEstimate).append(");").append(NEW_LINE);
                sb.append("        sb.append(\"").append(entityViewClassName).append("(\");").append(NEW_LINE);
                if (!members.isEmpty()) {
                    Iterator<MetaAttribute> iterator = members.iterator();
                    MetaAttribute member = iterator.next();
                    sb.append("        sb.append(\"").append(member.getPropertyName()).append(" = \").append(this.").append(member.getPropertyName()).append(");").append(NEW_LINE);
                    while (iterator.hasNext()) {
                        member = iterator.next();
                        if (!(member instanceof AnnotationMetaVersionAttribute)) {
                            sb.append("        sb.append(\", \");").append(NEW_LINE);
                            sb.append("        sb.append(\"").append(member.getPropertyName()).append(" = \").append(this.").append(member.getPropertyName()).append(");").append(NEW_LINE);
                        }
                    }
                }
                sb.append("        sb.append(')');").append(NEW_LINE);
                sb.append("        return sb.toString();").append(NEW_LINE);
            } else {
                sb.append("        return \"").append(entityViewClassName).append("(").append(entity.getIdMember().getPropertyName()).append(" = \" + this.").append(entity.getIdMember().getPropertyName()).append(" + \")\";").append(NEW_LINE);
            }
            sb.append("    }").append(NEW_LINE);
        }
    }

    private static boolean hasCustom(Context context, TypeElement typeElement, String methodName, String... argumentTypes) {
        if (typeElement.getQualifiedName().toString().equals("java.lang.Object")) {
            return false;
        }
        OUTER: for (Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement instanceof ExecutableElement && methodName.equals(enclosedElement.getSimpleName().toString())) {
                ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                List<? extends VariableElement> parameters = executableElement.getParameters();
                if (argumentTypes.length == parameters.size()) {
                    for (int i = 0; i < argumentTypes.length; i++) {
                        String argumentType = argumentTypes[i];
                        if (!argumentType.equals(parameters.get(i).asType().toString())) {
                            continue OUTER;
                        }
                    }

                    return true;
                }
            }
        }

        if (typeElement.getSuperclass().getKind() == TypeKind.NONE) {
            return false;
        }
        TypeElement superClass;
        if (typeElement.getSuperclass() instanceof DeclaredType) {
            superClass = context.getElementUtils().getTypeElement(((DeclaredType) typeElement.getSuperclass()).asElement().toString());
        } else {
            superClass = context.getElementUtils().getTypeElement(((TypeElement) typeElement.getSuperclass()).getQualifiedName());
        }
        return hasCustom(context, superClass, methodName, argumentTypes);
    }

    private static String getJpaManagedBaseClass(String entityClass, Context context) {
        TypeElement typeElement = context.getElementUtils().getTypeElement(entityClass);
        if (isEntity(typeElement)) {
            TypeElement entityElement = typeElement;
            do {
                typeElement = context.getElementUtils().getTypeElement(entityElement.getSuperclass().toString());
                if (isEntity(typeElement)) {
                    entityElement = typeElement;
                } else {
                    break;
                }
            } while (true);

            return entityElement.getQualifiedName().toString();
        }
        return typeElement.getQualifiedName().toString();
    }

    public static boolean isEntity(TypeElement typeElement) {
        for (AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().toString().equals(Constants.ENTITY)) {
                return true;
            }
        }

        return false;
    }

    private static void printClassDeclaration(StringBuilder sb, MetaEntityView entity, Context context) {
        sb.append("public class ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX);

        List<TypeVariable> typeArguments = (List<TypeVariable>) ((DeclaredType) entity.getTypeElement().asType()).getTypeArguments();
        if (!typeArguments.isEmpty()) {
            sb.append("<");
            printTypeVariable(sb, entity, typeArguments.get(0));
            for (int i = 1; i < typeArguments.size(); i++) {
                sb.append(", ");
                printTypeVariable(sb, entity, typeArguments.get(i));
            }
            sb.append(">");
        }

        if (entity.getTypeElement().getKind() == ElementKind.INTERFACE) {
            sb.append(" implements ");
        } else {
            sb.append(" extends ");
        }
        sb.append(entity.implementationImportType(entity.getBaseSuperclass()));

        if (typeArguments.isEmpty()) {
            if (!entity.getForeignPackageSuperTypeVariables().isEmpty()) {
                sb.append("<");
                sb.append(entity.getForeignPackageSuperTypeVariables().get(0));
                for (int i = 1; i < entity.getForeignPackageSuperTypeVariables().size(); i++) {
                    sb.append(", ");
                    sb.append(entity.getForeignPackageSuperTypeVariables().get(i));
                }
                sb.append(">");
            }
        } else {
            sb.append("<");
            if (!entity.getForeignPackageSuperTypeVariables().isEmpty()) {
                sb.append(entity.getForeignPackageSuperTypeVariables().get(0));
                for (int i = 1; i < entity.getForeignPackageSuperTypeVariables().size(); i++) {
                    sb.append(", ");
                    sb.append(entity.getForeignPackageSuperTypeVariables().get(i));
                }
                sb.append(", ");
            }
            sb.append(typeArguments.get(0));
            for (int i = 1; i < typeArguments.size(); i++) {
                sb.append(", ");
                sb.append(typeArguments.get(i));
            }
            sb.append(">");
        }

        if (entity.getTypeElement().getKind() == ElementKind.INTERFACE) {
            sb.append(", ");
        } else {
            sb.append(" implements ");
        }
        sb.append(entity.implementationImportType(Constants.ENTITY_VIEW_PROXY));
        if (entity.isUpdatable() || entity.isCreatable()) {
            sb.append(", ").append(entity.implementationImportType(Constants.DIRTY_STATE_TRACKABLE));
        }

        sb.append(" {");
        sb.append(NEW_LINE);
    }

    private static void printTypeVariable(StringBuilder sb, MetaEntityView entity, TypeVariable t) {
        sb.append(t);
        if (t.getLowerBound().getKind() == TypeKind.NULL) {
            sb.append(" extends ").append(entity.implementationImportType(t.getUpperBound().toString()));
        } else {
            sb.append(" super ").append(entity.implementationImportType(t.getLowerBound().toString()));
        }
    }

    private static void printConstructors(StringBuilder sb, MetaEntityView entity, Context context) {
        if (entity.hasEmptyConstructor()) {
            if (entity.getMembers().size() > 0) {
                printEmptyConstructor(sb, entity, context);
                sb.append(NEW_LINE);
            }

            if (entity.getIdMember() != null && entity.getMembers().size() > 1) {
                printIdConstructor(sb, entity, context);
                sb.append(NEW_LINE);
            }
        }
        for (MetaConstructor constructor : entity.getConstructors()) {
            printConstructor(sb, constructor, context);
            sb.append(NEW_LINE);
            printTupleConstructor(sb, constructor, context);
            sb.append(NEW_LINE);
            printTupleAssignmentConstructor(sb, constructor, context);
            sb.append(NEW_LINE);
        }
    }

    private static void printConstructor(StringBuilder sb, MetaConstructor constructor, Context context) {
        MetaEntityView entity = constructor.getHostingEntity();
        sb.append("    public ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append("(");
        boolean first = true;
        MetaAttribute idMember = entity.getIdMember();
        if (idMember != null) {
            sb.append(NEW_LINE);
            sb.append("        ").append(idMember.getImplementationTypeString()).append(" ").append(idMember.getPropertyName());
            first = false;
        }
        for (MetaAttribute member : entity.getMembers()) {
            if (first) {
                first = false;
            } else if (member != idMember) {
                sb.append(",");
            }
            if (member != idMember) {
                sb.append(NEW_LINE);
                sb.append("        ").append(member.getImplementationTypeString()).append(" ").append(member.getPropertyName());
            }
        }
        for (MetaAttribute member : constructor.getParameters()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(NEW_LINE);
            sb.append("        ").append(member.getImplementationTypeString()).append(" ").append(member.getPropertyName());
        }
        if (first) {
            sb.append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append(" noop, ").append(entity.implementationImportType(Constants.MAP)).append("<String, Object> optionalParameters) {");
        } else {
            sb.append(NEW_LINE);
            sb.append("    ) {");
        }

        sb.append(NEW_LINE);
        sb.append("        super(");
        first = true;
        for (MetaAttribute member : constructor.getParameters()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(NEW_LINE);
            sb.append("            ").append(member.getPropertyName());
        }

        if (first) {
            sb.append(");");
        } else {
            sb.append(NEW_LINE);
            sb.append("        );");
        }
        sb.append(NEW_LINE);

        if (entity.isCreatable() || entity.isUpdatable()) {
            sb.append("        Object[] initialStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
            sb.append("        Object[] mutableStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
        }

        for (MetaAttribute member : entity.getMembers()) {
            sb.append("        this.").append(member.getPropertyName()).append(" = ").append(member.getPropertyName()).append(";").append(NEW_LINE);
            if (member.getDirtyStateIndex() != -1) {
                sb.append("        mutableStateArr[").append(member.getDirtyStateIndex()).append("] = ");
                sb.append("initialStateArr[").append(member.getDirtyStateIndex()).append("] = ");
                sb.append(member.getPropertyName()).append(";").append(NEW_LINE);
            }
        }

        printDirtyTrackerRegistration(sb, entity);
        sb.append("    }");
        sb.append(NEW_LINE);
    }

    private static void printTupleConstructor(StringBuilder sb, MetaConstructor constructor, Context context) {
        MetaEntityView entity = constructor.getHostingEntity();
        if (constructor.getParameters().isEmpty()) {
            sb.append("    public ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append("(").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append(" noop, int offset, Object[] tuple) {").append(NEW_LINE);
            sb.append("        super();").append(NEW_LINE);
        } else {
            sb.append("    public ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append("(").append(NEW_LINE);
            sb.append("        ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append(" noop,").append(NEW_LINE);
            sb.append("        int offset,").append(NEW_LINE);
            sb.append("        Object[] tuple");
            for (MetaAttribute member : constructor.getParameters()) {
                sb.append(",");
                sb.append(NEW_LINE);
                sb.append("        ").append(member.getImplementationTypeString()).append(" ").append(member.getPropertyName());
            }
            sb.append(NEW_LINE).append("    ) {").append(NEW_LINE);
            sb.append("        super(");
            boolean first = true;
            int attributeCount = entity.getMembers().size();
            for (MetaAttribute member : constructor.getParameters()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(NEW_LINE);
                sb.append("            (").append(member.getImplementationTypeString()).append(") tuple[offset + ").append(attributeCount + member.getAttributeIndex()).append("]");
            }

            if (first) {
                sb.append(");");
            } else {
                sb.append(NEW_LINE);
                sb.append("        );");
            }
            sb.append(NEW_LINE);
        }

        if (entity.isCreatable() || entity.isUpdatable()) {
            sb.append("        Object[] initialStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
            sb.append("        Object[] mutableStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
        }

        for (MetaAttribute member : entity.getMembers()) {
            sb.append("        this.").append(member.getPropertyName()).append(" = (").append(member.getImplementationTypeString()).append(") tuple[offset + ").append(member.getAttributeIndex()).append("];").append(NEW_LINE);
            if (member.getDirtyStateIndex() != -1) {
                sb.append("        mutableStateArr[").append(member.getDirtyStateIndex()).append("] = ");
                sb.append("initialStateArr[").append(member.getDirtyStateIndex()).append("] = (").append(member.getImplementationTypeString());
                sb.append(") tuple[offset + ").append(member.getAttributeIndex()).append("];").append(NEW_LINE);
            }
        }

        printDirtyTrackerRegistration(sb, entity);
        sb.append("    }");
        sb.append(NEW_LINE);
    }

    private static void printTupleAssignmentConstructor(StringBuilder sb, MetaConstructor constructor, Context context) {
        MetaEntityView entity = constructor.getHostingEntity();
        if (constructor.getParameters().isEmpty()) {
            sb.append("    public ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append("(").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append(" noop, int offset, int[] assignment, Object[] tuple) {").append(NEW_LINE);
            sb.append("        super();").append(NEW_LINE);
        } else {
            sb.append("    public ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append("(").append(NEW_LINE);
            sb.append("        ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append(" noop,").append(NEW_LINE);
            sb.append("        int offset,").append(NEW_LINE);
            sb.append("        int[] assignment,").append(NEW_LINE);
            sb.append("        Object[] tuple");
            for (MetaAttribute member : constructor.getParameters()) {
                sb.append(",");
                sb.append(NEW_LINE);
                sb.append("        ").append(member.getImplementationTypeString()).append(" ").append(member.getPropertyName());
            }
            sb.append(NEW_LINE).append("    ) {").append(NEW_LINE);
            sb.append("        super(");
            boolean first = true;
            int attributeCount = entity.getMembers().size();
            for (MetaAttribute member : constructor.getParameters()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(NEW_LINE);
                sb.append("            (").append(member.getImplementationTypeString()).append(") tuple[offset + assignment[").append(attributeCount + member.getAttributeIndex()).append("]]");
            }

            if (first) {
                sb.append(");");
            } else {
                sb.append(NEW_LINE);
                sb.append("        );");
            }
            sb.append(NEW_LINE);
        }

        if (entity.isCreatable() || entity.isUpdatable()) {
            sb.append("        Object[] initialStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
            sb.append("        Object[] mutableStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
        }

        for (MetaAttribute member : entity.getMembers()) {
            sb.append("        this.").append(member.getPropertyName()).append(" = (").append(member.getImplementationTypeString()).append(") tuple[offset + assignment[").append(member.getAttributeIndex()).append("]];").append(NEW_LINE);
            if (member.getDirtyStateIndex() != -1) {
                sb.append("        mutableStateArr[").append(member.getDirtyStateIndex()).append("] = ");
                sb.append("initialStateArr[").append(member.getDirtyStateIndex()).append("] = (").append(member.getImplementationTypeString());
                sb.append(") tuple[offset + assignment[").append(member.getAttributeIndex()).append("]];").append(NEW_LINE);
            }
        }

        printDirtyTrackerRegistration(sb, entity);
        sb.append("    }");
        sb.append(NEW_LINE);
    }

    private static void printEmptyConstructor(StringBuilder sb, MetaEntityView entity, Context context) {
        boolean postCreateReflection = false;
        if (entity.getPostCreate() != null) {
            TypeElement declaringType = (TypeElement) entity.getPostCreate().getEnclosingElement();
            Set<Modifier> modifiers = entity.getPostCreate().getModifiers();
            if (!modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.PROTECTED)) {
                postCreateReflection = true;
                sb.append("    private static final ").append(entity.implementationImportType(Method.class.getName())).append(" $$_post_create;").append(NEW_LINE);
                sb.append("    static {").append(NEW_LINE);
                sb.append("        try {").append(NEW_LINE);
                sb.append("            Method m = ").append(entity.implementationImportType(declaringType.getQualifiedName().toString())).append(".class.getDeclaredMethod(\"").append(entity.getPostCreate().getSimpleName()).append("\"");
                if (!entity.getPostCreate().getParameters().isEmpty()) {
                    for (VariableElement parameter : entity.getPostCreate().getParameters()) {
                        sb.append(", ").append(entity.implementationImportType(parameter.asType().toString())).append(".class");
                    }
                }
                sb.append(");").append(NEW_LINE);
                sb.append("            m.setAccessible(true);").append(NEW_LINE);
                sb.append("            $$_post_create = m;").append(NEW_LINE);
                sb.append("        } catch (Exception ex) {").append(NEW_LINE);
                sb.append("            throw new RuntimeException(\"Could not initialize post construct accessor!\", ex);").append(NEW_LINE);
                sb.append("        }").append(NEW_LINE);
                sb.append("    }").append(NEW_LINE);
            }
        }
        sb.append("    public ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append("(").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append(" noop, ").append(entity.implementationImportType(Constants.MAP)).append("<String, Object> optionalParameters) {");
        sb.append(NEW_LINE);

        if (entity.isCreatable() || entity.isUpdatable()) {
            sb.append("        Object[] initialStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
            sb.append("        Object[] mutableStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
            if (entity.isCreatable()) {
                sb.append("        this.$$_isNew = true;").append(NEW_LINE);
            }
        }

        for (MetaAttribute member : entity.getMembers()) {
            sb.append("        this.").append(member.getPropertyName()).append(" = ");
            if (member.getDirtyStateIndex() != -1) {
                sb.append("(").append(member.getImplementationTypeString()).append(") (mutableStateArr[").append(member.getDirtyStateIndex()).append("] = ");
                sb.append("initialStateArr[").append(member.getDirtyStateIndex()).append("] = ");
            }
            if (member.getKind() == MappingKind.PARAMETER) {
                sb.append("(").append(member.getImplementationTypeString()).append(") optionalParameters.get(\"").append(member.getMapping()).append("\")");
            } else {
                member.appendDefaultValue(sb, true, entity.getImplementationImportContext());
            }

            if (member.getDirtyStateIndex() != -1) {
                sb.append(")");
            }
            sb.append(";").append(NEW_LINE);
        }

        printDirtyTrackerRegistration(sb, entity);
        if (entity.getPostCreate() != null) {
            if (postCreateReflection) {
                sb.append("        try {").append(NEW_LINE);
                sb.append("            $$_post_create.invoke(this");
            } else {
                sb.append("        ").append(entity.getPostCreate().getSimpleName().toString()).append("(");
            }
            if (!entity.getPostCreate().getParameters().isEmpty()) {
                if (postCreateReflection) {
                    sb.append(", ");
                }
                for (VariableElement parameter : entity.getPostCreate().getParameters()) {
                    String type = parameter.asType().toString();
                    switch (type) {
                        case Constants.ENTITY_VIEW_MANAGER:
                            sb.append(SERIALIZABLE_EVM_FIELD_NAME);
                            break;
                        default:
                            sb.append("(").append(type).append(") null");
                            break;
                    }
                    sb.append(", ");
                }
                sb.setLength(sb.length() - 2);
            }
            sb.append(");").append(NEW_LINE);
            if (postCreateReflection) {
                sb.append("        } catch (Exception ex) {").append(NEW_LINE);
                sb.append("            throw new RuntimeException(\"Could not invoke post create method\", ex);").append(NEW_LINE);
                sb.append("        }").append(NEW_LINE);
            }
        }
        sb.append("    }");
        sb.append(NEW_LINE);
    }

    private static void printDirtyTrackerRegistration(StringBuilder sb, MetaEntityView entity) {
        if (entity.isCreatable() || entity.isUpdatable()) {
            sb.append("        this.$$_initialState = initialStateArr;").append(NEW_LINE);
            sb.append("        this.$$_mutableState = mutableStateArr;").append(NEW_LINE);
            sb.append("        this.$$_initialized = true;").append(NEW_LINE);
            for (MetaAttribute member : entity.getMembers()) {
                if (member.getDirtyStateIndex() != -1 && member instanceof AnnotationMetaCollection || member.isSubview()) {
                    sb.append("        if (this.").append(member.getPropertyName()).append(" instanceof ").append(entity.implementationImportType(Constants.BASIC_DIRTY_TRACKER)).append(") {").append(NEW_LINE);
                    sb.append("            ((").append(entity.implementationImportType(Constants.BASIC_DIRTY_TRACKER)).append(") this.").append(member.getPropertyName()).append(").$$_setParent(this, ").append(member.getDirtyStateIndex()).append(");").append(NEW_LINE);
                    sb.append("        }").append(NEW_LINE);
                }
            }
        }
    }

    private static void printIdConstructor(StringBuilder sb, MetaEntityView entity, Context context) {
        MetaAttribute idMember = entity.getIdMember();
        sb.append("    public ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append("(");
        sb.append("        ").append(idMember.getImplementationTypeString()).append(" ").append(idMember.getPropertyName());
        sb.append(") {");
        sb.append(NEW_LINE);

        if (entity.isCreatable() || entity.isUpdatable()) {
            sb.append("        Object[] initialStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
            sb.append("        Object[] mutableStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
        }

        for (MetaAttribute member : entity.getMembers()) {
            if (member == idMember) {
                sb.append("        this.").append(member.getPropertyName()).append(" = ").append(member.getPropertyName()).append(";");
                sb.append(NEW_LINE);
            } else {
                sb.append("        this.").append(member.getPropertyName()).append(" = ");
                if (member.getDirtyStateIndex() != -1) {
                    sb.append("(").append(member.getImplementationTypeString()).append(") (mutableStateArr[").append(member.getDirtyStateIndex()).append("] = ");
                    sb.append("initialStateArr[").append(member.getDirtyStateIndex()).append("] = ");
                }

                member.appendDefaultValue(sb, false, entity.getImplementationImportContext());
                if (member.getDirtyStateIndex() != -1) {
                    sb.append(")");
                }
                sb.append(";").append(NEW_LINE);
            }
        }

        if (entity.isCreatable() || entity.isUpdatable()) {
            sb.append("        this.$$_initialState = initialStateArr;").append(NEW_LINE);
            sb.append("        this.$$_mutableState = mutableStateArr;").append(NEW_LINE);
            sb.append("        this.$$_initialized = true;").append(NEW_LINE);
        }
        sb.append("    }");
        sb.append(NEW_LINE);
    }

}
