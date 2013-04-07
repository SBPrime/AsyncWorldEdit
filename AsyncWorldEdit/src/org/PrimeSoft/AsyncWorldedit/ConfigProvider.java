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
package org.PrimeSoft.AsyncWorldedit;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

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
    
        
    private static boolean m_checkUpdate = false;
    private static boolean m_isConfigUpdate = false;
    private static long m_interval;
    private static int m_blocksCnt;
    private static int m_queueHardLimit;
    private static int m_queueSoftLimit;
    private static String m_configVersion;
    private static String m_logger;    

    public static String getLogger()
    {
        return m_logger;
    }
    
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
     * Is the configuration up to date
     * @return 
     */
    public static boolean isConfigUpdated() {
        return m_isConfigUpdate;
    }

    
    /**
     * Queue hard limit
     * @return 
     */
    public static int getQueueHardLimit()
    {
        return m_queueHardLimit;
    }
    
    /**
     * Queue soft limit
     * @return 
     */
    public static int getQueueSoftLimit()
    {
        return m_queueSoftLimit;
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
        parseRenderSection(mainSection);

        m_checkUpdate = mainSection.getBoolean("checkVersion", true);
        m_isConfigUpdate = mainSection.getInt("version", 0) == CONFIG_VERSION;
        m_logger = mainSection.getString("logger", "none").toLowerCase();
        return true;
    }
    
    /**
     * Parse render section
     * @param mainSection 
     */
    private static void parseRenderSection(ConfigurationSection mainSection) {
        ConfigurationSection renderSection = mainSection.getConfigurationSection("rendering");
        if (renderSection == null) {
            m_blocksCnt = 1000;
            m_interval = 15;
            m_queueHardLimit = 500000;
            m_queueSoftLimit = 250000;
        } else {
            m_blocksCnt = renderSection.getInt("blocks", 1000);
            m_interval = renderSection.getInt("interval", 15);
            m_queueSoftLimit = renderSection.getInt("queue-limit-soft", 250000);
            m_queueHardLimit = renderSection.getInt("queue-limit-hard", 500000);
        }
    }
}