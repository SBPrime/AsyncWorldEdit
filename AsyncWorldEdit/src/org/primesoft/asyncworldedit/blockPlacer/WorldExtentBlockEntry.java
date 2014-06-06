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

package org.primesoft.asyncworldedit.blockPlacer;

import com.sk89q.worldedit.Vector;
import org.primesoft.asyncworldedit.worldedit.extent.WorldExtent;

/**
 *
 * @author SBPrime
 */
public abstract class WorldExtentBlockEntry extends BlockPlacerEntry implements IBlockPlacerLocationEntry {
    protected final Vector m_location;
    protected final String m_worldName;

    public WorldExtentBlockEntry(WorldExtent worldExtent,
            int jobId, Vector location) {
        super(jobId);
        
        m_location = location;
        m_worldName = worldExtent.getName();
    }

    @Override
    public String getWorldName() {
        return m_worldName;
    }

    @Override
    public Vector getLocation() {
        return m_location;
    }

    @Override
    public boolean isDemanding() {
        return false;
    }
    
}
