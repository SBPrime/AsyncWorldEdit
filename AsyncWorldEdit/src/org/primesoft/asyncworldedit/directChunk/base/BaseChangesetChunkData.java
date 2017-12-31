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
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.util.Location;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.directChunk.IBiomeEntry;
import org.primesoft.asyncworldedit.api.directChunk.IBlockEntry;
import org.primesoft.asyncworldedit.api.directChunk.IChangesetChunkData;
import org.primesoft.asyncworldedit.api.directChunk.IChunkData;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkAPI;
import org.primesoft.asyncworldedit.api.directChunk.ISerializedEntity;
import org.primesoft.asyncworldedit.api.directChunk.IWrappedChunk;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.api.utils.IFunc;
import org.primesoft.asyncworldedit.core.AwePlatform;
import org.primesoft.asyncworldedit.directChunk.entries.BiomeEntry;
import org.primesoft.asyncworldedit.directChunk.entries.BlockEntry;

/**
 *
 * @author SBPrime
 */
public abstract class BaseChangesetChunkData extends ChunkDataCommon implements IChangesetChunkData {

    /**
     * The air block
     */
    private static final BaseBlock AIR = new BaseBlock(0);

    /**
     * The MTA mutex
     */
    private final Object m_mutex = new Object();

    /**
     * The chunk blocks
     */
    private final HashMap<Short, IBlockEntry> m_changedBlocks;

    /**
     * The chunk biomes
     */
    private final LinkedHashMap<Byte, IBiomeEntry> m_changedBiomes;

    /**
     * New serialized entities
     */
    private final HashMap<UUID, ISerializedEntity> m_addedEntity;

    /**
     * Removed serialized entities
     */
    private final HashMap<UUID, ISerializedEntity> m_removedEntity;

    /**
     * The task dispatcher
     */
    private final ITaskDispatcher m_dispatcher;

    /**
     * The wrapped chunk
     */
    private final IWrappedChunk m_wrappedChunk;

    /**
     * The current chunk data used for block get
     */
    private IChunkData m_fullChunkData;

    protected BaseChangesetChunkData(IWrappedChunk wrappedChunk, ITaskDispatcher dispatcher) {
        m_dispatcher = dispatcher;
        m_wrappedChunk = wrappedChunk;
        m_chunkCoords = new BlockVector2D(wrappedChunk.getX(), wrappedChunk.getZ());

        m_changedBlocks = new LinkedHashMap<Short, IBlockEntry>();
        m_changedBiomes = new LinkedHashMap<Byte, IBiomeEntry>();

        m_addedEntity = new LinkedHashMap<UUID, ISerializedEntity>();
        m_removedEntity = new LinkedHashMap<UUID, ISerializedEntity>();
    }

    /**
     * Get the list of added entities
     *
     * @return
     */
    @Override
    public ISerializedEntity[] getAddedEntities() {
        synchronized (m_mutex) {
            return m_addedEntity.values().toArray(new ISerializedEntity[0]);
        }
    }

    /**
     * Get the list of removed entities
     *
     * @return
     */
    @Override
    public ISerializedEntity[] getRemovedEntities() {
        synchronized (m_mutex) {
            return m_removedEntity.values().toArray(new ISerializedEntity[0]);
        }
    }

    @Override
    public void setBlock(int x, int y, int z, BaseBlock b) {
        final IDirectChunkAPI dcApi = AwePlatform.getInstance().getCore().getDirectChunkAPI();
        final int data = b.getData();
        final int type = b.getType();
        final char id = dcApi.getCombinedId(type, data);
        final CompoundTag ct = b.getNbtData();

        setTileEntity(x, y, z, id, ct);
    }

    @Override
    public void setBlock(int x, int y, int z, char id) {
        setTileEntity(x, y, z, id, null);
    }

    @Override
    public void setBlockAndEmission(int x, int y, int z, BaseBlock b, byte emission) {
        final IDirectChunkAPI dcApi = AwePlatform.getInstance().getCore().getDirectChunkAPI();
        final int data = b.getData();
        final int type = b.getType();
        final char id = dcApi.getCombinedId(type, data);
        final CompoundTag ct = b.getNbtData();

        setTileEntityAndEmission(x, y, z, id, ct, emission);
    }

    @Override
    public void setBlockAndEmission(int x, int y, int z, char id, byte emission) {
        setTileEntityAndEmission(x, y, z, id, null, emission);
    }

    @Override
    public void setTileEntity(int x, int y, int z, char id, CompoundTag ct) {
        if (isRelightEnabled()) {
            setTileEntityAndEmission(x, y, z, id, ct, (byte)-1);
        } else {
            setTileEntityAndEmission(x, y, z, id, ct, getLightEmissionLevel(id));
        }
    }

    
    @Override
    public void setTileEntityAndEmission(int x, int y, int z, char id, CompoundTag ct, byte emission) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("setBlock: invalid position %1$s,%2$s,%3$s", x, y, z));
            return;
        }

        short encodedPos = encodePosition(x, y, z);
        synchronized (m_mutex) {
            m_changedBlocks.put(encodedPos, emission < 0 ? 
                    new BlockEntry(id, x, y, z, ct) :
                    new BlockEntry(id, x, y, z, ct, emission));
        }
    }

    @Override
    public void setBiome(int x, int z, int biome) {
        if (!isValidPosition(x, z)) {
            log(String.format("setBiome: invalid position %1$s,%2$s", x, z));
            return;
        }

        byte encodedPos = encodeBiomePosition(x, z);
        synchronized (m_mutex) {
            m_changedBiomes.put(encodedPos, 
                    new BiomeEntry((byte)(biome & 0xff), x, z));
        }
    }
    
    @Override
    public void setEmissionLight(int x, int y, int z, byte emission) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("setEmissionLight: invalid position %1$s,%2$s,%3$s", x, y, z));
            return;
        }

        short encodedPos = encodePosition(x, y, z);

        synchronized (m_mutex) {
            IBlockEntry blockEntry = m_changedBlocks.get(encodedPos);

            if (blockEntry == null) {
                blockEntry = new BlockEntry(x, y, z, emission);
            } else {
                blockEntry.setEmission(emission);
            }

            m_changedBlocks.put(encodedPos, blockEntry);
        }
    }
    

    @Override
    public void setSkyLight(int x, int y, int z, byte emission) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("setSkyLight: invalid position %1$s,%2$s,%3$s", x, y, z));
            return;
        }

        short encodedPos = encodePosition(x, y, z);

        synchronized (m_mutex) {
            IBlockEntry blockEntry = m_changedBlocks.get(encodedPos);

            if (blockEntry == null) {
                blockEntry = new BlockEntry(x, y, z, (byte)-1, emission);
            }
            else {
                blockEntry.setSky(emission);
            }

            m_changedBlocks.put(encodedPos, blockEntry);
        }
    }

    @Override
    public boolean removeEntity(ISerializedEntity entity) {
        if (entity == null) {
            return false;
        }

        boolean result;
        UUID uuid = entity.getUuid();
        synchronized (m_mutex) {
            if (m_addedEntity.containsKey(uuid)) {
                m_addedEntity.remove(uuid);
                result = true;
            } else if (!m_removedEntity.containsKey(uuid)) {
                m_removedEntity.put(uuid, entity);
                result = true;
            } else {
                result = false;
            }

        }

        return result;
    }

    @Override
    public void addEntity(ISerializedEntity entity) {
        if (entity == null) {
            return;
        }

        UUID uuid = entity.getUuid();
        synchronized (m_mutex) {
            if (m_removedEntity.containsKey(uuid)) {
                m_removedEntity.remove(uuid);
            } else {
                if (m_addedEntity.containsKey(uuid)) {
                    m_addedEntity.remove(uuid);
                }

                m_addedEntity.put(uuid, entity);
            }
        }
    }

    @Override
    public ISerializedEntity addEntity(Vector pos, Entity entity) {
        if (entity == null) {
            return null;
        }

        if (!isValidPosition(pos)) {
            log(String.format("addEntity: invalid position %1$s", pos));
            return null;
        }

        BaseEntity bEntity = entity.getState();
        Location location = entity.getLocation();
        if (bEntity == null || !bEntity.hasNbtData()) {
            log("addEntity: no state or no nbt data.");
            return null;
        }

        CompoundTag ct = bEntity.getNbtData();
        ISerializedEntity result = createSerializedEntity(pos, location, ct, bEntity.getTypeId());

        addEntity(result);
        return result;
    }

    /**
     * Create new instance of serialized entity
     *
     * @param pos
     * @param location
     * @param ct
     * @param typeId
     * @return
     */
    protected abstract ISerializedEntity createSerializedEntity(Vector pos, Location location,
            CompoundTag ct, String typeId);

    @Override
    public IBlockEntry[] getChangedBlocks() {
        synchronized (m_mutex) {
            return m_changedBlocks.values().toArray(new IBlockEntry[0]);
        }
    }

    @Override
    public IBiomeEntry[] getChangedBiomes() {
        synchronized (m_mutex) {
            return m_changedBiomes.values().toArray(new IBiomeEntry[0]);
        }
    }

    @Override
    public int getMaterial(int x, int y, int z) {
        return getBlock(x, y, z).getType();
    }

    @Override
    public char getRawBlockData(int x, int y, int z) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("getRawBlockData: invalid position %1$s,%2$s,%3$s", x, y, z));
            return 0;
        }

        final short encodedPos = encodePosition(x, y, z);

        synchronized (m_mutex) {
            final IBlockEntry bEntry = m_changedBlocks.get(encodedPos);

            if (bEntry != null && bEntry.hasBlock()) {
                return bEntry.getId();
            } else {
                if (m_fullChunkData == null) {
                    m_fullChunkData = fullChunkData();
                }

                return m_fullChunkData.getRawBlockData(x, y, z);
            }
        }
    }

    @Override
    public BaseBlock getBlock(int x, int y, int z) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("getBlock: invalid position %1$s,%2$s,%3$s", x, y, z));
            return AIR;
        }

        final short encodedPos = encodePosition(x, y, z);

        synchronized (m_mutex) {
            final IBlockEntry bEntry = m_changedBlocks.get(encodedPos);

            if (bEntry != null && bEntry.hasBlock()) {
                final IDirectChunkAPI dcApi = AwePlatform.getInstance().getCore().getDirectChunkAPI();
                BaseBlock block = dcApi.convertId(bEntry.getId());
                if (bEntry.getNbt() != null) {
                    block.setNbtData(bEntry.getNbt());
                }

                return block;
            } else {
                if (m_fullChunkData == null) {
                    m_fullChunkData = fullChunkData();
                }

                return m_fullChunkData.getBlock(x, y, z);
            }
        }
    }

    @Override
    public int getBiome(int x, int z) {   
        if (!isValidPosition(x, z)) {
            log(String.format("getBiome: invalid position %1$s,%2$s", x, z));
            return 0;
        }

        final byte encodedPos = encodeBiomePosition(x, z);

        synchronized (m_mutex) {
            final IBiomeEntry bEntry = m_changedBiomes.get(encodedPos);

            if (bEntry != null) {
                return bEntry.getId() & 0xff;
            } else {
                if (m_fullChunkData == null) {
                    m_fullChunkData = fullChunkData();
                }

                return m_fullChunkData.getBiome(x, z);
            }
        }
    }

    @Override
    public byte getEmissionLight(int x, int y, int z) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("getEmissionLight: invalid position %1$s,%2$s,%3$s", x, y, z));
            return -1;
        }

        final short encodedPos = encodePosition(x, y, z);

        synchronized (m_mutex) {
            IBlockEntry blockEntry = m_changedBlocks.get(encodedPos);
            if (blockEntry != null && blockEntry.hasLight()) {
                return blockEntry.getEmission();
            }

            if (m_fullChunkData == null) {
                m_fullChunkData = fullChunkData();
            }

            return m_fullChunkData.getEmissionLight(x, y, z);
        }
    }
    
    @Override
    public byte getSkyLight(int x, int y, int z) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("getSkyLight: invalid position %1$s,%2$s,%3$s", x, y, z));
            return -1;
        }

        final short encodedPos = encodePosition(x, y, z);

        synchronized (m_mutex) {
            IBlockEntry blockEntry = m_changedBlocks.get(encodedPos);
            if (blockEntry != null && blockEntry.hasSkyLight()) {
                return blockEntry.getSky();
            }

            if (m_fullChunkData == null) {
                m_fullChunkData = fullChunkData();
            }

            return m_fullChunkData.getSkyLight(x, y, z);
        }
    }

    /**
     * Get the chunk entities
     *
     * @return
     */
    @Override
    public ISerializedEntity[] getEntity() {
        final HashMap<UUID, ISerializedEntity> result = new LinkedHashMap<UUID, ISerializedEntity>();
        synchronized (m_mutex) {
            if (m_fullChunkData == null) {
                m_fullChunkData = fullChunkData();
            }

            for (ISerializedEntity e : m_addedEntity.values()) {
                result.put(e.getUuid(), e);
            }
            for (ISerializedEntity e : m_fullChunkData.getEntity()) {
                UUID uuid = e.getUuid();

                if (!m_removedEntity.containsKey(uuid) && !result.containsKey(uuid)) {
                    result.put(uuid, e);
                }
            }
        }

        return result.values().toArray(new ISerializedEntity[0]);
    }

    /**
     * Get the chunk data using the task dispatcher
     *
     * @return
     */
    private IChunkData fullChunkData() {
        IWorld world = m_wrappedChunk.getWorld();
        return m_dispatcher.performSafeChunk(world, new IFunc<IChunkData>() {

            @Override
            public IChunkData execute() {
                return m_wrappedChunk.getData();
            }
        }, world, m_chunkCoords);
    }
}
