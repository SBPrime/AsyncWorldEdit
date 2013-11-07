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
package org.primesoft.asyncworldedit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import org.PrimeSoft.blocksHub.BlocksHub;
import org.PrimeSoft.blocksHub.IBlocksHubApi;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author SBPrime
 */
public class BlocksHubIntegration {

    private boolean m_isInitialized;
    private IBlocksHubApi m_blocksApi;

    /**
     * Get instance of the core blocks hub plugin
     *
     * @param plugin
     * @return
     */
    public static BlocksHub getBlocksHub(JavaPlugin plugin) {
        try {
            Plugin cPlugin = plugin.getServer().getPluginManager().getPlugin("BlocksHub");

            if ((cPlugin == null) || (!(cPlugin instanceof BlocksHub))) {
                return null;
            }

            return (BlocksHub) cPlugin;
        } catch (NoClassDefFoundError ex) {
            return null;
        }
    }

    public BlocksHubIntegration(JavaPlugin plugin) {
        BlocksHub bh = getBlocksHub(plugin);
        m_blocksApi = bh != null ? bh.getApi() : null;
        m_isInitialized = m_blocksApi != null && m_blocksApi.getVersion() >= 1.0;
    }

    public void logBlock(String player, World world, Location location,
            int oldBlockType, byte oldBlockData,
            int newBlockType, byte newBlockData) {
        if (!m_isInitialized || !ConfigProvider.getLogBlocks()) {
            return;
        }

        m_blocksApi.logBlock(player, world, location, oldBlockType, oldBlockData, newBlockType, newBlockData);
    }

    public boolean canPlace(String player, World world, Location location) {
        if (!m_isInitialized || !ConfigProvider.getCheckAccess()) {
            return true;
        }

        return m_blocksApi.canPlace(player, world, location);
    }

    public boolean canPlace(String name, World world, Vector location) {
        if (location == null)
        {
            return false;
        }
        if (!ConfigProvider.getCheckAccess())
        {
            return true;
        }
        Location l = new Location(world, location.getX(), location.getY(), location.getZ());
        return canPlace(name, world, l);
    }

    public void logBlock(String name, World world, Vector location, BaseBlock oldBlock, BaseBlock newBlock) {
        if (location == null || !ConfigProvider.getLogBlocks())
        {
            return;
        }
        
        if (oldBlock == null)
        {
            oldBlock = new BaseBlock(0);
        }
        if (newBlock == null)
        {
            newBlock = new BaseBlock(0);
        }
        
        Location l = new Location(world, location.getX(), location.getY(), location.getZ());
        logBlock(name, world, l, oldBlock.getType(), (byte) oldBlock.getData(),
                newBlock.getType(), (byte) newBlock.getData());
    }
}
