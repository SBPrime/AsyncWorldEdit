/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
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
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.util.Location;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.directChunk.IChunkData;
import org.primesoft.asyncworldedit.api.directChunk.IChunkSection;
import org.primesoft.asyncworldedit.api.directChunk.ISerializedEntity;
import org.primesoft.asyncworldedit.api.directChunk.ISerializedTileEntity;
import org.primesoft.asyncworldedit.api.utils.IInOutParam;
import org.primesoft.asyncworldedit.directChunk.ChunkSectionData;
import org.primesoft.asyncworldedit.utils.InOutParam;

/**
 * The basic chunk data implementation
 *
 * @author SBPrime
 */
public abstract class BaseChunkData extends ChunkDataCommon implements IChunkData {

    private final byte[] m_biomeData;
    private final int[] m_maxHeight;
    private final boolean[] m_gaps;
    private final int[] m_heightMap;

    private final IChunkSection[] m_chunkSections;
    private final HashMap<BlockVector, ISerializedTileEntity> m_tileEntities;
    private final List<ISerializedEntity> m_entities;
    private boolean m_done;
    private boolean m_lit;

    @Override
    public byte[] getBiomeData() {
        return m_biomeData;
    }

    @Override
    public void setBiomeData(byte[] data) {
        if (data == null || data.length != m_biomeData.length) {
            return;
        }

        System.arraycopy(data, 0, m_biomeData, 0, data.length);
    }

    @Override
    public int[] getMaxHeight() {
        return m_maxHeight;
    }

    @Override
    public void setMaxHeight(int[] data) {
        if (data == null || data.length != m_maxHeight.length) {
            return;
        }

        System.arraycopy(data, 0, m_maxHeight, 0, m_maxHeight.length);
    }

    @Override
    public boolean[] getGaps() {
        return m_gaps;
    }

    @Override
    public void setGaps(boolean[] data) {
        if (data == null || data.length != m_gaps.length) {
            return;
        }

        System.arraycopy(data, 0, m_gaps, 0, m_gaps.length);
    }

    @Override
    public int[] getHeightMap() {
        return m_heightMap;
    }

    @Override
    public void setHeightMap(int[] data) {
        if (data == null || data.length != m_heightMap.length) {
            return;
        }

        System.arraycopy(data, 0, m_heightMap, 0, m_heightMap.length);
    }

    @Override
    public void setTileEntity(ISerializedTileEntity[] entities) {
        synchronized (m_tileEntities) {
            m_tileEntities.clear();

            for (ISerializedTileEntity entry : entities) {
                BlockVector v = entry.getPosition();

                m_tileEntities.put(v, entry);
            }
        }
    }

    @Override
    public ISerializedTileEntity[] getTileEntity() {
        ISerializedTileEntity[] result;
        synchronized (m_tileEntities) {
            result = m_tileEntities.values().toArray(new ISerializedTileEntity[0]);
        }

        return result;
    }

    @Override
    public void setEntity(ISerializedEntity[] data) {
        synchronized (m_entities) {
            m_entities.clear();
            m_entities.addAll(Arrays.asList(data));
        }
    }

    @Override
    public ISerializedEntity[] getEntity() {
        ISerializedEntity[] result;
        synchronized (m_entities) {
            result = m_entities.toArray(new ISerializedEntity[0]);
        }

        return result;
    }

    /**
     * Remove a tile entity from possition
     *
     * @param v
     */
    protected void removeTileEntity(BlockVector v) {
        if (!isValidPosition(v)) {
            log(String.format("removeTileEntity: invalid position %1$s", v));
            return;
        }
        synchronized (m_tileEntities) {
            m_tileEntities.remove(v);
        }
    }

    /**
     * Set tile entity on position
     *
     * @param v
     * @param tEntity
     */
    protected void setTileEntity(BlockVector v, ISerializedTileEntity tEntity) {
        if (!isValidPosition(v)) {
            log(String.format("setTileEntity: invalid position %1$s", v));
            return;
        }

        synchronized (m_tileEntities) {
            m_tileEntities.put(v, tEntity);
        }
    }

    /**
     * Get tile entity for position
     *
     * @param v
     * @return
     */
    protected ISerializedTileEntity getTileEntity(BlockVector v) {
        if (!isValidPosition(v)) {
            log(String.format("getTileEntity: invalid position %1$s", v));
            return null;
        }

        synchronized (m_tileEntities) {
            if (m_tileEntities.containsKey(v)) {
                return m_tileEntities.get(v);
            }

            return null;
        }
    }

    @Override
    public boolean isDone() {
        return m_done;
    }

    @Override
    public void setDone(boolean done) {
        m_done = done;
    }

    @Override
    public boolean isLit() {
        return m_lit;
    }

    @Override
    public void setLit(boolean lit) {
        m_lit = lit;
    }

    @Override
    public void setChunkSection(int y, IChunkSection section) {
        if (y < 0 || y >= 16) {
            log(String.format("setChunkSection: invalid position %1$s", y));
            return;
        }

        m_chunkSections[y] = section;
    }

    @Override
    public IChunkSection getChunkSection(int y) {
        if (y < 0 || y >= 16) {
            log(String.format("getChunkSection: invalid position %1$s", y));
            return null;
        }

        return m_chunkSections[y];
    }

    /**
     * Create new instance of chunk data
     */
    protected BaseChunkData() {
        m_chunkCoords = new BlockVector2D(0, 0);
        m_tileEntities = new HashMap<BlockVector, ISerializedTileEntity>();
        m_entities = new LinkedList<ISerializedEntity>();

        m_maxHeight = new int[256];
        m_biomeData = new byte[256];
        m_gaps = new boolean[256];
        m_heightMap = new int[256];

        m_done = false;
        m_chunkSections = new IChunkSection[16];
        Arrays.fill(m_maxHeight, -999);
    }

    @Override
    public boolean removeEntity(ISerializedEntity entity) {
        if (entity == null) {
            return false;
        }

        boolean result;
        synchronized (m_entities) {
            result = m_entities.remove(entity);
        }

        return result;
    }

    @Override
    public void addEntity(ISerializedEntity entity) {
        if (entity == null) {
            return;
        }

        synchronized (m_entities) {
            m_entities.add(entity);
        }
    }

    @Override
    public BaseBlock getBlock(int x, int y, int z) {
        BlockVector2D cPos = getChunkCoords();
        InOutParam<ISerializedTileEntity> entity = InOutParam.Out();

        char combinedId = getBlock(x, y, z, entity);
        BaseBlock block = getDirectChunkAPI().getBaseBlock(combinedId, entity.isSet()
                ? entity.getValue().getRawData(cPos.getBlockX(), cPos.getBlockZ()) : null);

        return block;
    }

    @Override
    public int getMaterial(int x, int y, int z) {
        return getDirectChunkAPI().getMaterial(getBlock(x, y, z, null));
    }

    @Override
    public char getRawBlockData(int x, int y, int z) {
        return getBlock(x, y, z, null);
    }

    @Override
    public void setBlockAndEmission(int x, int y, int z, BaseBlock b, byte emission) {
        final int data = b.getData();
        final int type = b.getType();
        final char id = getDirectChunkAPI().getCombinedId(type, data);
        final CompoundTag ct = b.getNbtData();

        if (ct != null) {
            setTileEntityAndEmission(x, y, z, id, ct, emission);
        } else {
            setBlockAndEmission(x, y, z, id, emission);
        }
    }

    @Override
    public void setBlock(int x, int y, int z, BaseBlock b) {
        final int data = b.getData();
        final int type = b.getType();
        final char id = getDirectChunkAPI().getCombinedId(type, data);
        final CompoundTag ct = b.getNbtData();

        if (ct != null) {
            setTileEntity(x, y, z, id, ct);
        } else {
            setBlock(x, y, z, id);
        }
    }

    @Override
    public void setBlockAndEmission(int x, int y, int z, char id, byte emission) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("setBlock: invalid position %1$s,%2$s,%3$s", x, y, z));
            return;
        }

        int cy = y / 16;
        IChunkSection cs = getChunkSection(cy);
        if (cs == null) {
            cs = new ChunkSectionData(y, new char[16 * 16 * 16], true);
            setChunkSection(cy, cs);
        }

        char[] ids = cs.getBlockIds();
        int idx = (y % 16) * 256 + z * 16 + x;

        if (idx < 0 || idx >= ids.length) {
            return;
        }

        removeTileEntity(new BlockVector(x, y, z));

        ids[idx] = id;
        if (emission >= 0) {
            setEmission(cs, idx, emission);
        }
    }

    @Override
    public void setBlock(int x, int y, int z, char id) {
        if (isRelightEnabled()) {
            setBlockAndEmission(x, y, z, id, (byte) -1);
        } else {
            setBlockAndEmission(x, y, z, id, getLightEmissionLevel(id));
        }
    }

    @Override
    public void setTileEntityAndEmission(int x, int y, int z, char id, CompoundTag ct, byte emission) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("setTileEntity: invalid position %1$s,%2$s,%3$s", x, y, z));
            return;
        }

        int cy = y / 16;
        IChunkSection cs = getChunkSection(cy);
        if (cs == null) {
            cs = new ChunkSectionData(y, new char[16 * 16 * 16], true);
            setChunkSection(cy, cs);
        }

        BlockVector entityCoords = new BlockVector(x, y, z);
        ISerializedTileEntity entity = createTileEntity(entityCoords, ct);

        char[] ids = cs.getBlockIds();

        int idx = (y % 16) * 256 + z * 16 + x;

        if (idx < 0 || idx >= ids.length) {
            return;
        }

        setTileEntity(new BlockVector(x, y, z), entity);

        ids[idx] = id;

        if (emission >= 0) {
            setEmission(cs, idx, emission);
        }
    }

    @Override
    public void setTileEntity(int x, int y, int z, char id, CompoundTag ct) {
        if (isRelightEnabled()) {
            setTileEntityAndEmission(x, y, z, id, ct, (byte) -1);
        } else {
            setTileEntityAndEmission(x, y, z, id, ct, (byte) (getLightEmissionLevel(id) & 0xf));
        }

    }

    /**
     * Set the emission light level
     *
     * @param cs
     * @param idx
     * @param id
     */
    private void setEmission(IChunkSection cs, int idx, byte lightLevel) {
        setLight(cs.getEmittedLight(),
                idx, lightLevel & 0xf);
    }
    
    /**
     * Set light
     *
     * @param cs
     * @param idx
     * @param id
     */
    private void setLight(byte[] data, int idx, int level) {
        int lIdx = idx >> 1;
        int mask = 0xf << (4 * ((idx + 1) & 1));
        level = level & 0xf;

        data[lIdx]
                = (byte) ((data[lIdx] & mask)
                | level << (4 * (idx & 1)));
    }

    @Override
    public char getBlock(int x, int y, int z, IInOutParam<ISerializedTileEntity> tileEntity) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("getBlock: invalid position %1$s,%2$s,%3$s", x, y, z));
            return 0;
        }

        int cy = y / 16;
        IChunkSection cs = getChunkSection(cy);
        if (cs == null) {
            return 0;
        }

        char[] ids = cs.getBlockIds();
        int idx = (y % 16) * 256 + z * 16 + x;

        if (idx < 0 || idx >= ids.length) {
            return 0;
        }

        if (tileEntity != null) {
            ISerializedTileEntity entity = getTileEntity(new BlockVector(x, y, z));

            if (entity != null) {
                tileEntity.setValue(entity);
            }
        }

        return ids[idx];
    }

    @Override
    public void setEmissionLight(int x, int y, int z, byte emission) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("setEmissionLight: invalid position %1$s,%2$s,%3$s", x, y, z));
            return;
        }

        int cy = y / 16;
        IChunkSection cs = getChunkSection(cy);
        if (cs == null) {
            cs = new ChunkSectionData(y, new char[16 * 16 * 16], true);
            setChunkSection(cy, cs);
        }

        int idx = (y % 16) * 256 + z * 16 + x;
        byte[] lEmission = cs.getEmittedLight();
        if (lEmission != null) {
            setLight(lEmission, idx, emission);
        }
    }
    
    
    @Override
    public void setSkyLight(int x, int y, int z, byte emission) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("setSkyLight: invalid position %1$s,%2$s,%3$s", x, y, z));
            return;
        }

        int cy = y / 16;
        IChunkSection cs = getChunkSection(cy);
        if (cs == null) {
            cs = new ChunkSectionData(y, new char[16 * 16 * 16], true);
            setChunkSection(cy, cs);
        }

        int idx = (y % 16) * 256 + z * 16 + x;
        byte[] lEmission = cs.getSkyLight();
        if (lEmission != null) {
            setLight(lEmission, idx, emission);
        }
    }
    
    
    @Override
    public byte getEmissionLight(int x, int y, int z) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("getEmissionLight: invalid position %1$s,%2$s,%3$s", x, y, z));
            return -1;
        }

        int cy = y / 16;
        IChunkSection cs = getChunkSection(cy);
        if (cs == null) {
            return -1;
        }

        return getLight(y, z, x, cs.getEmittedLight());
    }

    private byte getLight(int y, int z, int x, byte[] light) {
        int idx = (y % 16) * 256 + z * 16 + x;
        int lIdx = idx >> 2;

        byte lightLevel;

        if (light == null || lIdx < 0 || lIdx >= light.length) {
            return -1;
        } else {
            lightLevel = light[lIdx];
        }

        int sh = 4 * (idx & 0x1);
        int mask = 0xf << sh;
        return (byte) ((lightLevel & mask) >> sh);
    }

    @Override
    public byte getSkyLight(int x, int y, int z) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("getSkyLight: invalid position %1$s,%2$s,%3$s", x, y, z));
            return -1;
        }

        int cy = y / 16;
        IChunkSection cs = getChunkSection(cy);
        if (cs == null) {
            return -1;
        }

        return getLight(y, z, x, cs.getSkyLight());
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

        ISerializedEntity result = createEntity(bEntity, pos, location, ct);

        addEntity(result);
        return result;
    }

    @Override
    public int getBiome(int x, int z) {
        if (!isValidPosition(x, z)) {
            log(String.format("getBiome: invalid position %1$s,%2$s", x, z));
            return 0;
        }

        return m_biomeData[(int)(encodeBiomePosition(x, z) & 0xff)] & 0xff;
    }

    @Override
    public void setBiome(int x, int z, int biome) {
        if (!isValidPosition(x, z)) {
            log(String.format("setBiome: invalid position %1$s,%2$s", x, z));
            return;
        }

        m_biomeData[(int)(encodeBiomePosition(x, z) & 0xff)] = (byte) (biome & 0xff);
    }

    /**
     * Create new tile entity
     *
     * @param entityCoords
     * @param ct
     * @return
     */
    protected abstract ISerializedTileEntity createTileEntity(BlockVector entityCoords, CompoundTag ct);

    /**
     * Create new entity
     *
     * @param entity
     * @param pos
     * @param location
     * @param ct
     * @return
     */
    protected abstract ISerializedEntity createEntity(BaseEntity entity, Vector pos, Location location, CompoundTag ct);
}
