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
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.registry.WorldData;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.bukkit.scheduler.BukkitScheduler;
import org.primesoft.asyncworldedit.AsyncWorldEditBukkit;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.blockshub.IBlocksHubIntegration;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.api.utils.IAction;
import org.primesoft.asyncworldedit.api.utils.IFunc;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.RegenerateEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.WorldActionEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.WorldFuncEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.WorldFuncEntryEx;
import org.primesoft.asyncworldedit.utils.FuncEx;
import org.primesoft.asyncworldedit.utils.MutexProvider;
import org.primesoft.asyncworldedit.utils.PositionHelper;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;
import org.primesoft.asyncworldedit.worldedit.WorldAsyncTask;
import org.primesoft.asyncworldedit.worldedit.WorldeditOperations;
import org.primesoft.asyncworldedit.worldedit.entity.EntityLazyWrapper;

/**
 *
 * @author SBPrime
 */
public class AsyncWorld extends AbstractWorldWrapper {

    /**
     * Wrap the world (if needed)
     *
     * @param world
     * @param player
     * @return
     */
    public static AsyncWorld wrap(World world, IPlayerEntry player) {
        if (world == null) {
            return null;
        }

        if (world instanceof AsyncWorld) {
            return (AsyncWorld) world;
        }

        return new AsyncWorld(world, player);
    }

    /**
     * The plugin
     */
    private final AsyncWorldEditBukkit m_plugin;

    /**
     * Bukkit schedule
     */
    private final BukkitScheduler m_schedule;

    /**
     * The player
     */
    private final IPlayerEntry m_player;

    /**
     * The bukkit world
     */
    private final IWorld m_bukkitWorld;

    /**
     * The block placer
     */
    private final IBlockPlacer m_blockPlacer;

    /**
     * The dispather
     */
    private final ITaskDispatcher m_dispatcher;

    /**
     * The blocks hub
     */
    private final IBlocksHubIntegration m_blocksHub;

    public AsyncWorld(World world, IPlayerEntry player) {
        super(world);

        m_plugin = (AsyncWorldEditBukkit)AsyncWorldEditBukkit.getInstance();
        m_player = player;
        m_schedule = m_plugin.getServer().getScheduler();
        m_blockPlacer = m_plugin.getBlockPlacer();
        m_dispatcher = m_plugin.getTaskDispatcher();
        m_blocksHub = m_plugin.getBlocksHub();

        m_bukkitWorld = m_plugin.getWorld(world.getName());        
    }

    @Override
    public String getName() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<String>() {
            @Override
            public String execute() {
                return m_parent.getName();
            }
        });
    }

    @Override
    public int getMaxY() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<Integer>() {
            @Override
            public Integer execute() {
                return m_parent.getMaxY();
            }
        });
    }

    @Override
    public boolean isValidBlockType(final int i) {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<Boolean>() {
            @Override
            public Boolean execute() {
                return m_parent.isValidBlockType(i);
            }
        });
    }

    @Override
    public boolean usesBlockData(final int i) {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<Boolean>() {
            @Override
            public Boolean execute() {
                return m_parent.usesBlockData(i);
            }
        });
    }

    @Override
    public Mask createLiquidMask() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<Mask>() {
            @Override
            public Mask execute() {
                return m_parent.createLiquidMask();
            }
        });
    }

    @Override
    public int getBlockType(final Vector vector) {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<Integer>() {
            @Override
            public Integer execute() {
                return m_parent.getBlockType(vector);
            }
        }, m_bukkitWorld, vector);
    }

    @Override
    public int getBlockData(final Vector vector) {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<Integer>() {

            @Override
            public Integer execute() {
                return m_parent.getBlockData(vector);
            }
        }, m_bukkitWorld, vector);
    }

    /**
     * Decide on the player UUID
     *
     * @param asyncParams
     * @return
     */
    private IPlayerEntry getPlayer(BaseAsyncParams... asyncParams) {
        IPlayerEntry result = m_player;

        for (BaseAsyncParams param : asyncParams) {
            if (!param.isEmpty()) {
                IPlayerEntry player = param.getPlayer();
                if (player != null && player.isPlayer()) {
                    result = player;
                }
            }
        }
        return result;
    }

    private boolean canPlaceData(IPlayerEntry player, IWorld world, Vector location, BaseBlock oldBlock, int newData) {
        return canPlace(player, world, location, oldBlock, new BaseBlock(oldBlock.getType(), newData));
    }

    private boolean canPlace(IPlayerEntry player, IWorld world, Vector location, BaseBlock oldBlock, int newType) {
        return canPlace(player, world, location, oldBlock, new BaseBlock(newType, oldBlock.getData()));
    }

    private boolean canPlace(IPlayerEntry player, IWorld world, Vector location, BaseBlock oldBlock, BaseBlock newBlock) {
        return m_blocksHub.canPlace(player, world, location, oldBlock, newBlock);
    }
    
    private boolean isSameData(BaseBlock oldBlock, int newData) {
        return isSame(oldBlock, new BaseBlock(oldBlock.getType(), newData));
    }
    
    private boolean isSame(BaseBlock oldBlock, int newType) {
        return isSame(oldBlock, new BaseBlock(newType, oldBlock.getData()));
    }
    
    private boolean isSame(BaseBlock oldBlock, BaseBlock newBlock) {
        return oldBlock.equals(newBlock) && !oldBlock.hasNbtData() && !newBlock.hasNbtData();
    }
    
    @Override
    public boolean setBlock(Vector vector, BaseBlock bb, final boolean bln) throws WorldEditException {
        final DataAsyncParams<BaseBlock> paramBlock = DataAsyncParams.extract(bb);
        final DataAsyncParams<Vector> paramVector = DataAsyncParams.extract(vector);

        final BaseBlock newBlock = paramBlock.getData();
        final Vector v = paramVector.getData();
        final IPlayerEntry player = getPlayer(paramBlock, paramVector);

        FuncEx<Boolean, WorldEditException> func = new FuncEx<Boolean, WorldEditException>() {
            @Override
            public Boolean execute() throws WorldEditException {
                final BaseBlock oldBlock = m_parent.getBlock(v);
                if (!canPlace(player, m_bukkitWorld, v, oldBlock, newBlock)
                        || isSame(oldBlock, newBlock)) {
                    return false;
                }

                final boolean result = m_parent.setBlock(v, newBlock, bln);
                if (result) {
                    logBlock(v, player, oldBlock, newBlock);
                }

                return result;
            }
        };

        if (paramBlock.isAsync() || paramVector.isAsync() || !m_dispatcher.isMainTask()) {
            if (!canPlace(player, m_bukkitWorld, vector, getBlock(v), newBlock)) {
                return false;
            }

            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntryEx(this, paramBlock.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public boolean setBlockType(Vector vector, final int i) {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        IFunc<Boolean> func = new IFunc<Boolean>() {
            @Override
            public Boolean execute() {
                final BaseBlock oldBlock = m_parent.getBlock(v);
                final BaseBlock newBlock = new BaseBlock(i, oldBlock.getData());
                if (!canPlace(player, m_bukkitWorld, v, oldBlock, newBlock)
                        || isSame(oldBlock, newBlock)) {
                    return false;
                }

                final boolean result = m_parent.setBlockType(v, i);
                if (result) {
                    logBlock(v, player, oldBlock, newBlock);
                }

                return result;
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            if (!canPlace(player, m_bukkitWorld, vector, getBlock(v), i)) {
                return false;
            }

            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this, param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public void setBlockData(Vector vector, final int i) {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        IFunc<Boolean> func = new IFunc<Boolean>() {
            @Override
            public Boolean execute() {
                final BaseBlock oldBlock = m_parent.getBlock(v);
                final BaseBlock newBlock = new BaseBlock(oldBlock.getType(), i);
                if (!canPlace(player, m_bukkitWorld, v, oldBlock, newBlock)
                        || isSame(oldBlock, newBlock)) {
                    return false;
                }

                m_parent.setBlockData(v, i);
                logBlock(v, player, oldBlock, newBlock);
                return true;
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            if (!canPlaceData(player, m_bukkitWorld, vector, getBlock(v), i)) {
                return;
            }

            m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this, param.getJobId(), v, func));
            return;
        }

        func.execute();
    }

    @Override
    public boolean setTypeIdAndData(Vector vector, final int i, final int i1) {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);
        final BaseBlock newBlock = new BaseBlock(i, i1);

        IFunc<Boolean> func = new IFunc<Boolean>() {
            @Override
            public Boolean execute() {
                final BaseBlock oldBlock = m_parent.getBlock(v);
                if (!canPlace(player, m_bukkitWorld, v, oldBlock, newBlock)
                        || isSame(oldBlock, newBlock)) {
                    return false;
                }

                final boolean result = m_parent.setTypeIdAndData(v, i, i1);
                if (result) {
                    logBlock(v, player, oldBlock, newBlock);
                }

                return result;
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            if (!canPlace(player, m_bukkitWorld, vector, getBlock(v), newBlock)) {
                return false;
            }

            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this, param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public int getBlockLightLevel(final Vector vector) {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<Integer>() {
            @Override
            public Integer execute() {
                return m_parent.getBlockLightLevel(vector);
            }
        }, m_bukkitWorld, vector);
    }

    @Override
    public boolean clearContainerBlockContents(final Vector vector) {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return false;
        }

        IFunc<Boolean> func = new IFunc<Boolean>() {
            @Override
            public Boolean execute() {
                return m_parent.clearContainerBlockContents(vector);
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this, param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public BaseBiome getBiome(final Vector2D vd) {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<BaseBiome>() {
            @Override
            public BaseBiome execute() {
                return m_parent.getBiome(vd);
            }
        }, m_bukkitWorld, new Vector(vd.getX(), 0, vd.getZ()));
    }

    @Override
    public WorldData getWorldData() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<WorldData>() {
            @Override
            public WorldData execute() {
                return m_parent.getWorldData();
            }
        });
    }

    @Override
    public Entity createEntity(final Location lctn, final BaseEntity be) {
        final DataAsyncParams<Location> paramLocation = DataAsyncParams.extract(lctn);
        final DataAsyncParams<BaseEntity> paramEntity = DataAsyncParams.extract(be);
        final Location location = paramLocation.getData();
        final BaseEntity entity = paramEntity.getData();
        final IPlayerEntry player = getPlayer(paramLocation, paramEntity);

        final EntityLazyWrapper entityWrapper = new EntityLazyWrapper(location, this);
        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, location.toVector())) {
            return entityWrapper; //Return the entity wrapper so WorldEdit does not complain
        }

        IFunc<Entity> func = new IFunc<Entity>() {
            @Override
            public Entity execute() {
                Entity result = m_parent.createEntity(location, entity);

                if (result != null) {
                    entityWrapper.setEntity(result);
                }
                return result;
            }
        };

        if (paramEntity.isAsync() || paramLocation.isAsync() || !m_dispatcher.isMainTask()) {
            if (!m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this, paramLocation.getJobId(), location.toVector(), func))) {
                return entityWrapper; //Return the entity erapper so WorldEdit does not complain
            }
            return entityWrapper;
        }

        return func.execute();
    }

    @Override
    public boolean setBiome(Vector2D vd, final BaseBiome bb) {
        final DataAsyncParams<Vector2D> paramVector = DataAsyncParams.extract(vd);
        final DataAsyncParams<BaseBiome> paramBiome = DataAsyncParams.extract(bb);
        final Vector2D v = paramVector.getData();
        final BaseBiome biome = paramBiome.getData();
        final IPlayerEntry player = getPlayer(paramBiome, paramVector);
        final Vector tmpV = new Vector(v.getX(), 0, v.getZ());

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, tmpV)) {
            return false;
        }

        IFunc<Boolean> func = new IFunc<Boolean>() {
            @Override
            public Boolean execute() {
                return m_parent.setBiome(v, biome);
            }
        };

        if (paramBiome.isAsync() || paramVector.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this, paramBiome.getJobId(), tmpV, func));
        }

        return func.execute();
    }

    @Override
    public void dropItem(Vector vector, final BaseItemStack bis, final int i) {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return;
        }

        IAction func = new IAction() {
            @Override
            public void execute() {
                m_parent.dropItem(v, bis, i);
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            m_blockPlacer.addTasks(player,
                    new WorldActionEntry(this, param.getJobId(), v, func));
            return;
        }

        func.execute();
    }

    @Override
    public void dropItem(final Vector vector, final BaseItemStack bis) {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return;
        }

        IAction func = new IAction() {
            @Override
            public void execute() {
                m_parent.dropItem(v, bis);
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            m_blockPlacer.addTasks(player,
                    new WorldActionEntry(this, param.getJobId(), v, func));
            return;
        }

        func.execute();
    }

    @Override
    public void simulateBlockMine(Vector vector) {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        IAction func = new IAction() {
            @Override
            public void execute() {
                BaseBlock air = new BaseBlock(0);
                BaseBlock oldBlock = m_parent.getBlock(v);
                if (!canPlace(player, m_bukkitWorld, v, oldBlock, air) || isSame(oldBlock, air)) {
                    return;
                }
                m_parent.simulateBlockMine(v);
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            if (!canPlace(player, m_bukkitWorld, v, getBlock(v), new BaseBlock(0))) {
                return;
            }

            m_blockPlacer.addTasks(player,
                    new WorldActionEntry(this, param.getJobId(), v, func));
            return;
        }

        func.execute();
    }

    @Override
    public List<? extends Entity> getEntities(final Region region) {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<List<? extends Entity>>() {
            @Override
            public List<? extends Entity> execute() {
                return m_parent.getEntities(region);
            }
        }, m_bukkitWorld, region);
    }

    @Override
    public List<? extends Entity> getEntities() {
        //No position we better invoke this operation
        return m_dispatcher.queueFastOperation(new IFunc<List<? extends Entity>>() {
            @Override
            public List<? extends Entity> execute() {
                return m_parent.getEntities();
            }
        });
    }

    @Override
    public boolean regenerate(final Region region, final EditSession editSession) {
        boolean isAsync = checkAsync(WorldeditOperations.regenerate);
        if (!isAsync) {
            return m_parent.regenerate(region, editSession);
        }

        final int jobId = getJobId();
        final EditSession session;
        final JobEntry job;

        if (editSession instanceof AsyncEditSession) {
            AsyncEditSession aSession = (AsyncEditSession) editSession;
            session = new CancelabeEditSession(aSession, aSession.getMask(), jobId);
            job = new JobEntry(m_player, (CancelabeEditSession) session, jobId, "regenerate");
        } else {
            session = editSession;
            job = new JobEntry(m_player, jobId, "regenerate");
        }

        m_blockPlacer.addJob(m_player, job);

        final int maxY = getMaxY();
        m_schedule.runTaskAsynchronously(m_plugin, new WorldAsyncTask(m_bukkitWorld, session,
                m_player, "regenerate", m_blockPlacer, job) {                    
                    @Override
                    public void task(EditSession editSession, IWorld world) throws MaxChangedBlocksException {
                        doRegen(editSession, region, maxY, world, jobId);
                    }

                });

        return true;
    }

    /**
     * Perfrom the regen operation
     *
     * @param eSession
     * @param region
     * @param world
     */
    private void doRegen(EditSession eSession, Region region, int maxY, IWorld world, int jobId) {
        BaseBlock[] history = new BaseBlock[16 * 16 * (maxY + 1)];

        for (Vector2D chunk : region.getChunks()) {
            Vector min = PositionHelper.chunkToPosition(chunk, 0);

            // First save all the blocks inside
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < (maxY + 1); ++y) {
                    for (int z = 0; z < 16; ++z) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;
                        history[index] = eSession.getBlock(pt);
                    }
                }
            }

            m_blockPlacer.addTasks(m_player, new RegenerateEntry(jobId, world, chunk));

            // Then restore
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < (maxY + 1); ++y) {
                    for (int z = 0; z < 16; ++z) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;

                        // We have to restore the block if it was outside
                        if (!region.contains(pt)) {
                            eSession.smartSetBlock(pt, history[index]);
                        } else { // Otherwise fool with history
                            eSession.rememberChange(pt, history[index],
                                    eSession.rawGetBlock(pt));
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean generateTree(final TreeGenerator.TreeType tt, final EditSession es, Vector vector) throws MaxChangedBlocksException {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return false;
        }

        FuncEx<Boolean, MaxChangedBlocksException> func = new FuncEx<Boolean, MaxChangedBlocksException>() {
            @Override
            public Boolean execute() throws MaxChangedBlocksException {
                return m_parent.generateTree(tt, es, v);
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntryEx(this, param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public boolean generateTree(final EditSession es, Vector vector) throws MaxChangedBlocksException {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return false;
        }

        FuncEx<Boolean, MaxChangedBlocksException> func = new FuncEx<Boolean, MaxChangedBlocksException>() {
            @Override
            public Boolean execute() throws MaxChangedBlocksException {
                return m_parent.generateTree(es, v);
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntryEx(this, param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public boolean generateBigTree(final EditSession es, Vector vector) throws MaxChangedBlocksException {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return false;
        }

        FuncEx<Boolean, MaxChangedBlocksException> func = new FuncEx<Boolean, MaxChangedBlocksException>() {
            @Override
            public Boolean execute() throws MaxChangedBlocksException {
                return m_parent.generateBigTree(es, v);
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntryEx(this, param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public boolean generateBirchTree(final EditSession es, Vector vector) throws MaxChangedBlocksException {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return false;
        }

        FuncEx<Boolean, MaxChangedBlocksException> func = new FuncEx<Boolean, MaxChangedBlocksException>() {
            @Override
            public Boolean execute() throws MaxChangedBlocksException {
                return m_parent.generateBirchTree(es, v);
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntryEx(this, param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public boolean generateRedwoodTree(final EditSession es, Vector vector) throws MaxChangedBlocksException {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return false;
        }

        FuncEx<Boolean, MaxChangedBlocksException> func = new FuncEx<Boolean, MaxChangedBlocksException>() {
            @Override
            public Boolean execute() throws MaxChangedBlocksException {
                return m_parent.generateRedwoodTree(es, v);
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntryEx(this, param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public boolean generateTallRedwoodTree(final EditSession es, Vector vector) throws MaxChangedBlocksException {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return false;
        }

        FuncEx<Boolean, MaxChangedBlocksException> func = new FuncEx<Boolean, MaxChangedBlocksException>() {
            @Override
            public Boolean execute() throws MaxChangedBlocksException {
                return m_parent.generateTallRedwoodTree(es, v);
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntryEx(this, param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public void checkLoadedChunk(final Vector vector) {
        m_dispatcher.performSafeChunk(MutexProvider.getMutex(getWorld()), new IAction() {
            @Override
            public void execute() {
                m_parent.checkLoadedChunk(vector);
            }
        }, m_bukkitWorld, PositionHelper.positionToChunk(vector));
    }

    @Override
    public void fixAfterFastMode(final Iterable<BlockVector2D> itrbl) {
        final Collection<BlockVector2D> tmp = new ArrayList<BlockVector2D>();
        for (Iterator<BlockVector2D> iterator = itrbl.iterator(); iterator.hasNext();) {
            tmp.add(iterator.next());
        }         
        
        m_dispatcher.performSafeChunk(MutexProvider.getMutex(getWorld()), new IAction() {
            @Override
            public void execute() {
                m_parent.fixAfterFastMode(tmp);
            }
        }, m_bukkitWorld, tmp);
    }

    @Override
    public void fixLighting(final Iterable<BlockVector2D> itrbl) {
        final Collection<BlockVector2D> tmp = new ArrayList<BlockVector2D>();
        for (Iterator<BlockVector2D> iterator = itrbl.iterator(); iterator.hasNext();) {
            tmp.add(iterator.next());
        }
        
        m_dispatcher.performSafeChunk(MutexProvider.getMutex(getWorld()), new IAction() {
            @Override
            public void execute() {
                m_parent.fixLighting(tmp);
            }
        }, m_bukkitWorld, tmp);
    }

    @Override
    public boolean playEffect(Vector vector, final int i, final int i1) {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        if (!m_blocksHub.hasAccess(player, m_bukkitWorld, v)) {
            return false;
        }

        IFunc<Boolean> func = new IFunc<Boolean>() {
            @Override
            public Boolean execute() {
                return m_parent.playEffect(v, i, i1);
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this, param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public boolean queueBlockBreakEffect(final Platform pltform, Vector vector, final int i, final double d) {
        final DataAsyncParams<Vector> param = DataAsyncParams.extract(vector);
        final Vector v = param.getData();
        final IPlayerEntry player = getPlayer(param);

        IFunc<Boolean> func = new IFunc<Boolean>() {
            @Override
            public Boolean execute() {
                BaseBlock air = new BaseBlock(0);
                BaseBlock oldBlock = m_parent.getBlock(v);
                if (!canPlace(player, m_bukkitWorld, v, oldBlock, air) || isSame(oldBlock, air)) {
                    return false;
                }

                return m_parent.queueBlockBreakEffect(pltform, v, i, d);
            }
        };

        if (param.isAsync() || !m_dispatcher.isMainTask()) {
            if (!canPlace(player, m_bukkitWorld, vector, getBlock(v), new BaseBlock(0))) {
                return false;
            }

            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntry(this, param.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public Vector getMinimumPoint() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<Vector>() {
            @Override
            public Vector execute() {
                return m_parent.getMinimumPoint();
            }
        });
    }

    @Override
    public Vector getMaximumPoint() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<Vector>() {
            @Override
            public Vector execute() {
                return m_parent.getMaximumPoint();
            }
        });
    }

    @Override
    public BaseBlock getBlock(final Vector vector) {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<BaseBlock>() {
            @Override
            public BaseBlock execute() {
                return m_parent.getBlock(vector);
            }
        }, m_bukkitWorld, vector);
    }

    @Override
    public BaseBlock getLazyBlock(final Vector vector) {
        return m_dispatcher.performSafeChunk(MutexProvider.getMutex(getWorld()), new IFunc<BaseBlock>() {
            @Override
            public BaseBlock execute() {
                return m_parent.getLazyBlock(vector);
            }
        }, m_bukkitWorld, PositionHelper.positionToChunk(vector));
    }

    @Override
    public boolean setBlock(final Vector vector, final BaseBlock bb) throws WorldEditException {
        final DataAsyncParams<BaseBlock> paramBlock = DataAsyncParams.extract(bb);
        final DataAsyncParams<Vector> paramVector = DataAsyncParams.extract(vector);

        final BaseBlock newBlock = paramBlock.getData();
        final Vector v = paramVector.getData();
        final IPlayerEntry player = getPlayer(paramBlock, paramVector);

        FuncEx<Boolean, WorldEditException> func = new FuncEx<Boolean, WorldEditException>() {

            @Override
            public Boolean execute() throws WorldEditException {
                final BaseBlock oldBlock = m_parent.getBlock(v);
                if (!canPlace(player, m_bukkitWorld, v, oldBlock, newBlock)
                        || isSame(oldBlock, newBlock)) {
                    return false;
                }

                final boolean result = m_parent.setBlock(vector, newBlock);
                if (result) {
                    logBlock(vector, player, oldBlock, newBlock);
                }

                return result;
            }
        };

        if (paramBlock.isAsync() || paramVector.isAsync() || !m_dispatcher.isMainTask()) {
            if (!canPlace(player, m_bukkitWorld, vector, getBlock(v), newBlock)) {
                return false;
            }

            return m_blockPlacer.addTasks(player,
                    new WorldFuncEntryEx(this, paramBlock.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public boolean useItem(Vector vector, final BaseItem bi, final Direction drctn) {
        final DataAsyncParams<Vector> paramVector = DataAsyncParams.extract(vector);
        final Vector v = paramVector.getData();
        final IPlayerEntry player = getPlayer(paramVector);

        IFunc<Boolean> func = new IFunc<Boolean>() {

            @Override
            public Boolean execute() {
                return m_parent.useItem(v, bi, drctn);
            }
        };

        if (paramVector.isAsync() || !m_dispatcher.isMainTask()) {
            return m_blockPlacer.addTasks(player, 
                    new WorldFuncEntry(this, paramVector.getJobId(), v, func));
        }

        return func.execute();
    }

    @Override
    public Operation commit() {
        return m_dispatcher.performSafe(MutexProvider.getMutex(getWorld()), new IFunc<Operation>() {
            @Override
            public Operation execute() {
                return m_parent.commit();
            }
        });
    }

    /**
     * Log placed block using blocks hub
     */
    private void logBlock(Vector location, IPlayerEntry player, BaseBlock oldBlock, BaseBlock newBlock) {
        m_blocksHub.logBlock(player, m_bukkitWorld, location, oldBlock, newBlock);
    }

    /**
     * This function checks if async mode is enabled for specific command
     *
     * @param operation
     */
    private boolean checkAsync(WorldeditOperations operation) {
        return ConfigProvider.isAsyncAllowed(operation) && m_player.getAweMode();
    }

    /**
     * Get next job id for current player
     *
     * @return Job id
     */
    private int getJobId() {
        return m_blockPlacer.getJobId(m_player);
    }
}
