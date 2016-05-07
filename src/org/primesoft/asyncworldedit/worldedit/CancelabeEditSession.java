package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Countable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stub for the API to compile
 * @author SBPrime
 */
public class CancelabeEditSession extends EditSessionStub {

    public CancelabeEditSession() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public boolean isCanceled() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public void cancel() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public int countBlock(Region region, Set<Integer> searchIDs) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public int countBlocks(Region region, Set<BaseBlock> searchBlocks) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public BaseBlock getBlock(Vector pt) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public BlockBag getBlockBag() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public int getBlockData(Vector pt) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public List<Countable<Integer>> getBlockDistribution(Region region) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public List<Countable<BaseBlock>> getBlockDistributionWithData(Region region) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public int getBlockType(Vector pt) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public int getHighestTerrainBlock(int x, int z, int minY, int maxY) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public int getHighestTerrainBlock(int x, int z, int minY, int maxY,
            boolean naturalOnly) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public Map<Integer, Integer> popMissingBlocks() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public BaseBlock rawGetBlock(Vector pt) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    
    /**
     * Perform a custom action
     *
     * @throws com.sk89q.worldedit.WorldEditException
     */
    @Override
    public void doCustomAction(final Change change, final boolean isDemanding) throws WorldEditException
    {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block, Stage stage)
            throws WorldEditException {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public boolean setBlock(Vector pt, BaseBlock block)
            throws MaxChangedBlocksException {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public boolean setBlock(Vector pt, Pattern pat)
            throws MaxChangedBlocksException {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public Entity createEntity(com.sk89q.worldedit.util.Location location, BaseEntity entity) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public boolean setBlockIfAir(Vector pt, BaseBlock block)
            throws MaxChangedBlocksException {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public boolean setChanceBlockIfAir(Vector pos, BaseBlock block, double c)
            throws MaxChangedBlocksException {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public void undo(EditSession sess) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public void doUndo(EditSession sess) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public void redo(EditSession sess) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public void doRedo(EditSession sess) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public boolean smartSetBlock(Vector pt, BaseBlock block) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public void resetAsync() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public ThreadSafeEditSession getParent() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public void flushQueue() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }
}
