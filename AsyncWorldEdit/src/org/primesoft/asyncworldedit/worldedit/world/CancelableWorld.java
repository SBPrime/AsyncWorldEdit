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
package org.primesoft.asyncworldedit.worldedit.world;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.World;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.primesoft.asyncworldedit.utils.SessionCanceled;
import org.primesoft.asyncworldedit.worldedit.BaseBlockWrapper;
import org.primesoft.asyncworldedit.worldedit.BlockVector2DWrapper;
import org.primesoft.asyncworldedit.worldedit.Vector2DWrapper;
import org.primesoft.asyncworldedit.worldedit.VectorWrapper;

/**
 *
 * @author SBPrime
 */
public class CancelableWorld implements World {

    private final World m_parent;
    private final int m_jobId;
    private final UUID m_player;
    private boolean m_isCanceled;

    public CancelableWorld(World parent, int jobId, UUID player) {
        m_parent = parent;
        m_isCanceled = false;
        m_jobId = jobId;
        m_player = player;
    }

    
    /**
     * Cancel all further operations
     */
    public void cancel() {
        m_isCanceled = true;
    }
    
    /**
     * Is world operation canceled
     * @return 
     */
    public boolean isCanceled() {
        return m_isCanceled;
    }

    @Override
    public String getName() {
        return m_parent.getName();
    }

    @Override
    public int getMaxY() {
        return m_parent.getMaxY();
    }

    @Override
    public boolean isValidBlockType(int i) {
        return m_parent.isValidBlockType(i);
    }

    @Override
    public boolean usesBlockData(int i) {
        return m_parent.usesBlockData(i);
    }

    @Override
    public Mask createLiquidMask() {
        return m_parent.createLiquidMask();
    }

    @Override
    public int getBlockType(Vector vector) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.getBlockType(vector);
    }

    @Override
    public int getBlockData(Vector vector) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.getBlockData(vector);
    }

    @Override
    public boolean setBlock(Vector vector, BaseBlock bb, boolean bln) throws WorldEditException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.setBlock(VectorWrapper.wrap(vector, m_jobId, true, m_player), 
                BaseBlockWrapper.wrap(bb, m_jobId, true, m_player), bln);
    }

    @Override
    public boolean setBlockType(Vector vector, int i) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.setBlockType(VectorWrapper.wrap(vector, m_jobId, true, m_player), i);
    }

    @Override
    public boolean setBlockTypeFast(Vector vector, int i) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.setBlockTypeFast(VectorWrapper.wrap(vector, m_jobId, true, m_player), i);
    }

    @Override
    public void setBlockData(Vector vector, int i) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        m_parent.setBlockData(VectorWrapper.wrap(vector, m_jobId, true, m_player), i);
    }

    @Override
    public void setBlockDataFast(Vector vector, int i) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        m_parent.setBlockDataFast(VectorWrapper.wrap(vector, m_jobId, true, m_player), i);
    }

    @Override
    public boolean setTypeIdAndData(Vector vector, int i, int i1) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.setTypeIdAndData(VectorWrapper.wrap(vector, m_jobId, true, m_player), i, i1);
    }

    @Override
    public boolean setTypeIdAndDataFast(Vector vector, int i, int i1) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.setTypeIdAndDataFast(VectorWrapper.wrap(vector, m_jobId, true, m_player), i, i1);
    }

    @Override
    public int getBlockLightLevel(Vector vector) {
        return m_parent.getBlockLightLevel(vector);
    }

    @Override
    public boolean clearContainerBlockContents(Vector vector) {
        return m_parent.clearContainerBlockContents(VectorWrapper.wrap(vector, m_jobId, true, m_player));
    }

    @Override
    public BiomeType getBiome(Vector2D vd) {
        return m_parent.getBiome(vd);
    }

    @Override
    public void setBiome(Vector2D vd, BiomeType bt) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        m_parent.setBiome(Vector2DWrapper.wrap(vd, m_jobId, true, m_player), bt);
    }

    @Override
    public void dropItem(Vector vector, BaseItemStack bis, int i) {
        m_parent.dropItem(VectorWrapper.wrap(vector, m_jobId, true, m_player), bis, i);
    }

    @Override
    public void dropItem(Vector vector, BaseItemStack bis) {
        m_parent.dropItem(VectorWrapper.wrap(vector, m_jobId, true, m_player), bis);
    }

    @Override
    public void simulateBlockMine(Vector vector) {
        m_parent.simulateBlockMine(VectorWrapper.wrap(vector, m_jobId, true, m_player));
    }

    @Override
    public LocalEntity[] getEntities(Region region) {
        return m_parent.getEntities(region);
    }

    @Override
    public int killEntities(LocalEntity... les) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.killEntities(les);
    }

    @Override
    public int killMobs(Vector vector, int i) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.killMobs(VectorWrapper.wrap(vector, m_jobId, true, m_player), i);
    }

    @Override
    public int killMobs(Vector vector, int i, boolean bln) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.killMobs(VectorWrapper.wrap(vector, m_jobId, true, m_player), i, bln);
    }

    @Override
    public int killMobs(Vector vector, double d, int i) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.killMobs(VectorWrapper.wrap(vector, m_jobId, true, m_player), d, i);
    }

    @Override
    public int removeEntities(EntityType et, Vector vector, int i) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.removeEntities(et, VectorWrapper.wrap(vector, m_jobId, true, m_player), i);
    }

    @Override
    public boolean regenerate(Region region, EditSession es) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.regenerate(region, es);
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType tt, EditSession es, Vector vector) throws MaxChangedBlocksException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.generateTree(tt, es, VectorWrapper.wrap(vector, m_jobId, true, m_player));
    }

    @Override
    public boolean generateTree(EditSession es, Vector vector) throws MaxChangedBlocksException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.generateTree(es, VectorWrapper.wrap(vector, m_jobId, true, m_player));
    }

    @Override
    public boolean generateBigTree(EditSession es, Vector vector) throws MaxChangedBlocksException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.generateBigTree(es, VectorWrapper.wrap(vector, m_jobId, true, m_player));
    }

    @Override
    public boolean generateBirchTree(EditSession es, Vector vector) throws MaxChangedBlocksException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.generateBirchTree(es, VectorWrapper.wrap(vector, m_jobId, true, m_player));
    }

    @Override
    public boolean generateRedwoodTree(EditSession es, Vector vector) throws MaxChangedBlocksException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.generateRedwoodTree(es, VectorWrapper.wrap(vector, m_jobId, true, m_player));
    }

    @Override
    public boolean generateTallRedwoodTree(EditSession es, Vector vector) throws MaxChangedBlocksException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.generateTallRedwoodTree(es, VectorWrapper.wrap(vector, m_jobId, true, m_player));
    }

    @Override
    public void checkLoadedChunk(Vector vector) {
        m_parent.checkLoadedChunk(vector);
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2D> itrbl) {
        List<BlockVector2D> tmp = new ArrayList<BlockVector2D>();
        for (Iterator<BlockVector2D> it = tmp.iterator(); it.hasNext();) {
            tmp.add(BlockVector2DWrapper.wrap(it.next(), m_jobId, true, m_player));            
        }
        m_parent.fixAfterFastMode(tmp);
    }

    @Override
    public void fixLighting(Iterable<BlockVector2D> itrbl) {
        List<BlockVector2D> tmp = new ArrayList<BlockVector2D>();
        for (Iterator<BlockVector2D> it = tmp.iterator(); it.hasNext();) {
            tmp.add(BlockVector2DWrapper.wrap(it.next(), m_jobId, true, m_player));            
        }
        m_parent.fixLighting(tmp);
    }

    @Override
    public boolean playEffect(Vector vector, int i, int i1) {
        return m_parent.playEffect(VectorWrapper.wrap(vector, m_jobId, true, m_player), i, i1);
    }

    @Override
    public boolean queueBlockBreakEffect(Platform pltform, Vector vector, int i, double d) {    
        return m_parent.queueBlockBreakEffect(pltform, VectorWrapper.wrap(vector, m_jobId, true, m_player), i, d);
    }

    @Override
    public Vector getMinimumPoint() {
        return m_parent.getMinimumPoint();
    }

    @Override
    public Vector getMaximumPoint() {
        return m_parent.getMaximumPoint();
    }

    @Override
    public BaseBlock getBlock(Vector vector) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.getBlock(vector);
    }

    @Override
    public BaseBlock getLazyBlock(Vector vector) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.getLazyBlock(vector);
    }

    @Override
    public boolean setBlock(Vector vector, BaseBlock bb) throws WorldEditException {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.setBlock(VectorWrapper.wrap(vector, m_jobId, true, m_player), 
                BaseBlockWrapper.wrap(bb, m_jobId, true, m_player));
    }
        

    @Override
    public Operation commit() {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.commit();
    }
}
