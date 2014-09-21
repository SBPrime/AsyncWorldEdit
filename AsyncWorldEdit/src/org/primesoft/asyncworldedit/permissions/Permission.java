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
    CANCEL_SELF("user.jobs.cancel"),

    /**
     * Allows the use of the Cancel command on another user
     */
    CANCEL_OTHER("admin.jobs.cancel"),

    /**
     * Allows the user to toggle AWE for himself
     */
    MODE_CHANGE("user.mode.change"),

    /**
     * Allows the user to toggle AWE for another user
     */
    MODE_CHANGE_OTHER("user.admin.change");

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
