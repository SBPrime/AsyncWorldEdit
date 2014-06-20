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
import java.util.UUID;
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
     * Number of ticks in one second
     */
    public static final int TICKS_PER_SECOND = 20;

    /**
     * Default user name when no user is available
     */
    public static final UUID DEFAULT_USER = UUID.randomUUID();

    /**
     * The config file version
     */
    private static final int CONFIG_VERSION = 1;

    private static boolean m_defaultMode = true;

    private static boolean m_checkUpdate = false;

    private static boolean m_allowMetrics = false;

    private static boolean m_isConfigUpdate = false;

    private static long m_interval;

    private static int m_timeCount;
    
    private static int m_vipTimeCount;
    
    private static int m_blocksCnt;

    private static int m_vipBlocksCnt;

    private static int m_queueHardLimit;

    private static int m_queueSoftLimit;

    private static int m_queueMaxSize;

    private static int m_queueTalkInterval;

    private static String m_configVersion;

    private static HashSet<WorldeditOperations> m_allowedOperations;

    private static boolean m_physicsFreez;

    private static boolean m_checkAccess;

    private static boolean m_logBlocks;
    
    private static boolean m_plotMeFixEnabled;
    
    private static boolean m_cleanOnLogout;
    
    private static boolean m_isTalkative;

    private static boolean m_useBarAPI;

    /**
     * Whether or not setting values to the BarAPI bar will be shown
     * 
     * @return {@code true} if showing messages, false otherwise
     */
    public static boolean useBarAPI() {
        return m_useBarAPI;
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
     * Is Metrics enabled
     *
     * @return true if enabled
     */
    public static boolean getAllowMetrics() {
    	return m_allowMetrics;
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
     * Maximum time in miliseconds spend on rendering blocks
     * @return 
     */
    public static int getBlockPlacingTime(){
        return m_timeCount;
    }
    
    
    /**
     * Maximum time in miliseconds spend on rendering blocks
     * @return 
     */
    public static int getVipBlockPlacingTime(){
        return m_vipTimeCount;
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
     * Is block login enabled
     *
     * @return
     */
    public static boolean getLogBlocks() {
        return m_logBlocks;
    }

    /**
     * Is block perms checking enabled
     *
     * @return
     */
    public static boolean getCheckAccess() {
        return m_checkAccess;
    }

    /**
     * Get the number of blocks placed for VIP players
     *
     * @return number of blocks
     */
    public static int getVipBlockCount() {
        return m_vipBlocksCnt;
    }

    public static int getQueueTalkInterval() {
        return m_queueTalkInterval;
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
     * Is AWE talkative
     * @return 
     */
    public static boolean isTalkative() {
        return m_isTalkative;
    }
    
    
    /**
     * Cancel player operations on logout
     * @return 
     */
    public static boolean cleanOnLogoutEnabled() {
        return m_cleanOnLogout;
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

    public static boolean isPhysicsFreezEnabled() {
        return m_physicsFreez;
    }

    /**
     * Is PlotMe Fix enabled?
     * 
     * @return 
     */
    public static boolean isPlotMeFixEnabled() {
        return m_plotMeFixEnabled;
    }
    
    /**
     * Load configuration
     *
     * @param plugin parent plugin
     * @return true if config loaded
     */
    public static boolean load(AsyncWorldEditMain plugin) {
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
        m_isTalkative = mainSection.getBoolean("talkative", true);
        m_defaultMode = mainSection.getBoolean("defaultOn", true);
        m_physicsFreez = mainSection.getBoolean("physicsFreez", true);
        m_plotMeFixEnabled = mainSection.getBoolean("plotMeFixEnabled", true);
        m_cleanOnLogout = mainSection.getBoolean("cleanOnLogout", true);
        m_useBarAPI = mainSection.getBoolean("use-barapi");

        parseRenderSection(mainSection);
        parseBlocksHubSection(mainSection.getConfigurationSection("blocksHub"));

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
            m_timeCount = 20;
            m_vipTimeCount = 10;
            m_blocksCnt = 1000;
            m_vipBlocksCnt = 1000;
            m_interval = 15;
            m_queueTalkInterval = 10;
            m_queueHardLimit = 500000;
            m_queueSoftLimit = 250000;
            m_queueMaxSize = 10000000;
        } else {
            m_blocksCnt = renderSection.getInt("blocks", 1000);
            m_vipBlocksCnt = renderSection.getInt("blocks-vip", 1000);
            m_timeCount = renderSection.getInt("time", 20);
            m_vipTimeCount = renderSection.getInt("time-vip", 10);
            m_interval = renderSection.getInt("interval", 15);
            m_queueTalkInterval = renderSection.getInt("talk-interval", 10);
            m_queueSoftLimit = renderSection.getInt("queue-limit-soft", 250000);
            m_queueHardLimit = renderSection.getInt("queue-limit-hard", 500000);
            m_queueMaxSize = renderSection.getInt("queue-max-size", 10000000);

            if (m_queueMaxSize <= 0) {
                AsyncWorldEditMain.log("Warinig: Block queue is disabled!");
            }
            
            if (m_timeCount < 0 && m_blocksCnt < 0)
            {
                AsyncWorldEditMain.log("Warinig: Time and blocks are set to unlimited!");
                m_timeCount = 20;
                m_blocksCnt = -1;
            }
            if (m_vipTimeCount < 0 && m_vipBlocksCnt < 0)
            {
                AsyncWorldEditMain.log("Warinig: Vip time and blocks are set to unlimited!");
                m_vipTimeCount = 20;
                m_vipBlocksCnt = -1;
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
            }
            catch (Exception e) {
                AsyncWorldEditMain.log("* unknown operation name " + string);
            }
        }
        if (result.isEmpty()) {
            //Add all entries
            AsyncWorldEditMain.log("Warning: No operations defined in config file. Enabling all.");
            for (WorldeditOperations op : WorldeditOperations.values()) {
                result.add(op);
            }
        }
        //PluginMain.Log("World edit operations:");
        //for (WorldeditOperations op : WorldeditOperations.values()) {
        //    AsyncWorldEditMain.Log("* " + op + "..." + (result.contains(op) ? "async" : "regular"));
        //}


        return result;
    }

    /**
     * Initialize blocks hub configuration
     * @param bhSection 
     */
    private static void parseBlocksHubSection(ConfigurationSection bhSection) {
        if (bhSection == null)
        {
            m_logBlocks = true;
            m_checkAccess = false;
        } else {
            m_logBlocks = bhSection.getBoolean("logBlocks", true);
            m_checkAccess = bhSection.getBoolean("checkAccess", false);
        }
    }
}