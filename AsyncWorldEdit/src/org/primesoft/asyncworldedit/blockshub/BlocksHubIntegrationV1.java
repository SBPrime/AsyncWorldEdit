/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.blockshub;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import org.PrimeSoft.blocksHub.IBlocksHubApi;
import org.bukkit.Location;
import org.bukkit.World;
import static org.primesoft.asyncworldedit.AsyncWorldEditBukkit.log;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.platform.bukkit.BukkitWorld;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
class BlocksHubIntegrationV1 implements IBlocksHubIntegration {
    private final IBlocksHubApi m_blocksApi;


    /**
     * Create new instance of the class
     * @param api
     *
     */
    public BlocksHubIntegrationV1(IBlocksHubApi api) {
        m_blocksApi = api;
    }

    /**
     * Log block change using BlocksHub
     *
     * @param playerEntry
     * @param world
     * @param location
     * @param oldBlockType
     * @param oldBlockData
     * @param newBlockType
     * @param newBlockData
     */
    private void logBlock(IPlayerEntry playerEntry, IWorld world, Location location,
            int oldBlockType, byte oldBlockData,
            int newBlockType, byte newBlockData) {
        World bWorld = getWorld(world);
        if (bWorld == null) {
            return;
        }

        String player = playerEntry.getName();

        m_blocksApi.logBlock(player, bWorld, location, oldBlockType, oldBlockData, newBlockType, newBlockData);
    }

    /**
     * Check if the block can be changed
     *
     * @param playerEntry
     * @param world
     * @param location
     * @param dc
     * @return
     */    
    private boolean hasAccess(IPlayerEntry playerEntry, IWorld world, Location location) {
        World bWorld = getWorld(world);
        if (bWorld == null) {
            return false;
        }
        
        String player = playerEntry.getName();

        try {
            return m_blocksApi.canPlace(player, bWorld, location);
        } catch (Exception ex) {
            log(String.format("Error checking block place perms: %1$s", ex.toString()));
            log(String.format("Player: %1$s", player));
            log(String.format("World: %1$s", world));
            log(String.format("Location: %1$s", location));
            ExceptionHelper.printException(ex, "Block checking error.");

            return true;
        }
    }

    
    /**
     * Check if the block can be changed
     *
     * @param playerEntry
     * @param world
     * @param location
     * @return
     */
    @Override
    public boolean canPlace(IPlayerEntry playerEntry, IWorld world, Vector location,
            BaseBlock oldBlock, BaseBlock newBlock) {
        return hasAccess(playerEntry, world, location);
    }


    @Override
    public boolean hasAccess(IPlayerEntry playerEntry, IWorld world, Vector location) {
        if (location == null) {
            return false;
        }
        
        World bWorld = getWorld(world);
        if (bWorld == null) {
            return false;
        }
        
        Location l = new Location(bWorld, location.getX(), location.getY(), location.getZ());

        try {
            return hasAccess(playerEntry, world, l);

        } catch (Exception ex) {
            String name = playerEntry.getName();

            log(String.format("Error checking block place perms: {0]", ex.toString()));
            log(String.format("Player: %1$s", name));
            log(String.format("World: %1$s", world));
            log(String.format("Location: %1$s", l));

            ExceptionHelper.printException(ex, "Block checking error.");
            return true;
        }
    }

    /**
     * Log block change using BlocksHub
     *
     * @param playerEntry
     * @param world
     * @param location
     * @param oldBlock
     * @param newBlock
     * @param dc
     */
    @Override
    public void logBlock(IPlayerEntry playerEntry, IWorld world, Vector location,
            BaseBlock oldBlock, BaseBlock newBlock) {
        if (location == null) {
            return;
        }
        
        World bWorld = getWorld(world);
        if (bWorld == null) {
            return;
        }        

        if (oldBlock == null) {
            oldBlock = new BaseBlock(0);
        }
        if (newBlock == null) {
            newBlock = new BaseBlock(0);
        }

        Location l = new Location(bWorld, location.getX(), location.getY(), location.getZ());
        try {
            logBlock(playerEntry, world, l, oldBlock.getType(), (byte) oldBlock.getData(),
                    newBlock.getType(), (byte) newBlock.getData());
        } catch (Exception ex) {
            String name = playerEntry.getName();

            log(String.format("Error logging block: %1$s", ex.toString()));
            log(String.format("Player: %1$s", name));
            log(String.format("World: %1$s", world));
            log(String.format("Location: %1$s", l));
            log(String.format("Old: %1$s", oldBlock));
            log(String.format("New: %1$s", newBlock));

            ExceptionHelper.printException(ex, "Error logging block.");
        }
    }

    private World getWorld(IWorld world) {
        if (world == null || !(world instanceof BukkitWorld)) {
            return null;
        }
        
        
        return ((BukkitWorld)world).getWorld();
    }
}
