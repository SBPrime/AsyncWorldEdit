/*
 * AsyncWorldEdit API
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit API contributors
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
package org.primesoft.asyncworldedit.api.directChunk;

import org.primesoft.asyncworldedit.api.utils.IInOutParam;

/**
 * The direct chunk data
 * @author SBPrime
 */
public interface IChunkData extends IBaseChunkData {
    /**
     * Get the biome data
     * @return 
     */
    byte[] getBiomeData();

    /**
     * Set the biome data
     * @param data 
     */
    void setBiomeData(byte[] data);

    /**
     * Get maximum block height
     * @return 
     */
    int[] getMaxHeight();

    /**
     * Set the maximum height data
     * @param data 
     */
    void setMaxHeight(int[] data);

    /**
     * Get the chunk height map
     * @return 
     */
    int[] getHeightMap();

    /**
     * Set the chunk height map
     * @param data 
     */
    void setHeightMap(int[] data);

    /**
     * Is the chunk done
     * @return 
     */
    boolean isDone();

    /**
     * Set the chunk done flag
     * @param done 
     */
    void setDone(boolean done);

    /**
     * Is the chunk lit
     * @return 
     */
    boolean isLit();

    /**
     * Set the chunk lit flag
     * @param lit 
     */
    void setLit(boolean lit);

    /**
     * Set chunk tile entities
     * @param entities 
     */
    void setTileEntity(ISerializedTileEntity[] entities);

    /**
     * Get the chunk tile entities
     * @return 
     */
    ISerializedTileEntity[] getTileEntity();

    /**
     * Set the chunk entities
     * @param data 
     */
    void setEntity(ISerializedEntity[] data);


    /**
     * Set chunk section data
     * @param y
     * @param section 
     */
    void setChunkSection(int y, IChunkSection section);

    /**
     * Get the chunk section data
     * @param y
     * @return 
     */
    IChunkSection getChunkSection(int y);

    /**
     * Get the chunk gaps
     * @return 
     */
    boolean[] getGaps();

    /**
     * Set the chunk gaps
     * @param data 
     */
    void setGaps(boolean[] data);
    
    
    /**
     * Get block from chunk data
     * @param x
     * @param y
     * @param z
     * @param tileEntity The TileEntity
     * @return
     */
    char getBlock(int x, int y, int z, IInOutParam<ISerializedTileEntity> tileEntity);
}
