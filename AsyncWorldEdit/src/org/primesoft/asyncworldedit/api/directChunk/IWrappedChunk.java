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

import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;

/**
 * Chunk manipulation class
 * @author SBPrime
 */
public interface IWrappedChunk {
    /**
     * Get the bukkit world
     * @return 
     */
    public IWorld getWorld();
    
    /**
     * Get the chunk X coordinate
     * @return 
     */
    public int getX();
    
    /**
     * Get the chunk Y coordinate
     * @return 
     */
    public int getZ();
    

    /**
     * Get the player that wrapped the chunk
     * @return 
     */
    public IPlayerEntry getPlayer();
    
    /**
     * Get the chunk data
     * @return 
     */
    public IChunkData getData();
    
    /**
     * Set the chunk data
     * @param data
     * @return 
     */
    public boolean setData(IChunkData data);
    
    /**
     * Set the chunk undo data
     * @param data
     * @return 
     */
    public boolean setData(IChunkUndoData data);
    
    
    /**
     * Set the chunk data
     * @param data
     * @return 
     */
    public IChunkUndoData setData(IChangesetData data);


    /**
     * Flush stored data to the server     
     */
    public void flush();
    
    
    /**
     * Initialise the lighting
     */
    public void initLighting();
    
    
    /**
     * Update the light for provided position
     * @param x
     * @param y
     * @param z 
     */
    public void updateLight(int x, int y, int z);
}
