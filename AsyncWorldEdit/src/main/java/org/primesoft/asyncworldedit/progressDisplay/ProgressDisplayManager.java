/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.progressDisplay;

import java.util.HashMap;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.MessageSystem;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplayManager;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

/**
 * This class controls all progress display integrator
 *
 * @author SBPrime
 */
public class ProgressDisplayManager implements IProgressDisplay, IProgressDisplayManager {

    /**
     * List of all registered backends
     */
    private final HashMap<Class<?>, IProgressDisplay> m_registeredProgressBackends = new HashMap<Class<?>, IProgressDisplay>();

    @Override
    public void disableMessage(IPlayerEntry playerEntry) {
        if (playerEntry == null || !playerEntry.isPlayer()
                || playerEntry.isDisposed()
                || playerEntry.getUUID() == null || playerEntry.getName() == null
                || playerEntry.getName().isEmpty()) {
            return;
        }

        if (!playerEntry.getMessaging(MessageSystem.BAR)) {
            return;
        }

        synchronized (m_registeredProgressBackends) {
            for (IProgressDisplay pd : m_registeredProgressBackends.values()) {
                try {
                    pd.disableMessage(playerEntry);
                } catch (Error e) {
                    ExceptionHelper.printException(e, String.format("Progress display %1$s thrown an error: ", pd.getName()));
                }
            }
        }
    }

    @Override
    public void setMessage(IPlayerEntry playerEntry, int jobsCount,
            int queuedBlocks, int maxQueuedBlocks, double timeLeft, double placingSpeed, double percentage) {
        if (playerEntry == null || !playerEntry.isPlayer()
                || playerEntry.isDisposed()
                || playerEntry.getUUID() == null || playerEntry.getName() == null
                || playerEntry.getName().isEmpty()) {
            return;
        }

        IPermissionGroup pg = playerEntry.getPermissionGroup();
        
        if (!playerEntry.getMessaging(MessageSystem.BAR)) {            
            return;
        }

        if (maxQueuedBlocks < pg.getBarApiProgresMinBlocks()) {
            disableMessage(playerEntry);
            return;
        }
        
        synchronized (m_registeredProgressBackends) {
            for (IProgressDisplay pd : m_registeredProgressBackends.values()) {
                try {
                    pd.setMessage(playerEntry, jobsCount, queuedBlocks, maxQueuedBlocks, timeLeft, placingSpeed, percentage);
                } catch (Error e) {
                    ExceptionHelper.printException(e, String.format("Progress display %1$s thrown an error:", pd.getName()));
                }
            }
        }
    }

    @Override
    public String getName() {
        return "The Progress display manager";
    }

    @Override
    public boolean registerProgressDisplay(IProgressDisplay backend) {
        if (backend == null) {
            return false;
        }
        synchronized (m_registeredProgressBackends) {
            Class<?> cls = backend.getClass();
            if (m_registeredProgressBackends.containsKey(cls)) {
                log(String.format("Duplicate display backend %1$s registration.", backend.getName()));
                return false;
            }

            m_registeredProgressBackends.put(cls, backend);

            log(String.format("Display backend %1$s registered.", backend.getName()));
        }

        return true;
    }

    @Override
    public boolean unregisterProgressDisplay(IProgressDisplay backend) {
        if (backend == null) {
            return false;
        }
        synchronized (m_registeredProgressBackends) {
            Class<?> cls = backend.getClass();
            if (!m_registeredProgressBackends.containsKey(cls)) {
                return false;
            }

            m_registeredProgressBackends.remove(cls);

            log(String.format("Display backend %1$s removed.", backend.getName()));
        }

        return true;
    }

    @Override
    public String formatMessage(int jobsCount, double speed, double timeLeft) {
        return MessageType.CMD_JOBS_PROGRESS_BAR.format(jobsCount, speed, timeLeft);
    }
}
