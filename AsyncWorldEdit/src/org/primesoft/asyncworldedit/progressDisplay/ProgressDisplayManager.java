/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.progressDisplay;

import java.util.HashMap;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;
import static org.primesoft.asyncworldedit.AsyncWorldEditMain.log;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplayManager;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
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
    public void disableMessage(PlayerEntry player) {
        if (player == null) {
            return;
        }

        if (!player.getPermissionGroup().isBarApiProgressEnabled()) {
            return;
        }

        synchronized (m_registeredProgressBackends) {
            for (IProgressDisplay pd : m_registeredProgressBackends.values()) {
                try {
                    pd.disableMessage(player);
                } catch (Error e) {
                    ExceptionHelper.printException(e, "Progress display " + pd.getName() + " thrown an error:");
                }
            }
        }
    }

    @Override
    public void setMessage(PlayerEntry player, int jobsCount, 
            int queuedBlocks, int maxQueuedBlocks, double timeLeft, double placingSpeed, double percentage) {
        if (player == null) {
            return;
        }

        if (!player.getPermissionGroup().isBarApiProgressEnabled()) {
            return;
        }

        synchronized (m_registeredProgressBackends) {
            for (IProgressDisplay pd : m_registeredProgressBackends.values()) {
                try {
                    pd.setMessage(player, jobsCount, queuedBlocks, maxQueuedBlocks, timeLeft, placingSpeed, percentage);
                } catch (Error e) {
                    ExceptionHelper.printException(e, "Progress display " + pd.getName() + " thrown an error:");
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
                log("Duplicate display backend " + backend.getName() + " registrtion.");
                return false;
            }

            m_registeredProgressBackends.put(cls, backend);

            log("Display backend " + backend.getName() + " registered.");
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

            log("Display backend " + backend.getName() + " removed.");
        }

        return true;
    }

}
