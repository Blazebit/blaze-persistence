/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.view.impl.proxy;

import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.SignatureAttribute;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ProxyFactory {

    private static final AtomicInteger classCounter = new AtomicInteger();
    private final ConcurrentMap<Class<?>, Class<?>> proxyClasses = new ConcurrentHashMap<Class<?>, Class<?>>();
    private final ClassPool pool;
    private final CtClass objectCc;

    public ProxyFactory() {
        this.pool = new ClassPool(ClassPool.getDefault());
        try {
            this.objectCc = pool.getCtClass("java.lang.Object");
        } catch (NotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> Class<? extends T> getProxy(ViewType<T> viewType) {
        Class<T> clazz = viewType.getJavaType();
        Class<? extends T> proxyClass = (Class<? extends T>) proxyClasses.get(clazz);

        if (proxyClass == null) {
            proxyClass = createProxyClass(viewType);
            Class<? extends T> oldProxyClass = (Class<? extends T>) proxyClasses.putIfAbsent(clazz, proxyClass);

            if (oldProxyClass != null) {
                proxyClass = oldProxyClass;
            }
        }

        return proxyClass;
    }

    private <T> Class<? extends T> createProxyClass(ViewType<T> viewType) {
        Class<?> clazz = viewType.getJavaType();
        CtClass cc = pool.makeClass(clazz.getName() + "_$$_javassist_entityview_" + classCounter.getAndIncrement());
        CtClass superCc;

        ClassPath classPath = new ClassClassPath(clazz);
        pool.insertClassPath(classPath);

        try {
            superCc = pool.get(clazz.getName());

            if (clazz.isInterface()) {
                cc.addInterface(superCc);
            } else {
                cc.setSuperclass(superCc);
            }

            Set<MethodAttribute<? super T, ?>> attributes = viewType.getAttributes();
            CtField[] attributeFields = new CtField[attributes.size()];
            CtClass[] attributeTypes = new CtClass[attributes.size()];
            int i = 1;

            // Create the id field
            MethodAttribute<? super T, ?> idAttribute = viewType.getIdAttribute();
            CtField idField = addMembersForAttribute(idAttribute, clazz, cc);
            attributeFields[0] = idField;
            attributeTypes[0] = idField.getType();
            attributes.remove(idAttribute);

            for (MethodAttribute<?, ?> attribute : attributes) {
                if (attribute == idAttribute) {
                    continue;
                }
                
                CtField attributeField = addMembersForAttribute(attribute, clazz, cc);
                attributeFields[i] = attributeField;
                attributeTypes[i] = attributeField.getType();
                i++;
            }

            CtClass equalsDeclaringClass = superCc.getMethod("equals", getEqualsDesc()).getDeclaringClass();
            if (equalsDeclaringClass != objectCc) {
                throw new IllegalArgumentException("The class '" + equalsDeclaringClass.getName() + "' declares 'boolean equals(java.lang.Object)' but is not allowed to!");
            }
            cc.addMethod(createEquals(cc, idField));

            CtClass hashCodeDeclaringClass = superCc.getMethod("hashCode", getHashCodeDesc()).getDeclaringClass();
            if (hashCodeDeclaringClass != objectCc) {
                throw new IllegalArgumentException("The class '" + hashCodeDeclaringClass.getName() + "' declares 'int hashCode()' but is not allowed to!");
            }
            cc.addMethod(createHashCode(cc, idField));

            // Add the default constructor only for interfaces since abstract classes may omit it
            if (clazz.isInterface()) {
                cc.addConstructor(createConstructor(cc, attributeFields, attributeTypes));
            }

            Set<MappingConstructor<T>> constructors = viewType.getConstructors();

            for (MappingConstructor<?> constructor : constructors) {
                // Copy default constructor parameters
                int constructorParameterCount = 1 + attributes.size() + constructor.getParameterAttributes().size();
                CtClass[] constructorAttributeTypes = new CtClass[constructorParameterCount];
                System.arraycopy(attributeTypes, 0, constructorAttributeTypes, 0, 1 + attributes.size());

                // Append super constructor parameters to default constructor parameters
                CtConstructor superConstructor = findConstructor(superCc, constructor);
                System.arraycopy(superConstructor.getParameterTypes(), 0, constructorAttributeTypes, 1 + attributes.size(), superConstructor.getParameterTypes().length);

                cc.addConstructor(createConstructor(cc, attributeFields, constructorAttributeTypes));
            }

            return cc.toClass();
        } catch (Exception ex) {
            throw new RuntimeException("Probably we did something wrong, please contact us if you see this message.", ex);
        } finally {
            pool.removeClassPath(classPath);
        }
    }

    private CtField addMembersForAttribute(MethodAttribute<?, ?> attribute, Class<?> clazz, CtClass cc) throws CannotCompileException, NotFoundException {
        Method getter = attribute.getJavaMethod();
        Method setter = ReflectionUtils.getSetter(clazz, attribute.getName());
        
        // Create the field from the attribute
        CtField attributeField = new CtField(getType(attribute), attribute.getName(), cc);
        attributeField.setModifiers(getModifiers(setter != null));
        String genericSignature = getGenericSignature(attribute, attributeField);
        if (genericSignature != null) {
            setGenericSignature(attributeField, genericSignature);
        }
        cc.addField(attributeField);
        
        boolean createBridges = !attribute.getJavaType().equals(getter.getReturnType());
        
        CtMethod attributeGetter = CtNewMethod.getter(getter.getName(), attributeField);
        
        if (genericSignature != null) {
            String getterGenericSignature = "()" + genericSignature;
            setGenericSignature(attributeGetter, getterGenericSignature);
        }
        
        if (createBridges) {
            CtMethod getterBridge = createGetterBridge(cc, getter, attributeGetter);
            cc.addMethod(getterBridge);
        }
        cc.addMethod(attributeGetter);
        
        if (setter != null) {
            CtMethod attributeSetter = CtNewMethod.setter(setter.getName(), attributeField);
            if (genericSignature != null) {
                String setterGenericSignature = "(" + genericSignature + ")V";
                setGenericSignature(attributeSetter, setterGenericSignature);
            }
            
            if (createBridges) {
                CtMethod setterBridge = createSetterBridge(cc, setter, attributeSetter);
                cc.addMethod(setterBridge);
            }
            cc.addMethod(attributeSetter);
        }
        
        return attributeField;
    }
    
    private void setGenericSignature(CtField field, String signature) {
        FieldInfo fieldInfo = field.getFieldInfo();
        fieldInfo.addAttribute(new SignatureAttribute(fieldInfo.getConstPool(), signature));
    }
    
    private void setGenericSignature(CtMethod method, String signature) {
        MethodInfo methodInfo = method.getMethodInfo();
        methodInfo.addAttribute(new SignatureAttribute(methodInfo.getConstPool(), signature));
    }

    private String getEqualsDesc() throws NotFoundException {
        CtClass returnType = CtClass.booleanType;
        CtClass parameterType = objectCc;
        return "(" + Descriptor.of(parameterType) + ")" + Descriptor.of(returnType);
    }

    private CtMethod createEquals(CtClass cc, CtField... fields) throws NotFoundException, CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        MethodInfo method = new MethodInfo(cp, "equals", getEqualsDesc());
        method.setAccessFlags(AccessFlag.PUBLIC);
        CtMethod m = CtMethod.make(method, cc);
        StringBuilder sb = new StringBuilder();

        sb.append('{');
        sb.append("\tif ($1 == null) { return false; }\n");
        sb.append("\tif ($0.getClass() != $1.getClass()) { return false; }\n");
        sb.append("\tfinal ").append(cc.getName()).append(" other = (").append(cc.getName()).append(") $1;\n");

        for (CtField field : fields) {
            sb.append("\tif ($0.").append(field.getName()).append(" != other.").append(field.getName());
            sb.append(" && ($0.").append(field.getName()).append(" == null");
            sb.append(" || !$0.").append(field.getName()).append(".equals(other.").append(field.getName()).append("))) {\n");
            sb.append("\t\treturn false;\n\t}\n");
        }

        sb.append("\treturn true;\n");
        sb.append('}');
        m.setBody(sb.toString());
        return m;
    }

    private String getHashCodeDesc() throws NotFoundException {
        CtClass returnType = CtClass.intType;
        return "()" + Descriptor.of(returnType);
    }

    private CtMethod createHashCode(CtClass cc, CtField... fields) throws NotFoundException, CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        CtClass returnType = CtClass.intType;
        String desc = "()" + Descriptor.of(returnType);
        MethodInfo method = new MethodInfo(cp, "hashCode", desc);
        method.setAccessFlags(AccessFlag.PUBLIC);
        CtMethod m = CtMethod.make(method, cc);
        StringBuilder sb = new StringBuilder();

        sb.append('{');
        sb.append("\tint hash = 3;\n");

        for (CtField field : fields) {
            sb.append("\thash = 83 * hash + ($0.").append(field.getName()).append(" != null ? ");
            sb.append("$0.").append(field.getName()).append(".hashCode() : 0);\n");
        }

        sb.append("\treturn hash;\n");
        sb.append('}');
        m.setBody(sb.toString());
        return m;
    }

    private CtMethod createGetterBridge(CtClass cc, Method getter, CtMethod attributeGetter) throws NotFoundException, CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        CtClass bridgeReturnType = pool.get(getter.getReturnType().getName());
        String desc = "()" + Descriptor.of(bridgeReturnType);
        MethodInfo bridge = new MethodInfo(cp, getter.getName(), desc);
        bridge.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.BRIDGE | AccessFlag.SYNTHETIC);

        Bytecode code = new Bytecode(cp, 2, 1);
        code.addAload(0);
        code.addInvokevirtual(cc, getter.getName(), attributeGetter.getReturnType(), null);
        code.addReturn(bridgeReturnType);

        bridge.setCodeAttribute(code.toCodeAttribute());
        return CtMethod.make(bridge, cc);
    }

    private CtMethod createSetterBridge(CtClass cc, Method setter, CtMethod attributeSetter) throws NotFoundException, CannotCompileException {
        ConstPool cp = cc.getClassFile2().getConstPool();
        CtClass bridgeParameterType = pool.get(setter.getParameterTypes()[0].getName());
        String desc = "(" + Descriptor.of(bridgeParameterType) + ")V";
        MethodInfo bridge = new MethodInfo(cp, setter.getName(), desc);
        bridge.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.BRIDGE | AccessFlag.SYNTHETIC);

        Bytecode code = new Bytecode(cp, 2, 2);
        code.addAload(0);
        code.addAload(1);
        code.addCheckcast(attributeSetter.getParameterTypes()[0]);
        code.addInvokevirtual(cc, setter.getName(), CtClass.voidType, attributeSetter.getParameterTypes());
        code.addReturn(CtClass.voidType);

        bridge.setCodeAttribute(code.toCodeAttribute());
        return CtMethod.make(bridge, cc);
    }

    private CtConstructor createConstructor(CtClass cc, CtField[] attributeFields, CtClass[] attributeTypes) throws CannotCompileException {
        CtConstructor ctConstructor = new CtConstructor(attributeTypes, cc);
        ctConstructor.setModifiers(Modifier.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("{\n\tsuper(");
        for (int i = attributeFields.length; i < attributeTypes.length; i++) {
            if (i != attributeFields.length) {
                sb.append(',');
            }

            sb.append('$').append(i + 1);
        }
        sb.append(");\n");

        for (int i = 0; i < attributeFields.length; i++) {
            sb.append("\tthis.").append(attributeFields[i].getName()).append(" = ").append('$').append(i + 1).append(";\n");
        }

        sb.append('}');
        ctConstructor.setBody(sb.toString());
        return ctConstructor;
    }

    private <T> CtConstructor findConstructor(CtClass superCc, MappingConstructor<T> constructor) throws NotFoundException {
        List<ParameterAttribute<? super T, ?>> parameterAttributes = constructor.getParameterAttributes();
        CtClass[] parameterTypes = new CtClass[parameterAttributes.size()];

        for (int i = 0; i < parameterAttributes.size(); i++) {
            parameterTypes[i] = pool.get(parameterAttributes.get(i).getJavaType().getName());
        }

        return superCc.getDeclaredConstructor(parameterTypes);
    }

    private CtClass getType(MethodAttribute<?, ?> attribute) throws NotFoundException {
        return pool.get(attribute.getJavaType().getName());
    }

    private int getModifiers(boolean hasSetter) {
        if (hasSetter) {
            return Modifier.PRIVATE;
        } else {
            return Modifier.PRIVATE | Modifier.FINAL;
        }
    }

    private String getGenericSignature(MethodAttribute<?, ?> attribute, CtField attributeField) throws NotFoundException {
        Class[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(attribute.getDeclaringType().getJavaType(), attribute.getJavaMethod());
        if (typeArguments.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder(typeArguments.length * 10);

        String simpleType = Descriptor.of(attributeField.getType());
        sb.append(simpleType, 0, simpleType.length() - 1);
        sb.append('<');

        for (int i = 0; i < typeArguments.length; i++) {
            if (typeArguments[i] == null) {
                throw new IllegalArgumentException("The type argument can not be resolved at index '" + i + "' for the attribute '" + attribute.getName() + "' of the class '" + attribute.getDeclaringType().getJavaType().getName() + "'!");
            }
            
            sb.append(Descriptor.of(typeArguments[i].getName()));
        }

        sb.append('>');
        sb.append(';');

        return sb.toString();
    }
}
