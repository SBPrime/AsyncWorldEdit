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

import org.primesoft.asyncworldedit.worldedit.blocks.BaseBlockWrapper;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.ChangeSetExtent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Countable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.ActionEntryEx;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.utils.ActionEx;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.utils.SessionCanceled;
import org.primesoft.asyncworldedit.worldedit.entity.BaseEntityWrapper;
import org.primesoft.asyncworldedit.worldedit.world.CancelableWorld;
import org.primesoft.asyncworldedit.worldedit.util.LocationWrapper;

/**
 *
 * @author SBPrime
 */
public class CancelabeEditSession extends EditSessionStub {

    private final ThreadSafeEditSession m_parent;

    private final CancelableWorld m_cWorld;

    private final int m_jobId;

    private final PlayerEntry m_player;

    /**
     * Number of queued blocks
     */
    private int m_blocksQueued;

    public CancelabeEditSession(ThreadSafeEditSession parent, Mask mask, int jobId) {
        super(parent.getEventBus(),
                new CancelableWorld(parent.getWorld(), jobId, parent.getPlayer()),
                parent.getBlockChangeLimit(), parent.getBlockBag(),
                parent.getEditSessionEvent());

        m_jobId = jobId;
        m_parent = parent;
        m_player = m_parent.getPlayer();
        m_cWorld = (CancelableWorld) getWorld();

        injectChangeSet(parent.getChangeSet());
        setMask(mask);
    }

    private void injectChangeSet(ChangeSet changeSet) {
        ChangeSetExtent changesetExtent = Reflection.get(EditSession.class, ChangeSetExtent.class,
                this, "changeSetExtent", "Unable to get the changeset");

        if (changesetExtent == null) {
            AsyncWorldEditMain.log("Unable to get the changeSet from EditSession, undo and redo broken.");
            return;
        }

        Reflection.set(EditSession.class, this, "changeSet", changeSet,
                "Unable to inject ChangeSet, undo and redo broken.");
        Reflection.set(ChangeSetExtent.class, changesetExtent, "changeSet", changeSet,
                "Unable to inject changeset to extent, undo and redo broken.");
    }

    public boolean isCanceled() {
        return m_cWorld.isCanceled();
    }

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
    public void doCustomAction(final Change change, final boolean isDemanding) throws WorldEditException
    {
        if (m_cWorld.isCanceled()) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        final ChangeSet cs = getChangeSet();        
        final UndoContext undoContext = new UndoContext();
        undoContext.setExtent(this);
        
        
        final ActionEx<WorldEditException> action = new ActionEx<WorldEditException>() {
            @Override
            public void execute() throws WorldEditException {
                cs.add(change);
                change.redo(undoContext);
            }
        };

        BlockPlacerEntry entry = new ActionEntryEx(m_jobId, action, isDemanding);

        m_parent.getBlockPlacer().addTasks(m_player, entry);
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
        Mask mask = sess.getMask();
        sess.setMask(getMask());
        m_parent.doRedo(sess);
        sess.setMask(mask);
    }

    @Override
    public boolean smartSetBlock(Vector pt, BaseBlock block) {
        if (m_cWorld.isCanceled()) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return super.smartSetBlock(VectorWrapper.wrap(pt, m_jobId, true, m_player),
                BaseBlockWrapper.wrap(block, m_jobId, true, m_player));
    }

    public void resetAsync() {
        m_parent.resetAsync();
    }

    public ThreadSafeEditSession getParent() {
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
