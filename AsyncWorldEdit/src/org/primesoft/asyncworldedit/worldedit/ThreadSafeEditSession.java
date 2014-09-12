/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionStub;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.util.eventbus.EventBus;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.World;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.BlocksHubIntegration;
import org.primesoft.asyncworldedit.ConfigProvider;
import org.primesoft.asyncworldedit.PlayerWrapper;
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
     * The blocks hub integrator
     */
    private final BlocksHubIntegration m_bh;

    /**
     * The dispatcher class
     */
    private final TaskDispatcher m_dispatcher;

    /**
     * Player wraper
     */
    private final PlayerWrapper m_wrapper;

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
    protected final UUID m_player;

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

    public UUID getPlayer() {
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
        return m_asyncForced || ((m_wrapper == null || m_wrapper.getMode()) && !m_asyncDisabled);
    }

    public ThreadSafeEditSession(AsyncWorldEditMain plugin,
            UUID player, EventBus eventBus, com.sk89q.worldedit.world.World world,
            int maxBlocks, @Nullable BlockBag blockBag, EditSessionEvent event) {

        super(eventBus, AsyncWorld.wrap(world, player), maxBlocks, blockBag, event);

        m_asyncTasks = new HashSet<JobEntry>();
        m_plugin = plugin;
        m_bh = plugin.getBlocksHub();
        m_blockPlacer = plugin.getBlockPlacer();
        m_dispatcher = plugin.getTaskDispatcher();

        m_player = player;
        m_wrapper = m_plugin.getPlayerManager().getPlayer(player);
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

    public boolean setBlockIfAir(Vector pt, BaseBlock block, int jobId)
            throws MaxChangedBlocksException {
        boolean isAsync = isAsyncEnabled();
        return super.setBlockIfAir(VectorWrapper.wrap(pt, m_jobId, isAsync, m_player),
                BaseBlockWrapper.wrap(block, jobId, isAsync, m_player));
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
        boolean r = super.setBlock(VectorWrapper.wrap(pt, m_jobId, isAsync, m_player),
                BaseBlockWrapper.wrap(block, jobId, isAsync, m_player));
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
        boolean result = m_asyncForced ||
                // use operation name?
                (/*ConfigProvider.isAsyncAllowed(operation) && */(m_wrapper == null || m_wrapper.getMode()));

        m_asyncDisabled = !result;
        return result;
    }

    /**
     * This function checks if async mode is enabled for specific command
     *
     * @param operation
     * @return 
     */
    public boolean checkAsync(WorldeditOperations operation) {
        boolean result = m_asyncForced || (ConfigProvider.isAsyncAllowed(operation)
                && (m_wrapper == null || m_wrapper.getMode()));

        m_asyncDisabled = !result;
        return result;
    }

    /**
     * Reset async disabled inner state (enable async mode)
     */
    public void resetAsync() {
        m_asyncDisabled = false;
    }

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
                    m_blockPlacer.cancelJob(m_player, job);
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

    public UndoSession doUndo() {
        UndoSession result = new UndoSession(m_eventBus);
        super.undo(result);
        return result;
    }

    public void doRedo(EditSession session) {
        super.redo(session);
    }
}
