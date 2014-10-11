/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
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
package org.primesoft.asyncworldedit.worldedit;

import org.primesoft.asyncworldedit.worldedit.util.LocationWrapper;
import org.primesoft.asyncworldedit.worldedit.blocks.BaseBlockWrapper;
import org.primesoft.asyncworldedit.worldedit.world.biome.BaseBiomeWrapper;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionStub;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.biome.BaseBiome;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.bukkit.World;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.PlayerEntry;
import org.primesoft.asyncworldedit.blockPlacer.*;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.UndoJob;
import org.primesoft.asyncworldedit.taskdispatcher.TaskDispatcher;
import org.primesoft.asyncworldedit.utils.Func;
import org.primesoft.asyncworldedit.worldedit.world.AsyncWorld;

/**
 *
 * @author SBPrime
 */
public class ThreadSafeEditSession extends EditSessionStub {

    /**
     * Maximum queued blocks
     */
    private final int MAX_QUEUED = 10000;

    /**
     * Plugin instance
     */
    protected final AsyncWorldEditMain m_plugin;

    /**
     * Async block placer
     */
    protected final BlockPlacer m_blockPlacer;

    /**
     * The dispatcher class
     */
    private final TaskDispatcher m_dispatcher;

    /**
     * Indicates that the async mode has been disabled (inner state)
     */
    private boolean m_asyncDisabled;

    /**
     * Force all functions to by performed in async mode this is used to
     * override the config by API calls
     */
    private boolean m_asyncForced;

    /**
     * Current job id
     */
    protected int m_jobId;

    /**
     * Number of queued blocks
     */
    private int m_blocksQueued;

    /**
     * Number of async tasks
     */
    private final HashSet<JobEntry> m_asyncTasks;

    /**
     * Player
     */
    protected final PlayerEntry m_player;

    /**
     * Current craftbukkit world
     */
    private final World m_bukkitWorld;

    /**
     * The event bus
     */
    private final EventBus m_eventBus;

    /**
     * The edit session event
     */
    private final EditSessionEvent m_editSessionEvent;

    /**
     * The parent world
     */
    private final com.sk89q.worldedit.world.World m_world;

    public BlockPlacer getBlockPlacer() {
        return m_blockPlacer;
    }

    public PlayerEntry getPlayer() {
        return m_player;
    }

    public World getCBWorld() {
        return m_bukkitWorld;
    }

    public EventBus getEventBus() {
        return m_eventBus;
    }

    public EditSessionEvent getEditSessionEvent() {
        return m_editSessionEvent;
    }

    protected boolean isAsyncEnabled() {
        return m_asyncForced || (m_player.getMode() && !m_asyncDisabled);
    }

    public ThreadSafeEditSession(AsyncWorldEditMain plugin,
            PlayerEntry player, EventBus eventBus, com.sk89q.worldedit.world.World world,
            int maxBlocks, @Nullable BlockBag blockBag, EditSessionEvent event) {

        super(eventBus, AsyncWorld.wrap(world, player), maxBlocks, blockBag, event);

        m_asyncTasks = new HashSet<JobEntry>();
        m_plugin = plugin;
        m_blockPlacer = plugin.getBlockPlacer();
        m_dispatcher = plugin.getTaskDispatcher();

        m_player = player;
        m_world = world;
        m_editSessionEvent = event;
        m_eventBus = eventBus;

        if (world != null) {
            m_bukkitWorld = plugin.getServer().getWorld(world.getName());
        } else {
            m_bukkitWorld = null;
        }

        m_asyncForced = false;
        m_asyncDisabled = false;
        m_jobId = -1;
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block, Stage stage) throws WorldEditException {
        boolean isAsync = isAsyncEnabled();
        boolean r = super.setBlock(VectorWrapper.wrap(position, m_jobId, isAsync, m_player),
                BaseBlockWrapper.wrap(block, m_jobId, isAsync, m_player), stage);
        if (r) {
            forceFlush();
        }
        return r;
    }

    public boolean setBlock(int jobId, Vector position, BaseBlock block, Stage stage) throws WorldEditException {
        boolean isAsync = isAsyncEnabled();
        boolean r = super.setBlock(VectorWrapper.wrap(position, jobId, isAsync, m_player),
                BaseBlockWrapper.wrap(block, jobId, isAsync, m_player), stage);
        if (r) {
            forceFlush();
        }
        return r;
    }

    
    public boolean setBlockIfAir(Vector pt, BaseBlock block, int jobId)
            throws MaxChangedBlocksException {
        boolean isAsync = isAsyncEnabled();
        return super.setBlockIfAir(VectorWrapper.wrap(pt, jobId, isAsync, m_player),
                BaseBlockWrapper.wrap(block, jobId, isAsync, m_player));
    }

    @Override
    public boolean setBlockIfAir(Vector position, BaseBlock block) throws MaxChangedBlocksException {
        boolean isAsync = isAsyncEnabled();
        return super.setBlockIfAir(VectorWrapper.wrap(position, m_jobId, isAsync, m_player),
                BaseBlockWrapper.wrap(block, m_jobId, isAsync, m_player));
    }
    
    

    public boolean setBlock(Vector pt, Pattern pat, int jobId)
            throws MaxChangedBlocksException {
        m_jobId = jobId;
        boolean isAsync = isAsyncEnabled();
        boolean r = super.setBlock(VectorWrapper.wrap(pt, jobId, isAsync, m_player), pat);
        if (r) {
            forceFlush();
        }
        m_jobId = -1;
        return r;
    }

    public boolean setBlock(Vector pt, BaseBlock block, int jobId)
            throws MaxChangedBlocksException {
        boolean isAsync = isAsyncEnabled();
        boolean r = super.setBlock(VectorWrapper.wrap(pt, jobId, isAsync, m_player),
                BaseBlockWrapper.wrap(block, jobId, isAsync, m_player));
        if (r) {
            forceFlush();
        }
        return r;
    }        

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        boolean isAsync = isAsyncEnabled();
        boolean r = super.setBiome(Vector2DWrapper.wrap(position, m_jobId, isAsync, m_player),
                BaseBiomeWrapper.wrap(biome, m_jobId, isAsync, m_player));
        if (r) {
            forceFlush();
        }
        return r;
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block) throws MaxChangedBlocksException {
        boolean isAsync = isAsyncEnabled();
        boolean r = super.setBlock(VectorWrapper.wrap(position, m_jobId, isAsync, m_player),
                BaseBlockWrapper.wrap(block, m_jobId, isAsync, m_player));
        if (r) {
            forceFlush();
        }
        return r;
    }

    @Override
    public boolean setBlock(Vector position, Pattern pattern) throws MaxChangedBlocksException {
        boolean isAsync = isAsyncEnabled();
        boolean r = super.setBlock(VectorWrapper.wrap(position, m_jobId, isAsync, m_player), pattern);

        if (r) {
            forceFlush();
        }
        return r;        
    }    

    @Override
    public boolean smartSetBlock(Vector pt, BaseBlock block) {
        return super.smartSetBlock(pt, block);
    }
    
    
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        boolean isAsync = isAsyncEnabled();
        return super.createEntity(LocationWrapper.wrap(location, m_jobId, isAsync, m_player),
                entity);
    }


    @Override
    public BaseBlock getBlock(final Vector position) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<BaseBlock>() {
            @Override
            public BaseBlock Execute() {
                return es.doGetBlock(position);
            }
        }, m_bukkitWorld, position);
    }

    @Override
    public int getBlockData(final Vector position) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return es.doGetBlockData(position);
            }
        }, m_bukkitWorld, position);
    }

    @Override
    public int getBlockType(final Vector position) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return es.doGetBlockType(position);
            }
        }, m_bukkitWorld, position);
    }

    @Override
    public BaseBlock getLazyBlock(final Vector position) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<BaseBlock>() {
            @Override
            public BaseBlock Execute() {
                return es.doGetLazyBlock(position);
            }
        }, m_bukkitWorld, position);
    }

    @Override
    public BaseBiome getBiome(final Vector2D position) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<BaseBiome>() {
            @Override
            public BaseBiome Execute() {
                return es.doGetBiome(position);
            }
        }, m_bukkitWorld, new Vector(position.getX(), 0, position.getZ()));
    }

    @Override
    public int getBlockChangeCount() {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return es.doGetBlockChangeCount();
            }
        });
    }

    @Override
    public int getBlockChangeLimit() {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return es.doGetBlockChangeLimit();
            }
        });
    }

    @Override
    public List<Countable<Integer>> getBlockDistribution(final Region region) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<List<Countable<Integer>>>() {
            @Override
            public List<Countable<Integer>> Execute() {
                return es.doGetBlockDistribution(region);
            }
        }, m_bukkitWorld, region);
    }

    @Override
    public List<Countable<BaseBlock>> getBlockDistributionWithData(final Region region) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<List<Countable<BaseBlock>>>() {
            @Override
            public List<Countable<BaseBlock>> Execute() {
                return es.doGetBlockDistributionWithData(region);
            }
        }, m_bukkitWorld, region);
    }

    @Override
    public List<? extends Entity> getEntities() {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<List<? extends Entity>>() {
            @Override
            public List<? extends Entity> Execute() {
                return es.doGetEntities();
            }
        });
    }

    @Override
    public List<? extends Entity> getEntities(final Region region) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<List<? extends Entity>>() {
            @Override
            public List<? extends Entity> Execute() {
                return es.doGetEntities(region);
            }
        }, m_bukkitWorld, region);
    }

    @Override
    public int getHighestTerrainBlock(final int x, final int z, final int minY, final int maxY) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return es.doGetHighestTerrainBlock(x, z, minY, maxY);
            }
        }, m_bukkitWorld, new Vector(x, minY, z));
    }
        
    @Override
    public int getHighestTerrainBlock(final int x, final int z, 
            final int minY, final int maxY, final boolean naturalOnly) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return es.doGetHighestTerrainBlock(x, z, minY, maxY, naturalOnly);
            }
        }, m_bukkitWorld, new Vector(x, minY, z));
    }

    @Override
    public Vector getMaximumPoint() {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<Vector>() {
            @Override
            public Vector Execute() {
                return es.doGetMaximumPoint();
            }
        });
    }

    @Override
    public Vector getMinimumPoint() {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(new Func<Vector>() {
            @Override
            public Vector Execute() {
                return es.doGetMinimumPoint();
            }
        });
    }

    /**
     * Do not change! Requires special processing
     * @param sess 
     */
    @Override
    public void undo(final EditSession sess) {
        final int jobId = getJobId();
        
        cancelJobs(jobId);

        UndoSession undoSession = doUndo();

        Mask oldMask = sess.getMask();
        sess.setMask(getMask());

        final Map.Entry<Vector, BaseBlock>[] blocks = undoSession.getEntries();
        final HashMap<Integer, HashMap<Integer, HashSet<Integer>>> placedBlocks = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>>();

        for (int i = blocks.length - 1; i >= 0; i--) {
            Map.Entry<Vector, BaseBlock> entry = blocks[i];
            Vector pos = entry.getKey();
            BaseBlock block = entry.getValue();

            int x = pos.getBlockX();
            int y = pos.getBlockY();
            int z = pos.getBlockZ();
            boolean ignore = false;

            HashMap<Integer, HashSet<Integer>> mapX = placedBlocks.get(x);
            if (mapX == null) {
                mapX = new HashMap<Integer, HashSet<Integer>>();
                placedBlocks.put(x, mapX);
            }

            HashSet<Integer> mapY = mapX.get(y);
            if (mapY == null) {
                mapY = new HashSet<Integer>();
                mapX.put(y, mapY);
            }
            if (mapY.contains(z)) {
                ignore = true;
            } else {
                mapY.add(z);
            }

            if (!ignore) {
                sess.smartSetBlock(pos, block);
            }
        }

        sess.flushQueue();
        sess.setMask(oldMask);
    }

    @Override
    public int size() {
        final int result = super.size();
        synchronized (m_asyncTasks) {
            if (result <= 0 && m_asyncTasks.size() > 0) {
                return 1;
            }
        }
        return result;
    }

    @Override
    public void flushQueue() {
        boolean queued = isQueueEnabled();
        super.flushQueue();
        m_blocksQueued = 0;
        if (queued) {
            resetAsync();
        }
    }

    /**
     * Force block flush when to many has been queued
     */
    protected void forceFlush() {
        if (isQueueEnabled()) {
            m_blocksQueued++;
            if (m_blocksQueued > MAX_QUEUED) {
                m_blocksQueued = 0;
                super.flushQueue();
            }
        }
    }

    /**
     * Enables or disables the async mode configuration bypass this function
     * should by used only by other plugins
     *
     * @param value true to enable async mode force
     */
    public void setAsyncForced(boolean value) {
        m_asyncForced = value;
    }

    /**
     * Check if async mode is forced
     *
     * @return
     */
    public boolean isAsyncForced() {
        return m_asyncForced;
    }

    /**
     * This function checks if async mode is enabled for specific command
     *
     * @param operationName
     * @return
     */
    public boolean checkAsync(String operationName) {
        try {
            return checkAsync(WorldeditOperations.valueOf(operationName));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This function checks if async mode is enabled for specific command
     *
     * @param operation
     * @return
     */
    public boolean checkAsync(WorldeditOperations operation) {
        boolean result = m_asyncForced || (ConfigProvider.isAsyncAllowed(operation)
                && m_player.getMode());

        m_asyncDisabled = !result;
        return result;
    }

    /**
     * Reset async disabled inner state (enable async mode)
     */
    public void resetAsync() {
        m_asyncDisabled = false;
    }

    /**
     * Cancel a job
     * @param jobId 
     */
    protected void cancelJobs(final int jobId) {
        int minId = jobId;

        synchronized (m_asyncTasks) {
            for (JobEntry job : m_asyncTasks) {
                int id = job.getJobId();
                if (id < minId) {
                    minId = id;
                }
                if (!(job instanceof UndoJob)) {
                    m_blockPlacer.cancelJob(m_player, id);
                }
            }
            minId--;
            if (minId >= 0 && minId != jobId) {
                JobEntry job = m_blockPlacer.getJob(m_player, minId);
                if (job != null && !(job instanceof UndoJob)) {
                    m_blockPlacer.cancelJob(m_player, job.getJobId());
                }
            }
        }
    }

    /**
     * Add async job
     *
     * @param job
     */
    public void addAsync(JobEntry job) {
        synchronized (m_asyncTasks) {
            m_asyncTasks.add(job);
        }
    }

    /**
     * Remov async job (done or canceled)
     *
     * @param job
     */
    public void removeAsync(JobEntry job) {
        synchronized (m_asyncTasks) {
            m_asyncTasks.remove(job);
        }
    }

    /**
     * Get next job id for current player
     *
     * @return Job id
     */
    protected int getJobId() {
        return m_blockPlacer.getJobId(m_player);
    }

    private BaseBlock doGetBlock(Vector position) {
        return super.getBlock(position);
    }

    private Integer doGetBlockData(Vector position) {
        return super.getBlockData(position);
    }

    private Integer doGetBlockType(Vector position) {
        return super.getBlockType(position);
    }

    private BaseBlock doGetLazyBlock(Vector position) {
        return super.getLazyBlock(position);
    }

    public BaseBiome doGetBiome(Vector2D position) {
        return super.getBiome(position);
    }

    public int doGetBlockChangeCount() {
        return super.getBlockChangeCount();
    }

    public int doGetBlockChangeLimit() {
        return super.getBlockChangeLimit();
    }

    public List<Countable<Integer>> doGetBlockDistribution(Region region) {
        return super.getBlockDistribution(region);
    }

    public List<Countable<BaseBlock>> doGetBlockDistributionWithData(Region region) {
        return super.getBlockDistributionWithData(region);
    }

    public List<? extends Entity> doGetEntities() {
        return super.getEntities();
    }

    public List<? extends Entity> doGetEntities(Region region) {
        return super.getEntities(region);
    }

    public int doGetHighestTerrainBlock(int x, int z, int minY, int maxY) {
        return super.getHighestTerrainBlock(x, z, minY, maxY);
    }

    public int doGetHighestTerrainBlock(int x, int z, int minY, int maxY, boolean naturalOnly) {
        return super.getHighestTerrainBlock(x, z, minY, maxY, naturalOnly);
    }

    public Vector doGetMaximumPoint() {
        return super.getMaximumPoint();
    }

    public Vector doGetMinimumPoint() {
        return super.getMinimumPoint();
    }

    public UndoSession doUndo() {
        UndoSession result = new UndoSession(m_eventBus);
        super.undo(result);
        return result;
    }

    public void doRedo(EditSession session) {
        super.redo(session);
    }
}
