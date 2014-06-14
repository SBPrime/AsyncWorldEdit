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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.foundation.Block;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerActionEntry;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerFuncEntry;
import org.primesoft.asyncworldedit.utils.Action;
import org.primesoft.asyncworldedit.utils.Func;

/**
 *
 * @author prime
 */
public class LocalWorldWrapper extends LocalWorld {

    private AsyncEditSession m_session;
    private final int m_jobId;
    private final LocalWorld m_parent;
    private final BlockPlacer m_blockPlacer;
    private final String m_player;
           
    public LocalWorldWrapper(LocalWorld parent, int jobId, String player, BlockPlacer blockPlacer) {
        m_player = player;
        m_jobId = jobId;
        m_parent = parent;
        m_blockPlacer = blockPlacer;
    }

    public void setSession(AsyncEditSession session) {
        m_session = session;
    }

    @Override
    public void checkLoadedChunk(Vector pt) {
        m_parent.checkLoadedChunk(pt);
    }

    @Override
    public boolean clearContainerBlockContents(final Vector pt) {
        AddJob(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.clearContainerBlockContents(pt);
            }
        });
        return true;

    }

    @Override
    public boolean copyFromWorld(final Vector pt, final BaseBlock block) {
        AddJob(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.copyFromWorld(pt, block);
            }
        });

        return true;
    }

    @Override
    public boolean copyToWorld(final Vector pt, final BaseBlock block) {
        AddJob(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.copyToWorld(pt, block);
            }
        });

        return true;
    }

    @Override
    public void dropItem(final Vector pt, final BaseItemStack item) {
        AddJob(new Action() {
            @Override
            public void Execute() {
                m_parent.dropItem(pt, item);
            }
        });
    }

    @Override
    public void dropItem(final Vector pt, final BaseItemStack item, final int times) {
        AddJob(new Action() {
            @Override
            public void Execute() {
                m_parent.dropItem(pt, item, times);
            }
        });
    }

    @Override
    public void fixAfterFastMode(final Iterable<BlockVector2D> chunks) {
        AddJob(new Action() {
            @Override
            public void Execute() {
                m_parent.fixAfterFastMode(chunks);
            }
        });
    }

    @Override
    public boolean generateBigTree(final EditSession editSession, final Vector pt) throws MaxChangedBlocksException {
        AddJob(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                try {
                    return m_parent.generateBigTree(editSession, pt);
                } catch (MaxChangedBlocksException ex) {
                    return false;
                }
            }
        });
        return true;
    }

    @Override
    public boolean generateBirchTree(final EditSession editSession, final Vector pt) throws MaxChangedBlocksException {        
        AddJob(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                try {
                    return m_parent.generateBirchTree(editSession, pt);
                } catch (MaxChangedBlocksException ex) {
                    return false;
                }
            }
        });
        return true;
    }

    @Override
    public boolean generateRedwoodTree(final EditSession editSession, final Vector pt) throws MaxChangedBlocksException {
        AddJob(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                try {
                    return m_parent.generateRedwoodTree(editSession, pt);
                } catch (MaxChangedBlocksException ex) {
                    return false;
                }
            }
        });
        return true;
    }

    @Override
    public boolean generateTallRedwoodTree(final EditSession editSession, final Vector pt) throws MaxChangedBlocksException {
        AddJob(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                try {
                    return m_parent.generateTallRedwoodTree(editSession, pt);
                } catch (MaxChangedBlocksException ex) {
                    return false;
                }
            }
        });
        return true;
    }

    @Override
    public boolean generateTree(final EditSession editSession, final Vector pt) throws MaxChangedBlocksException {
        AddJob(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                try {
                    return m_parent.generateTree(editSession, pt);
                } catch (MaxChangedBlocksException ex) {
                    return false;
                }
            }
        });
        return true;
    }

    @Override
    public boolean generateTree(final TreeGenerator.TreeType type, final EditSession editSession, final Vector pt) throws MaxChangedBlocksException {
        final CancelabeEditSession tmpSession = new CancelabeEditSession(m_session, editSession.getMask(), m_jobId);
        
        AddJob(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                try {
                    boolean result = m_parent.generateTree(type, tmpSession, pt);
                    tmpSession.flushQueue();
                    return result;
                } catch (MaxChangedBlocksException ex) {
                    return false;
                }
            }
        });
        return true;
    }

    @Override
    public BiomeType getBiome(Vector2D pt) {
        return m_parent.getBiome(pt);
    }

    @Override
    public BaseBlock getBlock(final Vector pt) {
        return m_session.performSafe(new Func<BaseBlock>() {

            @Override
            public BaseBlock Execute() {
                return m_parent.getBlock(pt);
            }
        }, pt);
    }

    @Override
    public int getBlockData(final Vector pt) {
        return m_session.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return m_parent.getBlockData(pt);
            }
        }, pt);
    }

    @Override
    public int getBlockLightLevel(final Vector pt) {
        return m_session.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return m_parent.getBlockLightLevel(pt);
            }
        }, pt);
    }

    @Override
    public int getBlockType(final Vector pt) {
        return m_session.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return m_parent.getBlockType(pt);
            }
        }, pt);
    }

    @Override
    public LocalEntity[] getEntities(Region region) {
        return m_parent.getEntities(region);
    }

    @Override
    public int getMaxY() {
        return m_parent.getMaxY();
    }

    @Override
    public String getName() {
        return m_parent.getName();
    }

    @Override
    public boolean isValidBlockType(int type) {
        return m_parent.isValidBlockType(type);
    }

    @Override
    public int killEntities(LocalEntity... entities) {
        return m_parent.killEntities(entities);
    }

    @Override
    public int killMobs(Vector origin, int radius) {
        return m_parent.killMobs(origin, radius);
    }

    @Override
    public int killMobs(Vector origin, double radius, int flags) {
        return m_parent.killMobs(origin, radius, flags);
    }

    @Override
    public int killMobs(Vector origin, int radius, boolean killPets) {
        return m_parent.killMobs(origin, radius, killPets);
    }

    @Override
    public boolean playEffect(Vector position, int type, int data) {
        return m_parent.playEffect(position, type, data);
    }

    @Override
    public boolean queueBlockBreakEffect(ServerInterface server, Vector position, int blockId, double priority) {
        return m_parent.queueBlockBreakEffect(server, position, blockId, priority);
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        return m_parent.regenerate(region, editSession);
    }

    @Override
    public int removeEntities(EntityType type, Vector origin, int radius) {
        return m_parent.removeEntities(type, origin, radius);
    }

    @Override
    public void setBiome(Vector2D pt, BiomeType biome) {
        m_parent.setBiome(pt, biome);
    }

    @Override
    public boolean setBlock(final Vector pt, final Block block, final boolean notifyAdjacent) {
        AddJob(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.setBlock(pt, block, notifyAdjacent);
            }
        });
        return true;
    }

    @Override
    public void setBlockData(final Vector pt, final int data) {
        AddJob(new Action() {
            @Override
            public void Execute() {
                m_parent.setBlockData(pt, data);
            }
        });
    }

    @Override
    public void setBlockDataFast(final Vector pt, final int data) {
        AddJob(new Action() {
            @Override
            public void Execute() {
                m_parent.setBlockDataFast(pt, data);
            }
        });
    }

    @Override
    public boolean setBlockType(final Vector pt, final int type) {
        AddJob(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.setBlockType(pt, type);
            }
        });
        return true;
    }

    @Override
    public boolean setBlockTypeFast(final Vector pt, final int type) {
        AddJob(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.setBlockTypeFast(pt, type);
            }
        });
        return true;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    @Override
    public int hashCode() {
        return m_parent.hashCode();
    }

    @Override
    public boolean setTypeIdAndData(final Vector pt, final int type, final int data) {
        AddJob(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.setTypeIdAndData(pt, type, data);
            }
        });
        return true;
    }

    @Override
    public boolean setTypeIdAndDataFast(final Vector pt, final int type, final int data) {
        AddJob(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.setTypeIdAndDataFast(pt, type, data);
            }
        });
        return true;
    }

    @Override
    public void simulateBlockMine(Vector pt) {
        m_parent.simulateBlockMine(pt);
    }

    @Override
    public String toString() {
        return m_parent.toString();
    }

    @Override
    public boolean usesBlockData(int type) {
        return m_parent.usesBlockData(type);
    }

    private void AddJob(Func<Boolean> func) {
        m_blockPlacer.addTasks(m_player, new BlockPlacerFuncEntry(m_session, m_jobId, func));
    }

    private void AddJob(Action func) {
        m_blockPlacer.addTasks(m_player, new BlockPlacerActionEntry(m_session, m_jobId, func));
    }

    @Override
    public boolean equals(Object o) {
       if (o instanceof LocalWorld) {
           return m_parent.equals(o);
       }
       
       return false;
    }
}
