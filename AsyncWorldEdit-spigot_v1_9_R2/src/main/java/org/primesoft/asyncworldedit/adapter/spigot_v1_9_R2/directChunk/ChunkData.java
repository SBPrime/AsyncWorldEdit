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
package org.primesoft.asyncworldedit.adapter.spigot_v1_9_R2.directChunk;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.util.Location;
import net.minecraft.server.v1_9_R2.ChunkSection;
import net.minecraft.server.v1_9_R2.DataPaletteBlock;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NibbleArray;
import org.primesoft.asyncworldedit.api.directChunk.IChunkSection;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkAPI;
import org.primesoft.asyncworldedit.api.directChunk.ISerializedEntity;
import org.primesoft.asyncworldedit.api.directChunk.ISerializedTileEntity;
import org.primesoft.asyncworldedit.utils.ArrayHelpers;
import org.primesoft.asyncworldedit.directChunk.base.BaseChunkData;
import org.primesoft.asyncworldedit.directChunk.ChunkSectionData;

/**
 *
 * @author SBPrime
 */
class ChunkData extends BaseChunkData {

    private boolean m_unknownQ;
    private boolean m_unknownR;

    private long m_unknownU;
    private int m_unknownV;

    public boolean getUnknownQ() {
        return m_unknownQ;
    }

    public void setUnknownQ(boolean data) {
        m_unknownQ = data;
    }

    public boolean getUnknownR() {
        return m_unknownR;
    }

    public void setUnknownR(boolean data) {
        m_unknownR = data;
    }

    public long getUnknownU() {
        return m_unknownU;
    }

    public void setUnknownU(long data) {
        m_unknownU = data;
    }

    public int getUnknownV() {
        return m_unknownV;
    }

    public void setUnknownV(int data) {
        m_unknownV = data;
    }

    void setChunkSections(ChunkSection[] chunkSection) {
        for (int y = 0; y < 16; y++) {
            ChunkSection cs = y < chunkSection.length ? chunkSection[y] : null;
            IChunkSection sectionData;

            if (cs != null) {
                DataPaletteBlock blocks = cs.getBlocks();
                NibbleArray data0003 = new NibbleArray();
                byte[] data0410 = new byte[4096];
                
                
                NibbleArray data1114 = blocks.exportData(data0410, data0003);                
                
                char[] ids = new char[4096];
                
                for (int i = 0;i< ids.length;i++) {
                    int p0003 = data0003.a(i) & 0xff;
                    byte p0410 = data0410[i];
                    byte p1114 = (byte)(data1114 != null ? data1114.a(i) : 0);
                    
                    ids[i] = (char)(p0003 | ((int)(p0410 & 0xff) << 4) | ((int)(p1114 & 0xff) << 12));
                }
                
                NibbleArray skylight = cs.getSkyLightArray();
                NibbleArray emittedLight = cs.getEmittedLightArray();
                sectionData = new ChunkSectionData(y, ArrayHelpers.clone(ids),
                        emittedLight != null ? ArrayHelpers.clone(emittedLight.asBytes()) : null,
                        skylight != null ? ArrayHelpers.clone(skylight.asBytes()) : null
                );
            } else {
                sectionData = null;
            }
            setChunkSection(y, sectionData);
        }
    }

    ChunkSection[] getChunkSections() {
        ChunkSection[] result = new ChunkSection[16];

        for (int y = 0; y < 16; y++) {
            IChunkSection cs = getChunkSection(y);
            if (cs != null) {
                char[] ids = cs.getBlockIds();
                byte[] skyLight = cs.getSkyLight();
                byte[] emittedLight = cs.getEmittedLight();

                ChunkSection chunkSection = new ChunkSection(y * 16, true, ArrayHelpers.clone(ids));
                chunkSection.a(new NibbleArray(ArrayHelpers.clone(emittedLight)));
                if (skyLight != null) {
                    chunkSection.b(new NibbleArray(ArrayHelpers.clone(skyLight)));
                }

                chunkSection.recalcBlockCounts();
                result[y] = chunkSection;
            } else {
                result[y] = new ChunkSection(y * 16, true, new char[16 * 16 * 16]);
            }
        }
        return result;
    }

    @Override
    protected ISerializedTileEntity createTileEntity(BlockVector entityCoords, CompoundTag ct) {
        return new SerializedTileEntity(entityCoords, Nbt.serialise(Nbt.convertTag(ct)));
    }    

    @Override
    protected ISerializedEntity createEntity(BaseEntity entity, Vector pos, Location location, CompoundTag ct) {
        NBTTagCompound nbt = Nbt.convertTag(ct);
        nbt.setString("id", entity.getTypeId());
        return new SerializedEntity(pos, location.getYaw(), location.getPitch(), nbt);
    }

    
    
    @Override
    protected IDirectChunkAPI getDirectChunkAPI() {
        return DirectChunkApi.getInstance();
    }
}
