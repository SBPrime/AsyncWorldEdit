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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.bags.BlockBag;

/**
 *
 * @author SBPrime
 */
public class AsyncEditSessionFactory extends EditSessionFactory {
    private BlockPlacer m_blockPlacer;
    
    public AsyncEditSessionFactory(BlockPlacer blockPlacer) {
        m_blockPlacer = blockPlacer;
    }

    
    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks) {
        return new AsyncEditSession(null, m_blockPlacer, world, maxBlocks);
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, LocalPlayer player) {
        return new AsyncEditSession(player.getName(), m_blockPlacer, world, maxBlocks);
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag) {
        return new AsyncEditSession(null, m_blockPlacer, world, maxBlocks, blockBag);
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, 
        LocalPlayer player) {
        return new AsyncEditSession(player.getName(), m_blockPlacer, world, maxBlocks, blockBag);
    }
    
}
