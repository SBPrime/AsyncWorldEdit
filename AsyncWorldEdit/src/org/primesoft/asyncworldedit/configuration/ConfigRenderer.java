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
public class ConfigRenderer {

    private final long m_interval;

    private final int m_queueMaxSizeHard;

    private int m_queueMaxSizeSoft;

    private final int m_queueTalkInterval;

    private final int m_cooldown;

    /**
     * Get maximum size of the queue
     *
     * @return
     */
    public  int getQueueMaxSizeHard() {
        return m_queueMaxSizeHard;
    }

    public  int getQueueMaxSizeSoft() {
        return m_queueMaxSizeSoft;
    }

    /**
     * Block drawing interval
     *
     * @return
     */
    public  long getInterval() {
        return m_interval;
    }

    public  int getQueueTalkInterval() {
        return m_queueTalkInterval;
    }

    public  int getQueueTalkCooldown() {
        return m_cooldown;
    }

    public ConfigRenderer(IConfigurationSection renderSection) {
        if (renderSection == null) {
            m_interval = 15;
            m_queueTalkInterval = 10;
            m_cooldown = 5 * 1000;
            m_queueMaxSizeHard = 10000000;
            m_queueMaxSizeSoft = 5000000;
        } else {
            m_interval = renderSection.getInt("interval", 15);
            m_queueTalkInterval = renderSection.getInt("talk-interval", 10);
            m_cooldown = renderSection.getInt("talk-cooldown", 5) * 1000;
            m_queueMaxSizeHard = renderSection.getInt("queue-max-size-hard", 10000000);
            m_queueMaxSizeSoft = renderSection.getInt("queue-max-size-soft", 5000000);

            if (m_queueMaxSizeHard <= 0) {
                log("Warinig: Block queue is disabled!");
            }
        }

        if (m_queueMaxSizeSoft > m_queueMaxSizeHard) {
            m_queueMaxSizeSoft = m_queueMaxSizeHard;

            log("Warinig: QueueMaxSize soft > hard!");
        }
    }
}
