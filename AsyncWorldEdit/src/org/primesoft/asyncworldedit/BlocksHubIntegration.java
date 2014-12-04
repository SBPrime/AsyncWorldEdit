/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.asyncworldedit;

import org.primesoft.asyncworldedit.configuration.ConfigProvider;
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

    private final boolean m_isInitialized;
    private final IBlocksHubApi m_blocksApi;

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
        BlocksHub bh = getBlocksHub(plugin);
        m_blocksApi = bh != null ? bh.getApi() : null;
        m_isInitialized = m_blocksApi != null && m_blocksApi.getVersion() >= 1.0;
    }

    public void logBlock(PlayerEntry playerEntry, World world, Location location,
            int oldBlockType, byte oldBlockData,
            int newBlockType, byte newBlockData) {
        if (!m_isInitialized || !ConfigProvider.getLogBlocks()) {
            return;
        }
        
        if (playerEntry == null) {
            return;
        }
        String player = playerEntry.getName();
        
        m_blocksApi.logBlock(player, world, location, oldBlockType, oldBlockData, newBlockType, newBlockData);
    }

    public boolean canPlace(PlayerEntry playerEntry, World world, Location location) {
        if (!m_isInitialized || !ConfigProvider.getCheckAccess()) {
            return true;
        }
        
        if (playerEntry == null) {
            return true;
        }
        String player = playerEntry.getName();
        
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

    public boolean canPlace(PlayerEntry playerEntry, World world, Vector location) {
        if (location == null) {
            return false;
        }
        if (!ConfigProvider.getCheckAccess()) {
            return true;
        }
        
        
        Location l = new Location(world, location.getX(), location.getY(), location.getZ());               
        
        try {
            return canPlace(playerEntry, world, l);

        } catch (Exception ex) {            
            String name = playerEntry.getName();

            AsyncWorldEditMain.log("Error checking block place perms: " + ex.toString());
            AsyncWorldEditMain.log("Player: " + name);
            AsyncWorldEditMain.log("World: " + world);
            AsyncWorldEditMain.log("Location: " + l);
            return true;
        }
    }

    public void logBlock(PlayerEntry playerEntry, World world, Vector location, 
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
            logBlock(playerEntry, world, l, oldBlock.getType(), (byte) oldBlock.getData(),
                    newBlock.getType(), (byte) newBlock.getData());
        } catch (Exception ex)
        {            
            String name = playerEntry.getName();
            
            AsyncWorldEditMain.log("Error logging block: " + ex.toString());
            AsyncWorldEditMain.log("Player: " + name);
            AsyncWorldEditMain.log("World: " + world);
            AsyncWorldEditMain.log("Location: " + l);
            AsyncWorldEditMain.log("Old: " + oldBlock);
            AsyncWorldEditMain.log("New: " + newBlock);
        }
    }
}