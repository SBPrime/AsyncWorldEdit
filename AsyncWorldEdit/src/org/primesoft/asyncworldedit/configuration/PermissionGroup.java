/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
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
