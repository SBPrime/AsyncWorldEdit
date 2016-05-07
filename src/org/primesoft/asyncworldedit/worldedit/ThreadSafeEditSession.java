package org.primesoft.asyncworldedit.worldedit;

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
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.biome.BaseBiome;
import java.util.Iterator;
import java.util.List;
import org.bukkit.World;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;

/**
 * Stub for the API to compile
 * @author SBPrime
 */
public class ThreadSafeEditSession extends EditSessionStub {
    public Object getMutex() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public IBlockPlacer getBlockPlacer() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public PlayerEntry getPlayer() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public World getCBWorld() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public EventBus getEventBus() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public EditSessionEvent getEditSessionEvent() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public ThreadSafeEditSession() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }
    

    @Override
    public boolean setBlock(Vector position, BaseBlock block, Stage stage) throws WorldEditException {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public boolean setBlock(int jobId, Vector position, BaseBlock block, Stage stage) throws WorldEditException {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public boolean setBlockIfAir(Vector pt, BaseBlock block, int jobId)
            throws MaxChangedBlocksException {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public boolean setBlockIfAir(Vector position, BaseBlock block) throws MaxChangedBlocksException {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public boolean setBlock(Vector pt, Pattern pat, int jobId)
            throws MaxChangedBlocksException {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public boolean setBlock(Vector pt, BaseBlock block, int jobId)
            throws MaxChangedBlocksException {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Perform a custom action
     *     
     * @throws com.sk89q.worldedit.WorldEditException
     */
    @Override
    public void doCustomAction(final Change change, boolean isDemanding) throws WorldEditException
    {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override

    public boolean setBlock(Vector position, BaseBlock block) throws MaxChangedBlocksException {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public boolean setBlock(Vector position, Pattern pattern) throws MaxChangedBlocksException {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public boolean smartSetBlock(Vector pt, BaseBlock block) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public BaseBlock getBlock(final Vector position) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public int getBlockData(final Vector position) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public int getBlockType(final Vector position) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public BaseBlock getLazyBlock(final Vector position) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public BaseBiome getBiome(final Vector2D position) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public int getBlockChangeCount() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public int getBlockChangeLimit() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public List<Countable<Integer>> getBlockDistribution(final Region region) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public List<Countable<BaseBlock>> getBlockDistributionWithData(final Region region) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public List<? extends Entity> getEntities() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public List<? extends Entity> getEntities(final Region region) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public int getHighestTerrainBlock(final int x, final int z, final int minY, final int maxY) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public int getHighestTerrainBlock(final int x, final int z,
            final int minY, final int maxY, final boolean naturalOnly) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public Vector getMaximumPoint() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public Vector getMinimumPoint() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Do not change! Requires special processing
     *
     * @param sess
     */
    @Override
    public void undo(final EditSession sess) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public void flushQueue() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Enables or disables the async mode configuration bypass this function
     * should by used only by other plugins
     *
     * @param value true to enable async mode force
     */
    public void setAsyncForced(boolean value) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Check if async mode is forced
     *
     * @return
     */
    public boolean isAsyncForced() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * This function checks if async mode is enabled for specific command
     *
     * @param operationName
     * @return
     */
    public boolean checkAsync(String operationName) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * This function checks if async mode is enabled for specific command
     *
     * @param operation
     * @return
     */
    public boolean checkAsync(WorldeditOperations operation) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Reset async disabled inner state (enable async mode)
     */
    public void resetAsync() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Add async job
     *
     * @param job
     */
    public void addAsync(JobEntry job) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Remov async job (done or canceled)
     *
     * @param job
     */
    public void removeAsync(JobEntry job) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }


    public BaseBiome doGetBiome(Vector2D position) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public int doGetBlockChangeCount() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public int doGetBlockChangeLimit() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public List<Countable<Integer>> doGetBlockDistribution(Region region) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public List<Countable<BaseBlock>> doGetBlockDistributionWithData(Region region) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public List<? extends Entity> doGetEntities() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public List<? extends Entity> doGetEntities(Region region) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public int doGetHighestTerrainBlock(int x, int z, int minY, int maxY) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public int doGetHighestTerrainBlock(int x, int z, int minY, int maxY, boolean naturalOnly) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public Vector doGetMaximumPoint() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public Vector doGetMinimumPoint() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public Iterator<Change> doUndo() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public void doRedo(EditSession session) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }
}