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

import com.sk89q.worldedit.math.convolution.GaussianKernel;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Stack;
import static org.primesoft.asyncworldedit.LoggerProvider.log;

/**
 * Reflection GET and SET operations.
 *
 * @author SBPrime
 */
public class Reflection {

    public static Class<?> classFromName(String p, String name, String message) {
        final String pattern = "%s.%s";
        try {
            return Class.forName(String.format(pattern, p, name));
        } catch (ClassNotFoundException ex) {
            log(String.format("%1$s: unsupported version, class %2$s not found.", message, name));
        }

        return null;
    }

    public static <T> T create(Class<T> resultClass,
            Constructor<?> ctor, String message, Object... args) {
        try {
            ctor.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");

            boolean accessible = modifiersField.isAccessible();
            if (!accessible) {
                modifiersField.setAccessible(true);
            }

            try {
                return resultClass.cast(ctor.newInstance(args));
            } finally {
                if (!accessible) {
                    modifiersField.setAccessible(false);
                }
            }
        } catch (InstantiationException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (InvocationTargetException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (IllegalArgumentException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (NoSuchFieldException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (SecurityException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (ClassCastException ex) {
            log(String.format("%1$s: unsupported version, unable to cast result.", message));
        }

        return null;
    }

    public static <T> T invoke(Object instance, Class<T> resultClass,
            Method method, String message, Object... args) {
        try {
            method.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");

            boolean accessible = modifiersField.isAccessible();
            if (!accessible) {
                modifiersField.setAccessible(true);
            }

            try {
                return resultClass.cast(method.invoke(instance, args));
            } finally {
                if (!accessible) {
                    modifiersField.setAccessible(false);
                }
            }
        } catch (InvocationTargetException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (IllegalArgumentException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (NoSuchFieldException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (SecurityException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (ClassCastException ex) {
            log(String.format("%1$s: unsupported version, unable to cast result.", message));
        }

        return null;
    }

    public static boolean invoke(Object instance,
            Method method, String message, Object... args) {
        try {
            method.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");

            boolean accessible = modifiersField.isAccessible();
            if (!accessible) {
                modifiersField.setAccessible(true);
            }

            try {
                method.invoke(instance, args);
                return true;
            } finally {
                if (!accessible) {
                    modifiersField.setAccessible(false);
                }
            }
        } catch (InvocationTargetException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (IllegalArgumentException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (NoSuchFieldException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (SecurityException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (ClassCastException ex) {
            log(String.format("%1$s: unsupported version, unable to cast result.", message));
        }

        return false;
    }

    public static <T> T get(Object instance, Class<T> fieldClass, Field field, String message) {
        try {
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");

            boolean accessible = modifiersField.isAccessible();
            if (!accessible) {
                modifiersField.setAccessible(true);
            }

            try {
                return fieldClass.cast(field.get(instance));
            } finally {
                if (!accessible) {
                    modifiersField.setAccessible(false);
                }
            }
        } catch (IllegalArgumentException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (NoSuchFieldException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (SecurityException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (ClassCastException ex) {
            log(String.format("%1$s: unsupported version, unable to cast result.", message));
        }

        return null;
    }

    public static Object get(Object instance, Field field, String message) {
        try {
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");

            boolean accessible = modifiersField.isAccessible();
            if (!accessible) {
                modifiersField.setAccessible(true);
            }

            try {
                return field.get(instance);
            } finally {
                if (!accessible) {
                    modifiersField.setAccessible(false);
                }
            }
        } catch (IllegalArgumentException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (NoSuchFieldException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (SecurityException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (ClassCastException ex) {
            log(String.format("%1$s: unsupported version, unable to cast result.", message));
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
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");

            boolean accessible = modifiersField.isAccessible();
            if (!accessible) {
                modifiersField.setAccessible(true);
            }

            try {
                return fieldClass.cast(field.get(instance));
            } finally {
                if (!accessible) {
                    modifiersField.setAccessible(false);
                }
            }
        } catch (IllegalArgumentException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (NoSuchFieldException ex) {
            log(String.format("%1$s: unsupported version, field %2$s not found.", message, fieldName));
        } catch (SecurityException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (ClassCastException ex) {
            log(String.format("%1$s: unsupported version, unable to cast result.", message));
        }

        return null;
    }

    public static void set(Object instance, String fieldName, Object value,
            String message) {
        set(instance.getClass(), instance, fieldName, value, message);
    }

    public static void set(Class<?> sourceClass, String fieldName, Object value,
            String message) {
        set(sourceClass, null, fieldName, value, message);
    }

    public static void set(Class<?> sourceClass,
            Object instance, String fieldName, Object value,
            String message) {
        try {
            Field field = sourceClass.getDeclaredField(fieldName);
            boolean accessible = field.isAccessible();

            //field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");

            int modifiers = modifiersField.getModifiers();
            boolean isFinal = (modifiers & Modifier.FINAL) == Modifier.FINAL;

            if (!accessible) {
                field.setAccessible(true);
            }
            if (isFinal) {
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
            }
            try {
                field.set(instance, value);
            } finally {
                if (isFinal) {
                    modifiersField.setInt(field, modifiers | Modifier.FINAL);
                }
                if (!accessible) {
                    field.setAccessible(false);
                }
            }
        } catch (IllegalArgumentException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (NoSuchFieldException ex) {
            log(String.format("%1$s: unsupported version, field %2$s not found.", message, fieldName));
        } catch (SecurityException ex) {
            log(String.format("%1$s: security exception.", message));
        }
    }

    public static boolean set(Object instance, Field field, Object value,
            String message) {
        try {
            boolean accessible = field.isAccessible();

            //field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");

            int modifiers = modifiersField.getModifiers();
            boolean isFinal = (modifiers & Modifier.FINAL) == Modifier.FINAL;

            if (!accessible) {
                field.setAccessible(true);
            }
            if (isFinal) {
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
            }
            try {
                field.set(instance, value);
                return true;
            } finally {
                if (isFinal) {
                    modifiersField.setInt(field, modifiers | Modifier.FINAL);
                }
                if (!accessible) {
                    field.setAccessible(false);
                }                
            }
        } catch (IllegalArgumentException ex) {
            log(String.format("%1$s: unsupported version.", message));
        } catch (IllegalAccessException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (NoSuchFieldException ex) {
            log(String.format("%1$s: unsupported version, field modifiers not found.", message));
        } catch (SecurityException ex) {
            log(String.format("%1$s: security exception.", message));
        }
        
        return false;
    }

    public static Method findMethod(Class<?> c, String methodName, String message, Class<?>... paramTypes) {
        try {
            return c.getDeclaredMethod(methodName, paramTypes);
        } catch (SecurityException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (NoSuchMethodException ex) {
            log(String.format("%1$s: unsupported version, method %2$s not found.", message, methodName));
        }

        return null;
    }

    public static Field findField(Class<?> c, String fieldName, String message) {
        try {
            return c.getDeclaredField(fieldName);
        } catch (SecurityException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (NoSuchFieldException ex) {
            log(String.format("%1$s: unsupported version, field %2$s not found.", message, fieldName));
        }

        return null;
    }

    public static Field findTypedField(Class<?> c, Class<?> fieldType, String fieldName, String message) {
        try {
            Field f = c.getDeclaredField(fieldName);
            if (f == null || !f.getType().equals(fieldType)) {
                return null;
            }
            return f;
        } catch (SecurityException ex) {
            log(String.format("%1$s: security exception.", message));
        } catch (NoSuchFieldException ex) {
            log(String.format("%1$s: unsupported version, field %2$s not found.", message, fieldName));
        }

        return null;
    }

    public static Constructor<?> findConstructor(Class<?> c, String message, Class<?>... paramTypes) {
        try {
            return c.getDeclaredConstructor(paramTypes);
        } catch (NoSuchMethodException ex) {
            log(String.format("%1$s: unsupported version, constructor not found.", message));
        } catch (SecurityException ex) {
            log(String.format("%1$s: security exception.", message));
        }

        return null;
    }
    
    /**
     * Scan the class hierarchy and returns all base classes and interfaces.
     * @param cls
     * @return 
     */
    public static Collection<Class<?>> scanHierarchy(Class<?> cls) {
        
        final Stack<Class<?>> toScan = new Stack<Class<?>>();
        final Stack<Class<?>> classes = new Stack<Class<?>>();
        
        toScan.add(cls);
        while (!toScan.isEmpty()) {
            Class<?> c = toScan.pop();
            if (c == null) {
                continue;
            }
            
            if (classes.contains(c)) {
                continue;
            }
            classes.add(c);
            
            Class<?> sClass = c.getSuperclass();
            if (sClass != null) {
                toScan.add(sClass);
            }
            
            Class<?>[] interfaces = c.getInterfaces();
            if (interfaces != null) {
                for (Class<?> i : interfaces) {
                    toScan.push(i);
                }                
            }
        }
        
        return Collections.unmodifiableCollection(classes);
    }
}
