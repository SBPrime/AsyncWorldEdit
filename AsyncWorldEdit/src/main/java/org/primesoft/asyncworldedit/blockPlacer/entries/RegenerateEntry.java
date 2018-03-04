/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.blockPlacer.entries;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.Region;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.inner.IChunkWatch;
import org.primesoft.asyncworldedit.api.utils.IAction;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerEntry;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

/**
 * Regenerate chunk entry
 * @author SBPrime
 */
public class RegenerateEntry extends BlockPlacerEntry {

    private final IWorld m_world;
    private final Vector2D m_chunk;
    private final IChunkWatch m_chunkWatcher;
    private final IAction m_finalize;

    /**
     *
     * @param jobId
     * @param world
     * @param chunk
     * @param finalizeAction
     * @param cw
     */
    public RegenerateEntry(int jobId, IWorld world, Vector2D chunk, 
            IAction finalizeAction, IChunkWatch cw) {
        super(jobId, true);

        m_chunk = chunk;
        m_world = world;
        
        m_chunkWatcher = cw;
        
        m_finalize = finalizeAction;
    }

    @Override
    public boolean process(IBlockPlacer bp) {
        String worldName =  m_world.getName();
        int x = m_chunk.getBlockX();
        int z = m_chunk.getBlockZ();
        
        try {
            m_chunkWatcher.loadChunk(x, z, worldName);
            m_chunkWatcher.setChunkUnloaded(x, z, worldName);
            
            m_world.regenerateChunk(x, z);
            
            m_chunkWatcher.setChunkUnloaded(x, z, worldName);            
            m_chunkWatcher.loadChunk(x, z, worldName);
            m_chunkWatcher.setChunkLoaded(x, z, worldName);

            return true;
            
        } catch (Throwable t) {
            ExceptionHelper.printException(t, "Error while regenerating chunk.");
            
            return false;
        }
        finally{
            if (m_finalize != null) {
                m_finalize.execute();
            }
        }
    }
}
