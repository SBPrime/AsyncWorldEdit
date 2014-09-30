/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
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
