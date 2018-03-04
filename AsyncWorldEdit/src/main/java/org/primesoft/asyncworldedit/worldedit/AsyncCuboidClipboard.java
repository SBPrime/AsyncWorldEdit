/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
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

import org.primesoft.asyncworldedit.configuration.WorldeditOperations;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.regions.Region;
import org.primesoft.asyncworldedit.core.AwePlatform;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.platform.api.IScheduler;
import org.primesoft.asyncworldedit.utils.SchedulerUtils;
import org.primesoft.asyncworldedit.utils.WaitFor;

/**
 * This clipboar is used to async clipboard operations Note: Do not use any
 * operations from this class, always use th parrent!
 *
 * @author SBPrime
 */
@Deprecated
public class AsyncCuboidClipboard extends ProxyCuboidClipboard {

    /**
     * The player
     */
    private final IPlayerEntry m_player;

    /**
     * The blocks placer
     */
    private final IBlockPlacer m_blockPlacer;

    /**
     * Parent clipboard
     */
    private final CuboidClipboard m_clipboard;

    /**
     * Bukkit schedule
     */
    private final IScheduler m_schedule;

    public AsyncCuboidClipboard(IPlayerEntry player, CuboidClipboard parrent) {
        super(new ProxyCuboidClipboard(parrent));

        AwePlatform platform = AwePlatform.getInstance();
        m_schedule = platform.getPlatform().getScheduler();
        m_clipboard = parrent;
        m_blockPlacer = platform.getCore().getBlockPlacer();
        m_player = player;
    }

    @Override
    public LocalEntity[] pasteEntities(final Vector pos) {
        boolean isAsync = checkAsync(WorldeditOperations.paste, null);
        if (!isAsync) {
            return super.pasteEntities(pos);
        }

        final int jobId = getJobId();
        final CuboidClipboardWrapper cc = new CuboidClipboardWrapper(m_player, m_clipboard, jobId);
        final JobEntry job = new JobEntry(m_player, jobId, "pasteEntities");
        m_blockPlacer.addJob(m_player, job);

        SchedulerUtils.runTaskAsynchronously(m_schedule, new ClipboardAsyncTask(cc, null, m_player, "pasteEntities",
                m_blockPlacer, job) {
                    @Override
                    public void task(CuboidClipboard cc)
                    throws MaxChangedBlocksException {
                        cc.pasteEntities(pos);
                    }
                });

        return new LocalEntity[0];
    }

    @Override
    public void place(final EditSession editSession, final Vector pos,
            final boolean noAir)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.paste, editSession);
        if (!isAsync) {
            super.place(editSession, pos, noAir);
            return;
        }

        final int jobId = getJobId();
        final EditSession session;
        final CuboidClipboardWrapper cc = new CuboidClipboardWrapper(m_player, m_clipboard, jobId);
        final JobEntry job;
        final WaitFor wait;

        if (editSession instanceof AsyncEditSession) {
            AsyncEditSession aSession = (AsyncEditSession) editSession;
            wait = aSession.getWait();
            session = new CancelabeEditSession(aSession, aSession.getMask(), jobId);
            job = new JobEntry(m_player, (CancelabeEditSession) session, jobId, "place");
        } else {
            session = editSession;
            wait = null;
            job = new JobEntry(m_player, jobId, "place");
        }

        m_blockPlacer.addJob(m_player, job);

        SchedulerUtils.runTaskAsynchronously(m_schedule, new ClipboardAsyncTask(cc, session, m_player, "place",
                m_blockPlacer, job) {
                    @Override
                    public void task(CuboidClipboard cc)
                    throws MaxChangedBlocksException {
                        if (wait != null) {
                            wait.checkAndWait(null);
                        }
                        cc.place(session, pos, noAir);
                    }
                });
    }

    @Override
    public void paste(final EditSession editSession, final Vector newOrigin,
            final boolean noAir)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.paste, editSession);
        if (!isAsync) {
            super.paste(editSession, newOrigin, noAir);
            return;
        }

        final int jobId = getJobId();
        final EditSession session;
        final CuboidClipboardWrapper cc = new CuboidClipboardWrapper(m_player, m_clipboard, jobId);
        final JobEntry job;
        final WaitFor wait;

        if (editSession instanceof AsyncEditSession) {
            AsyncEditSession aSession = (AsyncEditSession) editSession;
            wait = aSession.getWait();
            session = new CancelabeEditSession(aSession, aSession.getMask(), jobId);
            job = new JobEntry(m_player, (CancelabeEditSession) session, jobId, "place");
        } else {
            session = editSession;
            wait = null;
            job = new JobEntry(m_player, jobId, "place");
        }
        m_blockPlacer.addJob(m_player, job);

        SchedulerUtils.runTaskAsynchronously(m_schedule, new ClipboardAsyncTask(cc, session, m_player, "paste",
                m_blockPlacer, job) {
                    @Override
                    public void task(CuboidClipboard cc)
                    throws MaxChangedBlocksException {
                        if (wait != null) {
                            wait.checkAndWait(null);
                        }
                        cc.paste(session, newOrigin, noAir);
                    }
                });
    }

    @Override
    public void paste(final EditSession editSession, final Vector newOrigin,
            final boolean noAir, final boolean entities)
            throws MaxChangedBlocksException {
        boolean isAsync = checkAsync(WorldeditOperations.paste, editSession);
        if (!isAsync) {
            super.paste(editSession, newOrigin, noAir, entities);
            return;
        }

        final int jobId = getJobId();
        final EditSession session;
        final CuboidClipboardWrapper cc = new CuboidClipboardWrapper(m_player, m_clipboard, jobId);
        final JobEntry job;
        final WaitFor wait;

        if (editSession instanceof AsyncEditSession) {
            AsyncEditSession aSession = (AsyncEditSession) editSession;
            wait = aSession.getWait();
            session = new CancelabeEditSession(aSession, aSession.getMask(), jobId);
            job = new JobEntry(m_player, (CancelabeEditSession) session, jobId, "place");
        } else {
            session = editSession;
            wait = null;
            job = new JobEntry(m_player, jobId, "place");
        }
        m_blockPlacer.addJob(m_player, job);

        SchedulerUtils.runTaskAsynchronously(m_schedule, new ClipboardAsyncTask(cc, session, m_player, "paste",
                m_blockPlacer, job) {
                    @Override
                    public void task(CuboidClipboard cc)
                    throws MaxChangedBlocksException {
                        if (wait != null) {
                            wait.checkAndWait(null);
                        }
                        cc.paste(session, newOrigin, noAir, entities);
                    }
                });
    }

    @Override
    public void copy(EditSession editSession) {
        boolean isAsync = checkAsync(WorldeditOperations.copy, editSession);
        if (!isAsync) {
            super.copy(editSession);
            return;
        }

        final int jobId = getJobId();
        final EditSession session;
        final CuboidClipboardWrapper cc = new CuboidClipboardWrapper(m_player, m_clipboard, jobId);
        final JobEntry job;
        final WaitFor wait;

        if (editSession instanceof AsyncEditSession) {
            AsyncEditSession aSession = (AsyncEditSession) editSession;
            wait = aSession.getWait();
            session = new CancelabeEditSession(aSession, aSession.getMask(), jobId);
            job = new JobEntry(m_player, (CancelabeEditSession) session, jobId, "copy");
        } else {
            session = editSession;
            wait = null;
            job = new JobEntry(m_player, jobId, "copy");
        }
        m_blockPlacer.addJob(m_player, job);

        if (wait != null) {
            wait.setWait(cc, true);
        }
        SchedulerUtils.runTaskAsynchronously(m_schedule, new ClipboardAsyncTask(cc, session, m_player, "copy",
                m_blockPlacer, job) {
                    @Override
                    public void task(CuboidClipboard cc) throws MaxChangedBlocksException {
                        if (wait != null) {
                            wait.checkAndWait(cc);
                        }
                        cc.copy(session);
                        if (wait != null) {
                            wait.setWait(cc, false);
                        }
                    }
                });
    }

    @Override
    public void copy(EditSession editSession, final Region region) {
        boolean isAsync = checkAsync(WorldeditOperations.copy, editSession);
        if (!isAsync) {
            super.copy(editSession, region);
            return;
        }

        final int jobId = getJobId();
        final EditSession session;
        final CuboidClipboardWrapper cc = new CuboidClipboardWrapper(m_player, m_clipboard, jobId);
        final JobEntry job;
        final WaitFor wait;

        if (editSession instanceof AsyncEditSession) {
            AsyncEditSession aSession = (AsyncEditSession) editSession;
            wait = aSession.getWait();
            session = new CancelabeEditSession(aSession, aSession.getMask(), jobId);
            job = new JobEntry(m_player, (CancelabeEditSession) session, jobId, "copy");
        } else {
            session = editSession;
            wait = null;
            job = new JobEntry(m_player, jobId, "copy");
        }
        m_blockPlacer.addJob(m_player, job);

        if (wait != null) {
            wait.setWait(cc, true);
        }
        SchedulerUtils.runTaskAsynchronously(m_schedule, new ClipboardAsyncTask(cc, session, m_player, "copy",
                m_blockPlacer, job) {
                    @Override
                    public void task(CuboidClipboard cc) throws MaxChangedBlocksException {
                        if (wait != null) {
                            wait.checkAndWait(cc);
                        }
                        cc.copy(session, region);
                    }

                    @Override
                    protected void postProcess() {
                        if (wait != null) {
                            wait.setWait(cc, false);
                        }
                    }
                });
    }

    /**
     * This function checks if async mode is enabled for specific command
     *
     * @param operation
     */
    private boolean checkAsync(WorldeditOperations operation, EditSession session) {
        if (session != null && session instanceof AsyncEditSession) {
            return ((AsyncEditSession) session).checkAsync(operation);
        }
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
