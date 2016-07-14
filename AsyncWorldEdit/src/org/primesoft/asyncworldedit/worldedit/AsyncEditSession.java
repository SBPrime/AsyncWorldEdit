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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.biome.BaseBiome;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.bukkit.scheduler.BukkitScheduler;
import org.primesoft.asyncworldedit.AsyncWorldEditBukkit;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.RedoJob;
import org.primesoft.asyncworldedit.blockPlacer.entries.UndoJob;
import org.primesoft.asyncworldedit.utils.SchedulerUtils;
import org.primesoft.asyncworldedit.utils.WaitFor;

/**
 *
 * @author SBPrime
 */
public class AsyncEditSession extends ThreadSafeEditSession {
    /**
     * Bukkit schedule
     */
    private final BukkitScheduler m_schedule;


    /**
     * The function wait object
     */
    private final WaitFor m_wait = new WaitFor();
    

    /**
     * Get the wait object
     *
     * @return
     */
    public WaitFor getWait() {
        return m_wait;
    }
   

    public AsyncEditSession(AsyncWorldEditBukkit plugin,
            IPlayerEntry player, EventBus eventBus, com.sk89q.worldedit.world.World world,
            int maxBlocks, @Nullable BlockBag blockBag, EditSessionEvent event) {

        //super(eventBus, AsyncWorld.wrap(world, player), maxBlocks, blockBag, event);
        super(plugin, player, eventBus, world, maxBlocks, blockBag, event);
        
        m_schedule = plugin.getServer().getScheduler();
    }

    /**
     * Do not change! Requires special processing
     * @param sess 
     */
    @Override
    public void undo(final EditSession sess) {
        final int jobId = getJobId();

        cancelJobs(jobId);

        boolean isAsync = checkAsync(WorldeditOperations.undo);
        Mask mask = getMask();
        final CancelabeEditSession session = new CancelabeEditSession(this, mask, jobId);

        if (!isAsync) {
            session.undo(sess);
            return;
        }

        final LocalSession ls = getLocalSession();
        final JobEntry job = new UndoJob(m_player, session, jobId, "undo");
        m_blockPlacer.addJob(m_player, job);

        SchedulerUtils.runTaskAsynchronouslyInSequence(m_plugin, m_schedule, new AsyncTask(session, m_player, "undo",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        session.undo(sess);
                        return 0;
                    }
                }, ls);
    }

    /**
     * Get the local session for current player
     *
     * @return
     */
    private LocalSession getLocalSession() {
        final WorldEdit we = WorldEdit.getInstance();
        final SessionManager sm = we.getSessionManager();
        final LocalSession ls = sm.findByName(m_player.getName());
        return ls;
    }

    public void flushQueue(int jobId) {
        boolean queued = isQueueEnabled();
        m_jobId = jobId;
        super.flushQueue();
        m_jobId = -1;
        if (queued) {
            resetAsync();
        }
    }
    
    /**
     * Do not change! Requires special processing
     * @param sess 
     */
    @Override
    public void redo(final EditSession sess) {
        boolean isAsync = checkAsync(WorldeditOperations.redo);

        Mask mask = getMask();
        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, mask, jobId);
        if (!isAsync) {
            session.redo(sess);
            //doRedo(sess);
            return;
        }

        final LocalSession ls = getLocalSession();
        final JobEntry job = new RedoJob(m_player, session, jobId, "redo");
        m_blockPlacer.addJob(m_player, job);

        SchedulerUtils.runTaskAsynchronouslyInSequence(m_plugin, m_schedule, new AsyncTask(session, m_player, "redo",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        session.redo(sess);
                        return 0;
                    }
                }, ls);
    }

    
    /**
     * Does not use Operations - do not change!
     * @param region
     * @param zero
     * @param unit
     * @param biomeType
     * @param expressionString
     * @param hollow
     * @return
     * @throws ExpressionException
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int makeBiomeShape(final Region region, final Vector zero, final Vector unit,
            final BaseBiome biomeType, final String expressionString,
            final boolean hollow)
            throws ExpressionException, MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.makeBiomeShape);
        if (!isAsync) {
            return super.makeBiomeShape(region, zero, unit, biomeType, expressionString, hollow);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "makeBiomeShape");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "makeBiomeShape",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        try {
                            return session.makeBiomeShape(region, zero, unit, biomeType, expressionString, hollow);
                        } catch (ExpressionException ex) {
                            return 0;
                        }
                    }
                });

        return 0;
    }    
    

    /**
     * Does not use Operations - do not change!
     * @param region
     * @param pattern
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int makeFaces(final Region region, final Pattern pattern) throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.makeCuboidFaces);
        if (!isAsync) {
            return super.makeFaces(region, pattern);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "makeFaces");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "makeFaces",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.makeFaces(region, pattern);
                    }
                });

        return 0;
    }

    /**
     * Does not use Operations - do not change!
     * @param region
     * @param pattern
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int makeWalls(final Region region, final Pattern pattern) throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.makeCuboidWalls);
        if (!isAsync) {
            return super.makeWalls(region, pattern);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "makeWalls");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "makeWalls",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.makeWalls(region, pattern);
                    }
                });

        return 0;
    }
    
    /**
     * Does not use Operations - do not change!
     * @param pattern
     * @param pos1
     * @param pos2
     * @param radius
     * @param filled
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int drawLine(final Pattern pattern, final Vector pos1, final Vector pos2, final double radius,
            final boolean filled)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.drawLine);
        if (!isAsync) {
            return super.drawLine(pattern, pos1, pos2, radius, filled);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "drawLine");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "drawLine",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.drawLine(pattern, pos1, pos2, radius, filled);
                    }
                });

        return 0;
    }

    /**
     * Does not use Operations - do not change!
     * @param pattern
     * @param nodevectors
     * @param tension
     * @param bias
     * @param continuity
     * @param quality
     * @param radius
     * @param filled
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int drawSpline(final Pattern pattern,
            final List<Vector> nodevectors, final double tension, final double bias,
            final double continuity, final double quality, final double radius,
            final boolean filled)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.drawLine);
        if (!isAsync) {
            return super.drawSpline(pattern, nodevectors, tension, bias, continuity, quality, radius, filled);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "drawLine");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "drawLine",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.drawSpline(pattern, nodevectors, tension, bias, continuity, quality, radius, filled);
                    }
                });

        return 0;
    }

    
    /**
     * Does not use Operations - do not change!
     * @param pos
     * @param block
     * @param radius
     * @param height
     * @param filled
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int makeCylinder(final Vector pos, final Pattern block,
            final double radius, final int height,
            final boolean filled)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.makeCylinder);
        if (!isAsync) {
            return super.makeCylinder(pos, block, radius, height, filled);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "makeCylinder");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "makeCylinder",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.makeCylinder(pos, block, radius, height, filled);
                    }
                });

        return 0;
    }

    
    /**
     * Does not use Operations - do not change!
     * @param pos
     * @param block
     * @param radiusX
     * @param radiusZ
     * @param height
     * @param filled
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int makeCylinder(final Vector pos, final Pattern block,
            final double radiusX,
            final double radiusZ, final int height,
            final boolean filled)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.makeCylinder);
        if (!isAsync) {
            return super.makeCylinder(pos, block, radiusX, radiusZ, height, filled);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "makeCylinder");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "makeCylinder",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.makeCylinder(pos, block, radiusX, radiusZ, height, filled);
                    }
                });

        return 0;
    }

    
    /**
     * Does not use Operations - do not change!
     * @param pos
     * @param block
     * @param radius
     * @param filled
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int makeSphere(final Vector pos, final Pattern block,
            final double radius,
            final boolean filled)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.makeSphere);
        if (!isAsync) {
            return super.makeSphere(pos, block, radius, filled);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "makeSphere");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "makeSphere",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.makeSphere(pos, block, radius, filled);
                    }
                });

        return 0;
    }

    
    /**
     * Does not use Operations - do not change!
     * @param pos
     * @param block
     * @param radiusX
     * @param radiusY
     * @param radiusZ
     * @param filled
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int makeSphere(final Vector pos, final Pattern block,
            final double radiusX,
            final double radiusY, final double radiusZ,
            final boolean filled)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.makeSphere);
        if (!isAsync) {
            return super.makeSphere(pos, block, radiusX, radiusY, radiusZ, filled);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "makeSphere");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "makeSphere",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.makeSphere(pos, block, radiusX, radiusY, radiusZ, filled);
                    }
                });

        return 0;
    }

    /**
     * Does not use Operations - do not change!
     * @param pos
     * @param block
     * @param size
     * @param filled
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int makePyramid(final Vector pos, final Pattern block, final int size,
            final boolean filled)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.makePyramid);
        if (!isAsync) {
            return super.makePyramid(pos, block, size, filled);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "makePyramid");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "makePyramid",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.makePyramid(pos, block, size, filled);
                    }
                });

        return 0;
    }

    /**
     * Does not use Operations - do not change!
     * @param pos
     * @param radius
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int thaw(final Vector pos, final double radius)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.thaw);
        if (!isAsync) {
            return super.thaw(pos, radius);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "thaw");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "thaw",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.thaw(pos, radius);
                    }
                });

        return 0;
    }

    
    /**
     * Does not use Operations - do not change!
     * @param pos
     * @param radius
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int simulateSnow(final Vector pos, final double radius)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.simulateSnow);
        if (!isAsync) {
            return super.simulateSnow(pos, radius);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "simulateSnow");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "simulateSnow",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.simulateSnow(pos, radius);
                    }
                });

        return 0;
    }

    /**
     * Does not use Operations - do not change!
     * @param pos
     * @param radius
     * @param onlyNormalDirt
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int green(final Vector pos, final double radius, final boolean onlyNormalDirt)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.green);
        if (!isAsync) {
            return super.green(pos, radius, onlyNormalDirt);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "green");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "green",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.green(pos, radius, onlyNormalDirt);
                    }
                });

        return 0;
    }

    
    /**
     * Does not use Operations - do not change!
     * @param pos
     * @param radius
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int green(final Vector pos, final double radius)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.green);
        if (!isAsync) {
            return super.green(pos, radius);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "green");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "green",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.green(pos, radius);
                    }
                });

        return 0;
    }

    /**
     * TODO: Broken
     * @param basePos
     * @param size
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int makePumpkinPatches(final Vector basePos, final int size)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.makePumpkinPatches);
        if (!isAsync) {
            return super.makePumpkinPatches(basePos, size);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "makePumpkinPatches");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "makePumpkinPatches",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.makePumpkinPatches(basePos, size);
                    }
                });

        return 0;
    }

    /**
     * Does not use Operations - do not change!
     * @param basePos
     * @param size
     * @param density
     * @param treeGenerator
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int makeForest(final Vector basePos, final int size,
            final double density,
            final TreeGenerator treeGenerator)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.makeForest);
        if (!isAsync) {
            return super.makeForest(basePos, size, density, treeGenerator);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "makeForest");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "makeForest",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.makeForest(basePos, size, density, treeGenerator);
                    }
                });

        return 0;
    }

    /**
     * Does not use Operations - do not change!
     * @param region
     * @param zero
     * @param unit
     * @param pattern
     * @param expressionString
     * @param hollow
     * @return
     * @throws ExpressionException
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int makeShape(final Region region, final Vector zero,
            final Vector unit,
            final Pattern pattern, final String expressionString,
            final boolean hollow)
            throws ExpressionException, MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.makeShape);
        if (!isAsync) {
            return super.makeShape(region, zero, unit, pattern, expressionString, hollow);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "makeShape");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "makeShape",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        try {
                            return session.makeShape(region, zero, unit, pattern, expressionString, hollow);
                        } catch (ExpressionException ex) {                            
                            return 0;
                        }
                    }
                });

        return 0;
    }

    /**
     * Does not use Operations - do not change!
     * @param region
     * @param zero
     * @param unit
     * @param expressionString
     * @return
     * @throws ExpressionException
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int deformRegion(final Region region, final Vector zero,
            final Vector unit,
            final String expressionString)
            throws ExpressionException, MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.deformRegion);
        if (!isAsync) {
            return super.deformRegion(region, zero, unit, expressionString);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "deformRegion");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "deformRegion",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        try {
                            return session.deformRegion(region, zero, unit, expressionString);
                        } catch (ExpressionException ex) {
                            return 0;
                        }
                    }
                });

        return 0;
    }

    
    /**
     * Does not use Operations - do not change!
     * @param region
     * @param thickness
     * @param pattern
     * @return
     * @throws MaxChangedBlocksException 
     */
    @Override
    public int hollowOutRegion(final Region region, final int thickness,
            final Pattern pattern)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.hollowOutRegion);
        if (!isAsync) {
            return super.hollowOutRegion(region, thickness, pattern);
        }

        final int jobId = getJobId();
        final CancelabeEditSession session = new CancelabeEditSession(this, getMask(), jobId);
        final JobEntry job = new JobEntry(m_player, session, jobId, "hollowOutRegion");
        m_blockPlacer.addJob(m_player, job);

        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(session, m_player, "hollowOutRegion",
                m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        m_wait.checkAndWait(null);
                        return session.hollowOutRegion(region, thickness, pattern);
                    }
                });

        return 0;
    }

    //--------------------------------------------------------------------------//
    //-- ASYNCED OPERATIONS USING OPERATIONS -----------------------------------//
    //--------------------------------------------------------------------------//
    @Override
    public int center(Region region, Pattern pattern) throws MaxChangedBlocksException {
        return super.center(region, pattern); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    @Override
    public int drainArea(Vector origin, double radius) throws MaxChangedBlocksException {
        return super.drainArea(origin, radius); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int fillXZ(Vector origin, BaseBlock block, double radius, int depth, boolean recursive) throws MaxChangedBlocksException {
        return super.fillXZ(origin, block, radius, depth, recursive); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int fillXZ(Vector origin, Pattern pattern, double radius, int depth, boolean recursive) throws MaxChangedBlocksException {
        return super.fillXZ(origin, pattern, radius, depth, recursive); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int fixLiquid(Vector origin, double radius, int moving, int stationary) throws MaxChangedBlocksException {
        return super.fixLiquid(origin, radius, moving, stationary); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int makeCuboidFaces(Region region, BaseBlock block) throws MaxChangedBlocksException {
        return super.makeCuboidFaces(region, block); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int makeCuboidFaces(Region region, Pattern pattern) throws MaxChangedBlocksException {
        return super.makeCuboidFaces(region, pattern); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int makeCuboidWalls(Region region, BaseBlock block) throws MaxChangedBlocksException {
        return super.makeCuboidWalls(region, block); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int makeCuboidWalls(Region region, Pattern pattern) throws MaxChangedBlocksException {
        return super.makeCuboidWalls(region, pattern); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int moveCuboidRegion(Region region, Vector dir, int distance, boolean copyAir, BaseBlock replacement) throws MaxChangedBlocksException {
        return super.moveCuboidRegion(region, dir, distance, copyAir, replacement); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int moveRegion(Region region, Vector dir, int distance, boolean copyAir, BaseBlock replacement) throws MaxChangedBlocksException {
        return super.moveRegion(region, dir, distance, copyAir, replacement); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int naturalizeCuboidBlocks(Region region) throws MaxChangedBlocksException {
        return super.naturalizeCuboidBlocks(region); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int overlayCuboidBlocks(Region region, BaseBlock block) throws MaxChangedBlocksException {
        return super.overlayCuboidBlocks(region, block); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int overlayCuboidBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
        return super.overlayCuboidBlocks(region, pattern); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int removeAbove(Vector position, int apothem, int height) throws MaxChangedBlocksException {
        return super.removeAbove(position, apothem, height); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int removeBelow(Vector position, int apothem, int height) throws MaxChangedBlocksException {
        return super.removeBelow(position, apothem, height); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int removeNear(Vector position, int blockType, int apothem) throws MaxChangedBlocksException {
        return super.removeNear(position, blockType, apothem); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int replaceBlocks(Region region, Mask mask, Pattern pattern) throws MaxChangedBlocksException {
        return super.replaceBlocks(region, mask, pattern); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int replaceBlocks(Region region, Set<BaseBlock> filter, BaseBlock replacement) throws MaxChangedBlocksException {
        return super.replaceBlocks(region, filter, replacement); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int replaceBlocks(Region region, Set<BaseBlock> filter, Pattern pattern) throws MaxChangedBlocksException {
        return super.replaceBlocks(region, filter, pattern); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int setBlocks(Region region, BaseBlock block) throws MaxChangedBlocksException {
        return super.setBlocks(region, block); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int setBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
        return super.setBlocks(region, pattern); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int stackCuboidRegion(Region region, Vector dir, int count, boolean copyAir) throws MaxChangedBlocksException {
        return super.stackCuboidRegion(region, dir, count, copyAir); //To change body of generated methods, choose Tools | Templates.
    }
}