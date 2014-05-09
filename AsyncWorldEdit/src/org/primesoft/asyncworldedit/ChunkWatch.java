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

import java.util.HashSet;
import java.util.UUID;
import javax.print.DocFlavor;
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
     * Suppressed chunk entry (simple x,z)
     */
    public class ChunkEntry {
        private final int m_x;

        private final int m_z;
        
        private final String m_worldName;

        public ChunkEntry(int x, int z, String worldName) {
            m_x = x;
            m_z = z;
            m_worldName = worldName;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ChunkEntry)) {
                return false;
            }

            ChunkEntry ce = (ChunkEntry) obj;

            return m_x == ce.m_x && m_z == ce.m_z && 
                    ((m_worldName == null && ce.m_worldName == null) || (m_worldName != null && m_worldName.equals(ce.m_worldName)));
        }

        @Override
        public int hashCode() {
            return m_x ^ m_z ^ (m_worldName != null ? m_worldName.hashCode() : 0);
        }
    }

    /**
     * Suppressed chunks
     */
    private final HashSet<ChunkEntry> m_watchedChunks = new HashSet<ChunkEntry>();

    /**
     * Remove all chunk unload queues
     */
    public void clear() {
        synchronized (m_watchedChunks) {
            m_watchedChunks.clear();
        }
        
        PluginMain.log("[CHUNK] Watched chunks cleared");
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
            ChunkEntry ce = new ChunkEntry(cx, cz, worldName);
            if (!m_watchedChunks.contains(ce)) {
                m_watchedChunks.add(ce);
            }
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
            ChunkEntry ce = new ChunkEntry(cx, cz, worldName);
            if (m_watchedChunks.contains(ce)) {
                m_watchedChunks.remove(ce);
            }
        }
    }

    @EventHandler
    public void onChunkUnloadEvent(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();

        int cx = chunk.getX();
        int cz = chunk.getZ();
        String world = chunk.getWorld().getName();

        ChunkEntry ce = new ChunkEntry(cx, cz, world);

        synchronized (m_watchedChunks) {
            if (m_watchedChunks.contains(ce)) {
                event.setCancelled(true);
                PluginMain.log("[CHUNK] Suppressed chunk unload " + world + " " + cx + " " + cz);
            } else {
                PluginMain.log("[CHUNK] Chunk unload " + world + " "  + cx + " " + cz);
            }
        }
    }
}
