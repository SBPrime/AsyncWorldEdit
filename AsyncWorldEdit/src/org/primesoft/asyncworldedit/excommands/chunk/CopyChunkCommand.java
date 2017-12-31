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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import org.primesoft.asyncworldedit.api.worldedit.IAweEditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BaseBiome;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.directChunk.IChunkData;
import org.primesoft.asyncworldedit.api.directChunk.ISerializedEntity;
import org.primesoft.asyncworldedit.api.directChunk.ISerializedTileEntity;
import org.primesoft.asyncworldedit.api.directChunk.IWrappedChunk;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.directChunk.ChangesetChunkExtent;
import org.primesoft.asyncworldedit.directChunk.DcUtils;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.utils.MaskUtils;
import org.primesoft.asyncworldedit.utils.PositionHelper;
import org.primesoft.asyncworldedit.worldedit.extent.MultiThreadExtent;

/**
 *
 * @author SBPrime
 */
public class CopyChunkCommand extends DCCommand {

    private final Clipboard m_target;
    private final Region m_region;
    private final Mask m_sourceMask;
    private final MultiThreadExtent m_sourceMaskExtent;
    private final World m_world;
    private final boolean m_copyBiome;

    public CopyChunkCommand(Clipboard clipboard, Region region, IAsyncWorldEditCore awe,
            boolean copyBiome,
            Mask mask, IPlayerEntry playerEntry) {
        super(awe, playerEntry);
        if (mask == null) {
            mask = Masks.alwaysTrue();
        }

        m_target = clipboard;
        m_region = region.clone();
        m_world = m_region.getWorld();

        m_sourceMask = mask;
        m_sourceMaskExtent = new MultiThreadExtent();

        m_copyBiome = copyBiome;

        Extent orgExtent = MaskUtils.injectExtent(awe.getPlatform().getClasScanner(), m_sourceMask, m_sourceMaskExtent);
        m_sourceMaskExtent.setDefault(orgExtent);
    }

    @Override
    public String getName() {
        return "chunkCopy";
    }

    @Override
    public Integer task(IAweEditSession editSesstion) throws WorldEditException {
        final IWorld world = m_weIntegrator.getWorld(m_world);
        final Vector min = m_region.getMinimumPoint();
        final Vector max = m_region.getMaximumPoint();

        int cMaxX = PositionHelper.positionToChunk(max.getX());
        int cMaxZ = PositionHelper.positionToChunk(max.getZ());

        int cMinX = PositionHelper.positionToChunk(min.getX());
        int cMinZ = PositionHelper.positionToChunk(min.getZ());

        int changedBlocks = 0;

        final HashMap<Vector2D, LazyData<IChunkData>> dataCache
                = cacheChunks(cMinX, cMaxX, cMinZ, cMaxZ, world, editSesstion);

        for (Map.Entry<Vector2D, LazyData<IChunkData>> entrySet : dataCache.entrySet()) {
            Vector2D cPos = entrySet.getKey();
            LazyData<IChunkData> lcData = entrySet.getValue();

            final IChunkData cData = lcData.get();
            final ChangesetChunkExtent extent = new ChangesetChunkExtent(cData);
            m_sourceMaskExtent.setExtent(extent);

            changedBlocks += getBlocks(cPos, cData);
            changedBlocks += getEntities(cPos, cData);

            m_sourceMaskExtent.setExtent(null);
        }

        return changedBlocks;
    }

    /**
     * Get entities from DirectChunk data and store them into the clipboard
     *
     * @param cPos
     * @param cData
     * @return
     */
    private int getEntities(Vector2D cPos, IChunkData cData) {
        int changedBlocks = 0;
        final Vector chunkZero = PositionHelper.chunkToPosition(cPos, 0);

        for (ISerializedEntity entity : cData.getEntity()) {
            Stack<ISerializedEntity> stack = new Stack<ISerializedEntity>();
            do {
                stack.push(entity);
                entity = entity.getVehicle();
            } while (entity != null);

            CompoundTag vehicle;
            CompoundTag tagData = null;
            while (!stack.isEmpty()) {
                vehicle = tagData;
                entity = stack.pop();

                Vector pos = chunkZero.add(entity.getPosition());
                Vector posBlock = pos.toBlockPoint();
                if (!m_region.contains(posBlock) || !m_sourceMask.test(posBlock)) {
                    break;
                }

                tagData = entity.getRawData(cPos.getBlockX(), cPos.getBlockZ(), UUID.randomUUID());
                if (!tagData.containsKey("id")) {
                    log("The NBD data does not contain an ID entry");
                    break;
                }

                if (vehicle != null) {
                    Map<String, Tag> nbt = new HashMap<String, Tag>(tagData.getValue());
                    nbt.put("Riding", vehicle);
                    tagData = new CompoundTag(nbt);
                }

                m_target.createEntity(new Location(m_world, pos, entity.getYaw(), entity.getPitch()), new BaseEntity(tagData.getString("id"), tagData));
            }
        }

        return changedBlocks;
    }

    /**
     * Get blocks from DirectChunk data and store them into the clipboard
     *
     * @param cPos
     * @param cData
     * @return
     */
    private int getBlocks(Vector2D cPos, IChunkData cData) {
        final Vector chunkZero = PositionHelper.chunkToPosition(cPos, 0);
        int changedBlocks = 0;

        for (int x = 0; x < 16; x++) {
            final Vector xPos = chunkZero.add(x, 0, 0);
            for (int z = 0; z < 16; z++) {
                final Vector zPos = xPos.add(0, 0, z);
                final Vector2D zPos2d = zPos.toVector2D();

                if (m_copyBiome) {
                    m_target.setBiome(zPos2d, new BaseBiome(cData.getBiome(x, z)));
                }

                for (int py = 0; py < 256; py++) {
                    final Vector yPos = zPos.add(0, py, 0);

                    if (!m_region.contains(yPos) || !m_sourceMask.test(yPos)) {
                        continue;
                    }

                    InOutParam<ISerializedTileEntity> entity = InOutParam.Out();

                    char combinedId = cData.getBlock(x, py, z, entity);
                    BaseBlock block = m_chunkApi.convertId(combinedId);

                    if (entity.isSet()) {
                        block.setNbtData(entity.getValue().getRawData(cPos.getBlockX(), cPos.getBlockZ()));
                    }

                    try {
                        if (m_target.setBlock(yPos, block)) {
                            changedBlocks++;
                        }
                    } catch (WorldEditException ex) {
                        //This is never thrown but better log it
                        ExceptionHelper.printException(ex, "Unable to set clipboard block");
                    }
                }
            }
        }

        return changedBlocks;
    }

    private HashMap<Vector2D, LazyData<IChunkData>> cacheChunks(int cMinX, int cMaxX, int cMinZ, int cMaxZ,
            final IWorld world, IAweEditSession editSesstion) throws WorldEditException {
        HashMap<Vector2D, LazyData<IChunkData>> dataCatch
                = new HashMap<Vector2D, LazyData<IChunkData>>();
        for (int cx = cMinX; cx <= cMaxX; cx++) {
            for (int cz = cMinZ; cz <= cMaxZ; cz++) {
                final Vector2D cPos = new Vector2D(cx, cz);

                final IWrappedChunk chunk = DcUtils.wrapChunk(m_taskDispatcher, m_chunkApi,
                        m_world, world, getPlayer(), cPos);

                final LazyData<IChunkData> data = new LazyData<IChunkData>();
                editSesstion.doCustomAction(new GetChunkData(chunk, data), false);

                dataCatch.put(cPos, data);
            }
        }

        return dataCatch;
    }
}
