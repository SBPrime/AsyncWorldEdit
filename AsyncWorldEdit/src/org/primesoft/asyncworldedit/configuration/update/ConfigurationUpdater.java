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

import java.util.HashMap;
import org.primesoft.asyncworldedit.platform.api.IConfiguration;

/**
 * The automatic configuration updater.
 * Updates configuration files in sequence
 * @author SBPrime
 */
public class ConfigurationUpdater {
    private final static HashMap<Integer, IConfigurationUpdater> s_configurationUpdaters;
    
    
    /**
     * The config file version
     */
    public static final int CONFIG_VERSION = 18;

    
    static {
        s_configurationUpdaters = new HashMap<Integer, IConfigurationUpdater>();
        s_configurationUpdaters.put(1, new ConfigUpdater_v1_v2());
        s_configurationUpdaters.put(2, new ConfigUpdater_v2_v3());
        s_configurationUpdaters.put(3, new ConfigUpdater_v3_v4());
        s_configurationUpdaters.put(4, new ConfigUpdater_v4_v5());
        s_configurationUpdaters.put(5, new ConfigUpdater_v5_v6());
        s_configurationUpdaters.put(6, new ConfigUpdater_v6_v7());
        s_configurationUpdaters.put(7, new ConfigUpdater_v7_v8());
        s_configurationUpdaters.put(8, new ConfigUpdater_v8_v9());
        s_configurationUpdaters.put(9, new ConfigUpdater_v9_v10());
        s_configurationUpdaters.put(10, new ConfigUpdater_v10_v11());
        s_configurationUpdaters.put(11, new ConfigUpdater_v11_v12());
        s_configurationUpdaters.put(12, new ConfigUpdater_v12_v13());
        s_configurationUpdaters.put(13, new ConfigUpdater_v13_v14());
        s_configurationUpdaters.put(14, new ConfigUpdater_v14_v15());
        s_configurationUpdaters.put(15, new ConfigUpdater_v15_v16());
        s_configurationUpdaters.put(16, new ConfigUpdater_v16_v17());
        s_configurationUpdaters.put(17, new ConfigUpdater_v17_v18());
    }
    
    public static boolean updateConfig(IConfiguration config, int version) {
        int oldVersion = version;
        int newVersion = version;
        while (s_configurationUpdaters.containsKey(oldVersion)) {
            IConfigurationUpdater updater = s_configurationUpdaters.get(oldVersion);
            
            newVersion = updater.updateConfig(config);
            
            if (newVersion < 0) {
                return false;
            }
            
            oldVersion = newVersion;
        }
                
        return newVersion != version;
    }
}
