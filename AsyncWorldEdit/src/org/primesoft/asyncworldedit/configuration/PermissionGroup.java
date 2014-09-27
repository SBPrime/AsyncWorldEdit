/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
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
package org.primesoft.asyncworldedit.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.permissions.PermissionManager;

/**
 * The player permission group
 *
 * @author SBPrime
 */
public class PermissionGroup {

    /**
     * The default values permission group
     */
    private final static PermissionGroup s_defaultValue = new PermissionGroup();

    /**
     * Get the default group
     *
     * @return
     */
    public static PermissionGroup getDefaultGroup() {
        return s_defaultValue;
    }

    /**
     * Is the group default
     */
    private final boolean m_isDefault;

    /**
     * Maximum number of concurrent jobs
     */
    private final int m_maxJobs;

    /**
     * Kill all player jobs on logout
     */
    private final boolean m_cleanOnLogout;

    /**
     * The AWE mode when player logins
     */
    private final boolean m_isOnByDefault;

    /**
     * Number of blocks placed in each run
     */
    private final int m_rendererBlocks;

    /**
     * Maximum number of miliseconds spend on placing blocks
     */
    private final int m_rendererTime;

    /**
     * maximum size of the player block queue
     */
    private final int m_queueHardLimit;

    /**
     * number of blocks on the player queue when to stop placing blocks
     */
    private final int m_queueSoftLimit;

    /**
     * is async world edit talkative
     */
    private final boolean m_isTalkative;

    /**
     * Use the bar api to display progress
     */
    private final boolean m_useBarApi;

    /**
     * Use chat to display progress
     */
    private final boolean m_useChat;

    /**
     * The group name
     */
    private final String m_name;

    /**
     * Is the group default
     *
     * @return
     */
    public boolean isDefault() {
        return m_isDefault;
    }

    /**
     * Maximum number of concurrent jobs
     *
     * @return
     */
    public int getMaxJobs() {
        return m_maxJobs;
    }

    /**
     * Kill all player jobs on logout
     *
     * @return
     */
    public boolean getCleanOnLogout() {
        return m_cleanOnLogout;
    }

    /**
     * The AWE mode when player logins
     *
     * @return
     */
    public boolean isOnByDefault() {
        return m_isOnByDefault;
    }

    /**
     * Number of blocks placed in each run
     *
     * @return
     */
    public int getRendererBlocks() {
        return m_rendererBlocks;
    }

    /**
     * Maximum number of miliseconds spend on placing blocks
     *
     * @return
     */
    public int getRendererTime() {
        return m_rendererTime;
    }

    /**
     * maximum size of the player block queue
     *
     * @return
     */
    public int getQueueHardLimit() {
        return m_queueHardLimit;
    }

    /**
     * number of blocks on the player queue when to stop placing blocks
     *
     * @return
     */
    public int getQueueSoftLimit() {
        return m_queueSoftLimit;
    }

    /**
     * is async world edit talkative
     *
     * @return
     */
    public boolean isTalkative() {
        return m_isTalkative;
    }

    /**
     * Use the bar api to display progress
     *
     * @return
     */
    public boolean isBarApiProgressEnabled() {
        return m_useBarApi;
    }

    /**
     * Use chat to display progress
     *
     * @return
     */
    public boolean isChatProgressEnabled() {
        return m_useChat;
    }

    /**
     * The permission node
     *
     * @return
     */
    public String getPermissionNode() {
        return PermissionManager.AWE_PREFIX + "Groups." + m_name;
    }

    /**
     * The default values
     */
    private PermissionGroup() {
        m_cleanOnLogout = true;
        m_isDefault = true;
        m_isOnByDefault = true;
        m_isTalkative = true;
        m_maxJobs = -1;
        m_queueHardLimit = 500000;
        m_queueSoftLimit = 250000;
        m_rendererBlocks = 10000;
        m_rendererTime = 40;
        m_useBarApi = true;
        m_useChat = true;
        m_name = "default-values";
    }

    /**
     * Create the default group
     *
     * @param config
     * @param forceDefault
     */
    public PermissionGroup(ConfigurationSection config, boolean forceDefault) {
        this(config, s_defaultValue, forceDefault);
    }

    /**
     * Create new permission group based on configuration section and default
     * group values
     *
     * @param config
     * @param defaults
     * @param forceDefault
     */
    public PermissionGroup(ConfigurationSection config, PermissionGroup defaults,
            boolean forceDefault) {
        m_name = config.getName();
        m_isDefault = config.getBoolean("isDefault", forceDefault);
        m_maxJobs = validate(config.getInt("maxJobs", defaults.getMaxJobs()), defaults.getMaxJobs(), true);
        m_cleanOnLogout = config.getBoolean("cleanOnLogout", defaults.getCleanOnLogout());
        m_isOnByDefault = config.getBoolean("defaultMode", defaults.isOnByDefault());

        ConfigurationSection rendererSection = config.getConfigurationSection("renderer");
        ConfigurationSection queueSection = config.getConfigurationSection("queue");
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");

        int rendererBlocks = validate(rendererSection == null
                ? defaults.getRendererBlocks() : rendererSection.getInt("blocks", defaults.getRendererBlocks()),
                defaults.getRendererBlocks(), true);
        int rendererTime = validate(rendererSection == null
                ? defaults.getRendererTime() : rendererSection.getInt("time", defaults.getRendererTime()),
                defaults.getRendererTime(), true);

        if (rendererBlocks == -1 && rendererTime == -1) {
            AsyncWorldEditMain.log("Warning: Time and blocks are set to unlimited! For group " + m_name);
            rendererBlocks = s_defaultValue.getRendererBlocks();
            rendererTime = s_defaultValue.getRendererTime();
        }

        m_rendererBlocks = rendererBlocks;
        m_rendererTime = rendererTime;

        m_queueHardLimit = validate(queueSection == null
                ? defaults.getQueueHardLimit() : queueSection.getInt("limit-hard", defaults.getQueueHardLimit()),
                defaults.getQueueHardLimit(), false);
        m_queueSoftLimit = validate(queueSection == null
                ? defaults.getQueueSoftLimit() : queueSection.getInt("limit-soft", defaults.getQueueSoftLimit()),
                defaults.getQueueSoftLimit(), false);

        m_useBarApi = messagesSection == null
                ? defaults.isBarApiProgressEnabled() : messagesSection.getBoolean("progress-bar", defaults.isBarApiProgressEnabled());
        m_useChat = messagesSection == null
                ? defaults.isChatProgressEnabled() : messagesSection.getBoolean("progress-chat", defaults.isChatProgressEnabled());
        m_isTalkative = messagesSection == null
                ? defaults.isTalkative() : messagesSection.getBoolean("talkative", defaults.isTalkative());
    }

    /**
     * Validate integer value
     */
    private static int validate(int value, int defaultValue, boolean allowInfinite) {
        if (value == -1 && allowInfinite) {
            return value;
        }

        return (value < 1) ? defaultValue : value;
    }
}
