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
package org.primesoft.asyncworldedit.permissions;

/**
 * List of all permissions
 */
public enum Permission {

    /**
     * Allows the use of the Reload command
     */
    RELOAD_CONFIG("admin.version"),

    /**
     * Informs the user on login if the plugin is updated or not
     */
    ANNOUNCE_VERSION("admin.reload"),

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
     * Allows the user to toggle AWE for another user
     */
    MODE_CHANGE_OTHER("admin.mode.change");

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
    public String getNode() {        
        return PermissionManager.AWE_PREFIX + permission;
    }
}
