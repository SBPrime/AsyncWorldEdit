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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.expression.ExpressionException;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator;
import java.util.Map;
import java.util.Set;
import javax.swing.text.PlainDocument;
import org.bukkit.World;
import org.primesoft.asyncworldedit.BlockPlacer;
import org.primesoft.asyncworldedit.BlockPlacerEntry;
import org.primesoft.asyncworldedit.ConfigProvider;
import org.primesoft.asyncworldedit.PluginMain;

/**
 *
 * @author SBPrime
 */
public class AsyncEditSession extends EditSession
{
    private String m_player;

    private BlockPlacer m_blockPlacer;

    private World m_world;

    /**
     * Force all functions to by performed in async mode this is used to
     * override the config by API calls
     */
    private boolean m_asyncForced;

    /**
     * Indicates that the async mode has been disabled (inner state)
     */
    private boolean m_asyncDisabled;

    public String getPlayer()
    {
        return m_player;
    }

    public AsyncEditSession(PluginMain plugin, String player,
                            LocalWorld world, int maxBlocks)
    {
        super(world, maxBlocks);
        initialize(player, plugin, world);
    }

    public AsyncEditSession(PluginMain plugin, String player,
                            LocalWorld world, int maxBlocks, BlockBag blockBag)
    {
        super(world, maxBlocks, blockBag);
        initialize(player, plugin, world);
    }

    @Override
    public boolean rawSetBlock(Vector pt, BaseBlock block)
    {
        if (m_asyncForced || (PluginMain.hasAsyncMode(m_player) && !m_asyncDisabled))
        {
            return m_blockPlacer.addTasks(new BlockPlacerEntry(this, pt, block));
        } else
        {
            return super.rawSetBlock(pt, block);
        }
    }

    @Override
    public void flushQueue()
    {
        boolean queued = isQueueEnabled();
        super.flushQueue();
        if (queued)
        {
            resetAsync();
        }
    }

    @Override
    public void undo(EditSession sess)
    {
        checkAsync(WorldeditOperations.undo);
        UndoSession undoSession = new UndoSession();
        super.undo(undoSession);

        Map.Entry<Vector, BaseBlock>[] blocks = undoSession.getEntries();
        for (int i = blocks.length - 1; i >= 0; i--)
        {
            Map.Entry<Vector, BaseBlock> entry = blocks[i];
            sess.smartSetBlock(entry.getKey(), entry.getValue());
        }

        sess.flushQueue();
        if (!isQueueEnabled())
        {
            resetAsync();
        }
    }

    @Override
    public void redo(EditSession sess)
    {
        checkAsync(WorldeditOperations.redo);
        super.redo(sess);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
    }

    @Override
    public int fillXZ(Vector origin, BaseBlock block, double radius, int depth,
                      boolean recursive)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.fillXZ);
        int result = super.fillXZ(origin, block, radius, depth, recursive);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int fillXZ(Vector origin, Pattern pattern, double radius, int depth,
                      boolean recursive)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.fillXZ);
        int result = super.fillXZ(origin, pattern, radius, depth, recursive);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int removeAbove(Vector pos, int size, int height)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.removeAbove);
        int result = super.removeAbove(pos, size, height);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int removeBelow(Vector pos, int size, int height)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.removeBelow);
        int result = super.removeBelow(pos, size, height);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int removeNear(Vector pos, int blockType, int size)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.removeNear);
        int result = super.removeNear(pos, blockType, size);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int setBlocks(Region region, BaseBlock block)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.setBlocks);
        int result = super.setBlocks(region, block);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int setBlocks(Region region, Pattern pattern)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.setBlocks);
        int result = super.setBlocks(region, pattern);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int replaceBlocks(Region region,
                             Set<BaseBlock> fromBlockTypes, BaseBlock toBlock)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.replaceBlocks);
        int result = super.replaceBlocks(region, fromBlockTypes, toBlock);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int replaceBlocks(Region region,
                             Set<BaseBlock> fromBlockTypes, Pattern pattern)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.replaceBlocks);
        int result = super.replaceBlocks(region, fromBlockTypes, pattern);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int makeCuboidFaces(Region region, BaseBlock block)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.makeCuboidFaces);
        int result = super.makeCuboidFaces(region, block);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int makeCuboidFaces(Region region, Pattern pattern)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.makeCuboidFaces);
        int result = super.makeCuboidFaces(region, pattern);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int makeCuboidWalls(Region region, BaseBlock block)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.makeCuboidWalls);
        int result = super.makeCuboidWalls(region, block);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int makeCuboidWalls(Region region, Pattern pattern)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.makeCuboidWalls);
        int result = super.makeCuboidWalls(region, pattern);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int overlayCuboidBlocks(Region region, BaseBlock block)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.overlayCuboidBlocks);
        int result = super.overlayCuboidBlocks(region, block);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int overlayCuboidBlocks(Region region, Pattern pattern)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.overlayCuboidBlocks);
        int result = super.overlayCuboidBlocks(region, pattern);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int naturalizeCuboidBlocks(Region region)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.naturalizeCuboidBlocks);
        int result = super.naturalizeCuboidBlocks(region);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int stackCuboidRegion(Region region, Vector dir, int count,
                                 boolean copyAir)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.stackCuboidRegion);
        int result = super.stackCuboidRegion(region, dir, count, copyAir);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int moveCuboidRegion(Region region, Vector dir, int distance,
                                boolean copyAir, BaseBlock replace)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.moveCuboidRegion);
        int result = super.moveCuboidRegion(region, dir, distance, copyAir, replace);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int drainArea(Vector pos, double radius)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.drainArea);
        int result = super.drainArea(pos, radius);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int fixLiquid(Vector pos, double radius, int moving, int stationary)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.fixLiquid);
        int result = super.fixLiquid(pos, radius, moving, stationary);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int makeCylinder(Vector pos, Pattern block, double radius, int height,
                            boolean filled)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.makeCylinder);
        int result = super.makeCylinder(pos, block, radius, height, filled);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int makeCylinder(Vector pos, Pattern block, double radiusX,
                            double radiusZ, int height, boolean filled)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.makeCylinder);
        int result = super.makeCylinder(pos, block, radiusX, radiusZ, height, filled);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int makeSphere(Vector pos, Pattern block, double radius,
                          boolean filled)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.makeSphere);
        int result = super.makeSphere(pos, block, radius, filled);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int makeSphere(Vector pos, Pattern block, double radiusX,
                          double radiusY, double radiusZ, boolean filled)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.makeSphere);
        int result = super.makeSphere(pos, block, radiusX, radiusY, radiusZ, filled);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int makePyramid(Vector pos, Pattern block, int size, boolean filled)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.makePyramid);
        int result = super.makePyramid(pos, block, size, filled);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int thaw(Vector pos, double radius)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.thaw);
        int result = super.thaw(pos, radius);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int simulateSnow(Vector pos, double radius)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.simulateSnow);
        int result = super.simulateSnow(pos, radius);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int green(Vector pos, double radius)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.green);
        int result = super.green(pos, radius);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int makePumpkinPatches(Vector basePos, int size)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.makePumpkinPatches);
        int result = super.makePumpkinPatches(basePos, size);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int makeForest(Vector basePos, int size, double density,
                          TreeGenerator treeGenerator)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.makeForest);
        int result = super.makeForest(basePos, size, density, treeGenerator);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int makeShape(Region region, Vector zero, Vector unit,
                         Pattern pattern, String expressionString,
                         boolean hollow)
            throws ExpressionException, MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.makeShape);
        int result = super.makeShape(region, zero, unit, pattern, expressionString, hollow);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int deformRegion(Region region, Vector zero, Vector unit,
                            String expressionString)
            throws ExpressionException, MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.deformRegion);
        int result = super.deformRegion(region, zero, unit, expressionString);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    @Override
    public int hollowOutRegion(Region region, int thickness, Pattern pattern)
            throws MaxChangedBlocksException
    {
        checkAsync(WorldeditOperations.hollowOutRegion);
        int result = super.hollowOutRegion(region, thickness, pattern);
        if (!isQueueEnabled())
        {
            resetAsync();
        }
        return result;
    }

    public boolean doRawSetBlock(Vector pt, BaseBlock block)
    {
        return super.rawSetBlock(pt, block);
    }

    public World getCBWorld()
    {
        return m_world;
    }

    /**
     * Initialize the local veriables
     *
     * @param player edit session owner
     * @param plugin parent plugin
     * @param world edit session world
     */
    private void initialize(String player, PluginMain plugin, LocalWorld world)
    {
        m_player = player;
        m_blockPlacer = plugin.getBlockPlacer();
        if (world != null)
        {
            m_world = plugin.getServer().getWorld(world.getName());
        }
        m_asyncForced = false;
        m_asyncDisabled = false;
    }

    /**
     * Enables or disables the async mode configuration bypass this function
     * should by used only by other plugins
     *
     * @param value true to enable async mode force
     */
    public void setAsyncForced(boolean value)
    {
        m_asyncForced = value;
    }

    /**
     * Check if async mode is forced
     *
     * @return
     */
    public boolean isAsyncForced()
    {
        return m_asyncForced;
    }

    /**
     * This function checks if async mode is enabled for specific command
     *
     * @param operation
     */
    private void checkAsync(WorldeditOperations operation)
    {
        m_asyncDisabled = !ConfigProvider.isAsyncAllowed(operation);
    }

    /**
     * Reset async disabled inner state (enable async mode)
     */
    private void resetAsync()
    {
        m_asyncDisabled = false;
    }
}