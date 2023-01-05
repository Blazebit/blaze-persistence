/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.view.processor.serialization;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public abstract class SerializationField implements Comparable<SerializationField> {

    public abstract Element getElement();

    public abstract String getName();

    public abstract TypeKind getTypeKind();

    public abstract char getTypeCode();

    public abstract String getTypeString();

    public abstract boolean isPrimitive();

    @Override
    public int compareTo(SerializationField other) {
        boolean isPrim = isPrimitive();
        if (isPrim != other.isPrimitive()) {
            return isPrim ? -1 : 1;
        }
        return getName().compareTo(other.getName());
    }

    public static String getMethodSignature(ExecutableElement meth) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (VariableElement parameter : meth.getParameters()) {
            sb.append(getClassSignature(parameter.asType()));
        }
        sb.append(')');
        sb.append(getClassSignature(meth.getReturnType()));
        return sb.toString();
    }

    public static String getClassSignature(TypeMirror typeMirror) {
        StringBuilder sb = new StringBuilder();
        while (typeMirror.getKind() == TypeKind.ARRAY) {
            sb.append('[');
            typeMirror = ((ArrayType) typeMirror).getComponentType();
        }
        if (typeMirror.getKind().isPrimitive()) {
            switch (typeMirror.getKind()) {
                case INT:
                    sb.append('I');
                    break;
                case BYTE:
                    sb.append('B');
                    break;
                case LONG:
                    sb.append('J');
                    break;
                case FLOAT:
                    sb.append('F');
                    break;
                case DOUBLE:
                    sb.append('D');
                    break;
                case SHORT:
                    sb.append('S');
                    break;
                case CHAR:
                    sb.append('C');
                    break;
                case BOOLEAN:
                    sb.append('Z');
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported primitive type: " + typeMirror.toString());
            }
        } else {
            if (typeMirror.getKind() == TypeKind.TYPEVAR) {
                TypeVariable typeVariable = (TypeVariable) typeMirror;
                if (typeVariable.getLowerBound().getKind() == TypeKind.NULL) {
                    typeMirror = typeVariable.getUpperBound();
                } else {
                    typeMirror = typeVariable.getLowerBound();
                }
            }
            if (typeMirror.getKind() == TypeKind.VOID) {
                sb.append('V');
            } else {
                String className = typeMirror.toString();
                sb.ensureCapacity(sb.length() + className.length() + 2);
                sb.append('L');
                for (int i = 0; i < className.length(); i++) {
                    final char c = className.charAt(i);
                    if (c == '.') {
                        sb.append('/');
                    } else {
                        sb.append(c);
                    }
                }
                sb.append(';');
            }
        }
        return sb.toString();
    }

    public static long computeDefaultSUID(TypeElement clazz, List<SerializationField> fields) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);

            dout.writeUTF(clazz.getQualifiedName().toString());
            Set<Modifier> modifiers = clazz.getModifiers();

            int classMods = 0;
            if (modifiers.contains(Modifier.PUBLIC)) {
                classMods = classMods | java.lang.reflect.Modifier.PUBLIC;
            }
            if (modifiers.contains(Modifier.FINAL)) {
                classMods = classMods | java.lang.reflect.Modifier.FINAL;
            }
            if (clazz.getKind() == ElementKind.INTERFACE) {
                classMods = classMods | java.lang.reflect.Modifier.INTERFACE;
            }
            if (modifiers.contains(Modifier.ABSTRACT)) {
                classMods = classMods | java.lang.reflect.Modifier.ABSTRACT;
            }

            List<ExecutableElement> methods = new ArrayList<>();
            List<ExecutableElement> cons = new ArrayList<>();
            boolean hasStaticInitializer = false;
            for (Element enclosedElement : clazz.getEnclosedElements()) {
                if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                    cons.add((ExecutableElement) enclosedElement);
                } else if (enclosedElement.getKind() == ElementKind.METHOD) {
                    methods.add((ExecutableElement) enclosedElement);
                } else if (enclosedElement.getKind() == ElementKind.STATIC_INIT) {
                    hasStaticInitializer = true;
                }
            }


            /*
             * compensate for javac bug in which ABSTRACT bit was set for an
             * interface only if the interface declared methods
             */
            if ((classMods & java.lang.reflect.Modifier.INTERFACE) != 0) {
                classMods = (methods.size() > 0) ?
                    (classMods | java.lang.reflect.Modifier.ABSTRACT) :
                    (classMods & ~java.lang.reflect.Modifier.ABSTRACT);
            }
            dout.writeInt(classMods);

            /*
             * compensate for change in 1.2FCS in which
             * Class.getInterfaces() was modified to return Cloneable and
             * Serializable for array classes.
             */
            List<? extends TypeMirror> interfaces = clazz.getInterfaces();
            String[] ifaceNames = new String[interfaces.size()];
            for (int i = 0; i < interfaces.size(); i++) {
                ifaceNames[i] = ((TypeElement) ((DeclaredType) interfaces.get(i)).asElement()).getQualifiedName().toString();
            }
            Arrays.sort(ifaceNames);
            for (int i = 0; i < ifaceNames.length; i++) {
                dout.writeUTF(ifaceNames[i]);
            }

            MemberSignature[] fieldSigs = new MemberSignature[fields.size()];
            for (int i = 0; i < fields.size(); i++) {
                fieldSigs[i] = new MemberSignature((VariableElement) fields.get(i).getElement());
            }
            Arrays.sort(fieldSigs, new Comparator<MemberSignature>() {
                public int compare(MemberSignature ms1, MemberSignature ms2) {
                    return ms1.getName().compareTo(ms2.getName());
                }
            });
            for (int i = 0; i < fieldSigs.length; i++) {
                MemberSignature sig = fieldSigs[i];
                Set<Modifier> fieldModifiers = sig.getModifiers();
                int mods = 0;
                if (fieldModifiers.contains(Modifier.PUBLIC)) {
                    mods = mods | java.lang.reflect.Modifier.PUBLIC;
                }
                if (fieldModifiers.contains(Modifier.PRIVATE)) {
                    mods = mods | java.lang.reflect.Modifier.PRIVATE;
                }
                if (fieldModifiers.contains(Modifier.PROTECTED)) {
                    mods = mods | java.lang.reflect.Modifier.PROTECTED;
                }
                if (fieldModifiers.contains(Modifier.STATIC)) {
                    mods = mods | java.lang.reflect.Modifier.STATIC;
                }
                if (fieldModifiers.contains(Modifier.FINAL)) {
                    mods = mods | java.lang.reflect.Modifier.FINAL;
                }
                if (fieldModifiers.contains(Modifier.VOLATILE)) {
                    mods = mods | java.lang.reflect.Modifier.VOLATILE;
                }
                if (fieldModifiers.contains(Modifier.TRANSIENT)) {
                    mods = mods | java.lang.reflect.Modifier.TRANSIENT;
                }
                if (((mods & java.lang.reflect.Modifier.PRIVATE) == 0) ||
                    ((mods & (java.lang.reflect.Modifier.STATIC | java.lang.reflect.Modifier.TRANSIENT)) == 0)) {
                    dout.writeUTF(sig.getName());
                    dout.writeInt(mods);
                    dout.writeUTF(sig.getSignature());
                }
            }

            if (hasStaticInitializer) {
                dout.writeUTF("<clinit>");
                dout.writeInt(java.lang.reflect.Modifier.STATIC);
                dout.writeUTF("()V");
            }

            MemberSignature[] consSigs = new MemberSignature[cons.size()];
            for (int i = 0; i < cons.size(); i++) {
                consSigs[i] = new MemberSignature(cons.get(i));
            }
            Arrays.sort(consSigs, new Comparator<MemberSignature>() {
                public int compare(MemberSignature ms1, MemberSignature ms2) {
                    return ms1.getSignature().compareTo(ms2.getSignature());
                }
            });
            for (int i = 0; i < consSigs.length; i++) {
                MemberSignature sig = consSigs[i];
                Set<Modifier> constructorModifiers = sig.getModifiers();
                int mods = 0;
                if (constructorModifiers.contains(Modifier.PUBLIC)) {
                    mods = mods | java.lang.reflect.Modifier.PUBLIC;
                }
                if (constructorModifiers.contains(Modifier.PRIVATE)) {
                    mods = mods | java.lang.reflect.Modifier.PRIVATE;
                }
                if (constructorModifiers.contains(Modifier.PROTECTED)) {
                    mods = mods | java.lang.reflect.Modifier.PROTECTED;
                }
                if (constructorModifiers.contains(Modifier.STATIC)) {
                    mods = mods | java.lang.reflect.Modifier.STATIC;
                }
                if (constructorModifiers.contains(Modifier.FINAL)) {
                    mods = mods | java.lang.reflect.Modifier.FINAL;
                }
                if (constructorModifiers.contains(Modifier.SYNCHRONIZED)) {
                    mods = mods | java.lang.reflect.Modifier.SYNCHRONIZED;
                }
                if (constructorModifiers.contains(Modifier.NATIVE)) {
                    mods = mods | java.lang.reflect.Modifier.NATIVE;
                }
                if (constructorModifiers.contains(Modifier.ABSTRACT)) {
                    mods = mods | java.lang.reflect.Modifier.ABSTRACT;
                }
                if (constructorModifiers.contains(Modifier.STRICTFP)) {
                    mods = mods | java.lang.reflect.Modifier.STRICT;
                }
                if ((mods & java.lang.reflect.Modifier.PRIVATE) == 0) {
                    dout.writeUTF("<init>");
                    dout.writeInt(mods);
                    dout.writeUTF(sig.getSignature().replace('/', '.'));
                }
            }

            MemberSignature[] methSigs = new MemberSignature[methods.size()];
            for (int i = 0; i < methods.size(); i++) {
                methSigs[i] = new MemberSignature(methods.get(i));
            }
            Arrays.sort(methSigs, new Comparator<MemberSignature>() {
                public int compare(MemberSignature ms1, MemberSignature ms2) {
                    int comp = ms1.getName().compareTo(ms2.getName());
                    if (comp == 0) {
                        comp = ms1.getSignature().compareTo(ms2.getSignature());
                    }
                    return comp;
                }
            });
            for (int i = 0; i < methSigs.length; i++) {
                MemberSignature sig = methSigs[i];
                Set<Modifier> methodModifiers = sig.getModifiers();
                int mods = 0;
                if (methodModifiers.contains(Modifier.PUBLIC)) {
                    mods = mods | java.lang.reflect.Modifier.PUBLIC;
                }
                if (methodModifiers.contains(Modifier.PRIVATE)) {
                    mods = mods | java.lang.reflect.Modifier.PRIVATE;
                }
                if (methodModifiers.contains(Modifier.PROTECTED)) {
                    mods = mods | java.lang.reflect.Modifier.PROTECTED;
                }
                if (methodModifiers.contains(Modifier.STATIC)) {
                    mods = mods | java.lang.reflect.Modifier.STATIC;
                }
                if (methodModifiers.contains(Modifier.FINAL)) {
                    mods = mods | java.lang.reflect.Modifier.FINAL;
                }
                if (methodModifiers.contains(Modifier.SYNCHRONIZED)) {
                    mods = mods | java.lang.reflect.Modifier.SYNCHRONIZED;
                }
                if (methodModifiers.contains(Modifier.NATIVE)) {
                    mods = mods | java.lang.reflect.Modifier.NATIVE;
                }
                if (methodModifiers.contains(Modifier.ABSTRACT)) {
                    mods = mods | java.lang.reflect.Modifier.ABSTRACT;
                }
                if (methodModifiers.contains(Modifier.STRICTFP)) {
                    mods = mods | java.lang.reflect.Modifier.STRICT;
                }
                if ((mods & java.lang.reflect.Modifier.PRIVATE) == 0) {
                    dout.writeUTF(sig.getName());
                    dout.writeInt(mods);
                    dout.writeUTF(sig.getSignature().replace('/', '.'));
                }
            }

            dout.flush();

            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] hashBytes = md.digest(bout.toByteArray());
            long hash = 0;
            for (int i = Math.min(hashBytes.length, 8) - 1; i >= 0; i--) {
                hash = (hash << 8) | (hashBytes[i] & 0xFF);
            }
            return hash;
        } catch (IOException ex) {
            throw new InternalError(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new SecurityException(ex.getMessage());
        }
    }
}
