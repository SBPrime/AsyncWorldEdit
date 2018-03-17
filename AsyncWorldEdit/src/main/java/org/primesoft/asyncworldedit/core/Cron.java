/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2018, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.core;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.inner.ICron;
import org.primesoft.asyncworldedit.api.inner.IInnerSerializerManager;
import org.primesoft.asyncworldedit.api.inner.IWorldeditIntegratorInner;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.changesetSerializer.StreamProvider;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.configuration.ConfigUndo;
import org.primesoft.asyncworldedit.platform.api.IScheduler;
import org.primesoft.asyncworldedit.platform.api.ITask;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
public final class Cron implements ICron {

    private final IPlayerManager m_playerManager;
    private final AsyncWorldEditCore m_parrent;
    private boolean m_undoCleanupRunning = false;

    private final static class SessionEntry {

        public final IPlayerEntry Player;
        public long RemoveTime;

        public SessionEntry(IPlayerEntry player, long removeTime) {
            Player = player;
            RemoveTime = removeTime;
        }
    }

    /**
     * LIst of all delayed session removal tasks
     */
    private final ConcurrentMap<UUID, SessionEntry> m_tasksSessions = new ConcurrentHashMap<>();

    /**
     * Bukkit scheduler
     */
    private final IScheduler m_scheduler;

    /**
     * Current scheduler task
     */
    private ITask m_task;

    /**
     * TImestamp of last undo scann
     */
    private long m_lastUndoScann;

    public Cron(AsyncWorldEditCore aweCore) {
        m_scheduler = aweCore.getPlatform().getScheduler();
        m_playerManager = aweCore.getPlayerManager();
        m_parrent = aweCore;
        m_lastUndoScann = System.currentTimeMillis();

        loadConfig();
        
        new Thread(this::runUndoCleanup).start();
    }

    public void loadConfig() {
        final Cron cron = this;

        long interval = ConfigProvider.TICKS_PER_SECOND * 60;

        if (m_task != null) {
            m_task.cancel();
            m_task = null;
        }

        m_task = m_scheduler.runTaskTimer(this::onTime, interval, interval);
    }

    public void stop() {
        if (m_task != null) {
            m_task.cancel();
            m_task = null;
        }
    }

    @Override
    public void scheduleSessionForRemoval(IPlayerEntry entry, int delayTime) {
        final long time = System.currentTimeMillis() + delayTime;

        m_tasksSessions.compute(entry.getUUID(), (key, current) -> {
            if (current == null) {
                return new SessionEntry(entry, time);
            }

            current.RemoveTime = time;
            return current;
        });
    }

    private void onTime() {
        IWorldeditIntegratorInner integrator = m_parrent.getWorldEditIntegrator();
        final long now = System.currentTimeMillis();

        if (integrator != null) {
            m_tasksSessions.values().stream()
                    .filter(i -> i.RemoveTime <= now)
                    .map(i -> i.Player)
                    .filter(i -> {
                        UUID id = i.getUUID();
                        IPlayerEntry pe = m_playerManager.getOnlinePlayer(id);
                        m_tasksSessions.remove(id);
                        return pe == null;
                    })
                    .forEach(i -> integrator.removeSession(i));
        }

        ConfigUndo undoConfig = ConfigProvider.undo();
        if (undoConfig != null && ((now - m_lastUndoScann) / 60000) > undoConfig.undoFileCleanupInterval()) {
            m_lastUndoScann = now;

            if (!m_undoCleanupRunning) {
                new Thread(this::runUndoCleanup).start();
            }
        }
    }

    private void runUndoCleanup() {
        final ConfigUndo undoConfig = ConfigProvider.undo();

        if (undoConfig == null) {
            return;
        }

        final long keepUndoFor = undoConfig.keepUndoFileFor();
        if (keepUndoFor < 0) {
            return;
        }
        log("Undo cleanup started...");

        final long time = System.currentTimeMillis() - keepUndoFor * 60000;

        m_undoCleanupRunning = true;

        try {
            IInnerSerializerManager sm = m_parrent.getInnerChangesetSerializer();
            StreamProvider streamProvider = StreamProvider.getInstance();
            if (sm == null || streamProvider == null) {
                return;
            }

            sm.getUndoFiles()
                    .filter(i -> i.exists())
                    .filter(i -> {
                        Long timestamp = sm.getTimestamp(i);
                        return timestamp != null && timestamp <= time;
                    })
                    .filter(i -> !streamProvider.isInUse(i))
                    .forEach(i -> {
                try {
                    if (!i.delete()) {
                        log(String.format("\t * %1$s...error", i));
                    } else {
                        log(String.format("\t * %1$s...ok", i));
                    }
                } catch (Exception ex) {
                    log(String.format("\t * %1$s...error", i));
                }
            });
        } catch (IOException ex) {
            ExceptionHelper.printException(ex, "Unable to iterate undo files.");
        } finally {
            m_undoCleanupRunning = false;
            log("...undo cleanup done.");
        }
    }
}
