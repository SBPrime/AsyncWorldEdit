package org.primesoft.asyncworldedit;

import org.bukkit.entity.Player;

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
     * Allows the use of the VIP block queue
     */
    QUEUE_VIP("user.vip-queue"),

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
     * Sets AWE to ON when user logs in
     */
    MODE_ON("user.mode.on"),

    /**
     * Sets AWE to OFF when user logs in
     */
    MODE_OFF("user.mode.off"),

    /**
     * Allows the user to toggle AWE for another user
     */
    MODE_CHANGE_OTHER("user.admin.change"),

    /**
     * Informs the user of his queue status
     */
    TALKATIVE_QUEUE("user.talkative"),

    /**
     * Informs the user of his queue status through BarAPI
     */
    PROGRESS_BAR("user.progressBar"),

    /**
     * Allows the user to retain his queue on logout
     */
    IGNORE_CLEANUP("admin.noCleanupOnQuit");

    /**
     * Plugin permissions top node
     */
    private static final String s_prefix = "AWE.";

    /**
     * Plugin permission without base
     */
    private String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    /**
     * Get the full permission node
     *
     * @return Entire permission node including base
     */
    public String getNode() {
        return s_prefix + permission;
    }

}
