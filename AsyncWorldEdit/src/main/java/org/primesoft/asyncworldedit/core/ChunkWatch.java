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

import org.primesoft.asyncworldedit.api.inner.IChunkWatch;
import java.util.HashMap;
import java.util.HashSet;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.api.utils.IAction;

/**
 * This class suppresses chunk unloading
 *
 * @author SBPrime
 */
public abstract class ChunkWatch implements IChunkWatch {

    /**
     * Suppressed chunks
     */
    private final HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> m_watchedChunks = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();

    /**
     * List of all loaded chunks
     */
    private final HashMap<String, HashMap<Integer, HashSet<Integer>>> m_loadedChunks = new HashMap<String, HashMap<Integer, HashSet<Integer>>>();

    /**
     * The dispatcher
     */
    private ITaskDispatcher m_dispatcher = null;

    /**
     * Remove all chunk unload queues
     */
    @Override
    public void clear() {
        synchronized (m_watchedChunks) {
            m_watchedChunks.clear();
        }
    }

    /**
     * Add chunk to suppress chunk unload queue
     *
     * @param cx
     * @param cz
     * @param worldName
     */
    @Override
    public void add(int cx, int cz, String worldName) {
        synchronized (m_watchedChunks) {
            HashMap<Integer, HashMap<Integer, Integer>> worldEntry = m_watchedChunks.get(worldName);
            if (worldEntry == null) {
                worldEntry = new HashMap<Integer, HashMap<Integer, Integer>>();

                m_watchedChunks.put(worldName, worldEntry);
            }

            HashMap<Integer, Integer> cxEntry = worldEntry.get(cx);
            if (cxEntry == null) {
                cxEntry = new HashMap<Integer, Integer>();

                worldEntry.put(cx, cxEntry);
            }

            Integer value;
            if (cxEntry.containsKey(cz)) {
                value = cxEntry.get(cz);
            } else {
                value = 0;
            }

            value = value + 1;

            cxEntry.put(cz, value);
        }
    }

    /**
     * Remove chunk from suppress chunk unload queue
     *
     * @param cx
     * @param cz
     * @param worldName
     */
    @Override
    public void remove(int cx, int cz, String worldName) {
        synchronized (m_watchedChunks) {
            HashMap<Integer, HashMap<Integer, Integer>> worldEntry = m_watchedChunks.get(worldName);
            if (worldEntry == null) {
                return;
            }

            HashMap<Integer, Integer> cxEntry = worldEntry.get(cx);
            if (cxEntry == null) {
                return;
            }

            if (!cxEntry.containsKey(cz)) {
                return;
            }

            Integer value = cxEntry.get(cz) - 1;

            if (value <= 0) {
                cxEntry.remove(cz);
            } else {
                cxEntry.put(cz, value);
            }

            if (cxEntry.isEmpty()) {
                worldEntry.remove(cx);
            }

            if (worldEntry.isEmpty()) {
                m_watchedChunks.remove(worldName);
            }
        }
    }

    protected void chunkLoaded(String worldName, int cx, int cz) {
        synchronized (m_loadedChunks) {
            HashMap<Integer, HashSet<Integer>> worldEntry;

            if (m_loadedChunks.containsKey(worldName)) {
                worldEntry = m_loadedChunks.get(worldName);
            } else {
                worldEntry = new HashMap<Integer, HashSet<Integer>>();
                m_loadedChunks.put(worldName, worldEntry);
            }

            HashSet<Integer> cxEntry;
            if (worldEntry.containsKey(cx)) {
                cxEntry = worldEntry.get(cx);
            } else {
                cxEntry = new HashSet<Integer>();
                worldEntry.put(cx, cxEntry);
            }

            if (cxEntry.contains(cz)) {
                return;
            }

            cxEntry.add(cz);
        }
    }

    public boolean chunkUnloading(String worldName, int cx, int cz) {
        boolean cancel = false;
        synchronized (m_watchedChunks) {
            HashMap<Integer, HashMap<Integer, Integer>> watchedWorldEntry = m_watchedChunks.get(worldName);
            if (watchedWorldEntry != null) {
                HashMap<Integer, Integer> cxEntry = watchedWorldEntry.get(cx);
                if (cxEntry != null) {
                    cancel = cxEntry.containsKey(cz) && cxEntry.get(cz) > 0;
                }
            }

            if (cancel) {
                return true;
            }

            synchronized (m_loadedChunks) {
                HashMap<Integer, HashSet<Integer>> worldEntry;

                if (!m_loadedChunks.containsKey(worldName)) {
                    return false;
                }
                worldEntry = m_loadedChunks.get(worldName);

                if (!worldEntry.containsKey(cx)) {
                    return false;
                }

                HashSet<Integer> cxEntry = worldEntry.get(cx);
                if (!cxEntry.contains(cz)) {
                    return false;
                }

                cxEntry.remove(cz);

                if (cxEntry.isEmpty()) {
                    worldEntry.remove(cx);
                }
                if (worldEntry.isEmpty()) {
                    m_loadedChunks.remove(worldName);
                }
            }
        }

        return false;
    }

    /**
     * Set chunk data as unloaded
     *
     * @param cx
     * @param cz
     * @param worldName
     */
    @Override
    public void setChunkUnloaded(int cx, int cz, String worldName) {
        synchronized (m_loadedChunks) {
            HashMap<Integer, HashSet<Integer>> worldEntry;

            if (!m_loadedChunks.containsKey(worldName)) {
                return;
            }
            worldEntry = m_loadedChunks.get(worldName);

            if (!worldEntry.containsKey(cx)) {
                return;
            }

            HashSet<Integer> cxEntry = worldEntry.get(cx);
            if (!cxEntry.contains(cz)) {
                return;
            }

            cxEntry.remove(cz);

            if (cxEntry.isEmpty()) {
                worldEntry.remove(cx);
            }
            if (worldEntry.isEmpty()) {
                m_loadedChunks.remove(worldName);
            }
        }
    }
    
    /**
     * Set chunk data as unloaded
     *
     * @param cx
     * @param cz
     * @param worldName
     */
    @Override
    public void setChunkLoaded(int cx, int cz, String worldName) {
        chunkLoaded(worldName, cx, cz);
    }

    @Override
    public boolean isChunkLoaded(int cx, int cz, String worldName) {
        synchronized (m_loadedChunks) {
            if (!m_loadedChunks.containsKey(worldName)) {
                return false;
            }

            HashMap<Integer, HashSet<Integer>> worldEntry = m_loadedChunks.get(worldName);
            if (!worldEntry.containsKey(cx)) {
                return false;
            }

            HashSet<Integer> cxEntry = worldEntry.get(cx);
            return cxEntry.contains(cz);
        }
    }

    @Override
    public void loadChunk(final int cx, final int cz, final String worldName) {
        add(cx, cz, worldName);
        try {
            if (isChunkLoaded(cx, cz, worldName) || m_dispatcher == null) {
                return;
            }

            m_dispatcher.queueFastOperation(new IAction() {

                @Override
                public void execute() {
                    doLoadChunk(cx, cz, worldName);
                }
            });
        } finally {
            remove(cx, cz, worldName);
        }
    }

    /**
     * Register the chunk watcher events
     */
    public abstract void registerEvents();

    /**
     * Do the actual chunk loading
     *
     * @param cx
     * @param cz
     * @param worldName
     */
    protected abstract boolean doLoadChunk(int cx, int cz, String worldName);

    @Override
    public void setTaskDispat(ITaskDispatcher dispatcher) {
        m_dispatcher = dispatcher;
    }
}
