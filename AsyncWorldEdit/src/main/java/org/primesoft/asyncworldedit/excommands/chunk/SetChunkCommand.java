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

import com.sk89q.worldedit.BlockVector2D;
import org.primesoft.asyncworldedit.api.worldedit.IAweEditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.directChunk.IChangesetChunkData;
import org.primesoft.asyncworldedit.api.directChunk.IWrappedChunk;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.directChunk.ChangesetChunkExtent;
import org.primesoft.asyncworldedit.directChunk.DcUtils;
import org.primesoft.asyncworldedit.utils.PositionHelper;

/**
 *
 * @author SBPrime
 */
public class SetChunkCommand extends DCMaskCommand {

    private final Region m_region;
    private final Pattern m_pattern;    
    private final boolean m_fillChunk;

    public SetChunkCommand(Region region, boolean fillChunk,
            Pattern pattern, IAsyncWorldEditCore awe,
            Mask destinationMask, IPlayerEntry playerEntry) {
        super(awe, destinationMask, playerEntry);

        m_pattern = pattern;
        m_region = region;
        m_fillChunk = fillChunk;
    }

    @Override
    public String getName() {
        return "chunkSet";
    }

    @Override
    public Integer task(IAweEditSession editSesstion) throws WorldEditException {
        //Get the chunks in chunk coords
        final Set<Vector2D> chunks = m_region.getChunks();
        final World weWorld = m_region.getWorld();
        final IWorld world = m_weIntegrator.getWorld(weWorld);

        if (world == null) {
            return 0;
        }

        int changedBlocks = 0;

        final List<IWrappedChunk> dataCache = cacheChunks(chunks, weWorld, world);

        for (IWrappedChunk wChunk : dataCache) {
            final IChangesetChunkData cData = m_chunkApi.createLazyChunkData(wChunk);
            final ChangesetChunkExtent extent = new ChangesetChunkExtent(cData);
            maskSetExtent(extent);

            final Vector2D cPos = new BlockVector2D(wChunk.getX(), wChunk.getZ());
            final Vector chunkZero = PositionHelper.chunkToPosition(cPos, 0);
            for (int x = 0; x < 16; x++) {
                final Vector xPos = chunkZero.add(x, 0, 0);
                for (int z = 0; z < 16; z++) {
                    final Vector zPos = xPos.add(0, 0, z);
                    for (int py = 0; py < 256; py++) {
                        final Vector yPos = zPos.add(0, py, 0);

                        if (maskTest(yPos) && (m_fillChunk || m_region.contains(yPos))) {
                            final BaseBlock block = m_pattern.apply(yPos);
                            cData.setBlock(x, py, z, block);
                            changedBlocks++;
                        }
                    }
                }
            }
            maskSetExtent(null);

            editSesstion.doCustomAction(new SetChangesetChunkChange(wChunk, cData), false);
        }

        return changedBlocks;
    }

    /**
     * Catche the chunks
     *
     * @param world
     * @return
     * @throws WorldEditException
     */
    private List<IWrappedChunk> cacheChunks(Set<Vector2D> chunks,
            final World weWorld, final IWorld world) throws WorldEditException {
        List<IWrappedChunk> dataCatch = new ArrayList<IWrappedChunk>();
        for (Vector2D cPos : chunks) {
            final IWrappedChunk chunk = DcUtils.wrapChunk(m_taskDispatcher, m_chunkApi,
                    weWorld, world, getPlayer(), cPos);

            dataCatch.add(chunk);
        }

        return dataCatch;
    }
}
