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
package org.primesoft.asyncworldedit.worldedit.world;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.registry.WorldData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.utils.SessionCanceled;
import org.primesoft.asyncworldedit.worldedit.blocks.BaseBlockWrapper;
import org.primesoft.asyncworldedit.worldedit.BlockVector2DWrapper;
import org.primesoft.asyncworldedit.worldedit.Vector2DWrapper;
import org.primesoft.asyncworldedit.worldedit.VectorWrapper;
import org.primesoft.asyncworldedit.worldedit.entity.BaseEntityWrapper;
import org.primesoft.asyncworldedit.worldedit.util.LocationWrapper;

/**
 *
 * @author SBPrime
 */
public class CancelableWorld extends AbstractWorldWrapper {
    private final int m_jobId;
    private final PlayerEntry m_player;
    private boolean m_isCanceled;

    public CancelableWorld(World parent, int jobId, PlayerEntry player) {
        super(parent);
        
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
    public void setBlockData(Vector vector, int i) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        m_parent.setBlockData(VectorWrapper.wrap(vector, m_jobId, true, m_player), i);
    }

    @Override
    public boolean setTypeIdAndData(Vector vector, int i, int i1) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.setTypeIdAndData(VectorWrapper.wrap(vector, m_jobId, true, m_player), i, i1);
    }

    @Override
    public int getBlockLightLevel(Vector vector) {
        return m_parent.getBlockLightLevel(vector);
    }

    @Override
    public boolean clearContainerBlockContents(Vector vector) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }

        return m_parent.clearContainerBlockContents(VectorWrapper.wrap(vector, m_jobId, true, m_player));
    }

    @Override
    public BaseBiome getBiome(Vector2D vd) {
        return m_parent.getBiome(vd);
    }

    @Override
    public boolean setBiome(Vector2D vd, BaseBiome bt) {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        return m_parent.setBiome(Vector2DWrapper.wrap(vd, m_jobId, true, m_player), bt);
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
    public List<? extends Entity> getEntities() {        
        return m_parent.getEntities();
    }
    
    @Override
    public List<? extends Entity> getEntities(Region region) {
        return m_parent.getEntities(region);
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

    @Override
    public Entity createEntity(Location lctn, BaseEntity be) {
                if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.createEntity(LocationWrapper.wrap(lctn, m_jobId, true, m_player),
                BaseEntityWrapper.wrap(be, m_jobId, true, m_player));
    }

    @Override
    public WorldData getWorldData() {
        if (m_isCanceled) {
            throw new IllegalArgumentException(new SessionCanceled());
        }
        
        return m_parent.getWorldData();
    }
}
