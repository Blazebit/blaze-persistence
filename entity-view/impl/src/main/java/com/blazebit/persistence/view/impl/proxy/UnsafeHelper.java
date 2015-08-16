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

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
@SuppressWarnings("restriction")
public class UnsafeHelper {

	private static final Unsafe unsafe;

	static {
		Field f;
		try {
			f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			unsafe = (Unsafe) f.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Class<?> define(String name, byte[] bytes, final Class<?> declaringClass) {
		try {
			ClassLoader newLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
				public ClassLoader run() {
					return new ClassLoader(declaringClass.getClassLoader()) { };
				}
			});
			return unsafe.defineClass(name, bytes, 0, bytes.length, newLoader, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
