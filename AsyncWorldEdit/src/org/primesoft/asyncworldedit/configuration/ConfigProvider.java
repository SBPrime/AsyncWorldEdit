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
package org.primesoft.asyncworldedit.configuration;

import org.primesoft.asyncworldedit.configuration.update.ConfigurationUpdater;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.platform.api.IConfiguration;
import org.primesoft.asyncworldedit.platform.api.IConfigurationSection;

/**
 * This class contains configuration
 *
 * @author SBPrime
 */
public class ConfigProvider {

    /**
     * Number of ticks in one second
     */
    public static final int TICKS_PER_SECOND = 20;

    private static boolean m_checkUpdate = false;

    private static boolean m_isConfigUpdate = false;


    private static String m_configVersion;

    private static EnumSet<WorldeditOperations> m_disabledOperations;

    private static boolean m_physicsFreez;


    private static boolean m_debugMode;

    private static File m_pluginFolder;

    private static String m_stringsFile;

    private static int m_forceFlushBlockCount;


    /**
     * The default permissions group
     */
    private static PermissionGroup m_defaultGroup;

    /**
     * Permissions group
     */
    private static PermissionGroup[] m_groups;

    
    /**
     * The memory configuration
     */
    private static ConfigMemory m_configMemory;
    
    /**
     * The renderer configuration
     */
    private static ConfigRenderer m_configRenderer;
        
    /**
     * The BlocksHub configuration
     */
    private static ConfigBlocksHub m_configBlocksHub;
    
    
    /**
     * The dispatcher configuration
     */
    private static ConfigDispatcher m_configDispatcher;
    
    
    /**
     * The permission configuration
     */
    private static ConfigPermission m_configPermission;
    
    
    /**
     * The undo configuration
     */
    private static ConfigUndo m_configUndo;
    
    /**
     * The undo folder
     */
    private static File m_undoFolder;
    
    /**
     * The DIrectChunk API configuration
     */
    private static ConfigDirectChunkApi m_configDCApi;
        
    /**
     * Get the undo configuration
     * @return 
     */
    public static ConfigUndo undo() {
        return m_configUndo;
    }
    
    /**
     * Get the permission configuration
     * @return 
     */
    public static ConfigPermission permission() {
        return m_configPermission;
    }
    
    /**
     * The dispatcher configuration
     * @return 
     */
    public static ConfigDispatcher dispatcher() {
        return m_configDispatcher;
    }
    
    /**
     * The blocks hub config
     * @return 
     */
    public static ConfigBlocksHub blocksHub() {
        return m_configBlocksHub;
    }

    /**
     * Get the DirectChunk configuration
     * @return 
     */
    public static ConfigDirectChunkApi directChunk() {
        return m_configDCApi;
    }
    
    /**
     * Get the renderer configuration
     * @return 
     */
    public static ConfigRenderer renderer() {
        return m_configRenderer;
    }
    
    /**
     * Get the memory configuration
     * @return 
     */
    public static ConfigMemory memory() {
        return m_configMemory;
    }
    

    public static int getForceFlushBlocks() {
        return m_forceFlushBlockCount;
    }

    public static PermissionGroup getDefaultGroup() {
        return m_defaultGroup;
    }

    public static PermissionGroup[] getGroups() {
        return m_groups;
    }

    public static boolean isDebugOn() {
        return m_debugMode;
    }
    

    /**
     * Plugin root folder
     *
     * @return
     */
    public static File getPluginFolder() {
        return m_pluginFolder;
    }
    
    
    /**
     * The disk undo folder
     * @return
     */
    public static File getUndoFolder() {
        return m_undoFolder;
    }
    

    /**
     * Get the config version
     *
     * @return Current config version
     */
    public static String getConfigVersion() {
        return m_configVersion;
    }

    /**
     * Is update checking enabled
     *
     * @return true if enabled
     */
    public static boolean getCheckUpdate() {
        return m_checkUpdate;
    }

    /**
     * Is the configuration up to date
     *
     * @return
     */
    public static boolean isConfigUpdated() {
        return m_isConfigUpdate;
    }


    public static boolean isPhysicsFreezEnabled() {
        return m_physicsFreez;
    }

    public static String getStringsFile() {
        return m_stringsFile;
    }

    /**
     * Load configuration
     *
     * @param aweCore parent plugin
     * @return true if config loaded
     */
    public static boolean load(IAsyncWorldEditCore aweCore) {
        if (aweCore == null) {
            return false;
        }

        IConfiguration config = aweCore.getPlatform().getConfig();
        m_pluginFolder = config.getDataFolder();
        m_undoFolder = new File(m_pluginFolder, "undo");
        
        if (!m_undoFolder.exists()) {
            m_undoFolder.mkdirs();
        }
        
        IConfigurationSection mainSection = config.getConfigurationSection("awe");
        if (mainSection == null) {
            return false;
        }

        int configVersion = mainSection.getInt("version", 0);
        if (configVersion < ConfigurationUpdater.CONFIG_VERSION) {
            if (ConfigurationUpdater.updateConfig(config, configVersion)) {
                SimpleDateFormat formater = new SimpleDateFormat("yyyyMMddHHmmss");
                File oldConfig = new File(m_pluginFolder, "config.yml");
                File newConfig = new File(m_pluginFolder, String.format("config.v%1$s", formater.format(new Date())));

                oldConfig.renameTo(newConfig);
                
                config.save();

                int newVersion = mainSection.getInt("version", 0);
                log(String.format("Configuration updated from %1$s to %2$s.", configVersion, newVersion));
                if (newVersion != ConfigurationUpdater.CONFIG_VERSION) {
                    log(String.format("Unable to update config to the required version (%1$s).", ConfigurationUpdater.CONFIG_VERSION));
                }

                configVersion = mainSection.getInt("version", 0);
            } else {
                log(String.format("Unable to update config to the required version (%1$s).", ConfigurationUpdater.CONFIG_VERSION));
            }
        }

        m_configVersion = mainSection.getString("version", "?");
        m_checkUpdate = mainSection.getBoolean("checkVersion", true);
        m_isConfigUpdate = configVersion == ConfigurationUpdater.CONFIG_VERSION;
        m_physicsFreez = mainSection.getBoolean("physicsFreez", true);
        m_stringsFile = mainSection.getString("strings", "");
        m_debugMode = mainSection.getBoolean("debug", false);
        m_forceFlushBlockCount = mainSection.getInt("forceFlushBlocks", 1000);

        parseGroupsSection(mainSection.getConfigurationSection("permissionGroups"));
        m_configMemory = new ConfigMemory(mainSection.getConfigurationSection("memory"));
        m_configRenderer = new ConfigRenderer(mainSection.getConfigurationSection("rendering"));
        m_configBlocksHub = new ConfigBlocksHub(mainSection.getConfigurationSection("blocksHub"));
        m_configDispatcher = new ConfigDispatcher(mainSection.getConfigurationSection("dispatcher"));
        m_configDCApi = new ConfigDirectChunkApi(mainSection.getConfigurationSection("directChunk"));
        m_configPermission = new ConfigPermission(mainSection.getConfigurationSection("permissions"));
        m_configUndo = new ConfigUndo(mainSection.getConfigurationSection("undo"));
        
        m_disabledOperations = parseOperationsSection(mainSection);

        return true;
    }

    /**
     * This function checks if async mode is allowed for specific worldedit
     * operation
     *
     * @param operation
     * @return
     */
    public static boolean isAsyncAllowed(WorldeditOperations operation) {
        return !m_disabledOperations.contains(operation);
    }

    /**
     * Parse enabled operations section
     *
     * @param mainSection
     * @return
     */
    private static EnumSet<WorldeditOperations> parseOperationsSection(
            IConfigurationSection mainSection) {
        EnumSet<WorldeditOperations> result = EnumSet.noneOf(WorldeditOperations.class);

        for (String string : mainSection.getStringList("disabledOperations")) {
            try {
                result.add(WorldeditOperations.valueOf(string));
            } catch (Exception e) {
                log(String.format("* unknown operation name %1$s", string));
            }
        }
        if (m_debugMode) {
            log("World edit operations:");
            for (WorldeditOperations op : WorldeditOperations.values()) {
                log("* " + op + "..." + (result.contains(op) ? "regular" : "async"));
            }
        }

        return result;
    }


    /**
     * Parse the groups section
     *
     * @param groupsSection
     */
    private static void parseGroupsSection(IConfigurationSection groupsSection) {
        if (groupsSection == null) {
            m_defaultGroup = PermissionGroup.getDefaultGroup();
            m_groups = new PermissionGroup[]{m_defaultGroup};

            return;
        }

        IConfigurationSection defaultGroup = null;
        List<IConfigurationSection> subSections = new ArrayList<IConfigurationSection>();
        String[] groupNames = groupsSection.getSubNodes().toArray(new String[0]);

        for (String sectionName : groupNames) {
            IConfigurationSection section = groupsSection.getConfigurationSection(sectionName);
            if (section != null) {
                subSections.add(section);
                if (defaultGroup == null && section.getBoolean("isDefault", false)) {
                    defaultGroup = section;
                }
            }
        }

        if (subSections.isEmpty()) {
            m_defaultGroup = PermissionGroup.getDefaultGroup();
            m_groups = new PermissionGroup[]{m_defaultGroup};

            return;
        }

        if (defaultGroup == null) {
            defaultGroup = subSections.get(0);
        }

        m_defaultGroup = new PermissionGroup(defaultGroup, true);
        List<PermissionGroup> groups = new ArrayList<PermissionGroup>(subSections.size());
        for (IConfigurationSection subSection : subSections) {
            groups.add(new PermissionGroup(subSection, m_defaultGroup, false));
        }

        m_groups = groups.toArray(new PermissionGroup[0]);
    }
}
