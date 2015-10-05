/*
 * AsyncWorldEdit Premium is a commercial version of AsyncWorldEdit. This software 
 * has been sublicensed by the software original author according to p7 of
 * AsyncWorldEdit license.
 *
 * AsyncWorldEdit Premium - donation version of AsyncWorldEdit, a performance 
 * improvement plugin for Minecraft WorldEdit plugin.
 *
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
 *
 * All rights reserved.
 *
 * 1. You may: 
 *    install and use AsyncWorldEdit in accordance with the Software documentation
 *    and pursuant to the terms and conditions of this license
 * 2. You may not:
 *    sell, redistribute, encumber, give, lend, rent, lease, sublicense, or otherwise
 *    transfer Software, or any portions of Software, to anyone without the prior 
 *    written consent of Licensor
 * 3. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 4. The original author of the software is allowed to sublicense the software 
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
