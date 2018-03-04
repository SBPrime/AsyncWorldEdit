/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2017, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.adapter.spigot_v1_12_R1.directChunk;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.UUID;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.IBlockData;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.IChunk;
import org.primesoft.asyncworldedit.api.directChunk.IChunkData;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkAPI;
import org.primesoft.asyncworldedit.api.directChunk.IChangesetChunkData;
import org.primesoft.asyncworldedit.api.directChunk.ISerializedEntity;
import org.primesoft.asyncworldedit.api.directChunk.IWrappedChunk;
import org.primesoft.asyncworldedit.api.inner.IBlocksHubIntegration;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.platform.bukkit.BukkitChunk;
import org.primesoft.asyncworldedit.platform.bukkit.directChunk.BukkitDirectChunkAPI;

/**
 *
 * @author SBPrime
 */
public class DirectChunkApi extends BukkitDirectChunkAPI implements IDirectChunkAPI {
    /**
     * The DirectChunk API instance
     */
    private static IDirectChunkAPI s_instance;

    /**
     * Get instance of the API
     *
     * @param taskDispatcher
     * @param blocksHub
     */
    public static void create(ITaskDispatcher taskDispatcher, IBlocksHubIntegration blocksHub) {
        s_instance = new DirectChunkApi(taskDispatcher, blocksHub);
    }
    
    /**
     * Get instance of the API
     *
     * @return
     */
    public static IDirectChunkAPI getInstance() {
        if (s_instance == null) {
            throw new IllegalStateException("Class not initialized");
        }
        
        return s_instance;
    }

    private DirectChunkApi(ITaskDispatcher taskDispatcher, IBlocksHubIntegration blocksHub) {
        super(taskDispatcher, blocksHub);
    }

    @Override
    public IWrappedChunk wrapChunk(IChunk chunk, IPlayerEntry player) {
        if (chunk == null || !(chunk instanceof BukkitChunk)) {
            return null;
        }

        Chunk bChunk = ((BukkitChunk) chunk).getChunk();
        if (!(bChunk instanceof CraftChunk)) {
            return null;
        }

        return new WrappedChunk(m_blocksHub, bChunk.getWorld(), bChunk.getX(), bChunk.getZ(), player);
    }

    @Override
    public IChunkData createChunkData() {
        return new ChunkData();
    }

    @Override
    public IChangesetChunkData createLazyChunkData(IWrappedChunk chunk) {
        return new ChangesetChunkData(chunk, m_dispatcher);
    }

    @Override
    public int getMaterial(char type) {
        return Block.getId(Block.getById(type));
    }

    @Override
    public char getCombinedId(int type, int data) {
        Block block = Block.getById(type);
        IBlockData blockData = block.fromLegacyData(data);

        return (char) Block.REGISTRY_ID.getId(blockData);
    }

    @Override
    public BaseBlock convertId(char combinedId) {
        IBlockData blockData = Block.REGISTRY_ID.fromId((int) combinedId);

        if (blockData == null) {
            log(String.format("Unable to get BlockData from %1$s", (int)combinedId));
            return new BaseBlock(AIR);
        }

        Block block = blockData.getBlock();
        return new BaseBlock(Block.getId(block), block.toLegacyData(blockData));
    }

    @Override
    public ISerializedEntity createEntity(UUID uuid, Vector position, float yaw, float pitch, byte[] nbt) {
        return new SerializedEntity(uuid, position, yaw, pitch, nbt);
    }
}
