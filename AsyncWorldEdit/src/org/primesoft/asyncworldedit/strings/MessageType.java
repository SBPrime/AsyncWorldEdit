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
package org.primesoft.asyncworldedit.strings;

import org.bukkit.ChatColor;

/**
 *
 * @author SBPrime
 */
public enum MessageType {
    NOT_INITIALIZED("NOT_INITIALIZED", ChatColor.RED + "Module not initialized, contact administrator."),
    NO_PERMS("NO_PERMS", ChatColor.RED + "You have no permissions to do that."),
    PLAYER_NOT_FOUND("PLAYER_NOT_FOUND", ChatColor.RED + "Player " + ChatColor.WHITE + "%1$s" + ChatColor.RED + " not found."),
    INGAME("INGAME", ChatColor.RED + "Command available ingame."),
    NUMBER_EXPECTED("NUMBER_EXPECTED", ChatColor.RED + "Number expected."),
    //The version checker
    CHECK_VERSION_FORMAT("CHECK_VERSION_FORMAT", ChatColor.BLUE + " %1$s %2$s"),
    CHECK_VERSION_CONFIG("CHECK_VERSION_CONFIG", ChatColor.BLUE + " %1$s Please update your config file!"),
    CHECK_VERSION_OLD("CHECK_VERSION_OLD", "You have an old version of the plugin. Your version: %1$s, available version: %2$s"),
    CHECK_VERSION_LATEST("CHECK_VERSION_LATEST", "You have the latest version of the plugin."),
    CHECK_VERSION_UNKNOWN("CHECK_VERSION_UNKNOWN", "Your version of the plugin was not found on the plugin page. Your version: %1$s"),
    CHECK_VERSION_ERROR("CHECK_VERSION_ERROR", "Unable to check latest plugin version."),
    //Blocks placer
    BLOCK_PLACER_JOBS_LIMIT("BLOCK_PLACER_JOBS_LIMIT", ChatColor.RED + "You have too many jobs queued, operation canceled."),
    BLOCK_PLACER_GLOBAL_QUEUE_FULL("BLOCK_PLACER_GLOBAL_QUEUE_FULL", ChatColor.RED + "Out of space on AWE block queue."),
    BLOCK_PLACER_QUEUE_FULL("BLOCK_PLACER_QUEUE_FULL", ChatColor.RED + "Your block queue is full. Wait for items to finish drawing."),
    BLOCK_PLACER_QUEUE_UNLOCKED("BLOCK_PLACER_QUEUE_UNLOCKED", ChatColor.GREEN + "Your block queue is unlocked. You can use WorldEdit."),
    BLOCK_PLACER_CANCEL_UNDO("BLOCK_PLACER_CANCEL_UNDO", ChatColor.RED + "Warning: Undo jobs shuld not by canceled, ingoring!"),    
    BLOCK_PLACER_RUN("BLOCK_PLACER_RUN", ChatColor.LIGHT_PURPLE + "Running " + ChatColor.WHITE
                    + "%1$s" + ChatColor.LIGHT_PURPLE + " in full async mode."),
    BLOCK_PLACER_MAX_CHANGED("BLOCK_PLACER_MAX_CHANGED", ChatColor.RED + "Maximum block change limit."),    
    BLOCK_PLACER_CANCELED("BLOCK_PLACER_CANCELED", ChatColor.LIGHT_PURPLE + "Job canceled."),
    BLOCK_PLACER_DONE("BLOCK_PLACER_DONE", ChatColor.LIGHT_PURPLE + "Blocks processed: " + ChatColor.WHITE + "%1$s"),
    BLOCK_PLACER_DONE_WORLD("BLOCK_PLACER_DONE_WORLD", ChatColor.LIGHT_PURPLE + "World operation done."),
    BLOCK_PLACER_DONE_CLIP("BLOCK_PLACER_DONE_CLIP", ChatColor.LIGHT_PURPLE + "Clipboard operation done."),    
    //The reload command
    CMD_RELOAD_ERROR("CMD_RELOAD_ERROR", ChatColor.RED + "Error loading config"),
    CMD_RELOAD_DONE("CMD_RELOAD_DONE", ChatColor.GREEN + "Reload done"),
    //The help command
    CMD_HELP_GLOBAL("CMD_HELP_GLOBAL", ChatColor.YELLOW + "Async WorldEdit help:\n"
            + ChatColor.BLUE + "Help" + ChatColor.WHITE + " - display help screen\n"
            + ChatColor.BLUE + "Jobs" + ChatColor.WHITE + " - display queued block operations\n"
            + ChatColor.BLUE + "Cancel" + ChatColor.WHITE + " - cancel queued job\n"
            + ChatColor.BLUE + "Purge" + ChatColor.WHITE + " - remove all queued block operations\n"
            + ChatColor.BLUE + "Toggle" + ChatColor.WHITE + " - toggle AsyncWorldEdit on/off\n"
            + ChatColor.BLUE + "Reload" + ChatColor.WHITE + " - reload configuration\n"
            + ChatColor.YELLOW + "To display help on command use: " + ChatColor.BLUE + "/awe help <command>\n"),
    CMD_HELP_TOGGLE("CMD_HELP_TOGGLE", ChatColor.YELLOW + "Toggle " + ChatColor.WHITE + " - toggle AsyncWorldEdit on/off\n"
            + ChatColor.BLUE + " Toggle" + ChatColor.WHITE + " - toggle AsyncWorldEdit\n"
            + ChatColor.BLUE + " Toggle on" + ChatColor.WHITE + " - toggle AsyncWorldEdit on\n"
            + ChatColor.BLUE + " Toggle off" + ChatColor.WHITE + " - toggle AsyncWorldEdit off\n"
            + ChatColor.BLUE + " Toggle <u:playerName> " + ChatColor.WHITE + " - toggle AsyncWorldEdit\n"
            + ChatColor.BLUE + " Toggle <u:playerName> on" + ChatColor.WHITE + " - toggle AsyncWorldEdit on\n"
            + ChatColor.BLUE + " Toggle <u:playerName> off" + ChatColor.WHITE + " - toggle AsyncWorldEdit off"),
    CMD_HELP_PURGE("CMD_HELP_PURGE", ChatColor.YELLOW + "Purge " + ChatColor.WHITE + " - remove all queued block operations\n"
            + ChatColor.BLUE + " Purge" + ChatColor.WHITE + " - purges your operations\n"
            + ChatColor.BLUE + " Purge <u:playerName>" + ChatColor.WHITE + " - purges other player operations\n"
            + ChatColor.BLUE + " Purge all" + ChatColor.WHITE + " - purges all operations"),
    CMD_HELP_JOBS("CMD_HELP_JOBS", ChatColor.YELLOW + "Jobs " + ChatColor.WHITE + " - display queued block operations\n"
            + ChatColor.BLUE + " Jobs [page]" + ChatColor.WHITE + " - displays your operations\n"
            + ChatColor.BLUE + " Jobs <u:playerName> [page]" + ChatColor.WHITE + " - displays other player operations\n"
            + ChatColor.BLUE + " Jobs all [page]" + ChatColor.WHITE + " - displays all queued operations"),
    CMD_HELP_CANCEL("CMD_HELP_CANCEL", ChatColor.YELLOW + "Cancel " + ChatColor.WHITE + " - cancel queued job\n"
            + ChatColor.BLUE + " Cancel #id" + ChatColor.WHITE + " - cancel your job\n"
            + ChatColor.BLUE + " Cancel <u:playerName> #id" + ChatColor.WHITE + " - cancel other player job"),
    CMD_HELP_RELOAD("CMD_HELP_RELOAD", ChatColor.YELLOW + "Reload" + ChatColor.WHITE + " - Reload AWE configuration file\n"
            + ChatColor.BLUE + " Reload All" + ChatColor.WHITE + " - reload configuration entries and update groups\n"
            + ChatColor.BLUE + " Reload Config" + ChatColor.WHITE + " - reload only the configuration\n"
            + ChatColor.BLUE + " Reload Groups" + ChatColor.WHITE + " - update only the player groups"),
    //The toggle command and mode changed
    CMD_TOGGLE_MODE_CHANGED("CMD_TOGGLE_MODE_CHANGED", ChatColor.YELLOW + "Your " + ChatColor.BLUE + "AsyncWorldEdit "
            + ChatColor.YELLOW + "is now set to " + ChatColor.WHITE + "%1$s"),
    CMD_TOGGLE_MODE_DONE("CMD_TOGGLE_MODE_DONE", ChatColor.YELLOW + "AsyncWorldEdit is now set to " + 
            ChatColor.WHITE + "%1$s"),
    CMD_TOGGLE_MODE_ON("CMD_TOGGLE_MODE_ON", "On"),
    CMD_TOGGLE_MODE_OFF("CMD_TOGGLE_MODE_OFF", "Off"),
    //The jobs command
    CMD_JOBS_LONG("CMD_JOBS_LONG", ChatColor.WHITE + "%1$d"
            + ChatColor.YELLOW + " out of " + ChatColor.WHITE + "%2$d"
            + ChatColor.YELLOW + " blocks (" + ChatColor.WHITE + "%3$.2f%%"
            + ChatColor.YELLOW + ") queued. Placing speed: " + ChatColor.WHITE + "%4$.2fbps"
            + ChatColor.YELLOW + ", " + ChatColor.WHITE + "%5$.2fs" + ChatColor.YELLOW + " left."),
    CMD_JOBS_SHORT("CMD_JOBS_SHORT", ChatColor.WHITE + "%1$d"
            + ChatColor.YELLOW + " blocks queued. Placing speed: " + ChatColor.WHITE + "%2$.2fbps"
            + ChatColor.YELLOW + ", " + ChatColor.WHITE + "%3$.2fs" + ChatColor.YELLOW + " left."),
    CMD_JOBS_HEADER("CMD_JOBS_HEADER", ChatColor.YELLOW + "Jobs: "),
    CMD_JOBS_LINE("CMD_JOBS_LINE", ChatColor.YELLOW + " * %1$s" + ChatColor.YELLOW + " - %2$s"),
    CMD_JOBS_FORMAT("CMD_JOBS_FORMAT", ChatColor.WHITE + "[%1$s] %2$s"),
    CMD_JOBS_PROGRESS_BAR("CMD_JOBS_PROGRESS_BAR", ChatColor.YELLOW + "Jobs: " + ChatColor.WHITE + "%1$d"
            + ChatColor.YELLOW + ", Placing speed: " + ChatColor.WHITE + "%2$.2fbps"
            + ChatColor.YELLOW + ", " + ChatColor.WHITE + "%3$.2fs" + ChatColor.YELLOW + " left."),
    CMD_JOBS_PROGRESS_MSG("CMD_JOBS_PROGRESS_MSG", ChatColor.YELLOW + "[AWE] You have %1$s"),
    CMD_JOBS_PAGE("CMD_JOBS_PAGE", ChatColor.YELLOW + "page " + ChatColor.WHITE + "%1$s" + 
                    ChatColor.YELLOW +  " of " + ChatColor.WHITE + "%2$s"),
    CMD_JOBS_NONE("CMD_JOBS_NONE", ChatColor.YELLOW + "No operations queued."),
    CMD_JOBS_YOU("CMD_JOBS_YOU", ChatColor.YELLOW + "You have %1$s"),
    CMD_JOBS_OTHER("CMD_JOBS_OTHER", ChatColor.YELLOW + "Player " + ChatColor.WHITE + "%1$s"
                             + ChatColor.YELLOW + " has %2$s"),
    CMD_JOBS_OTHER_SHORT("CMD_JOBS_OTHER_SHORT", ChatColor.YELLOW + "Player " + ChatColor.WHITE
                            + "%1$s" + ChatColor.YELLOW + " has " + ChatColor.WHITE + "%2$s"
                            + ChatColor.YELLOW + " block operations queued."),
    CMD_JOBS_STATUS("CMD_JOBS_STATUS", ChatColor.YELLOW + "Job %1$s" + ChatColor.YELLOW + " - %2$s"),
    CMD_JOBS_STATUS_DONE("CMD_JOBS_STATUS_DONE", ChatColor.GREEN + "done"),
    CMD_JOBS_STATUS_CANCELED("CMD_JOBS_STATUS_CANCELED", ChatColor.RED + "canceled"),
    CMD_JOBS_STATUS_INITIALIZING("CMD_JOBS_STATUS_INITIALIZING", ChatColor.WHITE + "initializing"),
    CMD_JOBS_STATUS_PLACING_BLOCKS("CMD_JOBS_STATUS_PLACING_BLOCKS", ChatColor.GREEN + "placing blocks"),
    CMD_JOBS_STATUS_PREPARING("CMD_JOBS_STATUS_PREPARING", ChatColor.RED + "preparing blocks"),
    CMD_JOBS_STATUS_WAITING("CMD_JOBS_STATUS_WAITING", ChatColor.YELLOW + "waiting"),    
    //The cancel command
    CMD_CANCEL_REMOVED("CMD_CANCEL_REMOVED", ChatColor.WHITE + "%1$s" + ChatColor.YELLOW + " queue entries removed."),
    //The purge command
    CMD_PURGE_REMOVED("CMD_PURGE_REMOVED", ChatColor.WHITE + "%1$s" + ChatColor.YELLOW + " queue entries removed."),
    
;
    private final String m_key;
    private final String m_default;

    MessageType(String key, String defaultMessage) {
        m_key = key;
        m_default = defaultMessage;
    }

    /**
     * Format the message
     *
     * @param params
     * @return
     */
    public String format(Object... params) {
        return MessageProvider.formatMessage(this, params);
    }

    /**
     * Get message entry key
     *
     * @return
     */
    public String getKey() {
        return m_key;
    }

    /**
     * Get the default value
     *
     * @return
     */
    public String getDefault() {
        return m_default;
    }

    @Override
    public String toString() {
        return format();
    }
}
