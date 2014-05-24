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
package org.primesoft.asyncworldedit.worldedit.extent;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.World;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primesoft.asyncworldedit.BlocksHubIntegration;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.WorldExtentBlockEntry;
import org.primesoft.asyncworldedit.utils.Action;
import org.primesoft.asyncworldedit.utils.Func;
import org.primesoft.asyncworldedit.utils.FuncEx;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author SBPrime
 */
public class WorldExtent implements World {

    /**
     * The parrent world
     */
    private final World m_parent;

    /**
     * Parent edit session
     */
    private AsyncEditSession m_editSession;

    /**
     * The block placer
     */
    private BlockPlacer m_blockPlacer;

    /**
     * The blocks hub
     */
    private BlocksHubIntegration m_blocksHub;
    
    /**
     * Craft bukkit world - used for block loging
     */
    private org.bukkit.World m_cbWorld;

    public WorldExtent(World world) {
        m_parent = world;
    }

    /**
     *
     * @param editSession
     * @param bh
     * @param bWorld
     */
    public void Initialize(AsyncEditSession editSession, BlocksHubIntegration bh,
            org.bukkit.World bWorld) {
        m_editSession = editSession;
        m_blockPlacer = m_editSession.getBlockPlacer();
        m_blocksHub = bh;
        m_cbWorld = bWorld;
    }

    @Override
    public String getName() {
        return m_editSession.performSafe(new Func<String>() {
            @Override
            public String Execute() {
                return m_parent.getName();
            }
        });
    }

    @Override
    public int getMaxY() {
        return m_editSession.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return m_parent.getMaxY();
            }
        });
    }

    @Override
    public boolean isValidBlockType(final int i) {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.isValidBlockType(i);
            }
        });
    }

    @Override
    public boolean usesBlockData(final int i) {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.usesBlockData(i);
            }
        });
    }

    @Override
    public Mask createLiquidMask() {
        return m_editSession.performSafe(new Func<Mask>() {
            @Override
            public Mask Execute() {
                return m_parent.createLiquidMask();
            }
        });
    }

    @Override
    public int getBlockType(final Vector vector) {
        return m_editSession.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return m_parent.getBlockType(vector);
            }
        },vector);
    }

    @Override
    public int getBlockData(final Vector vector) {
        return m_editSession.performSafe(new Func<Integer>() {

            @Override
            public Integer Execute() {
                return m_parent.getBlockData(vector);
            }
        }, vector);
    }

    @Override
    public boolean setBlock(final Vector vector, BaseBlock bb, final boolean bln) throws WorldEditException {
        final WorldExtentParam param = WorldExtentParam.extract(bb);

        FuncEx<Boolean, WorldEditException> func = new FuncEx<Boolean, WorldEditException>() {

            @Override
            public Boolean Execute() throws WorldEditException {
                BaseBlock old = m_parent.getBlock(vector);
                boolean result = m_parent.setBlock(vector, param.getBlock(), bln);
                if (result) {
                    logBlock(param, vector, old);
                }

                return result;
            }
        };

        if (!param.isAsync()) {
            return func.Execute();
        }

        return m_blockPlacer.addTasks(param.getPlayer(), new WorldExtentBlockEntry(this, param.getJobId(), vector, func));
    }

    
    /**
     * Log placed block using blocks hub
     * @param param
     * @param old 
     */
    private void logBlock(WorldExtentParam param, Vector location, BaseBlock old) {
        m_blocksHub.logBlock(param.getPlayer(), m_cbWorld, location, old, param.getBlock());
    }

    @Override
    public boolean setBlockType(final Vector vector, final int i) {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.setBlockType(vector, i);
            }
        }, vector);
    }

    @Override
    public boolean setBlockTypeFast(final Vector vector, final int i) {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.setBlockTypeFast(vector, i);
            }
        }, vector);
    }

    @Override
    public void setBlockData(final Vector vector, final int i) {
        m_editSession.performSafe(new Action() {
            @Override
            public void Execute() {
                m_parent.setBlockData(vector, i);
            }
        }, vector);
    }

    @Override
    public void setBlockDataFast(final Vector vector, final int i) {
        m_editSession.performSafe(new Action() {
            @Override
            public void Execute() {
                m_parent.setBlockDataFast(vector, i);
            }
        }, vector);
    }

    @Override
    public boolean setTypeIdAndData(final Vector vector, final int i, final int i1) {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.setTypeIdAndData(vector, i, i1);
            }
        }, vector);
    }

    @Override
    public boolean setTypeIdAndDataFast(final Vector vector, final int i, final int i1) {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.setTypeIdAndDataFast(vector, i, i1);
            }
        }, vector);
    }

    @Override
    public int getBlockLightLevel(final Vector vector) {
        return m_editSession.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return m_parent.getBlockLightLevel(vector);
            }
        }, vector);
    }

    @Override
    public boolean clearContainerBlockContents(final Vector vector) {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.clearContainerBlockContents(vector);
            }
        }, vector);
    }

    @Override
    public BiomeType getBiome(final Vector2D vd) {
        return m_editSession.performSafe(new Func<BiomeType>() {
            @Override
            public BiomeType Execute() {
                return m_parent.getBiome(vd);
            }
        }, new Vector(vd.getX(), 0, vd.getZ()));
    }

    @Override
    public void setBiome(final Vector2D vd, final BiomeType bt) {
        m_editSession.performSafe(new Action() {
            @Override
            public void Execute() {
                m_parent.setBiome(vd, bt);
            }
        }, new Vector(vd.getX(), 0, vd.getZ()));
    }

    @Override
    public void dropItem(final Vector vector, final BaseItemStack bis, final int i) {
        m_editSession.performSafe(new Action() {
            @Override
            public void Execute() {
                m_parent.dropItem(vector, bis, i);
            }
        }, vector);
    }

    @Override
    public void dropItem(final Vector vector, final BaseItemStack bis) {
        m_editSession.performSafe(new Action() {
            @Override
            public void Execute() {
                m_parent.dropItem(vector, bis);
            }
        }, vector);
    }

    @Override
    public void simulateBlockMine(final Vector vector) {
        m_editSession.performSafe(new Action() {
            @Override
            public void Execute() {
                m_parent.simulateBlockMine(vector);
            }
        }, vector);
    }

    @Override
    public LocalEntity[] getEntities(final Region region) {
        return m_editSession.performSafe(new Func<LocalEntity[]>() {
            @Override
            public LocalEntity[] Execute() {
                return m_parent.getEntities(region);
            }
        });
    }

    @Override
    public int killEntities(final LocalEntity... les) {
        return m_editSession.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return m_parent.killEntities(les);
            }
        });
    }

    @Override
    public int killMobs(final Vector vector, final int i) {
        return m_editSession.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return m_parent.killMobs(vector, i);
            }
        }, vector);
    }

    @Override
    public int killMobs(final Vector vector, final int i, final boolean bln) {
        return m_editSession.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return m_parent.killMobs(vector, i, bln);
            }
        }, vector);
    }

    @Override
    public int killMobs(final Vector vector, final double d, final int i) {
        return m_editSession.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return m_parent.killMobs(vector, d, i);
            }
        }, vector);
    }

    @Override
    public int removeEntities(final EntityType et, final Vector vector, final int i) {
        return m_editSession.performSafe(new Func<Integer>() {
            @Override
            public Integer Execute() {
                return m_parent.removeEntities(et, vector, i);
            }
        }, vector);
    }

    @Override
    public boolean regenerate(final Region region, final EditSession es) {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.regenerate(region, es);
            }
        });
    }

    @Override
    public boolean generateTree(final TreeGenerator.TreeType tt, final EditSession es, final Vector vector) throws MaxChangedBlocksException {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                try {
                    return m_parent.generateTree(tt, es, vector);
                } catch (MaxChangedBlocksException ex) {
                    Logger.getLogger(WorldExtent.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        }, vector);
    }

    @Override
    public boolean generateTree(final EditSession es, final Vector vector) throws MaxChangedBlocksException {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                try {
                    return m_parent.generateTree(es, vector);
                } catch (MaxChangedBlocksException ex) {
                    Logger.getLogger(WorldExtent.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        }, vector);
    }

    @Override
    public boolean generateBigTree(final EditSession es, final Vector vector) throws MaxChangedBlocksException {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                try {
                    return m_parent.generateBigTree(es, vector);
                } catch (MaxChangedBlocksException ex) {
                    Logger.getLogger(WorldExtent.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        }, vector);
    }

    @Override
    public boolean generateBirchTree(final EditSession es, final Vector vector) throws MaxChangedBlocksException {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                try {
                    return m_parent.generateBirchTree(es, vector);
                } catch (MaxChangedBlocksException ex) {
                    Logger.getLogger(WorldExtent.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        }, vector);
    }

    @Override
    public boolean generateRedwoodTree(final EditSession es, final Vector vector) throws MaxChangedBlocksException {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                try {
                    return m_parent.generateRedwoodTree(es, vector);
                } catch (MaxChangedBlocksException ex) {
                    Logger.getLogger(WorldExtent.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        }, vector);
    }

    @Override
    public boolean generateTallRedwoodTree(final EditSession es, final Vector vector)
            throws MaxChangedBlocksException {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                try {
                    return m_parent.generateTallRedwoodTree(es, vector);
                } catch (MaxChangedBlocksException ex) {
                    Logger.getLogger(WorldExtent.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        }, vector);
    }

    @Override
    public void checkLoadedChunk(final Vector vector) {
        m_editSession.performSafe(new Action() {
            @Override
            public void Execute() {
                m_parent.checkLoadedChunk(vector);
            }
        }, vector);
    }

    @Override
    public void fixAfterFastMode(final Iterable<BlockVector2D> itrbl) {
        m_editSession.performSafe(new Action() {
            @Override
            public void Execute() {
                m_parent.fixAfterFastMode(itrbl);
            }
        });
    }

    @Override
    public void fixLighting(final Iterable<BlockVector2D> itrbl) {
        m_editSession.performSafe(new Action() {
            @Override
            public void Execute() {
                m_parent.fixLighting(itrbl);
            }
        });
    }

    @Override
    public boolean playEffect(final Vector vector, final int i, final int i1) {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.playEffect(vector, i, i1);
            }
        }, vector);
    }

    @Override
    public boolean queueBlockBreakEffect(final ServerInterface si, final Vector vector,
            final int i, final double d) {
        return m_editSession.performSafe(new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.queueBlockBreakEffect(si, vector, i, d);
            }
        }, vector);
    }

    @Override
    public Vector getMinimumPoint() {
        return m_editSession.performSafe(new Func<Vector>() {
            @Override
            public Vector Execute() {
                return m_parent.getMinimumPoint();
            }
        });
    }

    @Override
    public Vector getMaximumPoint() {
        return m_editSession.performSafe(new Func<Vector>() {
            @Override
            public Vector Execute() {
                return m_parent.getMaximumPoint();
            }
        });
    }

    @Override
    public BaseBlock getBlock(final Vector vector) {
        return m_editSession.performSafe(new Func<BaseBlock>() {
            @Override
            public BaseBlock Execute() {
                return m_parent.getBlock(vector);
            }
        }, vector);
    }

    @Override
    public BaseBlock getLazyBlock(final Vector vector) {
        return m_editSession.performSafe(new Func<BaseBlock>() {
            @Override
            public BaseBlock Execute() {
                return m_parent.getLazyBlock(vector);
            }
        }, vector);
    }

    @Override
    public boolean setBlock(final Vector vector, final BaseBlock bb) throws WorldEditException {
        final WorldExtentParam param = WorldExtentParam.extract(bb);

        FuncEx<Boolean, WorldEditException> func = new FuncEx<Boolean, WorldEditException>() {

            @Override
            public Boolean Execute() throws WorldEditException {
                BaseBlock old = m_parent.getBlock(vector);
                boolean result = m_parent.setBlock(vector, param.getBlock());
                if (result) {
                    logBlock(param, vector, old);
                }

                return result;
            }
        };

        if (!param.isAsync()) {
            return func.Execute();
        }

        return m_blockPlacer.addTasks(param.getPlayer(), new WorldExtentBlockEntry(this, param.getJobId(), vector, func));
    }

    @Override
    public Operation commit() {
        return m_editSession.performSafe(new Func<Operation>() {
            @Override
            public Operation Execute() {
                return m_parent.commit();
            }
        });
    }
}
