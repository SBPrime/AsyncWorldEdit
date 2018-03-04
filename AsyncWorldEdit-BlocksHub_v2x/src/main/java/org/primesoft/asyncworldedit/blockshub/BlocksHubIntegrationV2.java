/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
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
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.inner.IBlocksHubIntegration;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.blockshub.IBlocksHubApi;
import org.primesoft.blockshub.api.BlockData;

/**
 *
 * @author SBPrime
 */
public class BlocksHubIntegrationV2 implements IBlocksHubIntegration {

    private final IBlocksHubApi m_blocksApi;

    /**
     * Create new instance of the class
     *
     * @param api
     *
     */
    public BlocksHubIntegrationV2(IBlocksHubApi api) {
        m_blocksApi = api;
    }

    /**
     * Check if the block can be changed
     *
     * @param playerEntry
     * @param world
     * @param location
     * @param oldBlock
     * @param newBlock
     * @return
     */
    @Override
    public boolean canPlace(IPlayerEntry playerEntry, IWorld world, Vector location,
            BaseBlock oldBlock, BaseBlock newBlock) {
        return canPlace(playerEntry, world, location,
                oldBlock, newBlock, false);
    }

    /**
     * Check if the block can be changed
     *
     * @param playerEntry
     * @param world
     * @param location
     * @param dc
     * @param oldBlock
     * @param newBlock
     * @return
     */
    @Override
    public boolean canPlace(IPlayerEntry playerEntry, IWorld world, Vector location,
            BaseBlock oldBlock, BaseBlock newBlock,
            boolean dc) {
        if (location == null || world == null) {
            return false;
        }

        if (playerEntry == null 
                || playerEntry.isDisposed()
                || playerEntry.getUUID() == null) {
            return false;
        }

        BlockData oldData = (oldBlock == null) ? BlockData.AIR : new BlockData(oldBlock.getType(), oldBlock.getData());
        BlockData newData = (newBlock == null) ? BlockData.AIR : new BlockData(newBlock.getType(), newBlock.getData());

        try {
            return m_blocksApi.canPlace(playerEntry.getUUID(), world.getUUID(), 
                    new org.primesoft.blockshub.api.Vector(location.getX(), location.getY(), location.getZ()), oldData, newData);
        } catch (Exception ex) {
            log(String.format("Error checking block place perms: {0]", ex.toString()));
            log(String.format("Player: %1$s", playerEntry.getName()));
            log(String.format("World: %1$s", world.getName()));
            log(String.format("Location: %1$s", location));

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
            BaseBlock oldBlock, BaseBlock newBlock, boolean dc) {
        if (location == null || world == null) {
            return;
        }

        if (playerEntry == null 
                || playerEntry.isDisposed()
                || playerEntry.getUUID() == null) {
            return;
        }

        BlockData oldData = (oldBlock == null) ? BlockData.AIR : new BlockData(oldBlock.getType(), oldBlock.getData());
        BlockData newData = (newBlock == null) ? BlockData.AIR : new BlockData(newBlock.getType(), newBlock.getData());

        try {
            m_blocksApi.logBlock(new org.primesoft.blockshub.api.Vector(location.getX(), location.getY(), location.getZ()),
                    playerEntry.getUUID(), world.getUUID(),
                    oldData,
                    newData);
        } catch (Exception ex) {
            log(String.format("Error logging block: %1$s", ex.toString()));
            log(String.format("Player: %1$s", playerEntry.getName()));
            log(String.format("World: %1$s", world.getName()));
            log(String.format("Location: %1$s", location));
            log(String.format("Old: %1$s", oldBlock));
            log(String.format("New: %1$s", newBlock));

            ExceptionHelper.printException(ex, "Error logging block.");
        }
    }

    @Override
    public boolean hasAccess(IPlayerEntry playerEntry, IWorld world, Vector location) {
        return hasAccess(playerEntry, world, location, false);
    }

    @Override
    public boolean hasAccess(IPlayerEntry playerEntry, IWorld world, Vector location, boolean dc) {
        if (location == null || world == null) {
            return false;
        }

        if (playerEntry == null
                || playerEntry.isDisposed()
                || playerEntry.getUUID() == null) {
            return false;
        }

        try {
            return m_blocksApi.hasAccess(playerEntry.getUUID(), world.getUUID(), 
                    new org.primesoft.blockshub.api.Vector(location.getX(), location.getY(), location.getZ()));
        } catch (Exception ex) {
            log(String.format("Error checking block place perms: {0]", ex.toString()));
            log(String.format("Player: %1$s", playerEntry.getName()));
            log(String.format("World: %1$s", world.getName()));
            log(String.format("Location: %1$s", location));

            ExceptionHelper.printException(ex, "Block checking error.");
            return true;
        }
    }        
    
}
