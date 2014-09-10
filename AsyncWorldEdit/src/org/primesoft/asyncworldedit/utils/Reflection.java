/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primesoft.asyncworldedit.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;

/**
 * Reflection GET and SET operations.
 *
 * @author SBPrime
 */
public class Reflection {

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
            AsyncWorldEditMain.log(message + ": unsupported WorldEdit version.");
        } catch (IllegalAccessException ex) {
            AsyncWorldEditMain.log(message + ": security exception.");
        } catch (NoSuchFieldException ex) {
            AsyncWorldEditMain.log(message + ": unsupported WorldEdit version, field " + fieldName
                    + " not found.");
        } catch (SecurityException ex) {
            AsyncWorldEditMain.log(message + ": security exception.");
        } catch (ClassCastException ex) {
            AsyncWorldEditMain.log(message + ": unsupported WorldEdit version, unable to cast result.");
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
            AsyncWorldEditMain.log(message + ": unsupported WorldEdit version.");
        } catch (IllegalAccessException ex) {
            AsyncWorldEditMain.log(message + ": security exception.");
        } catch (NoSuchFieldException ex) {
            AsyncWorldEditMain.log(message + ": unsupported WorldEdit version, field " + fieldName
                    + " not found.");
        } catch (SecurityException ex) {
            AsyncWorldEditMain.log(message + ": security exception.");
        }
    }
    
    
    public static void set(Object instance, Field field, Object value,
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
            } finally {
                if (isFinal) {
                    modifiersField.setInt(field, modifiers | Modifier.FINAL);
                }
                if (!accessible) {
                    field.setAccessible(false);
                }
            }
        } catch (IllegalArgumentException ex) {
            AsyncWorldEditMain.log(message + ": unsupported WorldEdit version.");
        } catch (IllegalAccessException ex) {
            AsyncWorldEditMain.log(message + ": security exception.");
        } catch (NoSuchFieldException ex) {
            AsyncWorldEditMain.log(message + ": unsupported WorldEdit version, field modifiers not found.");
        } catch (SecurityException ex) {
            AsyncWorldEditMain.log(message + ": security exception.");
        }
    }
}
