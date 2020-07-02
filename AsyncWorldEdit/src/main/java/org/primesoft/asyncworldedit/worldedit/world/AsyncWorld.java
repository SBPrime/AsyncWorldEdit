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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Sets;
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
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.weather.WeatherType;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.inner.IBlocksHubIntegration;
import org.primesoft.asyncworldedit.api.inner.IChunkWatch;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.api.utils.IAction;
import org.primesoft.asyncworldedit.api.utils.IFunc;
import org.primesoft.asyncworldedit.api.utils.IFuncEx;
import org.primesoft.asyncworldedit.blockPlacer.entries.ActionEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.RegenerateEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.WorldActionEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.WorldFuncEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.WorldFuncEntryEx;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.configuration.WorldeditOperations;
import org.primesoft.asyncworldedit.core.AwePlatform;
import org.primesoft.asyncworldedit.platform.api.IMaterial;
import org.primesoft.asyncworldedit.platform.api.IScheduler;
import org.primesoft.asyncworldedit.utils.MutexProvider;
import org.primesoft.asyncworldedit.utils.PositionHelper;
import org.primesoft.asyncworldedit.utils.SchedulerUtils;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;
import org.primesoft.asyncworldedit.worldedit.WorldAsyncTask;
import org.primesoft.asyncworldedit.worldedit.blocks.BlockStates;
import org.primesoft.asyncworldedit.worldedit.entity.EntityLazyWrapper;
import org.primesoft.asyncworldedit.worldedit.regions.ChunkBaseRegionIterator;

import static org.primesoft.asyncworldedit.utils.PositionHelper.positionToChunk;

/**
 *
 * @author SBPrime
 */
public class AsyncWorld extends AbstractWorldWrapper {

    /**
     * Wrap the world (if needed)
     *
     */
    public static AsyncWorld wrap(World world, IPlayerEntry player) {
        if (world == null) {
            return null;
        }

        if (world instanceof AsyncWorld) {
            return (AsyncWorld) world;
        }

        return new AsyncWorld(world, player);
    }

    /**
     * Bukkit schedule
     */
    private final IScheduler m_schedule;

    /**
     * The player
     */
    private final IPlayerEntry m_player;

    /**
     * The bukkit world
     */
    private final IWorld m_bukkitWorld;

    /**
     * The block placer
     */
    private final IBlockPlacer m_blockPlacer;

    /**
     * The dispather
     */
    private final ITaskDispatcher m_dispatcher;

    /**
     * The blocks hub
     */
    private final IBlocksHubIntegration m_blocksHub;

    /**
     * Reference to chunk watcher
     */
    private final IChunkWatch m_chunkWatcher;

    private final static int CACHE_SIZE = 5000;

    private final Map<BlockVector3, BlockCacheEntry> m_blockCache = new LruMap<>(CACHE_SIZE);

    public AsyncWorld(World world, IPlayerEntry player) {
        super(world);

        AwePlatform awePlatform = AwePlatform.getInstance();
        IAsyncWorldEditCore aweCore = awePlatform.getCore();

        m_player = player;
        m_schedule = awePlatform.getPlatform().getScheduler();
        m_blockPlacer = aweCore.getBlockPlacer();
        m_dispatcher = aweCore.getTaskDispatcher();
        m_blocksHub = aweCore.getBlocksHubBridge();
        m_bukkitWorld = aweCore.getWorldEditIntegrator().getWorld(world);
        m_chunkWatcher = aweCore.getChunkWatch();
    }

    /**
     * Get next job id for current player
     *
     * @return Job id
     */
    private int getJobId() {
        return m_blockPlacer.getJobId(m_player);
    }

    /**
     * Decide on the player UUID
     *
     */
    private IPlayerEntry getPlayer(BaseAsyncParams... asyncParams) {
        IPlayerEntry result = m_player;

        for (BaseAsyncParams param : asyncParams) {
            if (!param.isEmpty()) {
                IPlayerEntry player = param.getPlayer();
                if (player != null && player.isPlayer()) {
                    result = player;
                }
            }
        }
        return result;
    }

    /**
     * This function checks if async mode is enabled for specific command
     */
    private boolean checkAsync(WorldeditOperations operation) {
        return ConfigProvider.isAsyncAllowed(operation) && m_player.getAweMode();
    }

    private boolean canPlace(IPlayerEntry player, IWorld world, BlockVector3 location,
            BlockStateHolder oldBlock, BlockStateHolder newBlock) {
        return m_blocksHub.canPlace(player, world, location, oldBlock, newBlock);
    }

    private boolean isSame(BlockStateHolder oldBlock, BlockStateHolder newBlock) {
        if (oldBlock == null && newBlock == null) {
            return true;
        }
        if ((oldBlock == null) || !oldBlock.equalsFuzzy(newBlock)) {
            return false;
        }

        return !isTileEntity(oldBlock.getBlockType());
    }

    /**
     * Log placed block using blocks hub
     */
    private void logBlock(BlockVector3 location, IPlayerEntry player,
            BlockStateHolder oldBlock, BlockStateHolder newBlock) {
        m_blocksHub.logBlock(player, m_bukkitWorld, location, oldBlock, newBlock, false);
    }

    @Override
    public String getName() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()),
                m_parent::getName);
    }

    @Override
    public int getMaxY() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()),
                m_parent::getMaxY);
    }

    @Override
    public Mask createLiquidMask() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()),
                m_parent::createLiquidMask);
    }

    @Override
    public boolean useItem(BlockVector3 position, BaseItem item, Direction face) {
        final DataAsyncParams<BlockVector3> paramVector = DataAsyncParams.extract(position);
        final BlockVector3 v = paramVector.getData();
        final IPlayerEntry player = getPlayer(paramVector);

        IFunc<Boolean> func = () -> m_parent.useItem(position, item, face);

        if (paramVector.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this.getName(), paramVector.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public boolean fullySupports3DBiomes() {
        return m_parent.fullySupports3DBiomes();
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(
            final BlockVector3 position,
            final B block,
            final SideEffectSet sideEffectSet) throws WorldEditException {

        final DataAsyncParams<B> paramBlock = DataAsyncParams.extract(block);
        final DataAsyncParams<BlockVector3> paramVector = DataAsyncParams.extract(position);

        final B newBlock = paramBlock.getData();
        final BlockVector3 v = paramVector.getData();
        final IPlayerEntry player = getPlayer(paramBlock, paramVector);

        IFuncEx<Boolean, WorldEditException> func = () -> {
            final BlockStateHolder oldBlock = m_parent.getBlock(v);
            if (!canPlace(player, m_bukkitWorld, v, oldBlock, newBlock)
                    || isSame(oldBlock, newBlock)) {
                return false;
            }

            final boolean result = m_parent.setBlock(v, newBlock, sideEffectSet);
            if (result) {
                logBlock(v, player, oldBlock, newBlock);
            }

            return result;
        };

        if (paramBlock.isAsync() || paramVector.isAsync() || !m_dispatcher.isMainTask()) {
            if (!canPlace(player, m_bukkitWorld, position, getBlock(v), newBlock)) {
                return false;
            }

            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntryEx(this.getName(), paramBlock.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public boolean setBlock(
            final BlockVector3 position,
            final BlockStateHolder block,
            final boolean notifyAndLight) throws WorldEditException {

        final DataAsyncParams<BlockStateHolder> paramBlock = DataAsyncParams.extract(block);
        final DataAsyncParams<BlockVector3> paramVector = DataAsyncParams.extract(position);

        final BlockStateHolder newBlock = paramBlock.getData();
        final BlockVector3 v = paramVector.getData();
        final IPlayerEntry player = getPlayer(paramBlock, paramVector);

        IFuncEx<Boolean, WorldEditException> func = () -> {
            final BlockStateHolder oldBlock = m_parent.getBlock(v);
            if (!canPlace(player, m_bukkitWorld, v, oldBlock, newBlock)
                    || isSame(oldBlock, newBlock)) {
                return false;
            }

            final boolean result = m_parent.setBlock(v, newBlock, notifyAndLight);
            if (result) {
                logBlock(v, player, oldBlock, newBlock);
            }

            return result;
        };

        if (paramBlock.isAsync() || paramVector.isAsync() || !m_dispatcher.isMainTask()) {
            if (!canPlace(player, m_bukkitWorld, position, getBlock(v), newBlock)) {
                return false;
            }

            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntryEx(this.getName(), paramBlock.getJobId(), v, func));
        }
        
        return func.execute();
    }

    @Override
    public int getBlockLightLevel(final BlockVector3 position) {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()),
                () -> m_parent.getBlockLightLevel(position), m_bukkitWorld, position);
    }

    @Override
    public boolean clearContainerBlockContents(final BlockVector3 position) {
        final DataAsyncParams<BlockVector3> param = DataAsyncParams.extract(position);
        final BlockVector3 v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return false;
        }

        IFunc<Boolean> func = () -> m_parent.clearContainerBlockContents(position);

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this.getName(), param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public void dropItem(Vector3 position, final BaseItemStack item, final int count) {
        final DataAsyncParams<Vector3> param = DataAsyncParams.extract(position);
        final Vector3 v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return;
        }

        IAction func = () -> m_parent.dropItem(v, item, count);

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            m_blockPlacer.addTasks(player,
                    new WorldActionEntry(this.getName(), param.getJobId(), v, func));
            return;
        }

        func.execute();
    }

    @Override
    public void dropItem(Vector3 position, final BaseItemStack item) {
        final DataAsyncParams<Vector3> param = DataAsyncParams.extract(position);
        final Vector3 v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return;
        }

        IAction func = () -> m_parent.dropItem(v, item);

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            m_blockPlacer.addTasks(player,
                    new WorldActionEntry(this.getName(), param.getJobId(), v, func));
            return;
        }

        func.execute();
    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {
        final DataAsyncParams<BlockVector3> param = DataAsyncParams.extract(position);
        final BlockVector3 v = param.getData();
        final IPlayerEntry player = getPlayer(param);
        
        IAction func = () -> {
            BlockState oldBlock = m_parent.getBlock(v);
            if (!canPlace(player, m_bukkitWorld, v, oldBlock, BlockStates.AIR) || isSame(oldBlock, BlockStates.AIR)) {
                return;
            }
            m_parent.simulateBlockMine(v);
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            if (!canPlace(player, m_bukkitWorld, v, getBlock(v), BlockStates.AIR)) {
                return;
            }

            m_blockPlacer.addTasks(player,
                    new WorldActionEntry(this.getName(), param.getJobId(), v, func));
            return;
        }

        func.execute();
    }

    @Override
    public boolean regenerate(final Region region, EditSession editSession) {        
        if (editSession instanceof CancelabeEditSession) {
            CancelabeEditSession ces = (CancelabeEditSession)editSession;
            doRegen(editSession, region, m_bukkitWorld, ces.getJobId());
            return true;
        }
        
        boolean isAsync = checkAsync(WorldeditOperations.regenerate);
        if (!isAsync) {
            return m_parent.regenerate(region, editSession);
        }

        final int jobId = getJobId();
        final EditSession session;
        final JobEntry job;
        
        if (editSession instanceof AsyncEditSession) {
            AsyncEditSession aSession = (AsyncEditSession) editSession;
            session = new CancelabeEditSession(aSession, aSession.getMask(), jobId);
            job = new JobEntry(m_player, (CancelabeEditSession) session, jobId, "regenerate");
        } else {
            session = editSession;
            job = new JobEntry(m_player, jobId, "regenerate");
        }

        m_blockPlacer.addJob(m_player, job);
        
        SchedulerUtils.runTaskAsynchronously(m_schedule, new WorldAsyncTask(m_bukkitWorld, session,
                m_player, "regenerate", m_blockPlacer, job) {
            @Override
            public void task(EditSession editSession, IWorld world) throws MaxChangedBlocksException {
                doRegen(editSession, region, world, jobId);
            }

        });

        return true;
    }

    /**
     * Perform the regen operation
     *
     */
    private void doRegen(EditSession eSession, Region region, IWorld world, int jobId) {        
        int yMin = region.getMinimumPoint().getBlockY();
        int ySize = region.getHeight();

        final Object wait = new Object();
        final IAction finalizeAction = () -> {
            synchronized (wait) {
                wait.notifyAll();
            }
        };
        
        
        for (BlockVector2 chunk : region.getChunks()) {
            final BlockVector3 min = PositionHelper.chunkToPosition(chunk, yMin);
            final boolean[] isInRegion = new boolean[16 * 16 * ySize];

            // First save all the blocks inside
            int index = 0;                    
            for (int y = 0; y < ySize; ++y) {
                for (int x = 0; x < 16; ++x) {
                    for (int z = 0; z < 16; ++z) {
                        BlockVector3 pt = min.add(x, y, z);                        
                        isInRegion[index] = region.contains(pt);
                        index++;
                    }
                }
            }

            Region cRegion = new CuboidRegion(min, min.add(15, ySize - 1, 15)) {
                @Override
                public Iterator<BlockVector3> iterator() {
                    return new ChunkBaseRegionIterator(this);
                }
                
                
                @Override
                public boolean contains(BlockVector3 position) {
                    if (!super.contains(position)) {
                        return false;
                    }
                    
                    int x = position.getX() - min.getX();
                    int y = position.getY() - min.getY();
                    int z = position.getZ() - min.getZ();
                    
                    int index = (y << 8) | (x << 4) | z;
                    return isInRegion[index];
                }
            };
            m_blockPlacer.addTasks(m_player, new RegenerateEntry(jobId, getWorld(), cRegion,
                    finalizeAction, eSession));

            synchronized (wait) {
                try {
                    wait.wait();
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    @Override
    public boolean generateTree(final TreeGenerator.TreeType type, final EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException {
        final DataAsyncParams<BlockVector3> param = DataAsyncParams.extract(position);
        final BlockVector3 v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return false;
        }

        IFuncEx<Boolean, MaxChangedBlocksException> func = () -> m_parent.generateTree(type, editSession, v);

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntryEx(this.getName(), param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public void checkLoadedChunk(final BlockVector3 position) {
        if (m_chunkWatcher.isChunkLoaded(positionToChunk((int) position.getX()), positionToChunk((int) position.getZ()), getName())) {
            return;
        }

        m_dispatcher.performSafeChunk(MutexProvider.getMutex(getWorld()), () -> m_parent.checkLoadedChunk(position),
                m_bukkitWorld, PositionHelper.positionToChunk(position));
    }

    @Override
    public void fixAfterFastMode(final Iterable<BlockVector2> chunks) {
        final Collection<BlockVector2> tmp = new ArrayList<>();
        for (Iterator<BlockVector2> iterator = chunks.iterator(); iterator.hasNext();) {
            tmp.add(iterator.next());
        }

        m_dispatcher.performSafeChunk(MutexProvider.getMutex(getWorld()), () -> m_parent.fixAfterFastMode(tmp), m_bukkitWorld, tmp);
    }

    @Override
    public void fixLighting(final Iterable<BlockVector2> chunks) {
        final Collection<BlockVector2> tmp = new ArrayList<>();
        for (final BlockVector2 chunk : chunks) {
            tmp.add(chunk);
        }

        m_dispatcher.performSafeChunk(MutexProvider.getMutex(getWorld()), () -> m_parent.fixLighting(tmp), m_bukkitWorld, tmp);
    }

    @Override
    public boolean playEffect(Vector3 position, final int type, final int data) {
        final DataAsyncParams<Vector3> param = DataAsyncParams.extract(position);
        final Vector3 v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return false;
        }

        IFunc<Boolean> func = () -> m_parent.playEffect(v, type, data);

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this.getName(), param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public boolean queueBlockBreakEffect(final Platform server, BlockVector3 position, final BlockType blockType, final double priority) {
        final DataAsyncParams<BlockVector3> param = DataAsyncParams.extract(position);
        final BlockVector3 v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        IFunc<Boolean> func = () -> {
            BlockStateHolder oldBlock = m_parent.getBlock(v);
            if (!canPlace(player, m_bukkitWorld, v, oldBlock, BlockStates.AIR) || isSame(oldBlock, BlockStates.AIR)) {
                return false;
            }

            return m_parent.queueBlockBreakEffect(server, v, blockType, priority);
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            if (!canPlace(player, m_bukkitWorld, v, getBlock(v), BlockStates.AIR)) {
                return false;
            }

            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this.getName(), param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public WeatherType getWeather() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), m_parent::getWeather);
    }

    @Override
    public long getRemainingWeatherDuration() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), m_parent::getRemainingWeatherDuration);
    }

    @Override
    public void setWeather(WeatherType weatherType) {
        final DataAsyncParams<WeatherType> paramWeatherType = DataAsyncParams.extract(weatherType);
        final WeatherType wt = paramWeatherType.getData();
        final IPlayerEntry player = getPlayer(paramWeatherType);

        IAction func = () -> m_parent.setWeather(wt);
        if (paramWeatherType.isAsync() || !m_dispatcher.isMainTask()) {
            m_blockPlacer.addTasks(player,
                    new ActionEntry(paramWeatherType.getJobId(), func, false));
        }

        func.execute();
    }

    @Override
    public void setWeather(WeatherType weatherType, long duration) {
        final DataAsyncParams<WeatherType> paramWeatherType = DataAsyncParams.extract(weatherType);
        final WeatherType wt = paramWeatherType.getData();
        final IPlayerEntry player = getPlayer(paramWeatherType);

        IAction func = () -> m_parent.setWeather(wt, duration);
        if (paramWeatherType.isAsync() || !m_dispatcher.isMainTask()) {
            m_blockPlacer.addTasks(player,
                    new ActionEntry(paramWeatherType.getJobId(), func, false));
        }

        func.execute();
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()),
                m_parent::getMinimumPoint);
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()),
                m_parent::getMaximumPoint);
    }

    @Override
    public List<? extends Entity> getEntities(final Region region) {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()),
                () -> m_parent.getEntities(region), m_bukkitWorld, region);
    }

    @Override
    public List<? extends Entity> getEntities() {
        return m_dispatcher.queueFastOperation((IFunc<? extends List<? extends Entity>>) m_parent::getEntities);
    }

    @Override
    public Entity createEntity(final Location location, final BaseEntity entity) {
        final DataAsyncParams<Location> paramLocation = DataAsyncParams.extract(location);
        final DataAsyncParams<BaseEntity> paramEntity = DataAsyncParams.extract(entity);
        final Location l = paramLocation.getData();
        final BaseEntity e = paramEntity.getData();
        final IPlayerEntry player = getPlayer(paramLocation, paramEntity);

        final EntityLazyWrapper entityWrapper = new EntityLazyWrapper(l, this);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, l.toVector())) {
            return entityWrapper; //Return the entity wrapper so WorldEdit does not complain
        }

        IFunc<Entity> func = () -> {
            Entity result = m_parent.createEntity(l, e);

            if (result != null) {
                entityWrapper.setEntity(result);
            }
            return result;
        };

        if (paramEntity.isAsync() || paramLocation.isAsync() || !m_dispatcher.isMainTask()) {
            if (!m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this.getName(), paramLocation.getJobId(), l.toVector(), func))) {
                return entityWrapper; //Return the entity erapper so WorldEdit does not complain
            }
            return entityWrapper;
        }

        return func.execute();
    }
    
    private BaseBlock getFullBlockDispatcher(final BlockVector3 position) {        
        BaseBlock result = m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), 
                () -> m_parent.getFullBlock(position),
            m_bukkitWorld, position);

        if (result != null && !result.hasNbtData()) {
            final BlockType bType = result.getBlockType();
            final boolean isTile = isTileEntity(bType);

            if (isTile) {
                result = m_dispatcher.queueFastOperation(() -> m_parent.getFullBlock(position));
            }
        }
        
        return result;
    }
            
    private <T> T getBlockEntry(final BlockVector3 position, final Function<BlockCacheEntry, T> get) {
        BlockCacheEntry result = m_blockCache.computeIfAbsent(position, _p -> {
            BaseBlock block = getFullBlockDispatcher(position);
            if (block == null) {
                return null;
            }
            
            return new BlockCacheEntry(block);
        });
        
        if (result == null) {
            return null;
        }
        
        return get.apply(result);
    }
    
    @Override
    public BlockState getBlock(final BlockVector3 position) {        
        return getBlockEntry(position, BlockCacheEntry::state);
    }

    @Override
    public BaseBlock getFullBlock(final BlockVector3 position) {
        return getBlockEntry(position, BlockCacheEntry::block);
    }

    @Override
    public BiomeType getBiome(BlockVector2 position) {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()),
                () -> m_parent.getBiome(position),
                m_bukkitWorld, BlockVector3.at(position.getX(), 0, position.getZ()));
    }

    @Override
    public boolean setBlock(final BlockVector3 position, final BlockStateHolder block) throws WorldEditException {
        m_blockCache.remove(position);

        final DataAsyncParams<BlockStateHolder> paramBlock = DataAsyncParams.extract(block);
        final DataAsyncParams<BlockVector3> paramVector = DataAsyncParams.extract(position);

        final BlockStateHolder newBlock = paramBlock.getData();
        final BlockVector3 v = paramVector.getData();
        final IPlayerEntry player = getPlayer(paramBlock, paramVector);

        IFuncEx<Boolean, WorldEditException> func = () -> {
            final BlockState oldBlock = m_parent.getBlock(v);
            if (!canPlace(player, m_bukkitWorld, v, oldBlock, newBlock)
                    || isSame(oldBlock, newBlock)) {
                return false;
            }

            final boolean result = m_parent.setBlock(position, newBlock);
            if (result) {
                logBlock(position, player, oldBlock, newBlock);
            }

            return result;
        };

        if (paramBlock.isAsync() || paramVector.isAsync() || !m_dispatcher.isMainTask()) {
            if (!canPlace(player, m_bukkitWorld, position, getBlock(v), newBlock)) {
                return false;
            }

            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntryEx(this.getName(), paramBlock.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public boolean setBiome(BlockVector2 vector, final BiomeType biome) {
        final DataAsyncParams<BlockVector2> paramVector = DataAsyncParams.extract(vector);
        final DataAsyncParams<BiomeType> paramBiome = DataAsyncParams.extract(biome);
        final BlockVector2 v = paramVector.getData();
        final BiomeType b = paramBiome.getData();
        final IPlayerEntry player = getPlayer(paramBiome, paramVector);
        final BlockVector3 tmpV = BlockVector3.at(v.getX(), 0, v.getZ());

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, tmpV)) {
            return false;
        }

        IFunc<Boolean> func = () -> m_parent.setBiome(v, b);

        if (paramBiome.isAsync() || paramVector.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this.getName(), paramBiome.getJobId(), tmpV, func));
        }

        return func.execute();
    }

    @Override
    public boolean setBiome(final BlockVector3 position, final BiomeType biome) {
        final DataAsyncParams<BlockVector3> paramPosition = DataAsyncParams.extract(position);
        final DataAsyncParams<BiomeType> paramBiome = DataAsyncParams.extract(biome);
        final BlockVector3 p = paramPosition.getData();
        final BiomeType b = paramBiome.getData();
        final IPlayerEntry player = getPlayer(paramBiome, paramPosition);
        final BlockVector3 tmpV = BlockVector3.at(p.getX(), 0, p.getZ());

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, tmpV)) {
            return false;
        }

        IFunc<Boolean> func = () -> m_parent.setBiome(p, b);

        if (paramBiome.isAsync() || paramPosition.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this.getName(), paramBiome.getJobId(), tmpV, func));
        }

        return func.execute();
    }

    @Override
    public Operation commit() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), m_parent::commit);
    }

    @Override
    public boolean notifyAndLightBlock(BlockVector3 position, BlockState block) throws WorldEditException {
        final DataAsyncParams<BlockState> paramBlock = DataAsyncParams.extract(block);
        final DataAsyncParams<BlockVector3> paramVector = DataAsyncParams.extract(position);

        final BlockState newBlock = paramBlock.getData();
        final BlockVector3 v = paramVector.getData();
        final IPlayerEntry player = getPlayer(paramBlock, paramVector);

        IFuncEx<Boolean, WorldEditException> func = () -> {
            final BlockState oldBlock = m_parent.getBlock(v);
            if (!canPlace(player, m_bukkitWorld, v, oldBlock, newBlock)
                    || isSame(oldBlock, newBlock)) {
                return false;
            }

            final boolean result = m_parent.notifyAndLightBlock(position, newBlock);
            if (result) {
                logBlock(position, player, oldBlock, newBlock);
            }

            return result;
        };

        if (paramBlock.isAsync() || paramVector.isAsync() || !m_dispatcher.isMainTask()) {
            if (!canPlace(player, m_bukkitWorld, position, getBlock(v), newBlock)) {
                return false;
            }

            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntryEx(this.getName(), paramBlock.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public Set<SideEffect> applySideEffects(
            final BlockVector3 position,
            final BlockState block,
            final SideEffectSet sideEffectSet) throws WorldEditException {

        final DataAsyncParams<BlockState> paramBlock = DataAsyncParams.extract(block);
        final DataAsyncParams<BlockVector3> paramVector = DataAsyncParams.extract(position);

        final BlockState newBlock = paramBlock.getData();
        final BlockVector3 v = paramVector.getData();
        final IPlayerEntry player = getPlayer(paramBlock, paramVector);

        IFuncEx<Set<SideEffect>, WorldEditException> func = () -> {
            final BlockState oldBlock = m_parent.getBlock(v);
            if (!canPlace(player, m_bukkitWorld, v, oldBlock, newBlock)
                    || isSame(oldBlock, newBlock)) {
                return Collections.EMPTY_SET;
            }

            return m_parent.applySideEffects(position, newBlock, sideEffectSet);
        };

        if (paramBlock.isAsync() || paramVector.isAsync() || !m_dispatcher.isMainTask()) {
            if (!canPlace(player, m_bukkitWorld, position, getBlock(v), newBlock)) {
                return Collections.EMPTY_SET;
            }

            return m_blockPlacer.addTasks(player, new WorldFuncEntryEx(this.getName(), paramBlock.getJobId(), v, func)) ?
                    Sets.intersection(AwePlatform.getInstance().getPlatform().getWorldEditIntegrator().getSupportedSideEffects(),
                            sideEffectSet.getSideEffectsToApply()) : Collections.EMPTY_SET;
        }

        return func.execute();
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), m_parent::getSpawnPosition);
    }

    private boolean isTileEntity(BlockType blockType) {
        if (blockType == null) {
            return false;
        }
        
        final IMaterial m = AwePlatform.getInstance().getPlatform().getMaterialLibrary().getMaterial(blockType.getId());
        return m != null && m.isTileEntity();
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

    private static class BlockCacheEntry {
        private final BaseBlock m_block;
        
        private volatile BlockState m_state;
        
        public BaseBlock block() { return m_block; }
        public BlockState state() {
            if (m_state == null) {
                m_state = m_block.toImmutableState();
            }
            
            return m_state;
        }
        
        public BlockCacheEntry(BaseBlock block) {
            m_block = block;
        }
    }
    
    private static class LruMap<TKey, TValue> extends LinkedHashMap<TKey, TValue> {

        private final int m_size;

        public LruMap(int size) {
            super(size + 1, 0.75f, true);

            m_size = size;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<TKey, TValue> entry) {
            return size() > m_size;
        }
    }
}
