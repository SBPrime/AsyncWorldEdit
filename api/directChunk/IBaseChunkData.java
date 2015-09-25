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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.Entity;

/**
 *
 * @author SBPrime
 */
public interface IBaseChunkData {

    /**
     * Get the chunk coords
     *
     * @return
     */
    BlockVector2D getChunkCoords();

    /**
     * Set the chunk coords
     *
     * @param coords
     */
    void setChunkCoords(BlockVector2D coords);

    /**
     * Set chunk block
     *
     * @param x X coordinate inside chunk (0-15)
     * @param y Y coordinate inside chunk (0-15)
     * @param z Z coordinate inside chunk (0-15)
     * @param b WorldEdit block
     */
    void setBlock(int x, int y, int z, BaseBlock b);

    /**
     * Set chunk block
     *
     * @param x X coordinate inside chunk (0-15)
     * @param y Y coordinate inside chunk (0-15)
     * @param z Z coordinate inside chunk (0-15)
     * @param id Material ID
     */
    void setBlock(int x, int y, int z, char id);

    /**
     * Set chunk tile eneity
     *
     * @param x X coordinate inside chunk (0-15)
     * @param y Y coordinate inside chunk (0-15)
     * @param z Z coordinate inside chunk (0-15)
     * @param id Material ID of the tile entitiy
     * @param ct Tile entity NBT data
     */
    void setTileEntity(int x, int y, int z, char id, CompoundTag ct);

    /**
     * Remove entity from chunk
     *
     * @param entity
     * @return
     */
    boolean removeEntity(ISerializedEntity entity);

    /**
     * Add entity to chunk
     *
     * @param entity
     */
    void addEntity(ISerializedEntity entity);

    /**
     * Add entity to chunk
     *
     * @param pos
     * @param entity
     * @return serialized entity
     */
    ISerializedEntity addEntity(Vector pos, Entity entity);

    
    /**
     * Get block from chunk data
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    BaseBlock getBlock(int x, int y, int z);
        
    
    /**
     * Get the chunk entities
     * @return 
     */
    ISerializedEntity[] getEntity();
}
