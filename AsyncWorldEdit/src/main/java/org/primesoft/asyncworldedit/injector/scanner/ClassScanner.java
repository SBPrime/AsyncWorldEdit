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
package org.primesoft.asyncworldedit.injector.scanner;

import com.sk89q.util.yaml.YAMLNode;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.world.FastModeExtent;
import com.sk89q.worldedit.function.operation.BlockMapEntryPlacer;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.ClipboardPattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.BlockRegistry;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Logger;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.classScanner.IClassFilter;
import org.primesoft.asyncworldedit.api.inner.IClassScanner;
import org.primesoft.asyncworldedit.api.inner.IClassScannerResult;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.configuration.PermissionGroup;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.worldedit.blocks.BaseBlockWrapper;

/**
 * The class scanner
 *
 * @author SBPrime
 */
public abstract class ClassScanner implements IClassScanner {
    /**
     * List of all filters
     */
    private final List<IClassFilter> m_filters = new LinkedList<IClassFilter>();
    
    
    private ClassScannerEntry[] m_blackList = new ClassScannerEntry[0];
    
    /**
     * Is the class scanner initialized
     */
    private boolean m_isInitialized = false;

    /**
     * Initialize the class scanner
     * @return 
     */
    @Override
    public IClassScanner initialize() {
        m_blackList = getBlackList();
        m_isInitialized = true;
        return this;
    }
    
    /**
     * Scan object (and all fields) for T
     *
     * @param types The types of classes to find
     * @param o Object to find
     * @return
     */
    @Override
    public List<IClassScannerResult> scan(Class<?> types[], Object o) {
        if (!m_isInitialized) {
            throw new IllegalStateException("Class scanner not initialized");
        }
        
        List<IClassScannerResult> result = new ArrayList<>();
        if (o == null) {
            return result;
        }

        Queue<ScannerQueueEntry> toScan = new ArrayDeque<ScannerQueueEntry>();
        HashSet<Object> scanned = new HashSet<Object>();

        boolean debugOn = ConfigProvider.isDebugOn();
        toScan.add(new ScannerQueueEntry(o, null, null));

        if (debugOn) {
            log("****************************************************************");
            log("* Scanning classes");
            log("****************************************************************");
        }

        /**
         * We do not need to check if first object (o) is of type "type" because
         * it will by impossible to inject it anyways.
         */
        while (!toScan.isEmpty()) {
            ScannerQueueEntry entry = toScan.poll();
            Object cObject = entry.getValue();
            Class<?> cClass = entry.getValueClass();
            if (cObject == null || cClass == null) {
                continue;
            }

            String sParent;
            if (debugOn) {
                sParent = String.format("%1$s:%2$s", Integer.toHexString(cObject.hashCode()), cObject.getClass().getName());
            } else {
                sParent = null;
            }

            if (scanned.contains(cObject)) {
                if (debugOn) {
                    log(String.format("* Skip:\t%1$s", sParent));
                }
            } else {
                int added = 0;
                if (debugOn) {
                    log(String.format("* Scanning:\t%1$s", sParent));
                }
                try {
                    for (ScannerQueueEntry f : unpack(cClass, cObject)) {
                        Object t = f.getValue();
                        Class<?> ct = f.getValueClass();
                        if (t != null && ct != null) {
                            String classMsg = null;
                            if (debugOn) {
                                final Field field = f.getField();
                                final String sValue = String.format("%1$s:%2$s", Integer.toHexString(f.getValue().hashCode()), f.getValueClass().getName());
                                final String sField = field != null ? field.getName() : "?";

                                classMsg = String.format("%s = %s", sField, sValue);
                            }

                            for (Class<?> type : types) {
                                if (type.isAssignableFrom(ct)) {
                                    if (debugOn) {
                                        log(String.format("* F %1$s", classMsg));
                                    }

                                    result.add(new ClassScannerResult(t, t.getClass(), f.getParent(), f.getField()));
                                    break;
                                }
                            }

                            if (!isPrimitive(ct) && !isBlackList(ct)
                                    && !isBlackList(cClass, f.getField())) {
                                toScan.add(f);
                                added++;

                                if (debugOn) {
                                    log(String.format("* + %1$s", classMsg));
                                }
                            } else if (debugOn) {
                                log(String.format("* - %1$s", classMsg));
                            }

                        }
                    }
                } catch (Throwable ex) {
                    log("-----------------------------------------------------------------------");
                    log("Warning: Class scanner encountered an error while scanning class");
                    log(String.format("Exception: %1$s, %2$s", ex.getClass().getName(),
                            ex.getMessage()));
                    ExceptionHelper.printStack(ex, "");
                    log(String.format("Class: %1$s", cClass));
                    log(String.format("Object: %1$s", cObject));
                    log("Send this message to the author of the plugin!");
                    log("https://github.com/SBPrime/AsyncWorldEdit/issues");
                    log("-----------------------------------------------------------------------");
                }
                scanned.add(cObject);
                if (debugOn) {
                    log(String.format("* Added:\t%1$s objects.", added));
                }
            }
        }

        if (debugOn) {
            log("****************************************************************");
        }
        return result;
    }

    /**
     * Checks if the class is a primitive (number or string)
     *
     * @param oClass
     * @return
     */
    private static boolean isPrimitive(Class<?> oClass) {
        return oClass.isPrimitive()
                || (Character.class.isAssignableFrom(oClass))
                || (Number.class.isAssignableFrom(oClass))
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
    private Iterable<ScannerQueueEntry> unpack(Class<?> oClass, Object o) {
        HashSet<ScannerQueueEntry> result = new HashSet<ScannerQueueEntry>();

        if (isPrimitive(oClass) || isBlackList(oClass)) {
            return result;
        }

        if (oClass.isArray()) {
            Class<?> componenClass = oClass;
            while (componenClass.isArray()) {
                componenClass = componenClass.getComponentType();
            }
            if (!isPrimitive(componenClass) && !isBlackList(componenClass)) {
                for (Object t : (Object[]) o) {
                    if (t != null) {
                        result.add(new ScannerQueueEntry(t, o, null));
                    }
                }
            }
        }

        if (Iterable.class.isAssignableFrom(oClass)) {
            for (Object t : (Iterable<Object>) o) {
                if (t != null) {
                    result.add(new ScannerQueueEntry(t, o, null));
                }
            }
        }

        for (Field f : getAllFields(oClass)) {
            boolean restore = !f.isAccessible();
            if (restore) {
                f.setAccessible(true);
            }
            try {
                Object t = f.get(o);
                if (t != null) {
                    result.add(new ScannerQueueEntry(t, o, f));
                }
            } catch (IllegalArgumentException ex) {
            } catch (IllegalAccessException ex) {
            }

            if (restore) {
                f.setAccessible(false);
            }
        }
        return result;
    }

    private boolean isBlackList(Class<?> oClass) {
        return isBlackList(oClass, null);
    }

    /**
     * Get the class scanner black list entries
     * @return 
     */
    protected ClassScannerEntry[] getBlackList() {
        return new ClassScannerEntry[]{
            new ClassScannerEntry("com.sk89q.worldedit.extent.reorder.MultiStageReorder$Stage3Committer"),
            new ClassScannerEntry(BlockMapEntryPlacer.class, "iterator"),
            new ClassScannerEntry(ChangeSet.class),
            new ClassScannerEntry(EditSession.class),
            new ClassScannerEntry(Region.class),
            new ClassScannerEntry(BlockVector.class),
            new ClassScannerEntry(World.class),
            new ClassScannerEntry(FastModeExtent.class),
            new ClassScannerEntry(Change.class),
            new ClassScannerEntry(Vector.class),
            new ClassScannerEntry(BaseBlock.class),
            new ClassScannerEntry(BaseBlockWrapper.class),
            new ClassScannerEntry(PermissionGroup.class),
            new ClassScannerEntry(IPlayerEntry.class),
            new ClassScannerEntry(Clipboard.class),
            new ClassScannerEntry(BlockRegistry.class),
            new ClassScannerEntry(RandomPattern.class),
            new ClassScannerEntry(ClipboardPattern.class),
            new ClassScannerEntry(BlockPattern.class),
            new ClassScannerEntry(YAMLNode.class),
            new ClassScannerEntry(Field.class),
            new ClassScannerEntry(Method.class),
            new ClassScannerEntry("com.sk89q.wepif.PermissionsResolver"),
            new ClassScannerEntry(Logger.class),
            new ClassScannerEntry(Player.class),
            new ClassScannerEntry(ChangeSet.class),
            new ClassScannerEntry(Entity.class)
        };
    }

    private boolean isBlackList(Class<?> oClass, Field f) {
        for (ClassScannerEntry c : m_blackList) {
            if (c.isMatch(oClass, f)) {
                return true;
            }
        }

        synchronized(m_filters){
            for(IClassFilter filter : m_filters) {
                if (!filter.accept(oClass, f)) {
                    return true;
                }
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

    @Override
    public void addFilter(IClassFilter filter) {
        if (filter == null) {
            return;
        }
        
        synchronized(m_filters) {
            if (m_filters.contains(filter)) {
                return;
            }
            
            m_filters.add(filter);
        }
    }

    @Override
    public void removeFilter(IClassFilter filter) {
        if (filter == null) {
            return;
        }
        
        synchronized(m_filters) {
            if (!m_filters.contains(filter)) {
                return;
            }
            
            m_filters.remove(filter);
        }
    }
    
}
