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
package org.primesoft.asyncworldedit;

import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 *
 * @author SBPrime
 */
public class ChunkWatch implements Listener {

    /**
     * Suppressed chunks
     */
    private final HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> m_watchedChunks = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();

    /**
     * List of all loaded chunks
     */
    private final HashMap<String, HashMap<Integer, HashSet<Integer>>> m_loadedChunks = new HashMap<String, HashMap<Integer, HashSet<Integer>>>();

    public void initialize(Server server) {
        for (World w : server.getWorlds()) {
            for (Chunk c : w.getLoadedChunks()) {
                chunkLoaded(w.getName(), c.getX(), c.getZ());
            }
        }

    }

    /**
     * Remove all chunk unload queues
     */
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

    @EventHandler
    public void onChunkLoadEvent(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        Integer cx = chunk.getX();
        Integer cz = chunk.getZ();
        String worldName = chunk.getWorld().getName();

        chunkLoaded(worldName, cx, cz);
    }

    @EventHandler
    public void onChunkUnloadEvent(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();

        int cx = chunk.getX();
        int cz = chunk.getZ();
        String worldName = chunk.getWorld().getName();
        boolean cancel = chunkUnloading(worldName, cx, cz);

        if (cancel) {
            event.setCancelled(cancel);
        }
    }
}
