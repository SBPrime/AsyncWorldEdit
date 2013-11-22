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
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author SBPrime
 */
public class CancelabeEditSession extends EditSession {
    public class SessionCanceled extends Exception {
    }
    private final AsyncEditSession m_parent;
    private boolean m_isCanceled;
    private int m_jobId;
    private Mask m_mask;

    public CancelabeEditSession(AsyncEditSession parent, int jobId) {
        super(parent.getWorld(), parent.getBlockChangeLimit());

        m_jobId = jobId;
        m_parent = parent;
        m_isCanceled = false;
        m_mask = parent.getMask();
    }

    public void cancel() {
        m_isCanceled = true;
    }

    @Override
    public int center(Region region, Pattern pattern) throws MaxChangedBlocksException {
        return m_parent.center(region, pattern);
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
    public void disableQueue() {
        m_parent.disableQueue();
    }

    @Override
    public void enableQueue() {
        m_parent.enableQueue();
    }


    @Override
    public void flushQueue() {
        m_parent.flushQueue(m_jobId);
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
    public Mask getMask() {
        return m_mask;
    }

    @Override
    public LocalWorld getWorld() {
        return m_parent.getWorld();
    }

    @Override
    public boolean hasFastMode() {
        return m_parent.hasFastMode();
    }

    @Override
    public boolean isQueueEnabled() {
        return m_parent.isQueueEnabled();
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
    public boolean rawSetBlock(Vector pt, BaseBlock block) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        if (m_mask != null) {
            if (!m_mask.matches(this, pt)) {
                return false;
            }
        }

        return m_parent.rawSetBlock(pt, m_jobId, block);
    }

    @Override
    public void rememberChange(Vector pt, BaseBlock existing, BaseBlock block) {
        m_parent.rememberChange(pt, existing, block);
    }

    @Override
    public boolean setBlock(Vector pt, BaseBlock block)
            throws MaxChangedBlocksException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.setBlock(pt, block);
    }

    @Override
    public boolean setBlock(Vector pt, Pattern pat) throws MaxChangedBlocksException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.setBlock(pt, pat);
    }

    @Override
    public void setBlockBag(BlockBag blockBag) {
        m_parent.setBlockBag(blockBag);
    }

    @Override
    public void setBlockChangeLimit(int maxBlocks) {
        m_parent.setBlockChangeLimit(maxBlocks);
    }

    @Override
    public boolean setBlockIfAir(Vector pt, BaseBlock block) throws MaxChangedBlocksException {
        return m_parent.setBlockIfAir(pt, block);
    }

    @Override
    public boolean setChanceBlockIfAir(Vector pos, BaseBlock block, double c) throws MaxChangedBlocksException {
        return m_parent.setChanceBlockIfAir(pos, block, c);
    }

    @Override
    public void setFastMode(boolean fastMode) {
        m_parent.setFastMode(fastMode);
    }

    @Override
    public void setMask(Mask mask) {
        m_mask = mask;
    }

    public void setWorld(LocalWorld world) {
        this.world = world;
    }

    @Override
    public int size() {
        return m_parent.size();
    }

    @Override
    public void undo(EditSession sess) {
        m_parent.doUndo(sess);
    }

    @Override
    public void redo(EditSession sess) {
        m_parent.doRedo(sess);
    }
    
    

    @Override
    public boolean smartSetBlock(Vector pt, BaseBlock block) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.smartSetBlock(pt, block);
    }

    public void resetAsync() {
        m_parent.resetAsync();
    }
    
    
    public AsyncEditSession getParent() {
        return m_parent;
    }

}
