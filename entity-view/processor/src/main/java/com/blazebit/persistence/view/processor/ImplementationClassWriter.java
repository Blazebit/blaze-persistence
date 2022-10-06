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

import com.blazebit.persistence.view.processor.annotation.AnnotationMetaCollection;
import com.blazebit.persistence.view.processor.annotation.AnnotationMetaVersionAttribute;
import com.blazebit.persistence.view.processor.serialization.FieldSerializationField;
import com.blazebit.persistence.view.processor.serialization.SerializationField;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ObjectStreamConstants;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class ImplementationClassWriter extends ClassWriter {

    public static final String IMPL_CLASS_NAME_SUFFIX = "Impl";
    // The following two must be aligned with com.blazebit.persistence.view.SerializableEntityViewManager
    public static final String EVM_FIELD_NAME = "ENTITY_VIEW_MANAGER";
    public static final String SERIALIZABLE_EVM_FIELD_NAME = "SERIALIZABLE_ENTITY_VIEW_MANAGER";
    private static final String SERIALIZATION_CLASS_NAME_SUFFIX = "Ser";
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String NEW_LINE = System.lineSeparator();

    private ImplementationClassWriter(FileObject fileObject, MetaEntityView entity, Context context, Collection<Runnable> mainThreadQueue, LongAdder elapsedTime) {
        super(fileObject, entity, entity.getImplementationImportContext(), context, mainThreadQueue, elapsedTime);
    }

    public static void writeFile(MetaEntityView entity, Context context, ExecutorService executorService, Collection<Runnable> mainThreadQueue, LongAdder implementationTime) {
        FileObject fileObject = ClassWriter.createFile(entity.getPackageName(), entity.getSimpleName() + IMPL_CLASS_NAME_SUFFIX, context, entity.getOriginatingElements());
        if (fileObject == null) {
            return;
        }
        executorService.submit(new ImplementationClassWriter(fileObject, entity, context, mainThreadQueue, implementationTime));
    }

    @Override
    public void generateBody(StringBuilder sb, MetaEntityView entity, Context context) {
        if (context.addGeneratedAnnotation()) {
            ClassWriter.writeGeneratedAnnotation(sb, entity.getImplementationImportContext(), context);
            sb.append(NEW_LINE);
        }
        if (context.isAddSuppressWarningsAnnotation()) {
            sb.append(ClassWriter.writeSuppressWarnings());
            sb.append(NEW_LINE);
        }

        sb.append("@").append(entity.implementationImportType(Constants.STATIC_IMPLEMENTATION)).append("(").append(entity.implementationImportType(entity.getQualifiedName())).append(".class)");
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

        for (EntityViewSpecialMemberMethod specialMember : entity.getSpecialMembers()) {
            if (Constants.ENTITY_VIEW_MANAGER.equals(specialMember.getReturnTypeName())) {
                sb.append("    @Override").append(NEW_LINE);
                sb.append("    public ").append(entity.implementationImportType(specialMember.getReturnTypeName())).append(" ").append(specialMember.getName()).append("() {").append(NEW_LINE);
                sb.append("        return ").append(SERIALIZABLE_EVM_FIELD_NAME).append(";").append(NEW_LINE);
                sb.append("    }").append(NEW_LINE);
                sb.append(NEW_LINE);
            } else {
                context.logMessage(Diagnostic.Kind.ERROR, "Unsupported special member: " + specialMember);
            }
        }

        MetaAttribute version = entity.getVersionMember();
        sb.append(NEW_LINE);
        sb.append("    private byte $$_kind;").append(NEW_LINE);
        if (entity.isCreatable() || entity.isUpdatable()) {
            sb.append("    private final Object[] $$_initialState;").append(NEW_LINE);
            sb.append("    private final Object[] $$_mutableState;").append(NEW_LINE);
            sb.append("    private final boolean $$_initialized;").append(NEW_LINE);
            sb.append("    private ").append(entity.implementationImportType(Constants.LIST)).append("<Object> $$_readOnlyParents;").append(NEW_LINE);
            sb.append("    private ").append(entity.implementationImportType(Constants.DIRTY_TRACKER)).append(" $$_parent;").append(NEW_LINE);
            sb.append("    private int $$_parentIndex;").append(NEW_LINE);
            sb.append("    private long $$_dirty;").append(NEW_LINE);
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
        sb.append("        return ").append(entity.implementationImportType(entity.getJpaManagedBaseClass())).append(".class;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        String entityViewClassName = entity.implementationImportType(entity.getQualifiedName());
        sb.append("    public Class<?> $$_getEntityViewClass() {").append(NEW_LINE);
        sb.append("        return ").append(entityViewClassName).append(".class;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public boolean $$_isNew() {").append(NEW_LINE);
        if (entity.isCreatable()) {
            sb.append("        return $$_kind == (byte) 2;").append(NEW_LINE);
        } else {
            sb.append("        return false;").append(NEW_LINE);
        }
        sb.append("    }").append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public boolean $$_isReference() {").append(NEW_LINE);
        sb.append("        return $$_kind == (byte) 1;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public void $$_setIsReference(boolean isReference) {").append(NEW_LINE);
        sb.append("        if (isReference) {").append(NEW_LINE);
        sb.append("            this.$$_kind = (byte) 1;").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            this.$$_kind = (byte) 0;").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
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
                if (member.getDirtyStateIndex() != -1 && member.getSetterName() != null) {
                    sb.append("            case ").append(member.getDirtyStateIndex()).append(": ");
                    sb.append(member.getSetterName()).append('(');
                    if (member.isPrimitive()) {
                        appendUnwrap(sb, member.getDeclaredJavaType(), "newObject");
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
                sb.append("        if (isNew) {").append(NEW_LINE);
                sb.append("            this.$$_kind = (byte) 2;").append(NEW_LINE);
                sb.append("        } else {").append(NEW_LINE);
                sb.append("            this.$$_kind = (byte) 0;").append(NEW_LINE);
                sb.append("        }").append(NEW_LINE);
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
                    appendUnwrap(sb, version.getDeclaredJavaType(), "version");
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

        if (entity.hasSelfConstructor()) {
            appendSerializationClass(sb, entity, context);
        }

        sb.append("}");
        sb.append(NEW_LINE);
    }

    private static void appendSerializationClass(StringBuilder sb, MetaEntityView entity, Context context) {
        String serializableClassSimpleName = entity.getSimpleName() + SERIALIZATION_CLASS_NAME_SUFFIX;
        for (MetaConstructor constructor : entity.getConstructors()) {
            if (constructor.hasSelfParameter()) {
                sb.append("    public static ").append(entity.getSimpleName()).append(" createSelf(");
                for (MetaAttribute member : entity.getMembers()) {
                    sb.append(member.getImplementationTypeString()).append(" ").append(member.getPropertyName()).append(", ");
                }
                sb.setCharAt(sb.length() - 2, ')');
                sb.append('{').append(NEW_LINE);
                sb.append("        try {").append(NEW_LINE);
                sb.append("            ").append(serializableClassSimpleName).append(" $ = (").append(serializableClassSimpleName).append(")new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(")
                        .append(serializableClassSimpleName).append(".EMPTY_INSTANCE_BYTES)).readObject();").append(NEW_LINE);
                for (MetaAttribute member : entity.getMembers()) {
                    sb.append("            $.").append(member.getPropertyName()).append(" = ").append(member.getPropertyName()).append(";").append(NEW_LINE);
                }
                sb.append("            return $;").append(NEW_LINE);
                sb.append("        } catch (Exception ex) {").append(NEW_LINE);
                sb.append("            throw new RuntimeException(ex);").append(NEW_LINE);
                sb.append("        }").append(NEW_LINE);
                sb.append("    }").append(NEW_LINE);
            }
        }

        sb.append("    private static class ").append(serializableClassSimpleName);

        List<JavaTypeVariable> typeArguments = entity.getTypeVariables();
        if (!typeArguments.isEmpty()) {
            sb.append("<");
            typeArguments.get(0).append(entity.getImplementationImportContext(), sb);
            for (int i = 1; i < typeArguments.size(); i++) {
                sb.append(", ");
                typeArguments.get(i).append(entity.getImplementationImportContext(), sb);
            }
            sb.append(">");
        }
        if (entity.getElementKind() == ElementKind.CLASS) {
            sb.append(" extends ").append(entity.implementationImportType(entity.getBaseSuperclass()));
        } else {
            sb.append(" implements ").append(entity.implementationImportType(entity.getQualifiedName()));
        }

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
            sb.append(typeArguments.get(0).getName());
            for (int i = 1; i < typeArguments.size(); i++) {
                sb.append(", ");
                sb.append(typeArguments.get(i).getName());
            }
            sb.append(">");
        }
        if (entity.getElementKind() == ElementKind.CLASS) {
            sb.append(" implements ");
        } else {
            sb.append(", ");
        }
        sb.append(entity.implementationImportType(Serializable.class.getName())).append(" {").append(NEW_LINE);
        sb.append("        private static final long serialVersionUID = 1L;").append(NEW_LINE);
        sb.append("        private static final byte[] EMPTY_INSTANCE_BYTES = new byte[]{");
        appendBytesAsHex(sb, generateEmptyInstanceBytes(entity.getPackageName() + "." + entity.getSimpleName() + IMPL_CLASS_NAME_SUFFIX + "$" + serializableClassSimpleName, entity, context));
        sb.setCharAt(sb.length() - 1, '}');
        sb.append(';').append(NEW_LINE);
        for (MetaAttribute member : entity.getMembers()) {
            sb.append("        public ").append(member.getImplementationTypeString()).append(" ").append(member.getPropertyName()).append(";").append(NEW_LINE);
        }
        for (MetaConstructor constructor : entity.getConstructors()) {
            sb.append("        private ").append(serializableClassSimpleName).append("(");
            if (constructor.getParameters().isEmpty()) {
                sb.append(") {").append(NEW_LINE);
                sb.append("            super();").append(NEW_LINE);
                sb.append("        }").append(NEW_LINE);
            } else {
                for (MetaAttribute parameter : constructor.getParameters()) {
                    sb.append(parameter.getImplementationTypeString()).append(" ").append(parameter.getPropertyName()).append(", ");
                }
                sb.setCharAt(sb.length() - 2, ')');
                sb.append("{").append(NEW_LINE);
                sb.append("            super(");
                for (MetaAttribute parameter : constructor.getParameters()) {
                    sb.append(parameter.getPropertyName()).append(", ");
                }
                sb.setCharAt(sb.length() - 2, ')');
                sb.setCharAt(sb.length() - 1, ';');
                sb.append(NEW_LINE);
                sb.append("        }").append(NEW_LINE);
            }
        }
        for (MetaAttribute member : entity.getMembers()) {
            sb.append("        @Override")
                    .append(NEW_LINE)
                    .append("        public ");

            sb.append(member.getImplementationTypeString());

            sb.append(' ')
                    .append(member.getGetterName())
                    .append("() {")
                    .append(NEW_LINE)
                    .append("            return ")
                    .append(member.getPropertyName())
                    .append(";")
                    .append(NEW_LINE)
                    .append("        }")
                    .append(NEW_LINE);

            if (member.getSetterName() != null) {
                sb.append("        @Override")
                        .append(NEW_LINE)
                        .append("        public void ")
                        .append(member.getSetterName())
                        .append('(');

                sb.append(member.getImplementationTypeString());

                sb.append(' ')
                        .append(member.getPropertyName())
                        .append(") {")
                        .append(NEW_LINE);

                sb.append("            this.")
                        .append(member.getPropertyName())
                        .append(" = ")
                        .append(member.getPropertyName())
                        .append(";")
                        .append(NEW_LINE);
                sb.append("        }").append(NEW_LINE);
            }
        }

        for (EntityViewSpecialMemberMethod specialMember : entity.getSpecialMembers()) {
            if (Constants.ENTITY_VIEW_MANAGER.equals(specialMember.getReturnTypeName())) {
                sb.append("        @Override").append(NEW_LINE);
                sb.append("        public ").append(entity.implementationImportType(specialMember.getReturnTypeName())).append(" ").append(specialMember.getName()).append("() {").append(NEW_LINE);
                sb.append("            return ").append(SERIALIZABLE_EVM_FIELD_NAME).append(";").append(NEW_LINE);
                sb.append("        }").append(NEW_LINE);
                sb.append(NEW_LINE);
            } else {
                context.logMessage(Diagnostic.Kind.ERROR, "Unsupported special member: " + specialMember);
            }
        }

        sb.append("    }").append(NEW_LINE);
    }

    private static void appendBytesAsHex(StringBuilder sb, byte[] bytes) {
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            sb.append(" (byte) 0x");
            sb.append(HEX_ARRAY[v >>> 4]);
            sb.append(HEX_ARRAY[v & 0x0F]);
            sb.append(',');
        }
    }

    private static byte[] generateEmptyInstanceBytes(String serializableClassName, MetaEntityView entity, Context context) {
        // Generate empty object serialization bytes according to https://www.javaworld.com/article/2072752/the-java-serialization-algorithm-revealed.html
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            DataOutputStream oos = new DataOutputStream(baos);
            oos.writeShort(ObjectStreamConstants.STREAM_MAGIC);
            oos.writeShort(ObjectStreamConstants.STREAM_VERSION);

            // Start object
            oos.writeByte(ObjectStreamConstants.TC_OBJECT);
            // Class descriptor
            oos.writeByte(ObjectStreamConstants.TC_CLASSDESC);
            // Class name
            oos.writeUTF(serializableClassName);
            // Serial version UID of the class
            oos.writeLong(1L);
            // Supported flags
            oos.writeByte(ObjectStreamConstants.SC_SERIALIZABLE);

            List<List<SerializationField>> serializationFieldHierarchy = new ArrayList<>();
            List<SerializationField> serializationFields = new ArrayList<>();
            for (MetaAttribute member : entity.getMembers()) {
                serializationFields.add(member.getSerializationField());
            }
            serializationFieldHierarchy.add(serializationFields);
            writeFields(serializationFields, oos);
            oos.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);

            // For the following to be safe, we trust that the foreign package checks in AnnotationMetaEntityView
            // initialize all super classes elements properly
            TypeElement superclass = entity.getTypeElement();
            TypeMirror serializableTypeMirror = context.getTypeElement(Serializable.class.getName()).asType();
            while (superclass.getKind() == ElementKind.CLASS && !superclass.getQualifiedName().toString().equals("java.lang.Object")) {
                // Class descriptor
                oos.writeByte(ObjectStreamConstants.TC_CLASSDESC);
                // Class name
                oos.writeUTF(superclass.getQualifiedName().toString());
                List<SerializationField> fields = new ArrayList<>();
                Long serialVersionUID = null;
                for (Element enclosedElement : superclass.getEnclosedElements()) {
                    if (enclosedElement.getKind() == ElementKind.FIELD) {
                        VariableElement variableElement = (VariableElement) enclosedElement;
                        Set<Modifier> modifiers = variableElement.getModifiers();
                        if (modifiers.contains(Modifier.STATIC)) {
                            if (modifiers.contains(Modifier.PRIVATE) && modifiers.contains(Modifier.FINAL) && variableElement.asType().getKind() == TypeKind.LONG) {
                                serialVersionUID = (Long) variableElement.getConstantValue();
                            }
                        } else if (!modifiers.contains(Modifier.TRANSIENT)) {
                            fields.add(new FieldSerializationField(variableElement));
                        }
                    }
                }
                if (context.getTypeUtils().isAssignable(superclass.asType(), serializableTypeMirror)) {
                    // Serial version UID of the class
                    if (serialVersionUID == null) {
                        oos.writeLong(SerializationField.computeDefaultSUID(superclass, fields));
                    } else {
                        oos.writeLong(serialVersionUID);
                    }
                    // Supported flags
                    oos.writeByte(ObjectStreamConstants.SC_SERIALIZABLE);
                } else {
                    // Serial version UID of the class
                    oos.writeLong(0L);
                    // Supported flags
                    oos.writeByte(0);
                }

                serializationFieldHierarchy.add(fields);
                writeFields(fields, oos);
                oos.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);

                superclass = (TypeElement) ((DeclaredType) superclass.getSuperclass()).asElement();
            }
            oos.writeByte(ObjectStreamConstants.TC_NULL);
            for (List<SerializationField> fields : serializationFieldHierarchy) {
                for (SerializationField serializationField : fields) {
                    if (serializationField.isPrimitive()) {
                        switch (serializationField.getTypeKind()) {
                            case INT:
                                oos.writeInt(0);
                                break;
                            case BYTE:
                                oos.writeByte(0);
                                break;
                            case LONG:
                                oos.writeLong(0);
                                break;
                            case FLOAT:
                                oos.writeFloat(0);
                                break;
                            case DOUBLE:
                                oos.writeDouble(0);
                                break;
                            case SHORT:
                                oos.writeShort(0);
                                break;
                            case CHAR:
                                oos.writeChar(0);
                                break;
                            case BOOLEAN:
                                oos.writeBoolean(false);
                                break;
                            case VOID:
                                oos.writeByte(ObjectStreamConstants.TC_NULL);
                                break;
                            default:
                                throw new UnsupportedOperationException("Unsupported primitive type: " + serializationField.getTypeCode());
                        }
                    } else {
                        oos.writeByte(ObjectStreamConstants.TC_NULL);
                    }
                }
            }
            oos.flush();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void writeFields(List<SerializationField> serializationFields, DataOutputStream oos) throws Exception {
        Collections.sort(serializationFields);
        oos.writeShort(serializationFields.size());
        for (SerializationField serializationField : serializationFields) {
            oos.writeByte(serializationField.getTypeCode());
            oos.writeUTF(serializationField.getName());
            if (!serializationField.isPrimitive()) {
                oos.writeByte(ObjectStreamConstants.TC_STRING);
                oos.writeUTF(serializationField.getTypeString());
//                oos.writeTypeString(serializationField.getTypeString());
            }
        }
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
        boolean generateEqualsHashCode = !entity.hasCustomEqualsOrHashCodeMethod();
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
                    Set<Modifier> modifiers = equalityMember.getGetterModifiers();
                    boolean containsProtected = modifiers.contains(Modifier.PROTECTED);
                    if (!modifiers.contains(Modifier.PUBLIC) && !containsProtected || containsProtected && !entity.getPackageName().equals(equalityMember.getGetterPackageName())) {
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
                        sb.append("            if (this.").append(member.getPropertyName()).append(" != other.").append(member.getGetterName()).append("()) {").append(NEW_LINE);
                    } else {
                        sb.append("            if (!").append(entity.implementationImportType(Objects.class.getName())).append(".equals(this.").append(member.getPropertyName()).append(", other.").append(member.getGetterName()).append("())) {").append(NEW_LINE);
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
                    String type = member.getDeclaredJavaType();
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
        boolean generateToString = !entity.hasCustomToStringMethod();
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

    private static void printClassDeclaration(StringBuilder sb, MetaEntityView entity, Context context) {
        sb.append("public class ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX);

        List<JavaTypeVariable> typeArguments = entity.getTypeVariables();
        if (!typeArguments.isEmpty()) {
            sb.append("<");
            typeArguments.get(0).append(entity.getImplementationImportContext(), sb);
            for (int i = 1; i < typeArguments.size(); i++) {
                sb.append(", ");
                typeArguments.get(i).append(entity.getImplementationImportContext(), sb);
            }
            sb.append(">");
        }

        if (entity.getElementKind() == ElementKind.INTERFACE) {
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
            sb.append(typeArguments.get(0).getName());
            for (int i = 1; i < typeArguments.size(); i++) {
                sb.append(", ");
                sb.append(typeArguments.get(i).getName());
            }
            sb.append(">");
        }

        if (entity.getElementKind() == ElementKind.INTERFACE) {
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

    private static void printConstructors(StringBuilder sb, MetaEntityView entity, Context context) {
        boolean postLoadReflection = preparePostLoad(sb, entity, context);
        if (entity.hasEmptyConstructor()) {
            if (entity.getMembers().size() > 0) {
                printCreateConstructor(sb, entity, context);
                sb.append(NEW_LINE);
            }

            if (entity.getIdMember() != null && entity.getMembers().size() > 1) {
                printIdConstructor(sb, entity, context);
                sb.append(NEW_LINE);
            }
        }
        for (MetaConstructor constructor : entity.getConstructors()) {
            printConstructor(sb, constructor, postLoadReflection, context);
            sb.append(NEW_LINE);
            printTupleConstructor(sb, constructor, postLoadReflection, context);
            sb.append(NEW_LINE);
            printTupleAssignmentConstructor(sb, constructor, postLoadReflection, context);
            sb.append(NEW_LINE);
        }
    }

    private static void printConstructor(StringBuilder sb, MetaConstructor constructor, boolean postLoadReflection, Context context) {
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
            sb.append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append(" noop, ").append(entity.implementationImportType(Constants.MAP)).append("<String, Object> ").append(BuilderClassWriter.OPTIONAL_PARAMS).append(") {");
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
            if (entity.getDefaultDirtyMask() != 0) {
                sb.append("        this.$$_dirty |= ").append(entity.getDefaultDirtyMask()).append("L;").append(NEW_LINE);
            }
            sb.append("        Object[] initialStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
            sb.append("        Object[] mutableStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
        }

        for (MetaAttribute member : entity.getMembers()) {
            boolean possiblyInitialized = appendPossiblyInitializedFragment(sb, constructor, member);
            sb.append("        this.").append(member.getPropertyName()).append(" = ").append(member.getPropertyName()).append(";").append(NEW_LINE);
            if (member.getDirtyStateIndex() != -1) {
                if (possiblyInitialized) {
                    sb.append("            if (this.").append(member.getPropertyName()).append(" == ");
                    if (member.isPrimitive()) {
                        member.appendDefaultValue(sb, false, true, entity.getImplementationImportContext());
                    } else {
                        sb.append("null");
                    }
                    sb.append(") {").append(NEW_LINE);
                    sb.append("                mutableStateArr[").append(member.getDirtyStateIndex()).append("] = ").append(member.getPropertyName()).append(";").append(NEW_LINE);
                    sb.append("            } else {").append(NEW_LINE);
                    sb.append("                mutableStateArr[").append(member.getDirtyStateIndex()).append("] = this.").append(member.getPropertyName()).append(";").append(NEW_LINE);
                    sb.append("            }").append(NEW_LINE);
                    sb.append("            initialStateArr[").append(member.getDirtyStateIndex()).append("] = ");
                    sb.append(member.getPropertyName()).append(";").append(NEW_LINE);
                } else {
                    sb.append("        mutableStateArr[").append(member.getDirtyStateIndex()).append("] = ");
                    sb.append("initialStateArr[").append(member.getDirtyStateIndex()).append("] = ");
                    sb.append(member.getPropertyName()).append(";").append(NEW_LINE);
                }
            }

            if (possiblyInitialized) {
                sb.append("        }").append(NEW_LINE);
            }
        }

        printDirtyTrackerRegistration(sb, entity);
        printPostLoad(sb, entity, postLoadReflection, context);
        sb.append("    }");
        sb.append(NEW_LINE);
    }

    private static void printTupleConstructor(StringBuilder sb, MetaConstructor constructor, boolean postLoadReflection, Context context) {
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
                sb.append("            ");
                if (member.isSelf()) {
                    sb.append("createSelf(");
                    for (MetaAttribute attribute : entity.getMembers()) {
                        sb.append(NEW_LINE).append("                (").append(attribute.getImplementationTypeString()).append(") tuple[offset + ").append(attribute.getAttributeIndex()).append("],");
                    }
                    sb.setLength(sb.length() - 1);
                    sb.append(NEW_LINE);
                    sb.append("            )");
                } else {
                    sb.append("(").append(member.getImplementationTypeString()).append(") tuple[offset + ").append(attributeCount + member.getAttributeIndex()).append("]");
                }
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
            if (entity.getDefaultDirtyMask() != 0) {
                sb.append("        this.$$_dirty |= ").append(entity.getDefaultDirtyMask()).append("L;").append(NEW_LINE);
            }
            sb.append("        Object[] initialStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
            sb.append("        Object[] mutableStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
        }

        for (MetaAttribute member : entity.getMembers()) {
            boolean possiblyInitialized = appendPossiblyInitializedFragment(sb, constructor, member);
            sb.append("        this.").append(member.getPropertyName()).append(" = (").append(member.getImplementationTypeString()).append(") tuple[offset + ").append(member.getAttributeIndex()).append("];").append(NEW_LINE);
            if (member.getDirtyStateIndex() != -1) {
                if (possiblyInitialized) {
                    sb.append("            if (this.").append(member.getPropertyName()).append(" == ");
                    if (member.isPrimitive()) {
                        member.appendDefaultValue(sb, false, true, entity.getImplementationImportContext());
                    } else {
                        sb.append("null");
                    }
                    sb.append(") {").append(NEW_LINE);
                    sb.append("                mutableStateArr[").append(member.getDirtyStateIndex()).append("] = tuple[offset + ").append(member.getAttributeIndex()).append("];").append(NEW_LINE);
                    sb.append("            } else {").append(NEW_LINE);
                    sb.append("                mutableStateArr[").append(member.getDirtyStateIndex()).append("] = this.").append(member.getPropertyName()).append(";").append(NEW_LINE);
                    sb.append("            }").append(NEW_LINE);
                    sb.append("            initialStateArr[").append(member.getDirtyStateIndex()).append("] = tuple[offset + ").append(member.getAttributeIndex()).append("];").append(NEW_LINE);
                } else {
                    sb.append("        mutableStateArr[").append(member.getDirtyStateIndex()).append("] = initialStateArr[").append(member.getDirtyStateIndex()).append("] = tuple[offset + ").append(member.getAttributeIndex()).append("];").append(NEW_LINE);
                }
            }
            
            if (possiblyInitialized) {
                sb.append("        }").append(NEW_LINE);
            }
        }

        printDirtyTrackerRegistration(sb, entity);
        printPostLoad(sb, entity, postLoadReflection, context);
        sb.append("    }");
        sb.append(NEW_LINE);
    }

    private static boolean appendPossiblyInitializedFragment(StringBuilder sb, MetaConstructor constructor, MetaAttribute member) {
        if (constructor == null) {
            for (MetaConstructor metaConstructor : member.getHostingEntity().getConstructors()) {
                if (metaConstructor.getParameters().isEmpty()) {
                    constructor = metaConstructor;
                    break;
                }
            }
        }
        boolean possiblyInitialized = constructor.isReal() && member.getSetterName() != null;
        if (possiblyInitialized) {
            MetaEntityView entity = member.getHostingEntity();
            sb.append("        if (this.").append(member.getPropertyName()).append(" == ");
            if (member.isPrimitive()) {
                member.appendDefaultValue(sb, false, true, entity.getImplementationImportContext());
            } else {
                sb.append("null");
            }
            sb.append(") {").append(NEW_LINE);
            sb.append("    ");
        }
        return possiblyInitialized;
    }

    private static void printTupleAssignmentConstructor(StringBuilder sb, MetaConstructor constructor, boolean postLoadReflection, Context context) {
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
                sb.append("            ");
                if (member.isSelf()) {
                    sb.append("createSelf(");
                    for (MetaAttribute attribute : entity.getMembers()) {
                        sb.append(NEW_LINE).append("                (").append(attribute.getImplementationTypeString()).append(") tuple[offset + assignment[").append(attribute.getAttributeIndex()).append("]],");
                    }
                    sb.setLength(sb.length() - 1);
                    sb.append(NEW_LINE);
                    sb.append("            )");
                } else {
                    sb.append("(").append(member.getImplementationTypeString()).append(") tuple[offset + assignment[").append(attributeCount + member.getAttributeIndex()).append("]]");
                }
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
            if (entity.getDefaultDirtyMask() != 0) {
                sb.append("        this.$$_dirty |= ").append(entity.getDefaultDirtyMask()).append("L;").append(NEW_LINE);
            }
            sb.append("        Object[] initialStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
            sb.append("        Object[] mutableStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
        }

        for (MetaAttribute member : entity.getMembers()) {
            boolean possiblyInitialized = appendPossiblyInitializedFragment(sb, constructor, member);
            sb.append("        this.").append(member.getPropertyName()).append(" = (").append(member.getImplementationTypeString()).append(") tuple[offset + assignment[").append(member.getAttributeIndex()).append("]];").append(NEW_LINE);
            if (member.getDirtyStateIndex() != -1) {
                if (possiblyInitialized) {
                    sb.append("            if (this.").append(member.getPropertyName()).append(" == ");
                    if (member.isPrimitive()) {
                        member.appendDefaultValue(sb, false, true, entity.getImplementationImportContext());
                    } else {
                        sb.append("null");
                    }
                    sb.append(") {").append(NEW_LINE);
                    sb.append("                mutableStateArr[").append(member.getDirtyStateIndex()).append("] = tuple[offset + assignment[").append(member.getAttributeIndex()).append("]];").append(NEW_LINE);
                    sb.append("            } else {").append(NEW_LINE);
                    sb.append("                mutableStateArr[").append(member.getDirtyStateIndex()).append("] = this.").append(member.getPropertyName()).append(";").append(NEW_LINE);
                    sb.append("            }").append(NEW_LINE);
                    sb.append("            initialStateArr[").append(member.getDirtyStateIndex()).append("] = tuple[offset + assignment[").append(member.getAttributeIndex()).append("]];").append(NEW_LINE);
                } else {
                    sb.append("        mutableStateArr[").append(member.getDirtyStateIndex()).append("] = initialStateArr[").append(member.getDirtyStateIndex()).append("] = tuple[offset + assignment[").append(member.getAttributeIndex()).append("]];").append(NEW_LINE);
                }
            }

            if (possiblyInitialized) {
                sb.append("        }").append(NEW_LINE);
            }
        }

        printDirtyTrackerRegistration(sb, entity);
        printPostLoad(sb, entity, postLoadReflection, context);
        sb.append("    }");
        sb.append(NEW_LINE);
    }

    private static void printCreateConstructor(StringBuilder sb, MetaEntityView entity, Context context) {
        EntityViewLifecycleMethod postCreate = entity.getPostCreateForReflection();
        boolean postCreateReflection = false;
        if (postCreate != null) {
            postCreateReflection = postCreate.needsReflectionCall();
            if (postCreateReflection) {
                sb.append("    private static final ").append(entity.implementationImportType(Method.class.getName())).append(" $$_post_create;").append(NEW_LINE);
                sb.append("    static {").append(NEW_LINE);
                sb.append("        try {").append(NEW_LINE);
                sb.append("            Method m = ").append(entity.implementationImportType(postCreate.getDeclaringTypeName())).append(".class.getDeclaredMethod(\"").append(postCreate.getName()).append("\"");
                if (!postCreate.getParameterTypes().isEmpty()) {
                    for (String parameterType : postCreate.getParameterTypes()) {
                        sb.append(", ").append(entity.implementationImportType(parameterType)).append(".class");
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
        sb.append("    public ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append("(").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append(" noop, ").append(entity.implementationImportType(Constants.MAP)).append("<String, Object> ").append(BuilderClassWriter.OPTIONAL_PARAMS).append(") {");
        sb.append(NEW_LINE);

        if (entity.isCreatable() || entity.isUpdatable()) {
            if (entity.getDefaultDirtyMask() != 0) {
                sb.append("        this.$$_dirty |= ").append(entity.getDefaultDirtyMask()).append("L;").append(NEW_LINE);
            }
            sb.append("        Object[] initialStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
            sb.append("        Object[] mutableStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
            if (entity.isCreatable()) {
                sb.append("        this.$$_kind = (byte) 2;").append(NEW_LINE);
            }
        }

        for (MetaAttribute member : entity.getMembers()) {
            boolean possiblyInitialized = appendPossiblyInitializedFragment(sb, null, member);
            sb.append("        this.").append(member.getPropertyName()).append(" = ");
            if (member.getDirtyStateIndex() != -1) {
                sb.append("(").append(member.getImplementationTypeString()).append(") (mutableStateArr[").append(member.getDirtyStateIndex()).append("] = ");
                sb.append("initialStateArr[").append(member.getDirtyStateIndex()).append("] = ");
            }
            if (member.getKind() == MappingKind.PARAMETER) {
                if (member.isPrimitive()) {
                    sb.append("!").append(BuilderClassWriter.OPTIONAL_PARAMS).append(".containsKey(\"").append(member.getMapping()).append("\") ? ");
                    member.appendDefaultValue(sb, false, true, entity.getImplementationImportContext());
                    sb.append(" : ");
                }
                sb.append("(").append(member.getImplementationTypeString()).append(") ").append(BuilderClassWriter.OPTIONAL_PARAMS).append(".get(\"").append(member.getMapping()).append("\")");
            } else {
                member.appendDefaultValue(sb, true, true, entity.getImplementationImportContext());
            }

            if (member.getDirtyStateIndex() != -1) {
                sb.append(")");
            }
            sb.append(";").append(NEW_LINE);

            if (possiblyInitialized) {
                sb.append("        }").append(NEW_LINE);
            }
        }

        printDirtyTrackerRegistration(sb, entity);
        if (postCreate != null) {
            if (postCreateReflection) {
                sb.append("        try {").append(NEW_LINE);
                sb.append("            $$_post_create.invoke(this");
            } else {
                sb.append("        ").append(postCreate.getName()).append("(");
            }
            if (!postCreate.getParameterTypes().isEmpty()) {
                if (postCreateReflection) {
                    sb.append(", ");
                }
                for (String parameterType : postCreate.getParameterTypes()) {
                    switch (parameterType) {
                        case Constants.ENTITY_VIEW_MANAGER:
                            sb.append(SERIALIZABLE_EVM_FIELD_NAME);
                            break;
                        default:
                            sb.append("(").append(parameterType).append(") null");
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

    private static boolean preparePostLoad(StringBuilder sb, MetaEntityView entity, Context context) {
        EntityViewLifecycleMethod postLoad = entity.getPostLoad();
        boolean postLoadReflection = false;
        if (postLoad != null) {
            postLoadReflection = postLoad.needsReflectionCall();
            if (postLoadReflection) {
                sb.append("    private static final ").append(entity.implementationImportType(Method.class.getName())).append(" $$_post_load;").append(NEW_LINE);
                sb.append("    static {").append(NEW_LINE);
                sb.append("        try {").append(NEW_LINE);
                sb.append("            Method m = ").append(entity.implementationImportType(postLoad.getDeclaringTypeName())).append(".class.getDeclaredMethod(\"").append(postLoad.getName()).append("\"");
                if (!postLoad.getParameterTypes().isEmpty()) {
                    for (String parameterType : postLoad.getParameterTypes()) {
                        sb.append(", ").append(entity.implementationImportType(parameterType)).append(".class");
                    }
                }
                sb.append(");").append(NEW_LINE);
                sb.append("            m.setAccessible(true);").append(NEW_LINE);
                sb.append("            $$_post_load = m;").append(NEW_LINE);
                sb.append("        } catch (Exception ex) {").append(NEW_LINE);
                sb.append("            throw new RuntimeException(\"Could not initialize post construct accessor!\", ex);").append(NEW_LINE);
                sb.append("        }").append(NEW_LINE);
                sb.append("    }").append(NEW_LINE);
            }
        }
        return postLoadReflection;
    }

    private static void printPostLoad(StringBuilder sb, MetaEntityView entity, boolean postLoadReflection, Context context) {
        EntityViewLifecycleMethod postLoad = entity.getPostLoad();
        if (postLoad != null) {
            if (postLoadReflection) {
                sb.append("        try {").append(NEW_LINE);
                sb.append("            $$_post_load.invoke(this");
            } else {
                sb.append("        ").append(postLoad.getName()).append("(");
            }
            if (!postLoad.getParameterTypes().isEmpty()) {
                if (postLoadReflection) {
                    sb.append(", ");
                }
                for (String parameterType : postLoad.getParameterTypes()) {
                    switch (parameterType) {
                        case Constants.ENTITY_VIEW_MANAGER:
                            sb.append(SERIALIZABLE_EVM_FIELD_NAME);
                            break;
                        default:
                            sb.append("(").append(parameterType).append(") null");
                            break;
                    }
                    sb.append(", ");
                }
                sb.setLength(sb.length() - 2);
            }
            sb.append(");").append(NEW_LINE);
            if (postLoadReflection) {
                sb.append("        } catch (Exception ex) {").append(NEW_LINE);
                sb.append("            throw new RuntimeException(\"Could not invoke post load method\", ex);").append(NEW_LINE);
                sb.append("        }").append(NEW_LINE);
            }
        }
    }

    private static void printIdConstructor(StringBuilder sb, MetaEntityView entity, Context context) {
        MetaAttribute idMember = entity.getIdMember();
        sb.append("    public ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append("(");
        sb.append("        ").append(idMember.getImplementationTypeString()).append(" ").append(idMember.getPropertyName());
        sb.append(") {");
        sb.append(NEW_LINE);
        sb.append("        this.$$_kind = (byte) 1;").append(NEW_LINE);
        if (entity.isCreatable() || entity.isUpdatable()) {
            if (entity.getDefaultDirtyMask() != 0) {
                sb.append("        this.$$_dirty |= ").append(entity.getDefaultDirtyMask()).append("L;").append(NEW_LINE);
            }
            sb.append("        Object[] initialStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
            sb.append("        Object[] mutableStateArr = new Object[").append(entity.getMutableAttributeCount()).append("];").append(NEW_LINE);
        }

        for (MetaAttribute member : entity.getMembers()) {
            boolean possiblyInitialized = appendPossiblyInitializedFragment(sb, null, member);
            if (member == idMember) {
                sb.append("        this.").append(member.getPropertyName()).append(" = ").append(member.getPropertyName()).append(";");
                sb.append(NEW_LINE);
            } else {
                sb.append("        this.").append(member.getPropertyName()).append(" = ");
                if (member.getDirtyStateIndex() != -1) {
                    sb.append("(").append(member.getImplementationTypeString()).append(") (mutableStateArr[").append(member.getDirtyStateIndex()).append("] = ");
                    // note that we are not initializing the initial state array on purpose because anything that is "dirty" should be considered for flushing
                    if (member.isCreateEmptyFlatViews()) {
                        sb.append("initialStateArr[").append(member.getDirtyStateIndex()).append("] = ");
                    }
                }

                member.appendDefaultValue(sb, true, false, entity.getImplementationImportContext());
                if (member.getDirtyStateIndex() != -1) {
                    sb.append(")");
                }
                sb.append(";").append(NEW_LINE);
            }

            if (possiblyInitialized) {
                sb.append("        }").append(NEW_LINE);
            }
        }

        printDirtyTrackerRegistration(sb, entity);
        sb.append("    }");
        sb.append(NEW_LINE);
    }

}
