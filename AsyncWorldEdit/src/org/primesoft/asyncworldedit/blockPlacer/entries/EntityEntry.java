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
package org.primesoft.asyncworldedit.blockPlacer.entries;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerEntry;
import org.primesoft.asyncworldedit.worldedit.CuboidClipboardWrapper;

/**
 * Paste entities task
 * @author SBPrime
 */
public class EntityEntry extends BlockPlacerEntry {

    /**
     * Entity data
     */
    private final Object m_data;
    /**
     * Paste location
     */
    private final Vector m_location;
    /**
     * The clipboard
     */
    private final CuboidClipboard m_clipboard;

    
    /**
     * New instance of the class
     * @param jobId
     * @param data
     * @param location
     * @param clipboard 
     */
    public EntityEntry(int jobId, Object data, Vector location, CuboidClipboard clipboard) {
        super(jobId);

        m_data = data;
        m_location = location;
        m_clipboard = clipboard;
    }
    
    @Override
    public boolean isDemanding() {
        return true;
    }

    @Override
    public boolean Process(BlockPlacer bp) {
        synchronized (m_clipboard) {
            Object old = CuboidClipboardWrapper.getEntities(m_clipboard);
            CuboidClipboardWrapper.setEntities(m_clipboard, m_data);
            m_clipboard.pasteEntities(m_location);

            CuboidClipboardWrapper.setEntities(m_clipboard, old);
        }
        
        return true;
    }
}