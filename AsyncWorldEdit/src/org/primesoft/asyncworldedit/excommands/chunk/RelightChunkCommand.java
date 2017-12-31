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
package org.primesoft.asyncworldedit.excommands.chunk;

import com.sk89q.worldedit.Vector;
import org.primesoft.asyncworldedit.api.worldedit.IAweEditSession;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import java.util.Set;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.directChunk.IWrappedChunk;
import org.primesoft.asyncworldedit.api.inner.IBlockRelighter;
import org.primesoft.asyncworldedit.api.inner.IInnerDirectChunkAPI;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.directChunk.DcUtils;
import org.primesoft.asyncworldedit.utils.PositionHelper;

/**
 *
 * @author SBPrime
 */
class RelightChunkCommand extends DCCommand {

    private final Region m_region;

    private final boolean m_vanillaRelight;

    RelightChunkCommand(Region region, IAsyncWorldEdit awe, IPlayerEntry playerEntry, boolean vanilla) {
        super(awe, playerEntry);
        m_region = region;
        m_vanillaRelight = vanilla;
    }

    @Override
    public String getName() {
        return "chunkRelight";
    }

    @Override
    public Integer task(IAweEditSession editSesstion) throws WorldEditException {
        //Get the chunks in chunk coords
        final World weWorld = m_region.getWorld();
        final IWorld world = m_weIntegrator.getWorld(weWorld);

        if (m_vanillaRelight) {
            return relightVanilla(m_region.getChunks(), weWorld, world, editSesstion);
        }

        return relightAsync(m_region, world);
    }

    private int relightAsync(final Region region, final IWorld world) {
        IBlockRelighter relighter = ((IInnerDirectChunkAPI) m_chunkApi).getBlockRelighter();

        int cnt = 0;
        for (Vector2D chunk : region.getChunks()) {
            synchronized (relighter.getDataMutex()) {
                double cx = PositionHelper.chunkToPosition(chunk.getBlockX());
                double cz = PositionHelper.chunkToPosition(chunk.getBlockZ());
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < 256; y++) {
                            int px = (int) (x + cx);
                            int pz = (int) (z + cz);
                            Vector v = new Vector(px, y, pz);
                            if (region.contains(v)) {
                                relighter.forceQueueBlock(world, px, y, pz);
                                cnt++;
                            }
                        }
                    }
                }
            }
        }

        return cnt;
    }

    private int relightVanilla(final Set<Vector2D> chunks, final World weWorld, final IWorld world, IAweEditSession editSesstion) throws WorldEditException {
        for (Vector2D pos : chunks) {
            final IWrappedChunk wChunk = DcUtils.wrapChunk(m_taskDispatcher, m_chunkApi, weWorld, world, getPlayer(), pos);

            relightChunk(editSesstion, wChunk);

            editSesstion.doCustomAction(new ChunkFlushChange(wChunk), true);
        }

        return chunks.size() * DcUtils.CHUNK_SIZE;
    }

    /**
     * Relight the chunk
     *
     * @param editSesstion
     * @param chunk
     * @throws com.sk89q.worldedit.WorldEditException
     */
    public static void relightChunk(final IAweEditSession editSesstion, final IWrappedChunk chunk) throws WorldEditException {
        editSesstion.doCustomAction(new RelightChange(chunk), false);
        for (int y = 0; y < 256; y++) {
            editSesstion.doCustomAction(new RelightChange(chunk, y), false);
        }
    }
}
