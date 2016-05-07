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
import com.sk89q.worldedit.blocks.BaseBlock;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.utils.IInOutParam;

/**
 * The direct chunk API class
 * @author SBPrime
 */
public interface IDirectChunkAPI {
    /**
     * Wrap bukkit chunk into direct chunk api
     * @param chunk
     * @param player
     * @return 
     */
    IWrappedChunk wrapChunk(Chunk chunk, IPlayerEntry player);
    
    
    /**
     * Create an empty chunk data
     * @return 
     */
    IChunkData createChunkData();
    
    
    /**
     * Create an lazy chunk data
     * @param chunk
     * @return 
     */
    IChangesetChunkData createLazyChunkData(IWrappedChunk chunk);
    
    
    /**
     * Converts material and data to chunk section id
     * @param m
     * @param data
     * @return 
     */
    char getCombinedId(Material m, int data);
    
    
    /**
     * Converts type and data to chunk section id
     * @param type
     * @param data
     * @return 
     */
    char getCombinedId(int type, int data);
    
    
    /**
     * Get WorldEdit base blocks
     * @param type
     * @param nbt
     * @return 
     */
    BaseBlock getBaseBlock(char type, CompoundTag nbt);
    
    
    /**
     * Convert combined ID to Material and data
     * @param combinedId
     * @param data
     * @return
     */
    Material convertId(char combinedId, IInOutParam<Integer> data);
}
