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
package org.primesoft.asyncworldedit.worldedit.world;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.weather.WeatherType;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.utils.SessionCanceled;
import org.primesoft.asyncworldedit.worldedit.AsyncWrapper;

/**
 *
 * @author SBPrime
 */
public class CancelableWorld extends AbstractWorldWrapper {
    private final int m_jobId;
    private final IPlayerEntry m_player;
    private boolean m_isCanceled;

    public CancelableWorld(World parent, int jobId, IPlayerEntry player) {
        super(parent);
        
        m_isCanceled = false;
        m_jobId = jobId;
        m_player = player;
    }
    
    /**
     * Cancel all further operations
     */
    public void cancel() {
        m_isCanceled = true;
    }

    /**
     * Is world operation canceled
     *
     * @return
     */
    public boolean isCanceled() {
        return m_isCanceled;
    }

    @Override
    public String getName() {
        return m_parent.getName();
    }

    @Override
    public int getMaxY() {
        return m_parent.getMaxY();
    }


    @Override
    public Mask createLiquidMask() {
        return m_parent.createLiquidMask();
    }

    @Override
    public boolean useItem(BlockVector3 position, BaseItem item, Direction face) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.useItem(position, item, face);
    }

    @Override
    public boolean fullySupports3DBiomes() {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.fullySupports3DBiomes();
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(
            final BlockVector3 position,
            final B block,
            final SideEffectSet sideEffectSet) throws WorldEditException {

        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.setBlock(position, block, sideEffectSet);
    }

    @Override
    public boolean setBlock(BlockVector3 position, BlockStateHolder block, boolean notifyAndLight) throws WorldEditException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.setBlock(AsyncWrapper.initialize(position, m_jobId, true, m_player),
                AsyncWrapper.initialize(block, m_jobId, true, m_player), notifyAndLight);
    }

    @Override
    public int getBlockLightLevel(BlockVector3 position) {
        return m_parent.getBlockLightLevel(position);
    }

    @Override
    public boolean clearContainerBlockContents(BlockVector3 position) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.clearContainerBlockContents(AsyncWrapper.initialize(position, m_jobId, true, m_player));
    }

    @Override
    public void dropItem(Vector3 position, BaseItemStack item, int count) {
        m_parent.dropItem(AsyncWrapper.initialize(position, m_jobId, true, m_player), item, count);
    }

    @Override
    public void dropItem(Vector3 position, BaseItemStack item) {
        m_parent.dropItem(AsyncWrapper.initialize(position, m_jobId, true, m_player), item);
    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {
        m_parent.simulateBlockMine(AsyncWrapper.initialize(position, m_jobId, true, m_player));
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.regenerate(region, editSession);
    }

    @Override
    public boolean regenerate(Region region, Extent editSession) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.regenerate(region, editSession);
    }


    @Override
    public boolean regenerate(Region region, Extent editSession, RegenOptions options) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.regenerate(region, editSession, options);
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.generateTree(type, editSession, 
                AsyncWrapper.initialize(position, m_jobId, true, m_player));
    }

    @Override
    public void checkLoadedChunk(BlockVector3 position) {
        m_parent.checkLoadedChunk(position);
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2> chunks) {
        List<BlockVector2> tmp = new ArrayList<>();
        for (Iterator<BlockVector2> it = chunks.iterator(); it.hasNext();) {
            tmp.add(AsyncWrapper.initialize(it.next(), m_jobId, true, m_player));
        }
        m_parent.fixAfterFastMode(tmp);
    }

    @Override
    public void fixLighting(Iterable<BlockVector2> chunks) {
        List<BlockVector2> tmp = new ArrayList<>();
        for (Iterator<BlockVector2> it = chunks.iterator(); it.hasNext();) {
            tmp.add(AsyncWrapper.initialize(it.next(), m_jobId, true, m_player));
        }
        m_parent.fixLighting(tmp);
    }

    @Override
    public boolean playEffect(Vector3 position, int type, int data) {
        return m_parent.playEffect(AsyncWrapper.initialize(position, m_jobId, true, m_player), type, data);
    }

    @Override
    public boolean queueBlockBreakEffect(Platform server, BlockVector3 position, BlockType blockType, double priority) {
        return m_parent.queueBlockBreakEffect(server, AsyncWrapper.initialize(position, m_jobId, true, m_player), blockType, priority);
    }

    @Override
    public WeatherType getWeather() {
        return m_parent.getWeather();
    }

    @Override
    public long getRemainingWeatherDuration() {
        return m_parent.getRemainingWeatherDuration();
    }

    @Override
    public void setWeather(WeatherType weatherType) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        m_parent.setWeather(AsyncWrapper.initialize(weatherType, m_jobId, m_isCanceled, m_player));
    }

    @Override
    public void setWeather(WeatherType weatherType, long duration) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        m_parent.setWeather(weatherType, duration);
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        return m_parent.getMinimumPoint();
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return m_parent.getMaximumPoint();
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return m_parent.getEntities(region);
    }

    @Override
    public List<? extends Entity> getEntities() {
        return m_parent.getEntities();
    }

    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.createEntity(AsyncWrapper.initialize(location, m_jobId, true, m_player),
                AsyncWrapper.initialize(entity, m_jobId, true, m_player));
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.getBlock(position);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.getFullBlock(position);
    }

    @Override
    public BiomeType getBiome(BlockVector2 position) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.getBiome(position);
    }

    @Override
    public BiomeType getBiome(final BlockVector3 position) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.getBiome(position);
    }

    @Override
    public boolean setBlock(BlockVector3 position, BlockStateHolder block) throws WorldEditException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.setBlock(AsyncWrapper.initialize(position, m_jobId, true, m_player),
                AsyncWrapper.initialize(block, m_jobId, true, m_player));
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.setBiome(AsyncWrapper.initialize(position, m_jobId, true, m_player), biome);
    }

    @Override
    public boolean setBiome(final BlockVector3 position, final BiomeType biome) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.setBiome(AsyncWrapper.initialize(position, m_jobId, true, m_player), biome);
    }

    @Override
    public Operation commit() {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.commit();
    }

    @Override
    public boolean notifyAndLightBlock(BlockVector3 bv, BlockState bs) throws WorldEditException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.notifyAndLightBlock(bv, bs);
    }

    @Override
    public Set<SideEffect> applySideEffects(final BlockVector3 blockVector3, final BlockState blockState, final SideEffectSet sideEffectSet) throws WorldEditException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.applySideEffects(blockVector3, blockState, sideEffectSet);
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.getSpawnPosition();
    }

    @Override
    public Path getStoragePath() {
        return m_parent.getStoragePath();
    }

    @Override
    public int getMinY() {
        return m_parent.getMinY();
    }

    @Override
    public String getId() {
        return m_parent.getId();
    }
}
