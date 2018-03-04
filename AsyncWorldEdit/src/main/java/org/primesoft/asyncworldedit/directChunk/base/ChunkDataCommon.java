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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import org.primesoft.asyncworldedit.api.directChunk.IChunkDataCommon;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkAPI;
import org.primesoft.asyncworldedit.configuration.ConfigDirectChunkApi;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.utils.PositionHelper;

/**
 *
 * @author SBPrime
 */
public abstract class ChunkDataCommon implements IChunkDataCommon {
    /**
     * Check if the provided position is valid
     *
     * @param v
     * @return
     */
    public static boolean isValidPosition(BlockVector v) {
        return isValidPosition(v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }

    /**
     * Check if the provided position is valid
     *
     * @param v
     * @return
     */
    public static boolean isValidPosition(Vector v) {
        return isValidPosition(v.getX(), v.getY(), v.getZ());
    }

    /**
     * Check if the provided position is valid
     *
     * @param x
     * @param y
     * @param z
     * @return
    W */
    public static boolean isValidPosition(double x, double y, double z) {
        return isValidPosition(PositionHelper.positionToBlockPosition(x), PositionHelper.positionToBlockPosition(y), PositionHelper.positionToBlockPosition(z));
    }

    /**
     * Check if the provided position is valid
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static boolean isValidPosition(int x, int y, int z) {
        return !(x < 0 || x >= 16 || y < 0 || y >= 256 || z < 0 || z >= 16);
    }
    
    
    /**
     * Check if the provided position is valid
     *
     * @param x
     * @param z
     * @return
     */
    public static boolean isValidPosition(int x, int z) {
        return !(x < 0 || x >= 16 || z < 0 || z >= 16);
    }

    /**
     * Decode the native biome position
     *
     * @param encoded
     * @param coords
     */
    public static void decodeBiomePosition(byte encoded, InOutParam<int[]> coords) {
        int x = encoded & 15;
        int z = (encoded >> 4) & 15;
        coords.setValue(new int[]{x, z});
    }

    /**
     * Decode the native position
     *
     * @param encoded
     * @param coords
     */
    public static void decodePosition(short encoded, InOutParam<int[]> coords) {
        int x = encoded & 15;
        int z = (encoded >> 4) & 15;
        int y = (encoded >> 8) & 255;
        coords.setValue(new int[]{x, y, z});
    }

    /**
     * Encode biome position position
     *
     * @param x
     * @param z
     * @return
     */
    public static byte encodeBiomePosition(int x, int z) {
        return (byte) ((x & 15) | ((z & 15) << 4));
    }

    /**
     * Encode position position
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static short encodePosition(int x, int y, int z) {
        return (short) ((x & 15) | ((z & 15) << 4) | ((y & 255) << 8));
    }
    
    /**
     * The chunk coordinates
     */
    protected BlockVector2D m_chunkCoords;

    /**
     * Get the direct chunk API
     *
     * @return
     */
    protected abstract IDirectChunkAPI getDirectChunkAPI();
    
    /**
     * Is relight enabled?
     * @return 
     */
    protected boolean isRelightEnabled() {
        ConfigDirectChunkApi dcConfig = ConfigProvider.directChunk();
        if (dcConfig == null) {
            return true;
        }
        
        return dcConfig.isAutoRelightEnabled();
    }
    
    /**
     * Get light level for block ID
     * @param id
     * @return 
     */
    protected byte getLightEmissionLevel(char id) {
        ConfigDirectChunkApi dcConfig = ConfigProvider.directChunk();
        if (dcConfig == null) {
            return getDirectChunkAPI().getLightEmissionLevel(id);
        }
        
        byte bl = dcConfig.getBlockLight();
        
        if (bl == -1) {
            return getDirectChunkAPI().getLightEmissionLevel(id);
        }
        
        return bl;
    }
    
    @Override
    public BlockVector2D getChunkCoords() {
        return m_chunkCoords;
    }

    @Override
    public void setChunkCoords(BlockVector2D coords) {
        m_chunkCoords = coords;
    }
}
