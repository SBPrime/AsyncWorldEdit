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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;

/**
 * This class is a LocalSession wrapper that provides clipboard injection points
 *
 * @author SBPrime
 */
public class LocalSessionWrapper extends LocalSession {

    /**
     * The parrent local session
     */
    private final LocalSession m_parrent;
    /**
     * Player
     */
    private final String m_player;

    /**
     * The parrent local session
     *
     * @return
     */
    public LocalSession getParrent() {
        return m_parrent;
    }

    public LocalSessionWrapper(String player, LocalConfiguration configuration,
            LocalSession parrent) {
        super(configuration);
        m_player = player;
        m_parrent = parrent;
    }

    @Override
    public void setClipboard(CuboidClipboard clipboard) {
        if ((clipboard instanceof AsyncCuboidClipboard) || clipboard == null) {
            super.setClipboard(clipboard);
        } else {
            super.setClipboard(new AsyncCuboidClipboard(m_player, clipboard));
        }
    }
}
