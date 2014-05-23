/*
 * The MIT License
 *
 * Copyright 2013 SBPrime.
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

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Countable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author SBPrime
 */
public class CancelabeEditSession extends EditSessionStub {

    public class SessionCanceled extends Exception {
    }
    private final AsyncEditSession m_parent;
    private boolean m_isCanceled;
    private final int m_jobId;
    private final UUID m_player;

    public CancelabeEditSession(AsyncEditSession parent, Mask mask, int jobId) {
        super(parent.getEventBus(), parent.getWorld(), parent.getBlockChangeLimit(),
                parent.getBlockBag(), parent.getEditSessionEvent());

        m_jobId = jobId;
        m_parent = parent;
        m_player = m_parent.getPlayer();
        m_isCanceled = false;
        setMask(mask);
    }

    public boolean isCanceled() {
        return m_isCanceled;
    }

    public void cancel() {
        m_isCanceled = true;
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
        return m_parent.getBlock(pt);
    }

    @Override
    public BlockBag getBlockBag() {
        return m_parent.getBlockBag();
    }

    @Override
    public int getBlockChangeCount() {
        return m_parent.getBlockChangeCount();
    }

    @Override
    public int getBlockChangeLimit() {
        return m_parent.getBlockChangeLimit();
    }

    @Override
    public int getBlockData(Vector pt) {
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
        return m_parent.getBlockType(pt);
    }

    @Override
    public int getHighestTerrainBlock(int x, int z, int minY, int maxY) {
        return m_parent.getHighestTerrainBlock(x, z, minY, maxY);
    }

    @Override
    public int getHighestTerrainBlock(int x, int z, int minY, int maxY, boolean naturalOnly) {
        return m_parent.getHighestTerrainBlock(x, z, minY, maxY, naturalOnly);
    }

    @Override
    public Map<Integer, Integer> popMissingBlocks() {
        return m_parent.popMissingBlocks();
    }

    @Override
    public BaseBlock rawGetBlock(Vector pt) {
        return m_parent.rawGetBlock(pt);
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block, Stage stage) throws WorldEditException {
        //TODO: Move to world/stack -> requires additional extent or wrapper
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return super.setBlock(position, BaseBlockWrapper.wrap(block, m_jobId, true, m_player), stage);
    }

    @Override
    public boolean setBlock(Vector pt, BaseBlock block)
            throws MaxChangedBlocksException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return super.setBlock(pt, BaseBlockWrapper.wrap(block, m_jobId, true, m_player));
    }

    @Override
    public boolean setBlock(Vector pt, Pattern pat) throws MaxChangedBlocksException {
            if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return super.setBlock(pt, pat);
    }

    @Override
    public boolean setBlockIfAir(Vector pt, BaseBlock block) throws MaxChangedBlocksException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return super.setBlockIfAir(pt, BaseBlockWrapper.wrap(block, m_jobId, true, m_player));
    }

    @Override
    public boolean setChanceBlockIfAir(Vector pos, BaseBlock block, double c) throws MaxChangedBlocksException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return super.setChanceBlockIfAir(pos, BaseBlockWrapper.wrap(block, m_jobId, true, m_player), c);
    }

    @Override
    public void undo(EditSession sess) {
        doUndo(sess);
    }

    public void doUndo(EditSession sess) {
        //checkAsync(WorldeditOperations.undo);
        UndoSession undoSession = m_parent.doUndo();

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
    public void redo(EditSession sess) {
        doRedo(sess);
    }

    public void doRedo(EditSession sess) {
        Mask mask = sess.getMask();
        sess.setMask(getMask());
        m_parent.doRedo(sess);
        //super.redo(sess);
        sess.setMask(mask);
    }

    @Override
    public boolean smartSetBlock(Vector pt, BaseBlock block) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return super.smartSetBlock(pt, BaseBlockWrapper.wrap(block, m_jobId, true, m_player));
    }

    public void resetAsync() {
        m_parent.resetAsync();
    }

    public AsyncEditSession getParent() {
        return m_parent;
    }
}