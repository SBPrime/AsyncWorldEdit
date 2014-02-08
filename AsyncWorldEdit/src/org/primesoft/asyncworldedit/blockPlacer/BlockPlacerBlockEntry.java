/*
 * The MIT License
 *
 * Copyright 2013 SBPrime.
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
import com.sk89q.worldedit.blocks.BaseBlock;
import org.bukkit.World;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Prime
 */
public class BlockPlacerBlockEntry extends BlockPlacerEntry {
    private final Vector m_location;
    private final BaseBlock m_newBlock;

    public Vector getLocation() {
        return m_location;
    }

    public BaseBlock getNewBlock() {
        return m_newBlock;
    }

    @Override
    public boolean isDemanding() {
        return false;
    }
    
    

    public BlockPlacerBlockEntry(AsyncEditSession editSession,
            int jobId, Vector location, BaseBlock newBlock) {
        super(editSession, jobId);
        m_location = location;
        m_newBlock = newBlock;
    }

    @Override
    public void Process(BlockPlacer bp) {        
        final World world = m_editSession.getCBWorld();
        
        m_editSession.doRawSetBlock(m_location, m_newBlock);
        if (world != null) {
            bp.getPhysicsWatcher().removeLocation(world.getName(), m_location);
        }
    }
}
