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
import com.sk89q.worldedit.expression.ExpressionException;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator;
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
        super(null, -1);

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
    public int deformRegion(Region region, Vector zero, Vector unit, String expressionString) throws ExpressionException, MaxChangedBlocksException {
        return m_parent.deformRegion(region, zero, unit, expressionString);
    }

    @Override
    public void disableQueue() {
        m_parent.disableQueue();
    }

    @Override
    public int drainArea(Vector pos, double radius) throws MaxChangedBlocksException {
        return m_parent.drainArea(pos, radius);
    }

    @Override
    public void enableQueue() {
        m_parent.enableQueue();
    }

    @Override
    public int fillXZ(Vector origin, BaseBlock block, double radius, int depth, boolean recursive) throws MaxChangedBlocksException {
        return m_parent.fillXZ(origin, block, radius, depth, recursive);
    }

    @Override
    public int fillXZ(Vector origin, Pattern pattern, double radius, int depth, boolean recursive) throws MaxChangedBlocksException {
        return m_parent.fillXZ(origin, pattern, radius, depth, recursive);
    }

    @Override
    public int fixLiquid(Vector pos, double radius, int moving, int stationary) throws MaxChangedBlocksException {
        return m_parent.fixLiquid(pos, radius, moving, stationary);
    }

    @Override
    public void flushQueue() {
        m_parent.flushQueue();
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
    public int green(Vector pos, double radius) throws MaxChangedBlocksException {
        return m_parent.green(pos, radius);
    }

    @Override
    public boolean hasFastMode() {
        return m_parent.hasFastMode();
    }

    @Override
    public int hollowOutRegion(Region region, int thickness, Pattern pattern) throws MaxChangedBlocksException {
        return m_parent.hollowOutRegion(region, thickness, pattern);
    }

    @Override
    public boolean isQueueEnabled() {
        return m_parent.isQueueEnabled();
    }

    @Override
    public int makeCuboidFaces(Region region, BaseBlock block) throws MaxChangedBlocksException {
        return m_parent.makeCuboidFaces(region, block);
    }

    @Override
    public int makeCuboidFaces(Region region, Pattern pattern) throws MaxChangedBlocksException {
        return m_parent.makeCuboidFaces(region, pattern);
    }

    @Override
    public int makeCuboidWalls(Region region, BaseBlock block) throws MaxChangedBlocksException {
        return m_parent.makeCuboidWalls(region, block);
    }

    @Override
    public int makeCuboidWalls(Region region, Pattern pattern) throws MaxChangedBlocksException {
        return m_parent.makeCuboidWalls(region, pattern);
    }

    @Override
    public int makeCylinder(Vector pos, Pattern block, double radius, int height, boolean filled) throws MaxChangedBlocksException {
        return m_parent.makeCylinder(pos, block, radius, height, filled);
    }

    @Override
    public int makeCylinder(Vector pos, Pattern block, double radiusX, double radiusZ, int height, boolean filled) throws MaxChangedBlocksException {
        return m_parent.makeCylinder(pos, block, radiusX, radiusZ, height, filled);
    }

    @Override
    public int makeForest(Vector basePos, int size, double density, TreeGenerator treeGenerator) throws MaxChangedBlocksException {
        return m_parent.makeForest(basePos, size, density, treeGenerator);
    }

    @Override
    public int makePumpkinPatches(Vector basePos, int size) throws MaxChangedBlocksException {
        return m_parent.makePumpkinPatches(basePos, size);
    }

    @Override
    public int makePyramid(Vector pos, Pattern block, int size, boolean filled) throws MaxChangedBlocksException {
        return m_parent.makePyramid(pos, block, size, filled);
    }

    @Override
    public int makeShape(Region region, Vector zero, Vector unit, Pattern pattern, String expressionString, boolean hollow) throws ExpressionException, MaxChangedBlocksException {
        return m_parent.makeShape(region, zero, unit, pattern, expressionString, hollow);
    }

    @Override
    public int makeSphere(Vector pos, Pattern block, double radius, boolean filled) throws MaxChangedBlocksException {
        return m_parent.makeSphere(pos, block, radius, filled);
    }

    @Override
    public int makeSphere(Vector pos, Pattern block, double radiusX, double radiusY, double radiusZ, boolean filled) throws MaxChangedBlocksException {
        return m_parent.makeSphere(pos, block, radiusX, radiusY, radiusZ, filled);
    }

    @Override
    public int naturalizeCuboidBlocks(Region region) throws MaxChangedBlocksException {
        return m_parent.naturalizeCuboidBlocks(region);
    }

    @Override
    public int overlayCuboidBlocks(Region region, BaseBlock block) throws MaxChangedBlocksException {
        return m_parent.overlayCuboidBlocks(region, block);
    }

    @Override
    public int overlayCuboidBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
        return m_parent.overlayCuboidBlocks(region, pattern);
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
    public void redo(EditSession sess) {
        m_parent.redo(sess);
    }

    @Override
    public void rememberChange(Vector pt, BaseBlock existing, BaseBlock block) {
        m_parent.rememberChange(pt, existing, block);
    }

    @Override
    public int removeAbove(Vector pos, int size, int height) throws MaxChangedBlocksException {
        return m_parent.removeAbove(pos, size, height);
    }

    @Override
    public int removeBelow(Vector pos, int size, int height) throws MaxChangedBlocksException {
        return m_parent.removeBelow(pos, size, height);
    }

    @Override
    public int removeNear(Vector pos, int blockType, int size) throws MaxChangedBlocksException {
        return m_parent.removeNear(pos, blockType, size);
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
    public int simulateSnow(Vector pos, double radius) throws MaxChangedBlocksException {
        return m_parent.simulateSnow(pos, radius);
    }

    @Override
    public int size() {
        return m_parent.size();
    }

    @Override
    public int stackCuboidRegion(Region region, Vector dir, int count, boolean copyAir) throws MaxChangedBlocksException {
        return m_parent.stackCuboidRegion(region, dir, count, copyAir);
    }

    @Override
    public int thaw(Vector pos, double radius) throws MaxChangedBlocksException {
        return m_parent.thaw(pos, radius);
    }

    @Override
    public void undo(EditSession sess) {
        m_parent.undo(sess);
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
