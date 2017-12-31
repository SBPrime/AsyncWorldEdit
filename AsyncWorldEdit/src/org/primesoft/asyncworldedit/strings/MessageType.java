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
package org.primesoft.asyncworldedit.strings;

/**
 *
 * @author SBPrime
 */
public enum MessageType {
    NOT_INITIALIZED("NOT_INITIALIZED"),
    NO_PERMS("NO_PERMS"),
    PLAYER_NOT_FOUND("PLAYER_NOT_FOUND"),
    INGAME("INGAME"),
    NUMBER_EXPECTED("NUMBER_EXPECTED"),
    //The version checker
    CHECK_VERSION_FORMAT("CHECK_VERSION_FORMAT"),
    CHECK_VERSION_CONFIG("CHECK_VERSION_CONFIG"),
    CHECK_VERSION_OLD("CHECK_VERSION_OLD"),
    CHECK_VERSION_LATEST("CHECK_VERSION_LATEST"),
    CHECK_VERSION_UNKNOWN("CHECK_VERSION_UNKNOWN"),
    CHECK_VERSION_ERROR("CHECK_VERSION_ERROR"),
    //Force save
    SAVE_START("SAVE_START"),
    SAVE_DONE("SAVE_DONE"),
    //Blocks placer
    BLOCK_PLACER_JOBS_LIMIT("BLOCK_PLACER_JOBS_LIMIT"),
    BLOCK_PLACER_GLOBAL_QUEUE_FULL("BLOCK_PLACER_GLOBAL_QUEUE_FULL"),
    BLOCK_PLACER_QUEUE_FULL("BLOCK_PLACER_QUEUE_FULL"),
    BLOCK_PLACER_MEMORY_LOW("BLOCK_PLACER_MEMORY_LOW"),
    BLOCK_PLACER_QUEUE_UNLOCKED("BLOCK_PLACER_QUEUE_UNLOCKED"),
    BLOCK_PLACER_CANCEL_UNDO("BLOCK_PLACER_CANCEL_UNDO"),
    BLOCK_PLACER_RUN("BLOCK_PLACER_RUN"),
    BLOCK_PLACER_MAX_CHANGED("BLOCK_PLACER_MAX_CHANGED"),
    BLOCK_PLACER_CANCELED("BLOCK_PLACER_CANCELED"),    
    BLOCK_PLACER_DONE("BLOCK_PLACER_DONE"),
    BLOCK_PLACER_DONE_WORLD("BLOCK_PLACER_DONE_WORLD"),
    BLOCK_PLACER_DONE_CLIP("BLOCK_PLACER_DONE_CLIP"),
    //Global text
    GLOBAL_YES("GLOBAL_YES"),
    GLOBAL_NO("GLOBAL_NO"),
    GLOBAL_ON("GLOBAL_ON"),
    GLOBAL_OFF("GLOBAL_OFF"),
    //Other texts
    LOADING_UNDO("LOADING_UNDO"),
    LOADING_REDO("LOADING_REDO"),
    //The reload command
    CMD_RELOAD_ERROR("CMD_RELOAD_ERROR"),
    CMD_RELOAD_DONE("CMD_RELOAD_DONE"),
    //The help command
    CMD_HELP_GLOBAL("CMD_HELP_GLOBAL"),
    CMD_HELP_TOGGLE("CMD_HELP_TOGGLE"),
    CMD_HELP_UNDO("CMD_HELP_UNDO"),
    CMD_HELP_PURGE("CMD_HELP_PURGE"),
    CMD_HELP_JOBS("CMD_HELP_JOBS"),
    CMD_HELP_CANCEL("CMD_HELP_CANCEL"),
    CMD_HELP_RELOAD("CMD_HELP_RELOAD"),
    CMD_HELP_MESSAGE("CMD_HELP_MESSAGE"),
    CMD_HELP_SPEED("CMD_HELP_SPEED"),
    //The undo command and mode changed
    CMD_UNDO_MODE_CHANGED("CMD_UNDO_MODE_CHANGED"),
    CMD_UNDO_MODE_DONE("CMD_UNDO_MODE_DONE"),
    CMD_UNDO_MODE_ON("CMD_UNDO_MODE_ON"),
    CMD_UNDO_MODE_OFF("CMD_UNDO_MODE_OFF"),
    //The toggle command and mode changed
    CMD_TOGGLE_MODE_CHANGED("CMD_TOGGLE_MODE_CHANGED"),
    CMD_TOGGLE_MODE_DONE("CMD_TOGGLE_MODE_DONE"),
    CMD_TOGGLE_MODE_ON("CMD_TOGGLE_MODE_ON"),
    CMD_TOGGLE_MODE_OFF("CMD_TOGGLE_MODE_OFF"),
    //The message configuration
    CMD_MESSAGE_OK("CMD_MESSAGE_OK"),
    CMD_MESSAGE_ON("CMD_MESSAGE_ON"),
    CMD_MESSAGE_OFF("CMD_MESSAGE_OFF"),
    CMD_MESSAGE_UNKNOWN("CMD_MESSAGE_UNKNOWN"),
    //The speed command
    CMD_SPEED("CMD_SPEED"),
    CMD_SPEED_INVALID("CMD_SPEED_INVALID"),
    //The jobs command
    CMD_JOBS_LONG("CMD_JOBS_LONG"),
    CMD_JOBS_SHORT("CMD_JOBS_SHORT"),
    CMD_JOBS_HEADER("CMD_JOBS_HEADER"),
    CMD_JOBS_LINE("CMD_JOBS_LINE"),
    CMD_JOBS_FORMAT("CMD_JOBS_FORMAT"),
    CMD_JOBS_PROGRESS_BAR("CMD_JOBS_PROGRESS_BAR"),
    CMD_JOBS_PROGRESS_MSG("CMD_JOBS_PROGRESS_MSG"),
    CMD_JOBS_PAGE("CMD_JOBS_PAGE"),
    CMD_JOBS_NONE("CMD_JOBS_NONE"),
    CMD_JOBS_YOU("CMD_JOBS_YOU"),
    CMD_JOBS_OTHER("CMD_JOBS_OTHER"),
    CMD_JOBS_OTHER_SHORT("CMD_JOBS_OTHER_SHORT"),
    CMD_JOBS_STATUS("CMD_JOBS_STATUS"),
    CMD_JOBS_STATUS_DONE("CMD_JOBS_STATUS_DONE"),
    CMD_JOBS_STATUS_CANCELED("CMD_JOBS_STATUS_CANCELED"),
    CMD_JOBS_STATUS_INITIALIZING("CMD_JOBS_STATUS_INITIALIZING"),
    CMD_JOBS_STATUS_PLACING_BLOCKS("CMD_JOBS_STATUS_PLACING_BLOCKS"),
    CMD_JOBS_STATUS_PREPARING("CMD_JOBS_STATUS_PREPARING"),
    CMD_JOBS_STATUS_WAITING("CMD_JOBS_STATUS_WAITING"),
    //The cancel command
    CMD_CANCEL_REMOVED("CMD_CANCEL_REMOVED"),
    //The purge command
    CMD_PURGE_REMOVED("CMD_PURGE_REMOVED"),
    //The extended commands global
    EX_CMD_BLOCKS_CHANGED("EX_CMD_BLOCKS_CHANGED"),
    //The extended commands - Schematics
    EX_CMD_SCHEMATIC_NOT_FOUND("EX_CMD_SCHEMATIC_NOT_FOUND"),
    EX_CMD_SCHEMATIC_UNKNOWN_FORMAT("EX_CMD_SCHEMATIC_UNKNOWN_FORMAT"),
    EX_CMD_SCHEMATIC_INVALID_FORMAT("EX_CMD_SCHEMATIC_INVALID_FORMAT"),
    EX_CMD_SCHEMATIC_READ_ERROR("EX_CMD_SCHEMATIC_READ_ERROR"),
    EX_CMD_SCHEMATIC_INFO("EX_CMD_SCHEMATIC_INFO"),
    EX_CMD_SCHEMATIC_LOADED("EX_CMD_SCHEMATIC_LOADED"),
    EX_CMD_SCHEMATIC_NO_ORIGIN("EX_CMD_SCHEMATIC_NO_ORIGIN"),
    
    //The extended commands - chunk replace
    EX_CMD_CHUNK_REPLACE_CHUNKS("EX_CMD_CHUNK_REPLACE_CHUNKS"),
    EX_CMD_CHUNK_REPLACE_PROGRESS("EX_CMD_CHUNK_REPLACE_PROGRESS")
;
    
    /**
     * The message provider
     */
    private static MessageProvider s_messageProvider = null;
    
    /**
     * Set the message provider
     * @param messageProvider 
     */
    public static void initializeMessageProvider(MessageProvider messageProvider) {
        s_messageProvider = messageProvider;
    }
    
    private final String m_key;

    MessageType(String key) {
        m_key = key;
    }

    /**
     * Format the message
     *
     * @param params
     * @return
     */
    public String format(Object... params) {
        MessageProvider mp = s_messageProvider;
        return mp != null ? mp.formatMessage(this, params) : m_key;
    }

    /**
     * Get message entry key
     *
     * @return
     */
    public String getKey() {
        return m_key;
    }

    @Override
    public String toString() {
        return format();
    }
}
