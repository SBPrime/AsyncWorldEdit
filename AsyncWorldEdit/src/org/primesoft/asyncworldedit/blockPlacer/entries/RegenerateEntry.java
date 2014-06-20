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
package org.primesoft.asyncworldedit.blockPlacer.entries;

import com.sk89q.worldedit.Vector2D;
import org.bukkit.World;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerEntry;

/**
 * Regenerate chunk entry
 * @author Prime
 */
public class RegenerateEntry extends BlockPlacerEntry {

    private final World m_world;
    private final Vector2D m_chunk;

    public RegenerateEntry(int jobId, World world, Vector2D chunk) {
        super(jobId);

        m_chunk = chunk;
        m_world = world;
    }
    
    @Override
    public boolean isDemanding() {
        return true;
    }

    @Override
    public boolean Process(BlockPlacer bp) {
        try {
            m_world.regenerateChunk(m_chunk.getBlockX(), m_chunk.getBlockZ());
            return true;
            
        } catch (Throwable t) {
            t.printStackTrace();
            
            return false;
        }
    }
}
