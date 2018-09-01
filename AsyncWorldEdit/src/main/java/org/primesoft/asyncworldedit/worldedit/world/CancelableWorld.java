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

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.weather.WeatherType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.utils.SessionCanceled;
import org.primesoft.asyncworldedit.worldedit.BlockVector2DWrapper;
import org.primesoft.asyncworldedit.worldedit.Vector2DWrapper;
import org.primesoft.asyncworldedit.worldedit.VectorWrapper;
import org.primesoft.asyncworldedit.worldedit.blocks.BlockStateHolderWrapper;
import org.primesoft.asyncworldedit.worldedit.entity.BaseEntityWrapper;
import org.primesoft.asyncworldedit.worldedit.util.LocationWrapper;
import org.primesoft.asyncworldedit.worldedit.world.weather.WeatherTypeWrapper;

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
    public boolean useItem(Vector position, BaseItem item, Direction face) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.useItem(position, item, face);
    }

    @Override
    public boolean setBlock(Vector position, BlockStateHolder block, boolean notifyAndLight) throws WorldEditException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.setBlock(VectorWrapper.wrap(position, m_jobId, true, m_player),
                BlockStateHolderWrapper.wrap(block, m_jobId, true, m_player), notifyAndLight);
    }

    @Override
    public int getBlockLightLevel(Vector position) {
        return m_parent.getBlockLightLevel(position);
    }

    @Override
    public boolean clearContainerBlockContents(Vector position) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.clearContainerBlockContents(VectorWrapper.wrap(position, m_jobId, true, m_player));
    }

    @Override
    public void dropItem(Vector position, BaseItemStack item, int count) {
        m_parent.dropItem(VectorWrapper.wrap(position, m_jobId, true, m_player), item, count);
    }

    @Override
    public void dropItem(Vector position, BaseItemStack item) {
        m_parent.dropItem(VectorWrapper.wrap(position, m_jobId, true, m_player), item);
    }

    @Override
    public void simulateBlockMine(Vector position) {
        m_parent.simulateBlockMine(VectorWrapper.wrap(position, m_jobId, true, m_player));
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.regenerate(region, editSession);
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, Vector position) throws MaxChangedBlocksException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.generateTree(type, editSession, 
                VectorWrapper.wrap(position, m_jobId, true, m_player));
    }

    @Override
    public void checkLoadedChunk(Vector position) {
        m_parent.checkLoadedChunk(position);
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2D> chunks) {
        List<BlockVector2D> tmp = new ArrayList<>();
        for (Iterator<BlockVector2D> it = chunks.iterator(); it.hasNext();) {
            tmp.add(BlockVector2DWrapper.wrap(it.next(), m_jobId, true, m_player));
        }
        m_parent.fixAfterFastMode(tmp);
    }

    @Override
    public void fixLighting(Iterable<BlockVector2D> chunks) {
        List<BlockVector2D> tmp = new ArrayList<>();
        for (Iterator<BlockVector2D> it = chunks.iterator(); it.hasNext();) {
            tmp.add(BlockVector2DWrapper.wrap(it.next(), m_jobId, true, m_player));
        }
        m_parent.fixLighting(tmp);
    }

    @Override
    public boolean playEffect(Vector position, int type, int data) {
        return m_parent.playEffect(VectorWrapper.wrap(position, m_jobId, true, m_player), type, data);
    }

    @Override
    public boolean queueBlockBreakEffect(Platform server, Vector position, BlockType blockType, double priority) {
        return m_parent.queueBlockBreakEffect(server, VectorWrapper.wrap(position, m_jobId, true, m_player), blockType, priority);
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
        
        m_parent.setWeather(WeatherTypeWrapper.wrap(weatherType, m_jobId, m_isCanceled, m_player));
    }

    @Override
    public void setWeather(WeatherType weatherType, long duration) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        m_parent.setWeather(weatherType, duration);
    }

    @Override
    public Vector getMinimumPoint() {
        return m_parent.getMinimumPoint();
    }

    @Override
    public Vector getMaximumPoint() {
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

        return m_parent.createEntity(LocationWrapper.wrap(location, m_jobId, true, m_player),
                BaseEntityWrapper.wrap(entity, m_jobId, true, m_player));
    }

    @Override
    public BlockState getBlock(Vector position) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.getBlock(position);
    }

    @Override
    public BaseBlock getFullBlock(Vector position) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.getFullBlock(position);
    }

    @Override
    public BaseBiome getBiome(Vector2D position) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.getBiome(position);
    }

    @Override
    public boolean setBlock(Vector position, BlockStateHolder block) throws WorldEditException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.setBlock(VectorWrapper.wrap(position, m_jobId, true, m_player),
                BlockStateHolderWrapper.wrap(block, m_jobId, true, m_player));
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.setBiome(Vector2DWrapper.wrap(position, m_jobId, true, m_player), biome);
    }

    @Override
    public Operation commit() {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.commit();
    }

}
