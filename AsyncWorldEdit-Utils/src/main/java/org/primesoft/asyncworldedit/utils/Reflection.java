/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.asyncworldedit.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

import sun.misc.Unsafe;

/**
 * Reflection GET and SET operations.
 *
 * @author SBPrime
 */
public class Reflection {
    private static final Field modifiersField;

    static {
        Field mf;

        try {
            mf = Field.class.getDeclaredField("modifiers");
            mf.setAccessible(true);
        } catch (NoSuchFieldException e) {
            mf = null;
        }

        modifiersField = mf;

        if (isJava8Plus()) {
            try {
                openModuleAccess();
            } catch (Exception ex) {
                ExceptionHelper.printException(ex, "Error opening modules.");
            }

        }
    }

    public static Unsafe unsafe() {
        return Reflection.get(Unsafe.class, Unsafe.class, "theUnsafe", "Unable to get unsafe");
    }

    private static boolean isJava8Plus() {
        try {
            return Reflection.class.getClassLoader().loadClass("java.lang.Module") != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Based on Lombok:
     * https://github.com/projectlombok/lombok/commit/9806e5cca4b449159ad0509dafde81951b8a8523
     */
    private static void openModuleAccess()
            throws NoSuchMethodException, NoSuchFieldException,
                    InvocationTargetException, IllegalAccessException,
            ClassNotFoundException {

        final Map.Entry<String, String[]>[] modulesToOpen = new Map.Entry[]{
                new AbstractMap.SimpleEntry("java.base", new String[]{"java.lang", "java.security", "java.util"})
        };

        ClassLoader cl = Reflection.class.getClassLoader();
        Class<?> cModule = cl.loadClass("java.lang.Module");
        Class<?> cModuleLayer = cl.loadClass("java.lang.ModuleLayer");

        Method mModuleImplAddOpens = cModule.getDeclaredMethod("implAddOpens", String.class, cModule);
        Method mModuleLayerFindModule = cModuleLayer.getDeclaredMethod("findModule", String.class);

        Object moduleLayer = cModuleLayer.cast(cModuleLayer.getMethod("boot").invoke(null));
        Object moduleAwe = cModule.cast(Class.class.getDeclaredMethod("getModule").invoke(Reflection.class));

        Unsafe u = unsafe();
        u.putBooleanVolatile(mModuleImplAddOpens,
            u.objectFieldOffset(HackyClass.class.getDeclaredField("field1")),
            true);

        for (Map.Entry<String, String[]> e : modulesToOpen) {
            Object module = cModule.cast(((Optional<?>)mModuleLayerFindModule.invoke(moduleLayer, e.getKey()))
                    .orElseThrow(() -> new RuntimeException("Unable to get '" + e.getKey() + "'")));

            for (String pn : e.getValue()) {
                mModuleImplAddOpens.invoke(module, pn, moduleAwe);
            }
        }
    }

    public static <T> T create(Class<T> resultClass,
            Constructor<?> ctor, String message, Object... args) {
        try {
            boolean accessible = ctor.isAccessible();
            if (!accessible) {
                ctor.setAccessible(true);
            }

            try {
                return resultClass.cast(ctor.newInstance(args));
            } finally {
                if (!accessible) {
                    ctor.setAccessible(false);
                }
            }
        } catch (InstantiationException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version.", message));
        } catch (InvocationTargetException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version.", message));
        } catch (IllegalArgumentException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        } catch (ClassCastException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version, unable to cast result.", message));
        }

        return null;
    }

    public static <T> T invoke(Object instance, Class<T> resultClass,
            Method method, String message, Object... args) {
        try {
            boolean accessible = method.isAccessible();

            if (!accessible) {
                method.setAccessible(true);
            }

            try {
                return resultClass.cast(method.invoke(instance, args));
            } finally {
                if (!accessible) {
                    method.setAccessible(false);
                }
            }
        } catch (InvocationTargetException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version.", message));
        } catch (IllegalArgumentException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        } catch (ClassCastException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version, unable to cast result.", message));
        }

        return null;
    }

    public static boolean invoke(Object instance,
            Method method, String message, Object... args) {
        try {
            boolean accessible = method.isAccessible();            
            if (!accessible) {
                method.setAccessible(true);
            }

            try {            
                method.invoke(instance, args);
            } finally {
                if (!accessible) {
                    method.setAccessible(false);
                }
            }
        } catch (InvocationTargetException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version.", message));
        } catch (IllegalArgumentException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        } catch (ClassCastException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version, unable to cast result.", message));
        }

        return false;
    }

    public static <T> T get(Object instance, Class<T> fieldClass, Field field, String message) {
        try {
            boolean accessible = field.isAccessible();            
            if (!accessible) {
                field.setAccessible(true);
            }

            try {
                return fieldClass.cast(field.get(instance));
            } finally {
                if (!accessible) {
                    field.setAccessible(false);
                }
            }
        } catch (IllegalArgumentException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        } catch (ClassCastException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version, unable to cast result.", message));
        }

        return null;
    }

    public static Object get(Object instance, Field field, String message) {
        try {
            boolean accessible = field.isAccessible();            
            if (!accessible) {
                field.setAccessible(true);
            }

            try {
                return field.get(instance);
            } finally {
                if (!accessible) {
                    field.setAccessible(false);
                }
            }
        } catch (IllegalArgumentException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        } catch (ClassCastException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version, unable to cast result.", message));
        }

        return null;
    }

    public static <T> T get(Class<?> sourceClass, Class<T> fieldClass,
            String fieldName, String message) {
        return get(sourceClass, fieldClass, null, fieldName, message);
    }

    public static <T> T get(Object instance, Class<T> fieldClass,
            String fieldName, String message) {
        return get(instance.getClass(), fieldClass, instance, fieldName, message);
    }

    public static <T> T get(Class<?> sourceClass, Class<T> fieldClass,
            Object instance, String fieldName,
            String message) {
        try {
            Field field = sourceClass.getDeclaredField(fieldName);
            boolean accessible = field.isAccessible();

            if (!accessible) {
                field.setAccessible(true);
            }

            try {
                return fieldClass.cast(field.get(instance));
            } finally {
                if (!accessible) {
                    field.setAccessible(false);
                }
            }
        } catch (IllegalArgumentException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        } catch (NoSuchFieldException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version, field %2$s not found.", message, fieldName));
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        } catch (ClassCastException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version, unable to cast result.", message));
        }

        return null;
    }

    public static boolean safeSet(Object instance, Field field, Object value,
                              String message) {
        try {
            boolean accessible = field.isAccessible();

            if (!accessible) {
                field.setAccessible(true);
            }

            int modifiers = field.getModifiers();
            boolean isFinal = (modifiers & Modifier.FINAL) == Modifier.FINAL;

            if (isFinal) {
                if (modifiersField == null) {
                    throw new IllegalAccessException("Field '" + field.getName() + "' is FINAL. Set not supported on this platform.");
                }

                try {
                    modifiersField.setInt(field, modifiers | Modifier.FINAL);
                } catch (IllegalAccessException e) {
                    throw new IllegalAccessException("Field '" + field.getName() + "' is FINAL. Unable to to modify FINAL flag.");
                }
            }

            field.set(instance, value);
            return true;
        } catch (IllegalArgumentException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        }

        return false;
    }
    
    public static Method findMethod(Class<?> c, String methodName, String message, Class<?>... paramTypes) {
        try {
            return c.getDeclaredMethod(methodName, paramTypes);
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        } catch (NoSuchMethodException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version, method %2$s not found.", message, methodName));
        }

        return null;
    }

    public static Constructor<?> findConstructor(Class<?> c, String message, Class<?>... paramTypes) {
        try {
            return c.getDeclaredConstructor(paramTypes);
        } catch (NoSuchMethodException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: unsupported version, constructor not found.", message));
        } catch (SecurityException ex) {
            ExceptionHelper.printException(ex, String.format("%1$s: security exception.", message));
        }

        return null;
    }

    private static class HackyClass {
        boolean field1;
    }
}
