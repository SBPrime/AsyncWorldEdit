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
package org.primesoft.asyncworldedit.directChunk.base;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.directChunk.IBiomeEntry;
import org.primesoft.asyncworldedit.api.directChunk.IBlockEntry;
import org.primesoft.asyncworldedit.api.directChunk.IChangesetData;
import org.primesoft.asyncworldedit.api.directChunk.IChunkData;
import org.primesoft.asyncworldedit.api.directChunk.IChunkUndoData;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkAPI;
import org.primesoft.asyncworldedit.api.directChunk.ISerializedEntity;
import org.primesoft.asyncworldedit.api.directChunk.IWrappedChunk;
import org.primesoft.asyncworldedit.api.inner.IBlocksHubIntegration;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.core.AwePlatform;
import org.primesoft.asyncworldedit.directChunk.entries.BiomeEntry;
import org.primesoft.asyncworldedit.directChunk.entries.BlockEntry;
import org.primesoft.asyncworldedit.excommands.chunk.ChunkUndoData;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.utils.PositionHelper;

/**
 *
 * @author SBPrime
 */
public abstract class BaseWrappedChunk implements IWrappedChunk {

    protected final IWorld m_world;
    protected final int m_cx;
    protected final int m_cz;
    protected final IBlocksHubIntegration m_blocksHub;
    protected final IPlayerEntry m_player;

    public BaseWrappedChunk(IBlocksHubIntegration blocksHub,
            IWorld world, int cx, int cz,
            IPlayerEntry player) {
        m_blocksHub = blocksHub;
        m_player = player;
        m_world = world;
        m_cx = cx;
        m_cz = cz;
    }

    @Override
    public IWorld getWorld() {
        return m_world;
    }

    @Override
    public int getX() {
        return m_cx;
    }

    @Override
    public int getZ() {
        return m_cz;
    }

    @Override
    public IPlayerEntry getPlayer() {
        return m_player;
    }

    @Override
    public boolean setData(IChunkData data) {
        if (data == null) {
            return false;
        }

        return setData(data,
                true, null,
                true, true, true, true, true, data.getEntity(), true, null);
    }

    @Override
    public boolean setData(IChunkUndoData data) {
        if (data == null) {
            return false;
        }

        return setData(data.getBlocks(), data.getChangedBiomes(),
                data.getRemovedEntitys(), data.getAddedEntitys(),
                null, null, null);
    }

    @Override
    public IChunkUndoData setData(IChangesetData data) {
        if (data == null) {
            return null;
        }

        final ISerializedEntity[] dataEntitiesRemove = data.getRemovedEntities();

        InOutParam<IBlockEntry[]> oldBlocks = InOutParam.Out();
        InOutParam<IBiomeEntry[]> oldBiomes = InOutParam.Out();
        InOutParam<UUID[]> addedEntities = InOutParam.Out();

        boolean result = setData(data.getChangedBlocks(), data.getChangedBiomes(),
                data.getAddedEntities(), getUUIDS(dataEntitiesRemove),
                oldBlocks, oldBiomes, addedEntities);

        if (!result) {
            return null;
        }

        return new ChunkUndoData(addedEntities.getValue(), dataEntitiesRemove,
                oldBlocks.getValue(), oldBiomes.getValue()
        );
    }

    /**
     * Extract uuids from serialized entities
     *
     * @param entities
     * @return
     */
    private UUID[] getUUIDS(ISerializedEntity[] entities) {
        if (entities == null) {
            return null;
        }
        UUID[] result = new UUID[entities.length];
        for (int i = 0; i < entities.length; i++) {
            result[i] = entities[i].getUuid();
        }
        return result;
    }

    /**
     * Set the actual chunk data
     *
     * @param data
     * @param removeAllEntities
     * @param entitiesToRemove
     * @param removeAllTileEntities
     * @param setSections
     * @param setBiome
     * @param setAdditional
     * @param setUnknown
     * @param entitiesToAdd
     * @param setTileEntities
     * @param addedEntities
     * @return
     */
    protected abstract boolean setData(IChunkData data,
            boolean removeAllEntities, UUID[] entitiesToRemove,
            boolean removeAllTileEntities,
            boolean setSections, boolean setBiome,
            boolean setAdditional, boolean setUnknown,
            ISerializedEntity[] entitiesToAdd, boolean setTileEntities,
            InOutParam<UUID[]> addedEntities);

    /**
     * Set data to current chunk and acquire the undo data
     *
     * @param dataBlocks
     * @param dataLights
     * @param dataBiomes
     * @param dataEntitiesAdd
     * @param dataEntitiesRemoveUUID
     * @param oldBlocks
     * @param oldBiomes
     * @param addedEntities
     * @return
     */
    private boolean setData(IBlockEntry[] dataBlocks,
            IBiomeEntry[] dataBiomes,
            ISerializedEntity[] dataEntitiesAdd, UUID[] dataEntitiesRemoveUUID,
            InOutParam<IBlockEntry[]> oldBlocks,
            InOutParam<IBiomeEntry[]> oldBiomes,
            InOutParam<UUID[]> addedEntities) {

        final IBlocksHubIntegration bh = m_blocksHub;

        //Fill chunk data
        IChunkData data = getData();

        //Set the provided data
        setBlocks(data, dataBlocks, bh,
                oldBlocks);

        setBiome(data, dataBiomes, oldBiomes);

        if (!setData(data,
                false, dataEntitiesRemoveUUID,
                true, true, true,
                false, false, //We do not set the additional data and unknown data
                dataEntitiesAdd, true, addedEntities)) {
            return false;
        }

        return true;
    }

    /**
     * Set blocks and light to chunk data
     *
     * @param cData
     * @param dataBlocks
     * @param bh
     * @return
     */
    private void setBlocks(final IChunkData cData,
            final IBlockEntry[] dataBlocks, final IBlocksHubIntegration bh,
            InOutParam<IBlockEntry[]> oldBlocks) {
        List<IBlockEntry> tOldBlocks = new ArrayList<IBlockEntry>();

        IDirectChunkAPI dcApi = AwePlatform.getInstance().getCore().getDirectChunkAPI();
        Vector chunkZero = PositionHelper.chunkToPosition(new BlockVector2D(m_cx, m_cz), 0);

        for (IBlockEntry block : dataBlocks) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            final byte emissionLight = cData.getEmissionLight(x, y, z);

            if (block.hasBlock()) {
                Vector pos = chunkZero.add(x, y, z);

                char id = block.getId();
                CompoundTag nbt = block.getNbt();

                BaseBlock old = cData.getBlock(x, y, z);
                BaseBlock newBlock = dcApi.getBaseBlock(id, nbt);

                if (bh == null || m_player == null || bh.canPlace(m_player, m_world, pos, old, newBlock, true)) {
                    final char oldId = dcApi.getCombinedId(old.getType(), old.getData());
                    final CompoundTag oldCt = old.getNbtData();

                    tOldBlocks.add(new BlockEntry(oldId, x, y, z, oldCt, emissionLight));
                    if (bh != null && m_player != null) {
                        bh.logBlock(m_player, m_world, pos, old, newBlock, true);
                    }

                    if (block.hasLight()) {
                        if (nbt == null) {
                            cData.setBlockAndEmission(x, y, z, id, block.getEmission());
                        } else {
                            cData.setTileEntityAndEmission(x, y, z, id, nbt, block.getEmission());
                        }
                    } else if (nbt == null) {
                        cData.setBlock(x, y, z, id);
                    } else {
                        cData.setTileEntity(x, y, z, id, nbt);
                    }

                    if (block.hasSkyLight()) {
                        cData.setSkyLight(x, y, z, block.getSky());
                    }
                }
            } else {
                if (block.hasLight()) {
                    cData.setEmissionLight(x, y, z, block.getEmission());
                }
                if (block.hasSkyLight()) {
                    cData.setSkyLight(x, y, z, block.getSky());
                }
            }
        }

        if (oldBlocks
                != null) {
            oldBlocks.setValue(tOldBlocks.toArray(new IBlockEntry[0]));
        }
    }

    /**
     * Set the biome data
     *
     * @param data
     * @param dataBiomes
     * @param oldBiomes
     */
    private void setBiome(IChunkData cData, IBiomeEntry[] dataBiomes, InOutParam<IBiomeEntry[]> oldBiomes) {
        List<IBiomeEntry> tOldBiome = new ArrayList<IBiomeEntry>();

        IDirectChunkAPI dcApi = AwePlatform.getInstance().getCore().getDirectChunkAPI();
        Vector chunkZero = PositionHelper.chunkToPosition(new BlockVector2D(m_cx, m_cz), 0);

        for (IBiomeEntry entry : dataBiomes) {
            int x = entry.getX();
            int z = entry.getZ();

            Vector pos = chunkZero.add(x, 0, z);
            int id = entry.getId();

            int old = cData.getBiome(x, z);
            tOldBiome.add(new BiomeEntry(old, x, z));

            cData.setBiome(x, z, id);
        }

        if (oldBiomes != null) {
            oldBiomes.setValue(tOldBiome.toArray(new IBiomeEntry[0]));
        }

    }
}
