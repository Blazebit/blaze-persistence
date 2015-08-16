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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.SignatureAttribute;

import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ProxyFactory {

    private static final AtomicInteger classCounter = new AtomicInteger();
    private final ConcurrentMap<Class<?>, Class<?>> proxyClasses = new ConcurrentHashMap<Class<?>, Class<?>>();
    private final ConcurrentMap<Class<?>, Class<?>> unsafeProxyClasses = new ConcurrentHashMap<Class<?>, Class<?>>();
    private final ClassPool pool;
    private final Object proxyLock = new Object();
    
    public ProxyFactory() {
        this.pool = new ClassPool(ClassPool.getDefault());
    }
    
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> getProxy(ViewType<T> viewType) {
        Class<T> clazz = viewType.getJavaType();
		Class<? extends T> proxyClass = (Class<? extends T>) proxyClasses.get(clazz);

        if (proxyClass == null) {
        	synchronized (proxyLock) {
        		proxyClass = (Class<? extends T>) proxyClasses.get(clazz);
                if (proxyClass == null) {
		            proxyClass = createProxyClass(viewType);
		            Class<? extends T> oldProxyClass = (Class<? extends T>) proxyClasses.putIfAbsent(clazz, proxyClass);
		
		            if (oldProxyClass != null) {
		                proxyClass = oldProxyClass;
		            }
                }
        	}
        }

        return proxyClass;
    }

    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> getUnsafeProxy(ViewType<T> viewType) {
        Class<T> proxyClass = (Class<T>) getProxy(viewType);
		Class<? extends T> unsafeProxyClass = (Class<? extends T>) unsafeProxyClasses.get(proxyClass);

        if (unsafeProxyClass == null) {
        	synchronized (proxyLock) {
	        	unsafeProxyClass = (Class<? extends T>) unsafeProxyClasses.get(proxyClass);
	        	if (unsafeProxyClass == null) {
		        	unsafeProxyClass = createUnsafeProxyClass(viewType, proxyClass);
		            Class<? extends T> oldUnsafeProxyClass = (Class<? extends T>) unsafeProxyClasses.putIfAbsent(proxyClass, unsafeProxyClass);
		
		            if (oldUnsafeProxyClass != null) {
		            	unsafeProxyClass = oldUnsafeProxyClass;
		            }
	        	}
        	}
        }

        return unsafeProxyClass;
    }

    @SuppressWarnings("unchecked")
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
            if (!"java.lang.Object".equals(equalsDeclaringClass.getName())) {
                throw new IllegalArgumentException("The class '" + equalsDeclaringClass.getName() + "' declares 'boolean equals(java.lang.Object)' but is not allowed to!");
            }
            cc.addMethod(createEquals(cc, idField));

            CtClass hashCodeDeclaringClass = superCc.getMethod("hashCode", getHashCodeDesc()).getDeclaringClass();
            if (!"java.lang.Object".equals(hashCodeDeclaringClass.getName())) {
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

//            cc.debugWriteFile("D:\\");
            return cc.toClass();
        } catch (Exception ex) {
            throw new RuntimeException("Probably we did something wrong, please contact us if you see this message.", ex);
        } finally {
            pool.removeClassPath(classPath);
        }
    }
    
    private void addFields(CtClass newClass, CtClass cc) throws NotFoundException, CannotCompileException {
    	Map<String, String> fieldClassMappings = new HashMap<String, String>();
    	CtClass current = cc;
    	
    	do {
    		for (CtField field : current.getDeclaredFields()) {
    			String oldClassMapping = fieldClassMappings.put(field.getName(), current.getName());
    			if (oldClassMapping != null) {
    				throw new IllegalArgumentException("Can not use unsafe instantiator because field name '" + field.getName() + "' appears in both classes which are on the same hierarchy [" + oldClassMapping + ", " + current.getName() + "]");
    			}
    			
    			newClass.addField(new CtField(field, newClass));
    		}
    	} while (!"java.lang.Object".equals((current = current.getSuperclass()).getName()));
    }
    
    private void addMethods(CtClass newClass, CtClass cc) throws NotFoundException, CannotCompileException {
    	Map<String, String> methodClassMappings = new HashMap<String, String>();
    	CtClass current = cc;
    	
    	do {
    		for (CtMethod method : current.getDeclaredMethods()) {
    			String name = method.getName() + Descriptor.toString(method.getSignature()) + Descriptor.of(method.getReturnType());
    			String oldClassMapping = methodClassMappings.put(name, current.getName());
    			if (oldClassMapping != null) {
    				throw new IllegalArgumentException("Can not use unsafe instantiator because method name '" + method.getName() + "' appears in both classes which are on the same hierarchy [" + oldClassMapping + ", " + current.getName() + "]");
    			}
    			
    			newClass.addMethod(new CtMethod(method, newClass, null));
    		}
    	} while (!"java.lang.Object".equals((current = current.getSuperclass()).getName()));
    }
    
    private void addConstructors(CtClass newClass, CtClass cc) throws NotFoundException, CannotCompileException, BadBytecode {
    	ConstPool cp = cc.getClassFile2().getConstPool();
    	
		for (CtConstructor constructor : cc.getDeclaredConstructors()) {
			CtConstructor newConstructor = new CtConstructor(constructor.getParameterTypes(), newClass);
			CodeAttribute codeAttribute = constructor.getMethodInfo2().getCodeAttribute();
			Bytecode newBytecode = new Bytecode(newClass.getClassFile2().getConstPool(), 3, constructor.getParameterTypes().length + 1);
			
		    CodeIterator ci = codeAttribute.iterator();
	    	int superConstructorIndex = ci.skipSuperConstructor();
	    	int superConstructorRef = ci.u16bitAt(superConstructorIndex + 1);
	    	String superConstructorType = cp.getMethodrefType(superConstructorRef);
	    	CtClass[] superConstructorArgumentTypes = Descriptor.getParameterTypes(superConstructorType, pool);
	    	int[] argumentPositions = new int[superConstructorArgumentTypes.length];
	    	
	    	// Simple translation from proxy to base class
	    	final int offset = (constructor.getParameterTypes().length - argumentPositions.length) + 1;
	    	for (int i = 0; i < argumentPositions.length; i++) {
	    		argumentPositions[i] = offset + i;
	    	}
	    	
	    	newBytecode.addAload(0);
	    	newBytecode.addInvokespecial("java.lang.Object", "<init>", "()V");

	    	copyBytecode(newClass, newBytecode, cp, ci);
	    	
	    	int newStartIndex = newBytecode.getSize();
	    	
	    	// Inline super class constructor with line table adaption
		    addConstructorCode(newClass, newBytecode, cc.getSuperclass(), superConstructorArgumentTypes, argumentPositions);
	    	newBytecode.add(Bytecode.RETURN);
	    	newConstructor.getMethodInfo2().setCodeAttribute(newBytecode.toCodeAttribute());
			newClass.addConstructor(newConstructor);
		}
    }

    private void addConstructorCode(CtClass newClass, Bytecode newBytecode, CtClass cc, CtClass[] constructorTypes, int[] argumentPositions) throws NotFoundException, BadBytecode {
    	ConstPool cp = cc.getClassFile2().getConstPool();
		CtConstructor constructor = cc.getDeclaredConstructor(constructorTypes);
		CodeAttribute codeAttribute = constructor.getMethodInfo2().getCodeAttribute();

	    CodeIterator ci = codeAttribute.iterator();
    	int superConstructorIndex = ci.skipSuperConstructor();
    	int superConstructorRef = ci.u16bitAt(superConstructorIndex + 1);
    	String superConstructorType = cp.getMethodrefType(superConstructorRef);
    	
    	// TODO: need remapping of arguments
    	if (!"java.lang.Object".equals(cc.getSuperclass().getName())) {
    		throw new UnsupportedOperationException("Not yet implemented");
//    		addConstructorCode(newBytecode, cc.getSuperclass(), Descriptor.getParameterTypes(superConstructorType, pool));
    	}

    	newBytecode.setMaxStack(Math.max(newBytecode.getMaxStack(), codeAttribute.getMaxStack()));
    	copyBytecode(newClass, newBytecode, cc.getName(), cp, ci, argumentPositions);
	}
    
    private void copyBytecode(CtClass newClass, Bytecode newBytecode, ConstPool cp, CodeIterator ci) throws BadBytecode {
    	while (ci.hasNext()) {
	        int address = ci.next();
    		int op = ci.byteAt(address);
    		
	        switch (op) {
	        case Bytecode.RETURN:
	        	// Skip returns
	        	break;
        	default:
        		copySingleBytecode(newClass, newBytecode, null, cp, ci, address);
	        }
    	}
    }
    
    private void copyBytecode(CtClass newClass, Bytecode newBytecode, String oldClass, ConstPool cp, CodeIterator ci, int[] argumentPositions) throws BadBytecode {
    	while (ci.hasNext()) {
	        int address = ci.next();
    		int op = ci.byteAt(address);
    		
	        switch (op) {
	        case Bytecode.RETURN:
	        	// Skip returns
	        	break;
	        case Bytecode.ALOAD_1:
	        	newBytecode.addAload(argumentPositions[0]);
	        	break;
	        case Bytecode.ALOAD_2:
	        	newBytecode.addAload(argumentPositions[1]);
	        	break;
	        case Bytecode.ALOAD_3:
	        	newBytecode.addAload(argumentPositions[2]);
	        	break;
	        case Bytecode.ALOAD:
	        	newBytecode.addAload(argumentPositions[ci.byteAt(address + 1) - 1]);
	        	break;
        	default:
        		copySingleBytecode(newClass, newBytecode, oldClass, cp, ci, address);
	        }
    	}
    }
    
    private void copySingleBytecode(CtClass newClass, Bytecode newBytecode, String oldClass, ConstPool cp, CodeIterator ci, int address) {
    	ConstPool newCp = newBytecode.getConstPool();
		int op = ci.byteAt(address);

		String className;
		String name;
		String type;
		int count;
		Object ldcValue;
		
        switch (op) {
        case Bytecode.ANEWARRAY:
        	className = cp.getClassInfo(ci.u16bitAt(address + 1));
        	newBytecode.addAnewarray(className);
        	break;
        case Bytecode.CHECKCAST:
        	className = cp.getClassInfo(ci.u16bitAt(address + 1));
        	newBytecode.addCheckcast(className);
        	break;
        case Bytecode.GETFIELD:
        	name = cp.getFieldrefName(ci.u16bitAt(address + 1));
        	type = cp.getFieldrefType(ci.u16bitAt(address + 1));
        	newBytecode.addGetfield(newClass, name, type);
        	break;
        case Bytecode.GETSTATIC:
        	className = cp.getFieldrefClassName(ci.u16bitAt(address + 1));
        	name = cp.getFieldrefName(ci.u16bitAt(address + 1));
        	type = cp.getFieldrefType(ci.u16bitAt(address + 1));
        	newBytecode.addGetstatic(className, name, type);
        	break;
        case Bytecode.GOTO:
        case Bytecode.GOTO_W:
            // TODO: Rewrite branches?
        	throw new IllegalArgumentException("Unsupported byte code op: " + op);
        case Bytecode.IF_ACMPEQ:
        case Bytecode.IF_ACMPNE:
        case Bytecode.IF_ICMPEQ:
        case Bytecode.IF_ICMPGE:
        case Bytecode.IF_ICMPGT:
        case Bytecode.IF_ICMPLE:
        case Bytecode.IF_ICMPLT:
        case Bytecode.IF_ICMPNE:
        case Bytecode.IFEQ:
        case Bytecode.IFGE:
        case Bytecode.IFGT:
        case Bytecode.IFLE:
        case Bytecode.IFLT:
        case Bytecode.IFNE:
        case Bytecode.IFNONNULL:
        case Bytecode.IFNULL:
            // TODO: Rewrite branches?
        	throw new IllegalArgumentException("Unsupported byte code op: " + op);
        case Bytecode.INSTANCEOF:
        	className = cp.getClassInfo(ci.u16bitAt(address + 1));
        	newBytecode.addInstanceof(className);
        	break;
        case 186: // Invoke-dynamic
        	throw new IllegalArgumentException("Unsupported byte code op: " + op);
        case Bytecode.INVOKEINTERFACE:
        	className = cp.getInterfaceMethodrefClassName(ci.u16bitAt(address + 1));
        	name = cp.getInterfaceMethodrefName(ci.u16bitAt(address + 1));
        	type = cp.getInterfaceMethodrefType(ci.u16bitAt(address + 1));
        	count = ci.byteAt(address + 3);
        	newBytecode.addInvokeinterface(className, name, type, count);
        	break;
        case Bytecode.INVOKESPECIAL:
        	className = cp.getMethodrefClassName(ci.u16bitAt(address + 1));
        	name = cp.getMethodrefName(ci.u16bitAt(address + 1));
        	type = cp.getMethodrefType(ci.u16bitAt(address + 1));
        	newBytecode.addInvokespecial(className, name, type);
        	break;
        case Bytecode.INVOKESTATIC:
        	className = cp.getMethodrefClassName(ci.u16bitAt(address + 1));
        	name = cp.getMethodrefName(ci.u16bitAt(address + 1));
        	type = cp.getMethodrefType(ci.u16bitAt(address + 1));
        	newBytecode.addInvokestatic(className, name, type);
        	break;
        case Bytecode.INVOKEVIRTUAL:
        	className = cp.getMethodrefClassName(ci.u16bitAt(address + 1));
        	name = cp.getMethodrefName(ci.u16bitAt(address + 1));
        	type = cp.getMethodrefType(ci.u16bitAt(address + 1));
        	newBytecode.addInvokevirtual(resolveCopyName(className, newClass.getName(), oldClass), name, type);
        	break;
        case Bytecode.JSR:
        case Bytecode.JSR_W:
            // TODO: Rewrite branches?
        	throw new IllegalArgumentException("Unsupported byte code op: " + op);
        case Bytecode.LDC:
        	ldcValue = cp.getLdcValue(ci.byteAt(address + 1));
        	count = addLdcValue(newCp, ldcValue);
        	newBytecode.addLdc(count);
        	break;
        case Bytecode.LDC2_W:
        	ldcValue = cp.getLdcValue(ci.u16bitAt(address + 1));
            if (ldcValue instanceof Long) {
            	newBytecode.addLdc2w((Long) ldcValue);
            } else if (ldcValue instanceof Double) {
            	newBytecode.addLdc2w((Double) ldcValue);
            } else {
            	throw new IllegalArgumentException("Unsupported ldc2w value: " + ldcValue);
            }
        	break;
        case Bytecode.LDC_W:
        	ldcValue = cp.getLdcValue(ci.u16bitAt(address + 1));
        	count = addLdcValue(newCp, ldcValue);
        	newBytecode.addLdc(count);
        	break;
        case Bytecode.LOOKUPSWITCH:
            // TODO: Switch?
        	throw new IllegalArgumentException("Unsupported byte code op: " + op);
        case Bytecode.MULTIANEWARRAY:
        	className = cp.getClassInfo(ci.u16bitAt(address + 1));
        	count = ci.byteAt(address + 3);
        	newBytecode.addMultiNewarray(className, count);
        	break;
        case Bytecode.NEW:
        	className = cp.getClassInfo(ci.u16bitAt(address + 1));
        	newBytecode.addNew(className);
        	break;
        case Bytecode.NEWARRAY:
        	int atype = ci.byteAt(address + 1);
        	newBytecode.addOpcode(Bytecode.NEWARRAY);
        	newBytecode.add(atype);
        	break;
        case Bytecode.PUTFIELD:
        	name = cp.getFieldrefName(ci.u16bitAt(address + 1));
        	type = cp.getFieldrefType(ci.u16bitAt(address + 1));
        	newBytecode.addPutfield(newClass, name, type);
        	break;
        case Bytecode.PUTSTATIC:
        	className = cp.getFieldrefClassName(ci.u16bitAt(address + 1));
        	name = cp.getFieldrefName(ci.u16bitAt(address + 1));
        	type = cp.getFieldrefType(ci.u16bitAt(address + 1));
        	newBytecode.addPutstatic(className, name, type);
        	break;
        case Bytecode.TABLESWITCH:
            // TODO: Switch?
        	throw new IllegalArgumentException("Unsupported byte code op: " + op);
        case Bytecode.WIDE:
            // TODO: Wide?
        	throw new IllegalArgumentException("Unsupported byte code op: " + op);
    	default:
        	for (int i = address; i < ci.lookAhead(); i++) {
        		newBytecode.add(ci.byteAt(i));
        	}
        }
    }
    
    private String resolveCopyName(String className, String newClassName, String oldClassName) {
		if (className.equals(oldClassName)) {
			return newClassName;
		}
		
		return className;
	}

	private int addLdcValue(ConstPool newCp, Object ldcValue) {
        if (ldcValue instanceof String) {
        	return newCp.addStringInfo((String) ldcValue);
        } else if (ldcValue instanceof Float) {
        	return newCp.addFloatInfo((Float) ldcValue);
        } else if (ldcValue instanceof Integer) {
        	return newCp.addIntegerInfo((Integer) ldcValue);
        } else if (ldcValue instanceof Long) {
        	return newCp.addLongInfo((Long) ldcValue);
        } else if (ldcValue instanceof Double) {
        	return newCp.addDoubleInfo((Double) ldcValue);
        } else {
        	throw new IllegalArgumentException("Unsupported ldc value: " + ldcValue);
        }
    }

	@SuppressWarnings("unchecked")
	private <T> Class<? extends T> createUnsafeProxyClass(ViewType<T> viewType, Class<?> proxyClass) {
        CtClass cc = pool.makeClass(proxyClass.getName() + "_unsafe");
        CtClass superCc;
        CtClass proxyCc;

        ClassPath classPath = new ClassClassPath(proxyClass);
        pool.insertClassPath(classPath);

        try {
            proxyCc = pool.get(proxyClass.getName());
            superCc = proxyCc.getSuperclass();

            if ("java.lang.Object".equals(superCc.getName())) {
                throw new IllegalArgumentException("Invalid entity view class that does not use a custom constructor!");
            }
            
            addFields(cc, proxyCc);
            addMethods(cc, proxyCc);
            addConstructors(cc, proxyCc);
//            cc.debugWriteFile("D:\\");
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
        
        createGettersAndSetters(attribute, clazz, cc, getter, setter, attributeField);
        
        return attributeField;
    }

	private void createGettersAndSetters(MethodAttribute<?, ?> attribute, Class<?> clazz, CtClass cc, Method getter, Method setter, CtField attributeField) throws CannotCompileException, NotFoundException {
		String genericSignature = attributeField.getGenericSignature();
		List<Method> bridgeGetters = getBridgeGetters(clazz, attribute, getter);
        
        CtMethod attributeGetter = CtNewMethod.getter(getter.getName(), attributeField);
        
        if (genericSignature != null) {
            String getterGenericSignature = "()" + genericSignature;
            setGenericSignature(attributeGetter, getterGenericSignature);
        }
        
        for (Method m : bridgeGetters) {
            CtMethod getterBridge = createGetterBridge(cc, m, attributeGetter);
            cc.addMethod(getterBridge);
        }
        cc.addMethod(attributeGetter);
        
        if (setter != null) {
            CtMethod attributeSetter = CtNewMethod.setter(setter.getName(), attributeField);
            List<Method> bridgeSetters = getBridgeSetters(clazz, attribute, setter);
            
            if (genericSignature != null) {
                String setterGenericSignature = "(" + genericSignature + ")V";
                setGenericSignature(attributeSetter, setterGenericSignature);
            }

            for (Method m : bridgeSetters) {
                CtMethod setterBridge = createSetterBridge(cc, m, attributeSetter);
                cc.addMethod(setterBridge);
            }
            cc.addMethod(attributeSetter);
        }
	}
    
    private List<Method> getBridgeGetters(Class<?> clazz, MethodAttribute<?, ?> attribute, Method getter) {
		List<Method> bridges = new ArrayList<Method>();
		String name = getter.getName();
		Class<?> attributeType = attribute.getJavaType();
		
		for (Class<?> c : ReflectionUtils.getSuperTypes(clazz)) {
			METHOD: for (Method m : c.getMethods()) {
				if (name.equals(m.getName()) && m.getReturnType().isAssignableFrom(attributeType) && !attributeType.equals(m.getReturnType())) {
					for (Method b : bridges) {
						if (b.getReturnType().equals(m.getReturnType())) {
							continue METHOD;
						}
					}
					
					bridges.add(m);
				}
			}
		}
		
		return bridges;
	}

	private List<Method> getBridgeSetters(Class<?> clazz, MethodAttribute<?, ?> attribute, Method setter) {
		List<Method> bridges = new ArrayList<Method>();
		String name = setter.getName();
		Class<?> attributeType = attribute.getJavaType();
		
		for (Class<?> c : ReflectionUtils.getSuperTypes(clazz)) {
			METHOD: for (Method m : c.getMethods()) {
				if (name.equals(m.getName()) && m.getParameterTypes()[0].isAssignableFrom(attributeType) && !attributeType.equals(m.getParameterTypes()[0])) {
					for (Method b : bridges) {
						if (b.getParameterTypes()[0].equals(m.getParameterTypes()[0])) {
							continue METHOD;
						}
					}
					bridges.add(m);
				}
			}
		}
		
		return bridges;
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
        return "(" + Descriptor.of("java.lang.Object") + ")" + Descriptor.of(returnType);
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
        Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(attribute.getDeclaringType().getJavaType(), attribute.getJavaMethod());
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
