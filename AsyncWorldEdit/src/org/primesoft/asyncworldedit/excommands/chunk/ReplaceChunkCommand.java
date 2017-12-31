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

import org.primesoft.asyncworldedit.api.worldedit.IAweEditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.directChunk.IChangesetChunkData;
import org.primesoft.asyncworldedit.api.directChunk.IWrappedChunk;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.map.IMapUtils;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.directChunk.ChangesetChunkExtent;
import org.primesoft.asyncworldedit.directChunk.DcUtils;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.utils.MaskUtils;
import org.primesoft.asyncworldedit.utils.PositionHelper;
import org.primesoft.asyncworldedit.worldedit.extent.MultiThreadExtent;

/**
 *
 * @author SBPrime
 */
public class ReplaceChunkCommand extends DCMaskCommand {

    private final Region m_region;
    private final boolean m_wholeWorld;

    private final Mask m_from;
    private final Pattern m_to;

    private final IMapUtils m_mapUtils;

    private final MultiThreadExtent m_mtExtent;

    public ReplaceChunkCommand(Region region, boolean wholeWorld,
            Mask from, Pattern to, IAsyncWorldEditCore awe,
            Mask destinationMask, IPlayerEntry playerEntry) {

        super(awe, destinationMask, playerEntry);

        m_from = from == null ? Masks.alwaysTrue() : from;
        m_to = to;
        m_region = region;
        m_wholeWorld = wholeWorld;

        m_mapUtils = awe.getMapUtils();

        m_mtExtent = new MultiThreadExtent();

        Extent orgExtent = MaskUtils.injectExtent(awe.getPlatform().getClasScanner(), from, m_mtExtent);
        m_mtExtent.setDefault(orgExtent);
    }

    @Override
    public String getName() {
        return "chunkReplace";
    }

    @Override
    public Integer task(IAweEditSession editSesstion) throws WorldEditException {
        //Get the chunks in chunk coords
        final World weWorld = m_region.getWorld();
        final IWorld world = m_weIntegrator.getWorld(weWorld);
        final IPlayerEntry player = getPlayer();

        if (world == null) {
            return 0;
        }

        final Vector2D[] chunks = m_wholeWorld
                ? m_mapUtils.getAllWorldChunks(world) : m_region.getChunks().toArray(new Vector2D[0]);

        int changedBlocks = 0;

        int cnt = chunks.length;
        int idx = 0;        
        
        player.say(MessageType.EX_CMD_CHUNK_REPLACE_CHUNKS.format(cnt));
        for (Vector2D cPos : chunks) {
            idx++;
            if (m_wholeWorld) {
                player.say(MessageType.EX_CMD_CHUNK_REPLACE_PROGRESS.format(idx, cnt));
            }
            final IWrappedChunk wChunk = DcUtils.wrapChunk(m_taskDispatcher, m_chunkApi,
                    weWorld, world, getPlayer(), cPos);

            final IChangesetChunkData cData = m_chunkApi.createLazyChunkData(wChunk);
            final ChangesetChunkExtent extent = new ChangesetChunkExtent(cData);
            final Vector chunkZero = PositionHelper.chunkToPosition(cPos, 0);

            m_mtExtent.setExtent(extent);
            maskSetExtent(extent);

            for (int x = 0; x < 16; x++) {
                final Vector xPos = chunkZero.add(x, 0, 0);
                for (int z = 0; z < 16; z++) {
                    final Vector zPos = xPos.add(0, 0, z);
                    for (int py = 0; py < 256; py++) {
                        final Vector yPos = zPos.add(0, py, 0);

                        if ((m_wholeWorld || m_region.contains(yPos))
                                && m_from.test(yPos) && maskTest(yPos)) {
                            final BaseBlock block = m_to.apply(yPos);
                            cData.setBlock(x, py, z, block);
                            changedBlocks++;
                        }
                    }
                }
            }

            m_mtExtent.setExtent(null);
            maskSetExtent(null);

            editSesstion.doCustomAction(new SetChangesetChunkChange(wChunk, cData), false);
        }

        return changedBlocks;
    }
}
