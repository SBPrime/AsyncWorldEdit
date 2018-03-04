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
package org.primesoft.asyncworldedit.configuration.update;

import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.platform.api.IConfiguration;
import org.primesoft.asyncworldedit.platform.api.IConfigurationSection;

/**
 *
 * @author SBPrime
 */
class ConfigUpdater_v1_v2 extends BaseConfigurationUpdater {

    private int m_blocksCnt;
    private int m_vipBlocksCnt;
    private int m_timeCount;
    private int m_vipTimeCount;
    private int m_queueSoftLimit;
    private int m_queueHardLimit;
    private boolean m_isTalkative;
    private boolean m_defaultMode;
    private boolean m_cleanOnLogout;
    private boolean m_useBarAPI;

    /**
     * Parse render section
     *
     * @param mainSection
     */
    private void parseRenderSection(IConfigurationSection mainSection) {
        IConfigurationSection renderSection = mainSection.getConfigurationSection("rendering");

        m_blocksCnt = getAndRemoveInt(renderSection, "blocks", 1000);
        m_vipBlocksCnt = getAndRemoveInt(renderSection, "blocks-vip", 1000);
        m_timeCount = getAndRemoveInt(renderSection, "time", 20);
        m_vipTimeCount = getAndRemoveInt(renderSection, "time-vip", 10);
        m_queueSoftLimit = getAndRemoveInt(renderSection, "queue-limit-soft", 250000);
        m_queueHardLimit = getAndRemoveInt(renderSection, "queue-limit-hard", 500000);

        if (m_timeCount < 0 && m_blocksCnt < 0) {
            m_timeCount = 20;
            m_blocksCnt = -1;
        }
        if (m_vipTimeCount < 0 && m_vipBlocksCnt < 0) {
            m_vipTimeCount = 10;
            m_vipBlocksCnt = -1;
        }
    }

    @Override
    public int updateConfig(IConfiguration config) {
        log("Updating configuration v1 --> v2");

        IConfigurationSection mainSection = config.getConfigurationSection("awe");
        if (mainSection == null) {
            return -1;
        }

        m_isTalkative = getAndRemoveBoolean(mainSection, "talkative", true);
        m_defaultMode = getAndRemoveBoolean(mainSection, "defaultOn", true);
        m_cleanOnLogout = getAndRemoveBoolean(mainSection, "cleanOnLogout", true);
        m_useBarAPI = getAndRemoveBoolean(mainSection, "use-barapi", false);
        
        parseRenderSection(mainSection);
        
        setIfNone(mainSection, "strings", "english.yml");        
        mainSection.set("version", 2);
        mainSection.set("allowMetrics", null);
        
        IConfigurationSection permissionSections = getOrCreate(mainSection, "permissionGroups");
        IConfigurationSection gDefault = getOrCreate(permissionSections, "default");        
        
        
        setIfNone(gDefault, "isDefault", true);
        setIfNone(gDefault, "maxJobs", 1);
        setIfNone(gDefault, "cleanOnLogout", m_cleanOnLogout);
        setIfNone(gDefault, "defaultMode", m_defaultMode);
        
        IConfigurationSection gdRenderer = getOrCreate(gDefault, "renderer");
        setIfNone(gdRenderer, "blocks", m_blocksCnt);
        setIfNone(gdRenderer, "time", m_timeCount);
        
        IConfigurationSection gdQueue = getOrCreate(gDefault, "queue");
        setIfNone(gdQueue, "limit-hard", m_queueHardLimit);
        setIfNone(gdQueue, "limit-soft", m_queueSoftLimit);
        
        IConfigurationSection gdMessages = getOrCreate(gDefault, "messages");
        setIfNone(gdMessages, "progress-bar", m_useBarAPI);
        setIfNone(gdMessages, "progress-chat", true);
        setIfNone(gdMessages, "talkative", m_isTalkative);
        
        IConfigurationSection gVip  = getOrCreate(permissionSections, "vip");
        setIfNone(gVip, "isDefault", false);
        setIfNone(gVip, "maxJobs", -1);
        setIfNone(gVip, "cleanOnLogout", false);
        
        IConfigurationSection gvRenderer = getOrCreate(gDefault, "renderer");
        setIfNone(gvRenderer, "blocks", m_vipBlocksCnt);
        setIfNone(gvRenderer, "time", m_vipTimeCount);
        
        return 2;
    }
}
