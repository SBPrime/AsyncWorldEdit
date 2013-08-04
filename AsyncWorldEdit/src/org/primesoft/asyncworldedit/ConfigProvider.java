/*
 * The MIT License
 *
 * Copyright 2013 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primesoft.asyncworldedit;

import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.primesoft.asyncworldedit.worldedit.WorldeditOperations;

/**
 * This class contains configuration
 *
 * @author SBPrime
 */
public class ConfigProvider {

    /**
     * Default user name when no user is available
     */
    public static final String DEFAULT_USER = "#worldedit";
    /**
     * The config file version
     */
    private static final int CONFIG_VERSION = 1;
    private static boolean m_defaultMode = true;
    private static boolean m_checkUpdate = false;
    private static boolean m_isConfigUpdate = false;
    private static boolean m_isWorldGuardEnabled = false;
    private static long m_interval;
    private static int m_blocksCnt;
    private static int m_vipBlocksCnt;
    private static int m_queueHardLimit;
    private static int m_queueSoftLimit;
    private static int m_queueMaxSize;
    private static String m_configVersion;
    private static String m_logger;
    private static HashSet<WorldeditOperations> m_allowedOperations;
    private static HashSet<String> m_enabledWorlds;

    public static String getLogger() {
        return m_logger;
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
     * Block drawing interval
     *
     * @return the interval
     */
    public static long getInterval() {
        return m_interval;
    }

    /**
     * Get the number of blocks placed
     *
     * @return number of blocks
     */
    public static int getBlockCount() {
        return m_blocksCnt;
    }

    /**
     * Get the number of blocks placed for VIP players
     *
     * @return number of blocks
     */
    public static int getVipBlockCount() {
        return m_vipBlocksCnt;
    }

    /**
     * Is the configuration up to date
     *
     * @return
     */
    public static boolean isConfigUpdated() {
        return m_isConfigUpdate;
    }

    /**
     * Is the world guard integration enabled
     * @return 
     */
    public static boolean isWorldGuardEnabled() {
        return m_isWorldGuardEnabled;
    }

    /**
     * Is the world being logged
     *
     * @return
     */
    public static boolean isLogging(String world) {
        return m_enabledWorlds.contains(world.toLowerCase());
    }

    /**
     * Queue hard limit
     *
     * @return
     */
    public static int getQueueHardLimit() {
        return m_queueHardLimit;
    }

    /**
     * Queue soft limit
     *
     * @return
     */
    public static int getQueueSoftLimit() {
        return m_queueSoftLimit;
    }

    /**
     * The default mode
     *
     * @return
     */
    public static boolean getDefaultMode() {
        return m_defaultMode;
    }

    /**
     * Get maximum size of the queue
     *
     * @return
     */
    public static int getQueueMaxSize() {
        return m_queueMaxSize;
    }

    /**
     * Load configuration
     *
     * @param plugin parent plugin
     * @return true if config loaded
     */
    public static boolean load(PluginMain plugin) {
        if (plugin == null) {
            return false;
        }

        plugin.saveDefaultConfig();

        Configuration config = plugin.getConfig();
        ConfigurationSection mainSection = config.getConfigurationSection("awe");
        if (mainSection == null) {
            return false;
        }

        m_configVersion = mainSection.getString("version", "?");
        m_checkUpdate = mainSection.getBoolean("checkVersion", true);
        m_isConfigUpdate = mainSection.getInt("version", 0) == CONFIG_VERSION;
        m_defaultMode = mainSection.getBoolean("defaultOn", true);
        m_isWorldGuardEnabled = mainSection.getBoolean("worldGuard", false);

        parseRenderSection(mainSection);
        parseLoggerSection(mainSection);

        m_allowedOperations = parseOperationsSection(mainSection);

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
        return m_allowedOperations.contains(operation);
    }

    /**
     * Parse render section
     *
     * @param mainSection
     */
    private static void parseRenderSection(ConfigurationSection mainSection) {
        ConfigurationSection renderSection = mainSection.getConfigurationSection("rendering");
        if (renderSection == null) {
            m_blocksCnt = 1000;
            m_vipBlocksCnt = 1000;
            m_interval = 15;
            m_queueHardLimit = 500000;
            m_queueSoftLimit = 250000;
            m_queueMaxSize = 10000000;
        } else {
            m_blocksCnt = renderSection.getInt("blocks", 1000);
            m_vipBlocksCnt = renderSection.getInt("blocks-vip", 1000);
            m_interval = renderSection.getInt("interval", 15);
            m_queueSoftLimit = renderSection.getInt("queue-limit-soft", 250000);
            m_queueHardLimit = renderSection.getInt("queue-limit-hard", 500000);
            m_queueMaxSize = renderSection.getInt("queue-max-size", 10000000);

            if (m_queueMaxSize <= 0) {
                PluginMain.Log("Warinig: Block queue is disabled!");
            }
        }
    }

    /**
     * Parse enabled operations section
     *
     * @param mainSection
     * @return
     */
    private static HashSet<WorldeditOperations> parseOperationsSection(
            ConfigurationSection mainSection) {
        HashSet<WorldeditOperations> result = new HashSet<WorldeditOperations>();

        for (String string : mainSection.getStringList("enabledOperations")) {
            try {
                result.add(WorldeditOperations.valueOf(string));
            } catch (Exception e) {
                PluginMain.Log("* unknown operation name " + string);
            }
        }
        if (result.isEmpty()) {
            //Add all entries
            PluginMain.Log("Warning: No operations defined in config file. Enabling all.");
            for (WorldeditOperations op : WorldeditOperations.values()) {
                result.add(op);
            }
        }
        PluginMain.Log("World edit operations:");
        for (WorldeditOperations op : WorldeditOperations.values()) {
            PluginMain.Log("* " + op + "..." + (result.contains(op) ? "async" : "regular"));
        }


        return result;
    }

    /**
     * Parse blocks logger section
     *
     * @param mainSection config section
     */
    private static void parseLoggerSection(ConfigurationSection mainSection) {
        ConfigurationSection loggerSection = mainSection.getConfigurationSection("logger");

        if (loggerSection == null) {
            m_logger = "none";
            m_enabledWorlds = new HashSet<String>();
        } else {
            m_logger = loggerSection.getString("type", "none").toLowerCase();
            m_enabledWorlds = new HashSet<String>();

            for (String world : loggerSection.getStringList("worlds")) {
                world = world.toLowerCase();
                if (!m_enabledWorlds.contains(world)) {
                    m_enabledWorlds.add(world);
                }
            }

            PluginMain.Log("WorldEdit logging:");
        }

        for (World world : Bukkit.getWorlds()) {
            PluginMain.Log("* " + world.getName() + "..." + (m_enabledWorlds.contains(world.getName().toLowerCase()) ? "enabled" : "disabled"));
        }
    }
}