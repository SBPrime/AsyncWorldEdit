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
