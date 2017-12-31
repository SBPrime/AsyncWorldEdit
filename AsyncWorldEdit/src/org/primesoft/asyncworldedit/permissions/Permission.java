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
package org.primesoft.asyncworldedit.permissions;

import org.primesoft.asyncworldedit.api.permissions.IPermission;

/**
 * List of all permissions
 */
public enum Permission implements IPermission {

    /**
     * Allows the use of the Reload command
     */
    RELOAD_CONFIG("admin.reload"),

    /**
     * Informs the user on login if the plugin is updated or not
     */
    ANNOUNCE_VERSION("admin.version"),

    /**
     * Bypass the maximum queue limit
     */
    QUEUE_BYPASS("admin.queue-bypass"),

    /**
     * Allows the use of the purge command
     */
    PURGE_SELF("user.purge"),

    /**
     * Allows the use of the Purge command on another user
     */
    PURGE_OTHER("admin.purge"),

    /**
     * Allows the use of the Purge command on all users
     */
    PURGE_ALL("admin.purge.all"),

    /**
     * Allows the use of the Jobs command
     */
    JOBS_SELF("user.jobs"),

    /**
     * Allows the use of the Jobs command on another user
     */
    JOBS_OTHER("admin.jobs"),

    /**
     * Allows the use of the Jobs command on all users
     */
    JOBS_ALL("admin.jobs.all"),

    /**
     * Allows the use of the Cancel command
     */
    CANCEL_SELF("user.cancel"),

    /**
     * Allows the use of the Cancel command on another user
     */
    CANCEL_OTHER("admin.cancel"),

    /**
     * Allows the user to toggle AWE for himself
     */
    MODE_CHANGE("user.mode.change"),
    
    
    /**
     * Allows the user to toggle undo for himself
     */
    UNDO_CHANGE("user.undo.change"),

    /**
     * Allows the user to toggle AWE for another user
     */
    MODE_CHANGE_OTHER("admin.mode.change"),
    
    /**
     * Allows the user to toggle undo for another user
     */
    UNDO_CHANGE_OTHER("admin.undo.change"),
    
    /**
     * Allows the uset to bypass the blockshub restriction
     */
    BYPASS_BLOCKS_HUB("admin.blockaccess.bypass_blockshub"),
    
    /**
     * Allows the uset to bypass the blocks whitelist
     */
    BYPASS_WHITELIST("admin.blockaccess.bypass_whitelist"),
    
    
    /**
     * Allows the user to change speed for himself
     */
    SPEED_CHANGE("user.speed.change"),

    /**
     * Allows the user to view speed for himself
     */
    SPEED_VIEW("user.speed.view"),

    /**
     * Allows the user to view speed for another user
     */
    SPEED_VIEW_OTHER("admin.speed.view"),
    
    /**
     * Allows the user to change speed for another user
     */
    SPEED_CHANGE_OTHER("admin.speed.change"),
    
    
    /**
     * Allows the user to change AWE messages  for himself
     */
    MESSAGES_CHANGE("user.messages.change"),

    /**
     * Allows the user to change AWE messages if the system is disabled
     */
    MESSAGES_CHANGE_OVERRIDE("admin.messages.override"),
    
    /**
     * Allows the user to change AWE messages for another user
     */
    MESSAGES_CHANGE_OTHER("admin.messages.change");
    

    /**
     * Plugin permission without base
     */
    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    /**
     * Get the full permission node
     *
     * @return Entire permission node including base
     */
    @Override
    public String getNode() {        
        return PermissionManager.AWE_PREFIX + permission;
    }
}
