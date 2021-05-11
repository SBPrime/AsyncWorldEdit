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
package org.primesoft.asyncworldedit.core;

import com.sk89q.worldedit.math.BlockVector3;
import org.primesoft.asyncworldedit.api.IPhysicsWatch;
import org.primesoft.asyncworldedit.configuration.ConfigPhysicsFreeze;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.platform.api.IPlatform;
import org.primesoft.asyncworldedit.platform.api.IScheduler;
import org.primesoft.asyncworldedit.platform.api.ITask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * This class is responsible for freezing all physics in edited regions
 *
 * @author SBPrime
 */
public abstract class PhysicsWatch implements IPhysicsWatch, IPlatform.PlatformEventListener {
    private final static int TEST_DELTA = 1;

    private final static long CHUNK_COOLDOWN = 1000;

    private final IScheduler m_scheduler;

    /**
     * Is physics watch enabled
     */
    protected boolean m_isEnabled;
    
    private Function<String, Boolean> m_check = i -> true;

    /**
     * MTA mutex
     */
    private final Object m_mutex;

    /**
     * Locked blocks
     */
    private final Map<String, Map<Integer, Map<Integer, ChunkData>>> m_locked;

    private ITask m_cleanupTask;

    /**
     * Create new instance of the class
     */
    protected PhysicsWatch(
            final IScheduler scheduler) {

        m_mutex = new Object();
        m_locked = new HashMap<>();
        m_scheduler = scheduler;
    }

    /**
     * enable the physics freeze
     */
    @Override
    public synchronized void enable() {
        m_isEnabled = true;

        if (m_cleanupTask != null) {
            m_cleanupTask.cancel();
        }
        m_cleanupTask = m_scheduler.runTaskTimer(this::doCleanup, m_scheduler.tps(), m_scheduler.tps());

        reloadConfig();
    }

    /**
     * disable the physics freeze
     */
    @Override
    public void disable() {
        m_isEnabled = false;
        synchronized (m_mutex) {
            m_locked.clear();
        }

        synchronized (this) {
            if (m_cleanupTask != null) {
                m_cleanupTask.cancel();
            }
        }
    }

    /**
     * Add new watched location
     *
     * @param name The world name
     * @param location The block location
     */
    @Override
    public void addLocation(String name, BlockVector3 location) {
        int x = location.getBlockX() / 16;
        //int y = location.getBlockY();
        int z = location.getBlockZ() / 16;

        synchronized (m_mutex) {
            if (!m_isEnabled) {
                return;
            }

            Map<Integer, Map<Integer, ChunkData>> xhash = m_locked.computeIfAbsent(name, i -> new HashMap<>());
            Map<Integer, ChunkData> zhash = xhash.computeIfAbsent(x, i -> new HashMap<>());
            zhash.compute(z, (_z, value) -> (value == null ? new ChunkData() : value).incRefs());
        }
    }

    /**
     * Remove watched location
     *
     * @param name the world name
     * @param location the location
     */
    @Override
    public void removeLocation(String name, BlockVector3 location) {
        int x = location.getBlockX() / 16;
        //int y = location.getBlockY();
        int z = location.getBlockZ() / 16;

        synchronized (m_mutex) {
            if (!m_isEnabled) {
                return;
            }

            final Map<Integer, Map<Integer, ChunkData>> xhash = m_locked.get(name);
            if (xhash == null) {
                return;
            }

            final Map<Integer, ChunkData> zhash = xhash.get(x);
            if (zhash == null) {
                return;
            }
            
            zhash.compute(z, (_z, value) -> {
                if (value == null) {
                    return null;
                }

                value.decRefs();
                return value;
            });

            if (zhash.isEmpty()) {
                xhash.remove(x);
            }
            if (xhash.isEmpty()) {
                m_locked.remove(name);
            }
        }
    }
    
    /**
     * Perform test if block event should by canceled
     */
    protected boolean cancelEvent(String name, int x, int y, int z, String material) {
        x = x / 16;
        z = z / 16;

        synchronized (m_mutex) {
            Map<Integer, Map<Integer, ChunkData>> xhash = m_locked.get(name);
            if (xhash == null) {
                return false;
            }

            for (int px = x - TEST_DELTA; px <= x + TEST_DELTA; px++) {
                final Map<Integer, ChunkData> zhash = xhash.get(px);
                if (zhash == null) {
                    continue;
                }

                for (int pz = z - TEST_DELTA; pz <= z + TEST_DELTA; pz++) {
                    ChunkData data = zhash.get(pz);
                    if (data == null) {
                        continue;
                    }

                    if (data.refCount <= 0 && (System.currentTimeMillis() - data.zeroTime) >= CHUNK_COOLDOWN) {
                        zhash.remove(pz);
                        if (zhash.isEmpty()) {
                            xhash.remove(x);
                        }
                        if (xhash.isEmpty()) {
                            m_locked.remove(name);
                        }

                        continue;
                    }

                    if (m_check.apply(material)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    
    /**
     * Register all physics events
     */
    public abstract void registerEvents();

    @Override
    public void onPlatformEvent(final IPlatform.PlatformEvent e) {
        if (!IPlatform.PlatformEvent.CONFIGURATION_RELOADED.equals(e)) {
            return;
        }

        reloadConfig();
    }

    protected void reloadConfig() {
        final ConfigPhysicsFreeze config = ConfigProvider.physicsFreeze();

        m_check = config::shouldFreeze;

    }

    private void doCleanup() {
        synchronized (m_mutex) {
            Set<String> toRemoveWorld = new HashSet<>();
            for (Map.Entry<String, Map<Integer, Map<Integer, ChunkData>>> entryWorld : m_locked.entrySet()) {
                final Map<Integer, Map<Integer, ChunkData>> xhash = entryWorld.getValue();

                Set<Integer> toRemoveX = new HashSet<>();
                for (Map.Entry<Integer, Map<Integer, ChunkData>> entryX : xhash.entrySet()) {
                    final Map<Integer, ChunkData> zhash = entryX.getValue();

                    Set<Integer> toRemoveZ = new HashSet<>();
                    for (Map.Entry<Integer, ChunkData> entryZ : zhash.entrySet()) {
                        ChunkData data = entryZ.getValue();

                        if (data.refCount <= 0 && (System.currentTimeMillis() - data.zeroTime) >= CHUNK_COOLDOWN) {
                           toRemoveZ.add(entryZ.getKey());
                        }
                    }

                    zhash.keySet().removeAll(toRemoveZ);
                    if (zhash.isEmpty()) {
                        toRemoveX.add(entryX.getKey());
                    }
                }

                xhash.keySet().removeAll(toRemoveX);
                if (xhash.isEmpty()) {
                    toRemoveWorld.add(entryWorld.getKey());
                }
            }
            m_locked.keySet().removeAll(toRemoveWorld);
        }
    }

    private static class ChunkData {
        private int refCount;

        private long zeroTime;

        public ChunkData incRefs() {
            refCount ++;
            return this;
        }

        public void decRefs() {
            if (refCount > 0) {
                refCount --;
            }

            if (refCount == 0) {
                zeroTime = System.currentTimeMillis();
            }
        }
    }
}
