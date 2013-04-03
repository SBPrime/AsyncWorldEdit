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
package org.PrimeSoft.AsyncWorldedit;

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 *
 * @author SBPrime
 */
public class BlockLogerEntry {    
    private Vector m_location;
    private BaseBlock m_newBlock;
    private String m_player;
    private AsyncEditSession m_editSession;
    private LocalWorld m_world;
    private boolean m_isRaw;

    public Vector getLocation() {
        return m_location;
    }

    public BaseBlock getNewBlock() {
        return m_newBlock;
    }
    
    public boolean isRaw()
    {
        return m_isRaw;
    }
    
    public AsyncEditSession getEditSession() {
        return m_editSession;
    }
    
    public LocalWorld getWorld()
    {
        return m_world;
    }
    
    public String getPlayer() {
        return m_player;
    }

    /*public BlockLogerEntry(String player, AsyncEditSession editSession) {
        initialize(player, editSession);
        
        m_finalize = true;
    }*/

    public BlockLogerEntry(String player, AsyncEditSession editSession,
            Vector location, BaseBlock newBlock, boolean raw) {
        initialize(player, editSession);
        m_location = location;
        m_newBlock = newBlock;
        m_isRaw = raw;                 
    }
    
    private void initialize(String player, AsyncEditSession editSession)
    {
        m_player = player;
        m_isRaw = false;
        m_location = null;
        m_newBlock = null;
        m_editSession = editSession;
        m_world = editSession.getWorld();
    }
}
