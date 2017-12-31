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

import java.util.EnumSet;
import org.primesoft.asyncworldedit.platform.api.IConfigurationSection;

/**
 *
 * @author SBPrime
 */
public class ConfigBlocksHub {
    private final EnumSet<AccessType> m_accessOverride;
    
    private final BHLevel m_checkAccess;

    private final BHLevel m_logBlocks;
    
    

    /**
     * Get block login integration level
     *
     * @return
     */
    public  BHLevel getLogBlocks() {
        return m_logBlocks;
    }

    /**
     * Get block perms checking integration level
     *
     * @return
     */
    public  BHLevel getCheckAccess() {
        return m_checkAccess;
    }
    
    
    /**
     * World access override for "special" player entries
     *
     * @param accessType
     * @return
     */
    public  boolean isAccessAllowed(AccessType accessType) {
        return m_accessOverride.contains(accessType);
    }
    
    public ConfigBlocksHub(IConfigurationSection bhSection) {
        m_accessOverride = EnumSet.noneOf(AccessType.class);
        if (bhSection == null) {
            m_logBlocks = BHLevel.Regular;
            m_checkAccess = BHLevel.Disabled;
            return;
        }

        IConfigurationSection logSection = bhSection.getConfigurationSection("log");
        if (logSection == null) {
            m_logBlocks = BHLevel.Regular;
        } else {
            boolean isEnabled = logSection.getBoolean("isEnabled", true);
            boolean isDcEnabled = logSection.getBoolean("isDcEnabled", false);

            if (!isEnabled) {
                m_logBlocks = BHLevel.Disabled;
            } else if (!isDcEnabled) {
                m_logBlocks = BHLevel.Regular;
            } else {
                m_logBlocks = BHLevel.All;
            }
        }

        IConfigurationSection accessSection = bhSection.getConfigurationSection("access");
        if (accessSection == null) {
            m_checkAccess = BHLevel.Disabled;
        } else {
            boolean isEnabled = accessSection.getBoolean("isEnabled", true);
            boolean isDcEnabled = accessSection.getBoolean("isDcEnabled", false);

            if (!isEnabled) {
                m_checkAccess = BHLevel.Disabled;
            } else if (!isDcEnabled) {
                m_checkAccess = BHLevel.Regular;
            } else {
                m_checkAccess = BHLevel.All;
            }

            if (accessSection.getBoolean("allowNull", false)) {
                m_accessOverride.add(AccessType.Null);
            }
            if (accessSection.getBoolean("allowUnknown", true)) {
                m_accessOverride.add(AccessType.Unknown);
            }
            if (accessSection.getBoolean("allowConsole", true)) {
                m_accessOverride.add(AccessType.Console);
            }
            if (accessSection.getBoolean("allowOffline", false)) {
                m_accessOverride.add(AccessType.Offline);
            }
        }
    }
    
}
