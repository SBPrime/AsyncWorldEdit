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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 *
 * @author SBPrime
 */
public class ChunkWatch implements Listener {

    /**
     * Suppressed chunks
     */
    private final HashMap<String, HashMap<Integer, HashSet<Integer>>> m_watchedChunks = new HashMap<String, HashMap<Integer, HashSet<Integer>>>();

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
            HashMap<Integer, HashSet<Integer>> worldEntry = m_watchedChunks.get(worldName);
            if (worldEntry == null) {
                worldEntry = new HashMap<Integer, HashSet<Integer>>();

                m_watchedChunks.put(worldName, worldEntry);
            }

            HashSet<Integer> cxEntry = worldEntry.get(cx);
            if (cxEntry == null) {
                cxEntry = new HashSet<Integer>();

                worldEntry.put(cx, cxEntry);
            }

            if (!cxEntry.contains(cz)) {
                return;
            }

            cxEntry.add(cz);
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
            HashMap<Integer, HashSet<Integer>> worldEntry = m_watchedChunks.get(worldName);
            if (worldEntry == null) {
                return;
            }

            HashSet<Integer> cxEntry = worldEntry.get(cx);
            if (cxEntry == null) {
                return;
            }

            if (!cxEntry.contains(cz)) {
                return;
            }

            cxEntry.remove(cz);

            if (cxEntry.isEmpty()) {
                worldEntry.remove(cx);
            }

            if (worldEntry.isEmpty()) {
                m_watchedChunks.remove(worldName);
            }
        }
    }

    @EventHandler
    public void onChunkUnloadEvent(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();

        int cx = chunk.getX();
        int cz = chunk.getZ();
        String worldName = chunk.getWorld().getName();

        synchronized (m_watchedChunks) {
            HashMap<Integer, HashSet<Integer>> worldEntry = m_watchedChunks.get(worldName);
            if (worldEntry == null) {
                return;
            }

            HashSet<Integer> cxEntry = worldEntry.get(cx);
            if (cxEntry == null) {
                return;
            }

            if (cxEntry.contains(cz)) {
                event.setCancelled(true);
            }
        }
    }
}
