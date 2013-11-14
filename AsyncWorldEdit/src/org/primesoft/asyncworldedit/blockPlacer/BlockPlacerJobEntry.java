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

import org.bukkit.ChatColor;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;

/**
 *
 * @author SBPrime
 */
public class BlockPlacerJobEntry extends BlockPlacerEntry {

    /**
     * Job name
     */
    private final String m_name;
    /**
     * Is the job started
     */
    private boolean m_isStarted;
    /**
     * Cancelable edit session
     */
    private CancelabeEditSession m_cEditSession;

    public BlockPlacerJobEntry(AsyncEditSession editSession, CancelabeEditSession cEditSession,
            int jobId, String name) {
        super(editSession, jobId);

        m_name = name;
        m_isStarted = false;
        m_cEditSession = cEditSession;
    }

    /**
     * Is the job started
     *
     * @return
     */
    public boolean isStarted() {
        return m_isStarted;
    }

    public String getName() {
        return m_name;
    }

    public void start() {
        m_isStarted = true;
    }

    public void cancel() {
        if (m_cEditSession != null) {
            m_cEditSession.cancel();
        }
    }

    @Override
    public String toString() {
        return ChatColor.WHITE + "[" + getJobId() + "] " + getName();
    }
}
