package org.primesoft.asyncworldedit.worldedit;

import org.bukkit.entity.Player;

/**
 * List of all permissions
 */
public enum Permission {

    /**
     * Allows the use of the Reload command
     */
    Reload_Config("admin.version"),

    /**
     * Informs the user on login if the plugin is updated or not
     */
    Announce_Version("admin.reload"),

    /**
     * Bypass the maximum queue limit
     */
    Queue_Bypass("admin.queue-bypass"),

    /**
     * Allows the use of the VIP block queue
     */
    Queue_Vip("user.vip-queue"),

    /**
     * Allows the use of the purge command
     */
    Purge_Self("user.purge"),

    /**
     * Allows the use of the Purge command on another user
     */
    Purge_Other("admin.purge"),

    /**
     * Allows the use of the Purge command on all users
     */
    Purge_All("admin.purge.all"),

    /**
     * Allows the use of the Jobs command
     */
    Jobs_Self("user.jobs"),

    /**
     * Allows the use of the Jobs command on another user
     */
    Jobs_Other("admin.jobs"),

    /**
     * Allows the use of the Jobs command on all users
     */
    Jobs_All("admin.jobs.all"),

    /**
     * Allows the use of the Cancel command
     */
    Cancel_Self("user.jobs.cancel"),

    /**
     * Allows the use of the Cancel command on another user
     */
    Cancel_Other("admin.jobs.cancel"),

    /**
     * Allows the user to toggle AWE for himself
     */
    Mode_Change("user.mode.change"),

    /**
     * Sets AWE to ON when user logs in
     */
    Mode_On("user.mode.on"),

    /**
     * Sets AWE to OFF when user logs in
     */
    Mode_Off("user.mode.off"),

    /**
     * Allows the user to toggle AWE for another user
     */
    Mode_Change_Other("user.admin.change"),

    /**
     * Informs the user of his queue status
     */
    Talkative_Queue("user.talkative"),

    /**
     * Informs the user of his queue status through BarAPI
     */
    Progress_Bar("user.progressBar"),

    /**
     * Allows the user to retain his queue on logout
     */
    Ignore_Cleanup("admin.noCleanupOnQuit");

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
