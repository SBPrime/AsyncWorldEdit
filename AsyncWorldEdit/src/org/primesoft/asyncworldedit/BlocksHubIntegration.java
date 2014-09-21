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

import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.UUID;
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

    private final boolean m_isInitialized;
    private final IBlocksHubApi m_blocksApi;
    private final PlayerManager m_playerManager;

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

    public BlocksHubIntegration(AsyncWorldEditMain plugin) {
        m_playerManager = plugin.getPlayerManager();
        BlocksHub bh = getBlocksHub(plugin);
        m_blocksApi = bh != null ? bh.getApi() : null;
        m_isInitialized = m_blocksApi != null && m_blocksApi.getVersion() >= 1.0;
    }

    public void logBlock(UUID playerUuid, World world, Location location,
            int oldBlockType, byte oldBlockData,
            int newBlockType, byte newBlockData) {
        if (!m_isInitialized || !ConfigProvider.getLogBlocks()) {
            return;
        }

        PlayerWrapper pw = m_playerManager.getPlayer(playerUuid);
        if (pw == null) {
            return;
        }
        String player = pw.getName();
        
        m_blocksApi.logBlock(player, world, location, oldBlockType, oldBlockData, newBlockType, newBlockData);
    }

    public boolean canPlace(UUID playerUuid, World world, Location location) {
        if (!m_isInitialized || !ConfigProvider.getCheckAccess()) {
            return true;
        }

        PlayerWrapper pw = m_playerManager.getPlayer(playerUuid);
        if (pw == null) {
            return true;
        }
        String player = pw.getName();
        
        try {
            return m_blocksApi.canPlace(player, world, location);
        } catch (Exception ex) {
            AsyncWorldEditMain.log("Error checking block place perms: " + ex.toString());
            AsyncWorldEditMain.log("Player: " + player);
            AsyncWorldEditMain.log("World: " + world);
            AsyncWorldEditMain.log("Location: " + location);
            return true;
        }
    }

    public boolean canPlace(UUID playerUuid, World world, Vector location) {
        if (location == null) {
            return false;
        }
        if (!ConfigProvider.getCheckAccess()) {
            return true;
        }
        
        
        Location l = new Location(world, location.getX(), location.getY(), location.getZ());               
        
        try {
            return canPlace(playerUuid, world, l);

        } catch (Exception ex) {
            PlayerWrapper pw = m_playerManager.getPlayer(playerUuid);
            String name = pw == null ? playerUuid.toString() : pw.getName();

            AsyncWorldEditMain.log("Error checking block place perms: " + ex.toString());
            AsyncWorldEditMain.log("Player: " + name);
            AsyncWorldEditMain.log("World: " + world);
            AsyncWorldEditMain.log("Location: " + l);
            return true;
        }
    }

    public void logBlock(UUID playerUuid, World world, Vector location, 
            BaseBlock oldBlock, BaseBlock newBlock) {
        if (location == null || !ConfigProvider.getLogBlocks()) {
            return;
        }

        if (oldBlock == null) {
            oldBlock = new BaseBlock(0);
        }
        if (newBlock == null) {
            newBlock = new BaseBlock(0);
        }

        Location l = new Location(world, location.getX(), location.getY(), location.getZ());
        try {
            logBlock(playerUuid, world, l, oldBlock.getType(), (byte) oldBlock.getData(),
                    newBlock.getType(), (byte) newBlock.getData());
        } catch (Exception ex)
        {
            PlayerWrapper pw = m_playerManager.getPlayer(playerUuid);
            String name = pw == null ? playerUuid.toString() : pw.getName();
            
            AsyncWorldEditMain.log("Error logging block: " + ex.toString());
            AsyncWorldEditMain.log("Player: " + name);
            AsyncWorldEditMain.log("World: " + world);
            AsyncWorldEditMain.log("Location: " + l);
            AsyncWorldEditMain.log("Old: " + oldBlock);
            AsyncWorldEditMain.log("New: " + newBlock);
        }
    }
}
