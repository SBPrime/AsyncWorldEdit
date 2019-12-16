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
package org.primesoft.asyncworldedit.worldedit;

import org.primesoft.asyncworldedit.api.worldedit.IThreadSafeEditSession;
import org.primesoft.asyncworldedit.configuration.WorldeditOperations;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.AweEditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.ChangeSetExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.inventory.BlockBagExtent;
import com.sk89q.worldedit.extent.reorder.MultiStageReorder;
import com.sk89q.worldedit.extent.world.ChunkLoadingExtent;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.blockPlacer.*;
import org.primesoft.asyncworldedit.blockPlacer.entries.ActionEntryEx;
import org.primesoft.asyncworldedit.api.blockPlacer.entries.IJobEntry;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.blockPlacer.entries.UndoJob;
import org.primesoft.asyncworldedit.api.utils.IActionEx;
import org.primesoft.asyncworldedit.configuration.DebugLevel;
import org.primesoft.asyncworldedit.events.EditSessionLimitChanged;
import org.primesoft.asyncworldedit.utils.ExtentUtils;
import org.primesoft.asyncworldedit.utils.MutexProvider;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.worldedit.extent.ExtendedChangeSetExtent;
import org.primesoft.asyncworldedit.worldedit.extent.inventory.FixedBlockBagExtent;
import org.primesoft.asyncworldedit.worldedit.extent.inventory.ThreadSafeBlockBag;
import org.primesoft.asyncworldedit.worldedit.history.changeset.FileChangeSet;
import org.primesoft.asyncworldedit.worldedit.history.changeset.IExtendedChangeSet;
import org.primesoft.asyncworldedit.worldedit.history.changeset.MemoryMonitorChangeSet;
import org.primesoft.asyncworldedit.worldedit.history.changeset.NullChangeSet;
import org.primesoft.asyncworldedit.worldedit.history.changeset.ThreadSafeChangeSet;
import org.primesoft.asyncworldedit.worldedit.world.AsyncWorld;
import org.primesoft.asyncworldedit.injector.injected.extent.reorder.IResetable;

/**
 *
 * @author SBPrime
 */
public class ThreadSafeEditSession extends AweEditSession implements IThreadSafeEditSession {

    /**
     * Async block placer
     */
    protected final IBlockPlacer m_blockPlacer;

    /**
     * The dispatcher class
     */
    private final ITaskDispatcher m_dispatcher;

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
     * Force all functions to override the config by API calls
     */
    private boolean m_asyncForceDisabled;

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
    private final HashSet<IJobEntry> m_asyncTasks;

    /**
     * Player
     */
    protected final IPlayerEntry m_player;

    /**
     * Current craftbukkit world
     */
    private final IWorld m_bukkitWorld;

    /**
     * The event bus
     */
    private final EventBus m_eventBus;

    /**
     * The edit session event
     */
    private final EditSessionEvent m_editSessionEvent;

    /**
     * The mutex
     */
    private final Object m_mutex = new Object();

    /**
     * The root change set
     */
    private ChangeSet m_rootChangeSet;

    /**
     * The AWE core;
     */
    protected final IAsyncWorldEditCore m_aweCore;

    private final List<IResetable> m_resetable = new LinkedList<>();

    /**
     * Is the class fully initialized
     */
    private boolean m_isInitialized;

    @Override
    public Object getMutex() {
        return m_mutex;
    }

    @Override
    public IBlockPlacer getBlockPlacer() {
        return m_blockPlacer;
    }

    @Override
    public IPlayerEntry getPlayer() {
        return m_player;
    }

    @Override
    public IWorld getCBWorld() {
        return m_bukkitWorld;
    }

    @Override
    public EventBus getEventBus() {
        return m_eventBus;
    }

    @Override
    public EditSessionEvent getEditSessionEvent() {
        return m_editSessionEvent;
    }

    /**
     * Get the root changeset entry
     *
     * @return
     */
    @Override
    public ChangeSet getRootChangeSet() {
        return m_rootChangeSet;
    }

    protected boolean isAsyncEnabled() {
        return m_asyncForced
                || (m_player.getAweMode() && !m_asyncDisabled && !m_asyncForceDisabled);
    }

    public ThreadSafeEditSession(IAsyncWorldEditCore core,
            IPlayerEntry player, EventBus eventBus, com.sk89q.worldedit.world.World world,
            int maxBlocks, @Nullable BlockBag blockBag, EditSessionEvent event) {

        super(eventBus, AsyncWorld.wrap(world, player), maxBlocks, ThreadSafeBlockBag.warap(blockBag), event);

        m_asyncTasks = new HashSet<>();

        m_aweCore = core;
        m_blockPlacer = core.getBlockPlacer();
        m_dispatcher = core.getTaskDispatcher();

        m_player = player;
        m_editSessionEvent = event;
        m_eventBus = eventBus;

        if (world != null) {
            m_bukkitWorld = m_aweCore.getWorld(world.getName());
        } else {
            m_bukkitWorld = null;
        }

        m_asyncForced = false;
        m_asyncForceDisabled = false;
        m_asyncDisabled = false;

        boolean isDebug = ConfigProvider.messages().debugLevel().isAtLeast(DebugLevel.INFO);
        if (isDebug) {
            ExtentUtils.dumpExtents("TSES: ", "Original extents:", this);
        }

        injectExtents(player, core);

        if (isDebug) {
            ExtentUtils.dumpExtents("TSES: ", "Injected extents:", this);
        }

        m_jobId = -1;
        m_isInitialized = true;
    }

    private void injectExtents(IPlayerEntry playerEntry, IAsyncWorldEditCore core) {
        Extent[] extentList = ExtentUtils.getExtentList(this).toArray(new Extent[0]);
        for (Extent e : extentList) {
            if (e instanceof NullExtent) {
                return;
            }
        }

        injectBlockBagExtent(extentList);
        injectChangeSet(extentList, playerEntry, core);

        for (Extent e : extentList) {
            if (e instanceof IResetable) {
                m_resetable.add((IResetable) e);
            }
        }
    }

    private void injectBlockBagExtent(Extent[] extentList) {
        BlockBagExtent blockBagExtent = Reflection.get(EditSession.class, BlockBagExtent.class,
                this, "blockBagExtent", "Unable to get the blockBagExtent");
        Extent beforeExtent = ExtentUtils.findBeforer(extentList, blockBagExtent);
        Extent afterExtent = blockBagExtent != null ? blockBagExtent.getExtent() : null;

        BlockBag blockBag = getBlockBag();

        if (blockBagExtent == null
                || afterExtent == null || beforeExtent == null) {
            log("Unable to get the blockBagExtent from EditSession, block bag broken.");
            return;
        }

        BlockBagExtent newBlockBag = new FixedBlockBagExtent(m_aweCore.getBlocksHubBridge(), m_player, m_bukkitWorld, afterExtent, blockBag);

        if (!ExtentUtils.setExtent(beforeExtent, newBlockBag)) {
            log("Unable to set the blockBagExtent from EditSession, block bag broken.");
            return;
        }

        Reflection.set(EditSession.class, this, "blockBagExtent", newBlockBag,
                "Unable to set the blockBagExtent from EditSession, block bag broken.");
    }

    private void injectChangeSet(Extent[] extentList, IPlayerEntry playerEntry, IAsyncWorldEditCore core) {
        Extent beforeExtent = null;
        ChangeSet changeSet = getChangeSet();
        if (changeSet == null) {
            log("Unable to get the changeSet from EditSession, undo and redo broken.");
            return;
        }

        for (int i = 0; i < extentList.length; i++) {
            Extent current = extentList[i];
            if (current instanceof ChangeSetExtent) {
                ChangeSetExtent changesetExtent = (ChangeSetExtent)current;                
                Extent afterExtent = changesetExtent.getExtent();

                if (afterExtent == null || beforeExtent == null) {
                    log("Unable to get the changesetExtent from EditSession, undo broken.");
                    continue;
                }

                IPermissionGroup pg = playerEntry.getPermissionGroup();
                boolean undoDisabled = pg != null && playerEntry.isUndoOff();

                ChangeSet newChangeSet;

                if (undoDisabled) {
                    newChangeSet = new NullChangeSet();
                    m_rootChangeSet = newChangeSet;
                } else {
                    if (ConfigProvider.undo().storeOnDisk()) {
                        changeSet = new FileChangeSet(core, playerEntry);
                    }

                    IExtendedChangeSet aweChangeSet = new MemoryMonitorChangeSet(playerEntry, m_dispatcher, new ThreadSafeChangeSet(changeSet));

                    ExtendedChangeSetExtent extendedChangeSetExtent = new ExtendedChangeSetExtent(null, afterExtent, aweChangeSet);
                    ExtentUtils.setExtent(beforeExtent, extendedChangeSetExtent);

                    newChangeSet = aweChangeSet;
                    m_rootChangeSet = changeSet;
                }

                Reflection.set(EditSession.class, this, "changeSet", newChangeSet,
                        "Unable to inject ChangeSet, undo and redo broken.");
                Reflection.set(ChangeSetExtent.class, changesetExtent, "changeSet", newChangeSet,
                        "Unable to inject changeset to extent, undo and redo broken.");
            }
            beforeExtent = current;
        }
    }

    @Override
    public void setBlockChangeLimit(int limit) {
        int oldLimit = getBlockChangeLimit();

        super.setBlockChangeLimit(limit);

        m_eventBus.post(new EditSessionLimitChanged(this, m_player, oldLimit, limit));
    }

    @Override
    public void setBlockBag(BlockBag blockBag) {
        super.setBlockBag(ThreadSafeBlockBag.warap(blockBag));
    }

    @Override
    public boolean setBlock(BlockVector3 position, BlockStateHolder block, Stage stage) throws WorldEditException {
        boolean isAsync = isAsyncEnabled();
        boolean r = super.setBlock(AsyncWrapper.initialize(position, m_jobId, isAsync, m_player),
                AsyncWrapper.initialize(block, m_jobId, isAsync, m_player), stage);
        if (r) {
            forceFlush();
        }
        return r;
    }

    @Override
    public boolean setBlock(int jobId, BlockVector3 position, BlockStateHolder block, Stage stage) throws WorldEditException {
        boolean isAsync = isAsyncEnabled();
        boolean r = super.setBlock(AsyncWrapper.initialize(position, jobId, isAsync, m_player),
                AsyncWrapper.initialize(block, jobId, isAsync, m_player), stage);
        if (r) {
            forceFlush();
        }
        return r;
    }

    @Override
    public boolean setBlock(BlockVector3 pt, Pattern pat, int jobId)
            throws MaxChangedBlocksException {
        m_jobId = jobId;
        boolean isAsync = isAsyncEnabled();
        boolean r = super.setBlock(AsyncWrapper.initialize(pt, jobId, isAsync, m_player), pat);
        if (r) {
            forceFlush();
        }
        m_jobId = -1;
        return r;
    }

    @Override
    public boolean setBlock(BlockVector3 pt, BlockStateHolder block, int jobId)
            throws MaxChangedBlocksException {
        boolean isAsync = isAsyncEnabled();
        boolean r = super.setBlock(AsyncWrapper.initialize(pt, jobId, isAsync, m_player),
                AsyncWrapper.initialize(block, jobId, isAsync, m_player));
        if (r) {
            forceFlush();
        }
        return r;
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        boolean isAsync = isAsyncEnabled();
        boolean r = super.setBiome(AsyncWrapper.initialize(position, m_jobId, isAsync, m_player),
                AsyncWrapper.initialize(biome, m_jobId, isAsync, m_player));
        if (r) {
            forceFlush();
        }
        return r;
    }

    /**
     * Perform a custom action
     *
     * @throws com.sk89q.worldedit.WorldEditException
     */
    @Override
    public void doCustomAction(final Change change, boolean isDemanding) throws WorldEditException {
        final boolean isAsync = isAsyncEnabled();
        final ChangeSet cs = getChangeSet();
        final IExtendedChangeSet ecs = (cs instanceof IExtendedChangeSet) ? (IExtendedChangeSet) cs : null;

        final UndoContext undoContext = new UndoContext();
        undoContext.setExtent(this);

        final IBlockPlacer blockPlacer = getBlockPlacer();
        final Change safeChange = new BlockPlacerChange(change, blockPlacer, isDemanding);

        final IActionEx<WorldEditException> action = () -> change.redo(undoContext);

        if (ecs != null) {
            ecs.addExtended(safeChange, null);
        } else {
            cs.add(safeChange);
        }

        if (!isAsync) {
            action.execute();

            return;
        }

        BlockPlacerEntry entry = new ActionEntryEx(m_jobId, action, isDemanding);

        m_blockPlacer.addTasks(m_player, entry);
    }

    @Override
    public boolean setBlock(BlockVector3 position, BlockStateHolder block) throws MaxChangedBlocksException {
        boolean isAsync = isAsyncEnabled();
        boolean r = super.setBlock(AsyncWrapper.initialize(position, m_jobId, isAsync, m_player),
                AsyncWrapper.initialize(block, m_jobId, isAsync, m_player));
        if (r) {
            forceFlush();
        }
        return r;
    }

    @Override
    public boolean setBlock(BlockVector3 position, Pattern pattern) throws MaxChangedBlocksException {
        boolean isAsync = isAsyncEnabled();
        boolean r = super.setBlock(AsyncWrapper.initialize(position, m_jobId, isAsync, m_player), pattern);

        if (r) {
            forceFlush();
        }
        return r;
    }

    @Override
    public boolean smartSetBlock(BlockVector3 pt, BlockStateHolder block) {
        return super.smartSetBlock(pt, block);
    }

    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        boolean isAsync = isAsyncEnabled();
        return super.createEntity(AsyncWrapper.initialize(location, m_jobId, isAsync, m_player),
                AsyncWrapper.initialize(entity, m_jobId, isAsync, m_player));
    }

    @Override
    public BlockState getBlock(final BlockVector3 position) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), () -> es.doGetBlock(position), m_bukkitWorld, position);
    }

    @Override
    public BaseBlock getFullBlock(final BlockVector3 position) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), () -> es.doGetFullBlock(position), m_bukkitWorld, position);
    }

    @Override
    public BiomeType getBiome(final BlockVector2 position) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()),
                () -> es.doGetBiome(position), m_bukkitWorld, BlockVector3.at(position.getX(), 0, position.getZ()));
    }

    @Override
    public int getBlockChangeCount() {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(MutexProvider.getMutex(this),
                () -> es.doGetBlockChangeCount());
    }

    @Override
    public int getBlockChangeLimit() {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(MutexProvider.getMutex(this), es::doGetBlockChangeLimit);
    }

    @Override
    public List<? extends Entity> getEntities() {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()),
                () -> es.doGetEntities());
    }

    @Override
    public List<? extends Entity> getEntities(final Region region) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()),
                () -> es.doGetEntities(region), m_bukkitWorld, region);
    }

    @Override
    public int getHighestTerrainBlock(final int x, final int z, final int minY, final int maxY) {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()),
                () -> es.doGetHighestTerrainBlock(x, z, minY, maxY), m_bukkitWorld, BlockVector3.at(x, minY, z));
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()),
                es::doGetMaximumPoint);
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        final ThreadSafeEditSession es = this;

        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()),
                es::doGetMinimumPoint);
    }

    /**
     * Do not change! Requires special processing
     *
     * @param sess
     */
    @Override
    public void undo(final EditSession sess) {
        final int jobId = getJobId();

        cancelJobs(jobId);

        UndoProcessor.processUndo(this, this, sess);
    }

    @Override
    public void redo(EditSession sess) {
        RedoProcessor.processRedo(this, this, sess);
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
    public void flushSession() {
        if (!m_isInitialized) {
            return;
        }
        
        boolean queued = isQueueEnabled();
        super.flushSession();

        m_resetable.forEach(IResetable::reset);

        m_blocksQueued = 0;
        if (queued) {
            resetAsync();
        }
    }

    /**
     * Force block flush when to many has been queued
     */
    protected void forceFlush() {
        int maxBlocks = ConfigProvider.getForceFlushBlocks();

        if (isQueueEnabled() && (maxBlocks != -1)) {
            m_blocksQueued++;
            if (m_blocksQueued > maxBlocks) {
                m_blocksQueued = 0;
                super.flushSession();

                m_resetable.forEach(IResetable::reset);
            }
        }
    }

    /**
     * Enables or disables the async mode configuration bypass this function
     * should by used only by other plugins
     *
     * @param value true to enable async mode force
     */
    @Override
    public void setAsyncForced(boolean value) {
        m_asyncForced = value;
    }

    /**
     * Force diabsle the AWE features for this edit session
     *
     * @param value
     */
    @Override
    public final void setAsyncForcedDisable(boolean value) {
        m_asyncForceDisabled = value;
    }

    /**
     * Check if async mode is forced
     *
     * @return
     */
    @Override
    public boolean isAsyncForced() {
        return m_asyncForced;
    }

    /**
     * This function checks if async mode is enabled for specific command
     *
     * @param operationName
     * @return
     */
    @Override
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
        boolean result = m_asyncForced
                || (ConfigProvider.isAsyncAllowed(operation)
                && m_player.getAweMode() && !m_asyncForceDisabled);

        m_asyncDisabled = !result;
        return result;
    }

    /**
     * Reset async disabled inner state (enable async mode)
     */
    @Override
    public void resetAsync() {
        m_asyncDisabled = false;
    }

    /**
     * Cancel a job
     *
     * @param jobId
     */
    protected void cancelJobs(final int jobId) {
        int minId = jobId;

        synchronized (m_asyncTasks) {
            for (IJobEntry job : m_asyncTasks) {
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
                IJobEntry job = m_blockPlacer.getJob(m_player, minId);
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
    @Override
    public void addAsync(IJobEntry job) {
        synchronized (m_asyncTasks) {
            m_asyncTasks.add(job);
        }
    }

    /**
     * Remov async job (done or canceled)
     *
     * @param job
     */
    @Override
    public void removeAsync(IJobEntry job) {
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

    private BlockState doGetBlock(BlockVector3 position) {
        return super.getBlock(position);
    }

    private BaseBlock doGetFullBlock(BlockVector3 position) {
        return super.getFullBlock(position);
    }

    public BiomeType doGetBiome(BlockVector2 position) {
        return super.getBiome(position);
    }

    public int doGetBlockChangeCount() {
        return super.getBlockChangeCount();
    }

    public int doGetBlockChangeLimit() {
        return super.getBlockChangeLimit();
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

    public BlockVector3 doGetMaximumPoint() {
        return super.getMaximumPoint();
    }

    public BlockVector3 doGetMinimumPoint() {
        return super.getMinimumPoint();
    }

    @Override
    public Iterator<Change> doUndo() {
        return getChangeSet().backwardIterator();
    }

    @Override
    public Iterator<Change> doRedo() {
        return getChangeSet().forwardIterator();
    }        
}
