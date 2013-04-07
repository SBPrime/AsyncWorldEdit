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
package org.PrimeSoft.AsyncWorldedit.Worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import org.PrimeSoft.AsyncWorldedit.BlockPlacer;
import org.PrimeSoft.AsyncWorldedit.BlockPlacerEntry;
import org.PrimeSoft.AsyncWorldedit.PluginMain;
import org.bukkit.World;

/**
 *
 * @author SBPrime
 */
public class AsyncEditSession extends EditSession {
    private String m_player;
    private BlockPlacer m_blockPlacer;
    private World m_world;
    
    public String getPlayer()
    {
        return m_player;
    }

    public AsyncEditSession(PluginMain plugin, String player,
            LocalWorld world, int maxBlocks) {
        super(world, maxBlocks);

        m_player = player;
        m_blockPlacer = plugin.getBlockPlacer();
        m_world = plugin.getServer().getWorld(world.getName());
    }

    public AsyncEditSession(PluginMain plugin, String player,
            LocalWorld world, int maxBlocks, BlockBag blockBag) {
        super(world, maxBlocks, blockBag);

        m_player = player;
        m_blockPlacer = plugin.getBlockPlacer();
        m_world = plugin.getServer().getWorld(world.getName());
    }

    @Override
    public boolean rawSetBlock(Vector pt, BaseBlock block) {
        return m_blockPlacer.addTasks(new BlockPlacerEntry(this, pt, block));
    }

    public void doRawSetBlock(Vector pt, BaseBlock block) {
        super.rawSetBlock(pt, block);
    }

    public World getCBWorld() {
        return m_world;
    }
}