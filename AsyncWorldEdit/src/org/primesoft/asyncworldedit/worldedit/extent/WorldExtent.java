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
import java.util.UUID;
import org.primesoft.asyncworldedit.BlocksHubIntegration;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.entries.WorldExtentActionEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.WorldExtentFuncEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.WorldExtentFuncEntryEx;
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
        }, vector);
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
    public boolean setBlock(Vector vector, BaseBlock bb, final boolean bln) throws WorldEditException {
        final WorldExtentParam<BaseBlock> paramBlock = WorldExtentParam.extract(bb);
        final WorldExtentParam<Vector> paramVector = WorldExtentParam.extract(vector);

        final BaseBlock newBlock = paramBlock.getData();
        final Vector v = paramVector.getData();
        final UUID player = paramBlock.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        FuncEx<Boolean, WorldEditException> func = new FuncEx<Boolean, WorldEditException>() {

            @Override
            public Boolean Execute() throws WorldEditException {
                final BaseBlock oldBlock = m_parent.getBlock(v);
                
                if (oldBlock.equals(newBlock)) {
                    return false;
                }
                
                final boolean result = m_parent.setBlock(v, newBlock, bln);
                if (result) {
                    logBlock(v, player, oldBlock, newBlock);
                }

                return result;
            }
        };

        if (paramBlock.isAsync() || paramVector.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntryEx(this, paramBlock.getJobId(), v, func));
        }

        return func.Execute();
    }

    @Override
    public boolean setBlockType(Vector vector, final int i) {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        Func<Boolean> func = new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                final BaseBlock oldBlock = m_parent.getBlock(v);
                if (oldBlock.getType() == i) {
                    return false;
                }

                final boolean result = m_parent.setBlockType(v, i);
                if (result) {
                    logBlock(v, player, oldBlock, new BaseBlock(i, oldBlock.getData()));
                }

                return result;
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntry(this, param.getJobId(), v, func));
        }

        return func.Execute();
    }

    @Override
    public boolean setBlockTypeFast(Vector vector, final int i) {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        Func<Boolean> func = new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                final BaseBlock oldBlock = m_parent.getBlock(v);
                if (oldBlock.getType() == i) {
                    return false;
                }

                final boolean result = m_parent.setBlockTypeFast(v, i);
                if (result) {
                    logBlock(v, player, oldBlock, new BaseBlock(i, oldBlock.getData()));
                }

                return result;
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntry(this, param.getJobId(), v, func));
        }

        return func.Execute();
    }

    @Override
    public void setBlockData(Vector vector, final int i) {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return;
        }

        Func<Boolean> func = new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                final BaseBlock oldBlock = m_parent.getBlock(v);
                if (oldBlock.getData() == i) {
                    return false;
                }
                m_parent.setBlockData(v, i);
                logBlock(v, player, oldBlock, new BaseBlock(oldBlock.getType(), i));
                return true;
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntry(this, param.getJobId(), v, func));
            return;
        }

        func.Execute();
    }

    @Override
    public void setBlockDataFast(Vector vector, final int i) {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return;
        }

        Func<Boolean> func = new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                final BaseBlock oldBlock = m_parent.getBlock(v);
                if (oldBlock.getData() == i) {
                    return false;
                }

                m_parent.setBlockDataFast(v, i);
                logBlock(v, player, oldBlock, new BaseBlock(oldBlock.getType(), i));
                return true;
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntry(this, param.getJobId(), v, func));
            return;
        }

        func.Execute();
    }

    @Override
    public boolean setTypeIdAndData(Vector vector, final int i, final int i1) {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        Func<Boolean> func = new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                final BaseBlock oldBlock = m_parent.getBlock(v);

                if (oldBlock.getType() == i && oldBlock.getData() == i1) {
                    return false;
                }

                final boolean result = m_parent.setTypeIdAndData(v, i, i1);
                if (result) {
                    logBlock(v, player, oldBlock, new BaseBlock(i, i1));
                }

                return result;
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntry(this, param.getJobId(), v, func));
        }

        return func.Execute();
    }

    @Override
    public boolean setTypeIdAndDataFast(Vector vector, final int i, final int i1) {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        Func<Boolean> func = new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                final BaseBlock oldBlock = m_parent.getBlock(v);
                if (oldBlock.getType() == i && oldBlock.getData() == i1) {
                    return false;
                }
                
                final boolean result = m_parent.setTypeIdAndDataFast(v, i, i1);
                if (result) {
                    logBlock(v, player, oldBlock, new BaseBlock(i, i1));
                }

                return result;
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntry(this, param.getJobId(), v, func));
        }

        return func.Execute();
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
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        Func<Boolean> func = new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.clearContainerBlockContents(vector);
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntry(this, param.getJobId(), v, func));
        }

        return func.Execute();
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
    public void setBiome(Vector2D vd, final BiomeType bt) {
        final WorldExtentParam<Vector2D> param = WorldExtentParam.extract(vd);
        final Vector2D v = param.getData();
        final UUID player = param.getPlayer();
        final Vector tmpV = new Vector(v.getX(), 0, v.getZ());

        if (!m_blocksHub.canPlace(player, m_cbWorld, tmpV)) {
            return;
        }

        Action func = new Action() {
            @Override
            public void Execute() {
                m_parent.setBiome(v, bt);
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            m_blockPlacer.addTasks(player,
                    new WorldExtentActionEntry(this, param.getJobId(), tmpV, func));
            return;
        }

        func.Execute();
    }

    @Override
    public void dropItem(Vector vector, final BaseItemStack bis, final int i) {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return;
        }

        Action func = new Action() {
            @Override
            public void Execute() {
                m_parent.dropItem(v, bis, i);
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            m_blockPlacer.addTasks(player,
                    new WorldExtentActionEntry(this, param.getJobId(), v, func));
            return;
        }

        func.Execute();
    }

    @Override
    public void dropItem(final Vector vector, final BaseItemStack bis) {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return;
        }

        Action func = new Action() {
            @Override
            public void Execute() {
                m_parent.dropItem(v, bis);
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            m_blockPlacer.addTasks(player,
                    new WorldExtentActionEntry(this, param.getJobId(), v, func));
            return;
        }

        func.Execute();
    }

    @Override
    public void simulateBlockMine(Vector vector) {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return;
        }

        Action func = new Action() {
            @Override
            public void Execute() {
                m_parent.simulateBlockMine(v);
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            m_blockPlacer.addTasks(player,
                    new WorldExtentActionEntry(this, param.getJobId(), v, func));
            return;
        }

        func.Execute();
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
    public boolean generateTree(final TreeGenerator.TreeType tt, final EditSession es, Vector vector) throws MaxChangedBlocksException {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        FuncEx<Boolean, MaxChangedBlocksException> func = new FuncEx<Boolean, MaxChangedBlocksException>() {
            @Override
            public Boolean Execute() throws MaxChangedBlocksException {
                return m_parent.generateTree(tt, es, v);
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntryEx(this, param.getJobId(), v, func));
        }

        return func.Execute();
    }

    @Override
    public boolean generateTree(final EditSession es, Vector vector) throws MaxChangedBlocksException {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        FuncEx<Boolean, MaxChangedBlocksException> func = new FuncEx<Boolean, MaxChangedBlocksException>() {
            @Override
            public Boolean Execute() throws MaxChangedBlocksException {
                return m_parent.generateTree(es, v);
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntryEx(this, param.getJobId(), v, func));
        }

        return func.Execute();
    }

    @Override
    public boolean generateBigTree(final EditSession es, Vector vector) throws MaxChangedBlocksException {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        FuncEx<Boolean, MaxChangedBlocksException> func = new FuncEx<Boolean, MaxChangedBlocksException>() {
            @Override
            public Boolean Execute() throws MaxChangedBlocksException {
                return m_parent.generateBigTree(es, v);
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntryEx(this, param.getJobId(), v, func));
        }

        return func.Execute();
    }

    @Override
    public boolean generateBirchTree(final EditSession es, Vector vector) throws MaxChangedBlocksException {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        FuncEx<Boolean, MaxChangedBlocksException> func = new FuncEx<Boolean, MaxChangedBlocksException>() {
            @Override
            public Boolean Execute() throws MaxChangedBlocksException {
                return m_parent.generateBirchTree(es, v);
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntryEx(this, param.getJobId(), v, func));
        }

        return func.Execute();
    }

    @Override
    public boolean generateRedwoodTree(final EditSession es, Vector vector) throws MaxChangedBlocksException {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        FuncEx<Boolean, MaxChangedBlocksException> func = new FuncEx<Boolean, MaxChangedBlocksException>() {
            @Override
            public Boolean Execute() throws MaxChangedBlocksException {
                return m_parent.generateRedwoodTree(es, v);
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntryEx(this, param.getJobId(), v, func));
        }

        return func.Execute();
    }

    @Override
    public boolean generateTallRedwoodTree(final EditSession es, Vector vector) throws MaxChangedBlocksException {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        FuncEx<Boolean, MaxChangedBlocksException> func = new FuncEx<Boolean, MaxChangedBlocksException>() {
            @Override
            public Boolean Execute() throws MaxChangedBlocksException {
                return m_parent.generateTallRedwoodTree(es, v);
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntryEx(this, param.getJobId(), v, func));
        }

        return func.Execute();
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
    public boolean playEffect(Vector vector, final int i, final int i1) {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        Func<Boolean> func = new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.playEffect(v, i, i1);
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntry(this, param.getJobId(), v, func));
        }

        return func.Execute();
    }

    @Override
    public boolean queueBlockBreakEffect(final ServerInterface si, Vector vector,
            final int i, final double d) {
        final WorldExtentParam<Vector> param = WorldExtentParam.extract(vector);
        final Vector v = param.getData();
        final UUID player = param.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        Func<Boolean> func = new Func<Boolean>() {
            @Override
            public Boolean Execute() {
                return m_parent.queueBlockBreakEffect(si, v, i, d);
            }
        };

        if (param.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntry(this, param.getJobId(), v, func));
        }

        return func.Execute();
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
        final WorldExtentParam<BaseBlock> paramBlock = WorldExtentParam.extract(bb);
        final WorldExtentParam<Vector> paramVector = WorldExtentParam.extract(vector);

        final BaseBlock newBlock = paramBlock.getData();
        final Vector v = paramVector.getData();
        final UUID player = paramBlock.getPlayer();

        if (!m_blocksHub.canPlace(player, m_cbWorld, v)) {
            return false;
        }

        FuncEx<Boolean, WorldEditException> func = new FuncEx<Boolean, WorldEditException>() {

            @Override
            public Boolean Execute() throws WorldEditException {
                final BaseBlock oldBlock = m_parent.getBlock(vector);
                
                if (oldBlock.equals(newBlock)) {
                    return false;
                }
                
                final boolean result = m_parent.setBlock(vector, newBlock);
                if (result) {
                    logBlock(vector, player, oldBlock, newBlock);
                }

                return result;
            }
        };

        if (paramBlock.isAsync() || paramVector.isAsync() || !m_blockPlacer.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldExtentFuncEntryEx(this, paramBlock.getJobId(), v, func));
        }

        return func.Execute();
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

    /**
     * Log placed block using blocks hub
     */
    private void logBlock(Vector location, UUID player, BaseBlock oldBlock, BaseBlock newBlock) {
        m_blocksHub.logBlock(player, m_cbWorld, location, oldBlock, newBlock);
    }
}
