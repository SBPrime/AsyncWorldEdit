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

import org.primesoft.asyncworldedit.api.worldedit.ICancelabeEditSession;
import org.primesoft.asyncworldedit.worldedit.blocks.BaseBlockWrapper;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.ChangeSetExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.inventory.BlockBagExtent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Countable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.primesoft.asyncworldedit.core.AwePlatform;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.worldedit.IThreadSafeEditSession;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerChange;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.ActionEntryEx;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.api.utils.IActionEx;
import org.primesoft.asyncworldedit.events.EditSessionLimitChanged;
import org.primesoft.asyncworldedit.utils.ExtentUtils;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.utils.SessionCanceled;
import org.primesoft.asyncworldedit.worldedit.entity.BaseEntityWrapper;
import org.primesoft.asyncworldedit.worldedit.extent.ExtendedChangeSetExtent;
import org.primesoft.asyncworldedit.worldedit.extent.inventory.FixedBlockBagExtent;
import org.primesoft.asyncworldedit.worldedit.history.changeset.FileChangeSet;
import org.primesoft.asyncworldedit.worldedit.history.changeset.IExtendedChangeSet;
import org.primesoft.asyncworldedit.worldedit.history.changeset.NullChangeSet;
import org.primesoft.asyncworldedit.worldedit.world.CancelableWorld;
import org.primesoft.asyncworldedit.worldedit.util.LocationWrapper;

/**
 *
 * @author SBPrime
 */
public class CancelabeEditSession extends AweEditSession implements ICancelabeEditSession {

    private final IThreadSafeEditSession m_parent;

    private final CancelableWorld m_cWorld;

    private final int m_jobId;

    private final IPlayerEntry m_player;

    /**
     * Number of queued blocks
     */
    private int m_blocksQueued;

    public CancelabeEditSession(IThreadSafeEditSession parent, Mask mask, int jobId) {
        super(parent.getEventBus(),
                new CancelableWorld(parent.getWorld(), jobId, parent.getPlayer()),
                parent.getBlockChangeLimit(), parent.getBlockBag(),
                parent.getEditSessionEvent());

        m_jobId = jobId;
        m_parent = parent;
        m_player = m_parent.getPlayer();
        m_cWorld = (CancelableWorld) getWorld();

        ChangeSet tmp = m_parent.getRootChangeSet();
        if (tmp instanceof FileChangeSet) {
            ((FileChangeSet) tmp).setCancelable(this);
        }

        boolean isDebug = ConfigProvider.isDebugOn();
        if (isDebug) {
            ExtentUtils.dumpExtents("Original extents:", this);
        }

        injectExtents(parent.getPlayer());

        if (isDebug) {
            ExtentUtils.dumpExtents("Injected extents:", this);
        }

        setMask(mask);
    }

    @Override
    public int getJobId() {
        return m_jobId;
    }

    @Override
    public IPlayerEntry getPlayer() {
        return m_player;
    }

    private void injectExtents(IPlayerEntry playerEntry) {
        List<Extent> extentList = ExtentUtils.getExtentList(this);

        injectBlockBagExtent(extentList);
        injectChangeSet(extentList, m_parent.getChangeSet(), playerEntry);
    }

    private void injectBlockBagExtent(List<Extent> extentList) {
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

        BlockBagExtent newBlockBag = new FixedBlockBagExtent(
                AwePlatform.getInstance().getCore().getBlocksHubBridge(),
                m_player, m_parent.getCBWorld(), afterExtent, blockBag);

        if (!ExtentUtils.setExtent(beforeExtent, newBlockBag)) {
            log("Unable to set the blockBagExtent from EditSession, block bag broken.");
            return;
        }

        Reflection.set(EditSession.class, this, "blockBagExtent", newBlockBag,
                "Unable to set the blockBagExtent from EditSession, block bag broken.");
    }

    private void injectChangeSet(List<Extent> extentList, ChangeSet changeSet, IPlayerEntry playerEntry) {
        ChangeSetExtent changesetExtent = Reflection.get(EditSession.class, ChangeSetExtent.class,
                this, "changeSetExtent", "Unable to get the changeset");
        if (changesetExtent == null) {
            log("Unable to get the changeSet from EditSession, undo and redo broken.");
            return;
        }

        Extent beforeExtent = ExtentUtils.findBeforer(extentList, changesetExtent);
        Extent afterExtent = changesetExtent.getExtent();
        if (afterExtent == null || beforeExtent == null) {
            log("Unable to get the changesetExtent from EditSession, undo broken.");
            return;
        }

        IPermissionGroup pg = playerEntry.getPermissionGroup();
        boolean undoDisabled = playerEntry.isUndoOff();

        if (undoDisabled) {
            changeSet = new NullChangeSet();
        } else  if (changeSet instanceof IExtendedChangeSet) {
            IExtendedChangeSet aweChangeSet = (IExtendedChangeSet) changeSet;

            ExtendedChangeSetExtent extendedChangeSetExtent = new ExtendedChangeSetExtent(this, afterExtent, aweChangeSet);
            ExtentUtils.setExtent(beforeExtent, extendedChangeSetExtent);
        } else {
            log(String.format("Expected changeSet: IExtendedChangeSet but got %1$s, undo broken.",
                    changeSet != null ? changeSet.getClass().getName() : "<null>"));
            
        }

        Reflection.set(EditSession.class, this, "changeSet", changeSet,
                "Unable to inject ChangeSet, undo and redo broken.");
        Reflection.set(ChangeSetExtent.class, changesetExtent, "changeSet", changeSet,
                "Unable to inject changeset to extent, undo and redo broken.");
    }

    @Override
    public boolean isCanceled() {
        return m_cWorld.isCanceled();
    }

    @Override
    public void cancel() {
        m_cWorld.cancel();
    }

    @Override
    public int countBlock(Region region, Set<Integer> searchIDs) {
        return m_parent.countBlock(region, searchIDs);
    }

    @Override
    public int countBlocks(Region region, Set<BaseBlock> searchBlocks) {
        return m_parent.countBlocks(region, searchBlocks);
    }

    @Override
    public BaseBlock getBlock(Vector pt) {
        if (m_cWorld.isCanceled()) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.getBlock(pt);
    }

    @Override
    public BlockBag getBlockBag() {
        return m_parent.getBlockBag();
    }

    @Override
    public int getBlockData(Vector pt) {
        if (m_cWorld.isCanceled()) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.getBlockData(pt);
    }

    @Override
    public List<Countable<Integer>> getBlockDistribution(Region region) {
        return m_parent.getBlockDistribution(region);
    }

    @Override
    public List<Countable<BaseBlock>> getBlockDistributionWithData(Region region) {
        return m_parent.getBlockDistributionWithData(region);
    }

    @Override
    public int getBlockType(Vector pt) {
        if (m_cWorld.isCanceled()) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.getBlockType(pt);
    }

    @Override
    public int getHighestTerrainBlock(int x, int z, int minY, int maxY) {
        return m_parent.getHighestTerrainBlock(x, z, minY, maxY);
    }

    @Override
    public int getHighestTerrainBlock(int x, int z, int minY, int maxY,
            boolean naturalOnly) {
        return m_parent.getHighestTerrainBlock(x, z, minY, maxY, naturalOnly);
    }

    @Override
    public Map<Integer, Integer> popMissingBlocks() {
        return m_parent.popMissingBlocks();
    }

    @Override
    public BaseBlock rawGetBlock(Vector pt) {
        if (m_cWorld.isCanceled()) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.rawGetBlock(pt);
    }

    /**
     * Perform a custom action
     *
     * @throws com.sk89q.worldedit.WorldEditException
     */
    @Override
    public void doCustomAction(final Change change, final boolean isDemanding) throws WorldEditException {
        if (m_cWorld.isCanceled()) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        final ChangeSet cs = getChangeSet();
        final IExtendedChangeSet ecs = (cs instanceof IExtendedChangeSet) ? (IExtendedChangeSet) cs : null;

        final UndoContext undoContext = new UndoContext();
        undoContext.setExtent(this);

        final IBlockPlacer blockPlacer = m_parent.getBlockPlacer();
        final Change safeChange = new BlockPlacerChange(change, blockPlacer, isDemanding);

        final IActionEx<WorldEditException> action = new IActionEx<WorldEditException>() {
            @Override
            public void execute() throws WorldEditException {
                change.redo(undoContext);
            }
        };

        if (ecs != null) {
            ecs.addExtended(safeChange, this);
        } else {
            cs.add(safeChange);
        }

        BlockPlacerEntry entry = new ActionEntryEx(m_jobId, action, isDemanding);
        blockPlacer.addTasks(m_player, entry);
    }

    @Override
    public void setBlockChangeLimit(int limit) {
        m_parent.setBlockChangeLimit(limit);
    }

    @Override
    public int getBlockChangeLimit() {
        return m_parent.getBlockChangeCount();
    }

    @Override
    public int getBlockChangeCount() {
        return m_parent.getBlockChangeCount();
    }
        
    
    @Override
    public void rememberChange(Vector position, BaseBlock existing, BaseBlock block) {
        final ChangeSet cs = getChangeSet();
        final IExtendedChangeSet ecs = (cs instanceof IExtendedChangeSet) ? (IExtendedChangeSet) cs : null;
        final Change change = new BlockChange(position.toBlockVector(), existing, block);

        if (ecs != null) {
            try {
                ecs.addExtended(change, this);
            } catch (WorldEditException ex) {
            }
        } else {
            cs.add(change);
        }
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block, Stage stage)
            throws WorldEditException {
        if (m_cWorld.isCanceled()) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        forceFlush();
        return super.setBlock(VectorWrapper.wrap(position, m_jobId, true, m_player),
                BaseBlockWrapper.wrap(block, m_jobId, true, m_player), stage);
    }

    @Override
    public boolean setBlock(Vector pt, BaseBlock block)
            throws MaxChangedBlocksException {

        if (m_cWorld.isCanceled()) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return super.setBlock(VectorWrapper.wrap(pt, m_jobId, true, m_player),
                BaseBlockWrapper.wrap(block, m_jobId, true, m_player));
    }

    @Override
    public boolean setBlock(Vector pt, Pattern pat)
            throws MaxChangedBlocksException {

        if (m_cWorld.isCanceled()) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return super.setBlock(VectorWrapper.wrap(pt, m_jobId, true, m_player), pat);
    }

    @Override
    public Entity createEntity(com.sk89q.worldedit.util.Location location, BaseEntity entity) {
        if (m_cWorld.isCanceled()) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return super.createEntity(LocationWrapper.wrap(location, m_jobId, true, m_player),
                BaseEntityWrapper.wrap(entity, m_jobId, true, m_player));
    }

    @Override
    public boolean setBlockIfAir(Vector pt, BaseBlock block)
            throws MaxChangedBlocksException {
        if (m_cWorld.isCanceled()) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return super.setBlockIfAir(VectorWrapper.wrap(pt, m_jobId, true, m_player),
                BaseBlockWrapper.wrap(block, m_jobId, true, m_player));
    }

    @Override
    public boolean setChanceBlockIfAir(Vector pos, BaseBlock block, double c)
            throws MaxChangedBlocksException {
        if (m_cWorld.isCanceled()) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return super.setChanceBlockIfAir(VectorWrapper.wrap(pos, m_jobId, true, m_player),
                BaseBlockWrapper.wrap(block, m_jobId, true, m_player), c);
    }

    @Override
    public void undo(EditSession sess) {
        doUndo(sess);
    }

    public void doUndo(EditSession sess) {
        UndoProcessor.processUndo(m_parent, this, sess);
    }

    @Override
    public void redo(EditSession sess) {
        doRedo(sess);
    }

    public void doRedo(EditSession sess) {
        RedoProcessor.processRedo(m_parent, this, sess);
    }

    @Override
    public boolean smartSetBlock(Vector pt, BaseBlock block) {
        if (m_cWorld.isCanceled()) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return super.smartSetBlock(VectorWrapper.wrap(pt, m_jobId, true, m_player),
                BaseBlockWrapper.wrap(block, m_jobId, true, m_player));
    }

    @Override
    public void resetAsync() {
        m_parent.resetAsync();
    }

    @Override
    public IThreadSafeEditSession getParent() {
        return m_parent;
    }

    @Override
    public void flushQueue() {
        m_blocksQueued = 0;
        super.flushQueue();
    }

    /**
     * Force block flush when to many has been queued
     */
    private void forceFlush() {
        int maxBlocks = ConfigProvider.getForceFlushBlocks();

        if (isQueueEnabled() && (maxBlocks != -1)) {
            m_blocksQueued++;
            if (m_blocksQueued > maxBlocks) {
                m_blocksQueued = 0;
                super.flushQueue();
            }
        }
    }
}
