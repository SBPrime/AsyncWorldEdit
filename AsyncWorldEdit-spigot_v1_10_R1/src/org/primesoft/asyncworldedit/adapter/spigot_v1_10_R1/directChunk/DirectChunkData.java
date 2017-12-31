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
package org.primesoft.asyncworldedit.adapter.spigot_v1_10_R1.directChunk;

import net.minecraft.server.v1_10_R1.Block;
import net.minecraft.server.v1_10_R1.Chunk;
import net.minecraft.server.v1_10_R1.ChunkSection;
import net.minecraft.server.v1_10_R1.NibbleArray;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkAPI;
import org.primesoft.asyncworldedit.api.directChunk.IWrappedChunk;
import org.primesoft.asyncworldedit.directChunk.DcUtils;
import org.primesoft.asyncworldedit.directChunk.base.BaseDirectChunkData;
import static org.primesoft.asyncworldedit.directChunk.base.ChunkDataCommon.isValidPosition;

/**
 *
 * @author SBPrime
 */
public class DirectChunkData extends BaseDirectChunkData {

    /**
     * *
     * The chunk
     */
    private final Chunk m_chunk;

    DirectChunkData(IWrappedChunk parrent, Chunk chunk) {
        super(parrent);
        m_chunk = chunk;

    }

    /**
     * Get the chunk sections
     *
     * @return
     */
    private ChunkSection[] getChunkSections() {
        return m_chunk.getSections();
    }

    /**
     * Get the chunk section
     *
     * @param y
     * @param force
     * @return
     */
    private ChunkSection getChunkSection(int y, boolean force) {
        int cy = y >> 4;

        ChunkSection[] sections = getChunkSections();
        if (sections == null) {
            return null;
        }

        if (cy < 0 || cy >= sections.length) {
            return null;
        }

        ChunkSection result = sections[cy];

        if (result == null || result == Chunk.a) {
            if (!force) {
                return null;
            }

            result = new ChunkSection(y, true);
            result.a(new NibbleArray(DcUtils.newSectionEmittedLight()));

            sections[cy] = result;
        }

        return result;
    }

    @Override
    public byte getEmissionLight(int x, int y, int z) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("getEmissionLight: invalid position %1$s,%2$s,%3$s", x, y, z));
            return 0;
        }

        ChunkSection cs = getChunkSection(y, false);
        if (cs == null) {
            return 0;
        }

        return (byte) cs.c(x, y & 0xf, z);
    }
    
    @Override
    public byte getSkyLight(int x, int y, int z) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("getSkyLight: invalid position %1$s,%2$s,%3$s", x, y, z));
            return 0;
        }

        ChunkSection cs = getChunkSection(y, false);
        if (cs == null) {
            return 0;
        }

        return (byte) cs.b(x, y & 0xf, z);
    }

    @Override
    public int getMaterial(int x, int y, int z) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("getRawBlockData: invalid position %1$s,%2$s,%3$s", x, y, z));
            return 0;
        }

        ChunkSection cs = getChunkSection(y, false);
        if (cs == null) {
            return 0;
        }

        return Block.getId(cs.getType(x, y & 0xf, z).getBlock());
    }

    @Override
    public char getRawBlockData(int x, int y, int z) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("getRawBlockData: invalid position %1$s,%2$s,%3$s", x, y, z));
            return 0;
        }

        ChunkSection cs = getChunkSection(y, false);
        if (cs == null) {
            return 0;
        }

        return (char) Block.REGISTRY_ID.getId(cs.getType(x, y & 0xf, z));
    }

    @Override
    public void setEmissionLight(int x, int y, int z, byte lightLevel) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("setEmissionLight: invalid position %1$s,%2$s,%3$s", x, y, z));
            return;
        }

        ChunkSection cs = getChunkSection(y, true);
        if (cs == null) {
            return;
        }

        cs.b(x, y & 0xf, z, lightLevel);
    }
    
    @Override
    public void setSkyLight(int x, int y, int z, byte lightLevel) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("setSkyLight: invalid position %1$s,%2$s,%3$s", x, y, z));
            return;
        }

        ChunkSection cs = getChunkSection(y, true);
        if (cs == null) {
            return;
        }

        cs.a(x, y & 0xf, z, lightLevel);
    }

    @Override
    public void setBlockAndEmission(int x, int y, int z, char id, byte emission) {
        if (!isValidPosition(x, y, z)) {
            log(String.format("setBlockAndEmission: invalid position %1$s,%2$s,%3$s", x, y, z));
            return;
        }

        ChunkSection cs = getChunkSection(y, true);
        if (cs == null) {
            return;
        }

        cs.setType(x, y & 0xf, z, Block.REGISTRY_ID.fromId(id));
        if (emission >= 0) {
            cs.b(x, y & 0xf, z, emission);
        }
    }

    @Override
    public int getBiome(int x, int z) {
        if (!isValidPosition(x, z)) {
            log(String.format("getBiome: invalid position %1$s,%2$s", x, z));
            return 0;
        }

        byte[] biomeIndex = m_chunk.getBiomeIndex();
        return biomeIndex[(int)(encodeBiomePosition(x, z) & 0xff)] & 0xff;
    }

    @Override
    public void setBiome(int x, int z, int biome) {
        if (!isValidPosition(x, z)) {
            log(String.format("setBiome: invalid position %1$s,%2$s", x, z));
            return;
        }

        byte[] biomeIndex = m_chunk.getBiomeIndex();
        biomeIndex[(int)(encodeBiomePosition(x, z) & 0xff)] = (byte) (biome & 0xff);
    }

    @Override
    protected IDirectChunkAPI getDirectChunkAPI() {
        return DirectChunkApi.getInstance();
    }
}
