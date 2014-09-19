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
package org.primesoft.asyncworldedit.injector.scanner;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * The class scanner
 *
 * @author SBPrime
 */
public class ClassScanner {

    private final static Class<?>[] s_blackList = new Class<?>[]{
        ChangeSet.class,
        EditSession.class,
        Region.class,
        BlockVector.class,
        World.class,
        Change.class
    };

    /**
     * Scan object (and all fields) for T
     *
     * @param <T> The type of class to find
     * @param type The type of class to find
     * @param o Object to find
     * @return
     */
    public static <T> List<ClassScannerResult<T>> scan(Class<T> type, Object o) {
        List<ClassScannerResult<T>> result = new ArrayList<ClassScannerResult<T>>();
        if (o == null) {
            return result;
        }

        Queue<ScannerQueueEntry> toScan = new ArrayDeque<ScannerQueueEntry>();
        HashSet<Object> scanned = new HashSet<Object>();
        
        toScan.add(new ScannerQueueEntry(o, null, null));

        while (!toScan.isEmpty()) {
            ScannerQueueEntry entry = toScan.poll();
            Object cObject = entry.getValue();

            Class<?> cClass = cObject.getClass();

            if (type.isAssignableFrom(cClass)) {
                //Should duplicates by ignored?
                result.add(new ClassScannerResult<T>((T) cObject, entry.getParent(), entry.getField()));
            }
            if (!scanned.contains(cObject)) {
                for (ScannerQueueEntry f : unpack(cClass, cObject)) {
                    Object t = f.getValue();
                    if (t != null) {
                        toScan.add(f);
                    }
                }
                scanned.add(cObject);
            }
        }

        return result;
    }

    /**
     * Chaecks if the class is a primitive (number or string)
     *
     * @param oClass
     * @return
     */
    private static boolean isPrimitive(Class<?> oClass) {
        return (Number.class.isAssignableFrom(oClass))
                || (Boolean.class.isAssignableFrom(oClass))
                || (String.class.isAssignableFrom(oClass))
                || (UUID.class.isAssignableFrom(oClass));
    }

    /**
     * Get all fields from a class
     *
     * @param oClass
     * @param o
     * @return
     */
    private static Iterable<ScannerQueueEntry> unpack(Class<?> oClass, Object o) {
        HashSet<ScannerQueueEntry> result = new HashSet<ScannerQueueEntry>();        

        if (isPrimitive(oClass) || isBlackList(oClass)) {
            return result;
        }

        System.out.println("Scanning: " + oClass);
        if (oClass.isArray()) {
            for (Object t : (Object[]) o) {
                result.add(new ScannerQueueEntry(t, o, null));
            }
        }

        if (Object[].class.isAssignableFrom(oClass)) {
            for (Object t : (Object[]) o) {
                result.add(new ScannerQueueEntry(t, o, null));
            }
        }

        if (Iterable.class.isAssignableFrom(oClass)) {
            for (Object t : (Iterable<Object>) o) {
                result.add(new ScannerQueueEntry(t, o, null));
            }
        }

        for (Field f : getAllFields(oClass)) {
            boolean restore = !f.isAccessible();
            if (restore) {
                f.setAccessible(true);
            }
            try {
                result.add(new ScannerQueueEntry(f.get(o), o, f));
            } catch (IllegalArgumentException ex) {
            } catch (IllegalAccessException ex) {
            }

            if (restore) {
                f.setAccessible(false);
            }
        }
        return result;
    }

    private static boolean isBlackList(Class<?> oClass) {
        for (Class<?> c : s_blackList) {
            if (c.isAssignableFrom(oClass)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get all fields for class (including supper)
     *
     * @param oClass
     * @param fields
     * @return
     */
    private static List<Field> getAllFields(Class<?> oClass) {
        List<Field> result = new ArrayList<Field>();

        while (oClass != null) {
            result.addAll(Arrays.asList(oClass.getDeclaredFields()));
            oClass = oClass.getSuperclass();
        }

        return result;
    }
}
