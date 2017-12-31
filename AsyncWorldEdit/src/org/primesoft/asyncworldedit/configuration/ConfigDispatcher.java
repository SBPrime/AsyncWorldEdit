/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.configuration;

import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.platform.api.IConfigurationSection;

/**
 *
 * @author SBPrime
 */
public class ConfigDispatcher {

    /**
     * Maximum number of dispatcher idle runs
     */
    private int m_maxIdle;

    /**
     * Maximum number of jobs performed in one run
     */
    private int m_maxJobs;

    /**
     * Maximum dime spend in one run
     */
    private int m_maxTime;

    public int getMaxIdle() {
        return m_maxIdle;
    }

    public int getMaxJobs() {
        return m_maxJobs;
    }

    public int getMaxTime() {
        return m_maxTime;
    }

    public ConfigDispatcher(IConfigurationSection dSection) {
        if (dSection == null) {
            m_maxIdle = 200;
            m_maxJobs = 2000;
            m_maxTime = 20;
        } else {
            m_maxIdle = dSection.getInt("max-idle-runs", 200);
            m_maxJobs = dSection.getInt("max-jobs", 2000);
            m_maxTime = dSection.getInt("max-time", 20);
        }

        if (m_maxTime < 1) {
            m_maxTime = 10;
            log("Warning: Dispatcher time is set to lower then 1ms, changing to 10ms.");
        }
        if (m_maxJobs < 1) {
            m_maxJobs = 100;
            log("Warning: Dispatcher max jobs is lower then 1, changing to 100");
        }

        if (m_maxIdle < 1) {
            m_maxIdle = 10;
            log("Warning: Dispatcher max idle is lower then 1, changing to 10");
        }
    }

}
