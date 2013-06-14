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
package org.primesoft.asyncworldedit;

import org.bukkit.entity.Player;


/**
 * @author SBPrime
 */
public class PermissionManager {
    /**
    * List of all permissions
    */
    public enum Perms {
        ReloadConfig,
        AnnounceVersion,
        QueueBypass, QueueVip,
        Purge_Self, Purge_Other, Purge_All,
        Jobs_Self, Jobs_Other, Jobs_All, Filter,
        Mode_Change, Mode_On, Mode_Off,
        Mode_Change_Other
    }
    
    
    /**
     * Plugin permissions top node
     */
    private static String s_prefix = "AWE.";
    

    /**
     * Check if player has a specific permission
     * @param player player
     * @param perms permission to check
     * @return True if permission pressent
     */
    public static boolean isAllowed(Player player, Perms perms) {
        if (player == null) {
            return false;
        }

        String s = getPermString(perms);
        if (s == null) {
            return false;
        }

        return player.hasPermission(s);
    }

    
    /**
     * Convert permission to string
     * @param perms Permission
     * @return Permission node
     */
    @SuppressWarnings("incomplete-switch")
	private static String getPermString(Perms perms) {
        switch (perms) {
            case AnnounceVersion:
                return s_prefix + "admin.version";
            case ReloadConfig:
                return s_prefix + "admin.reload";
            case Purge_Self:
                return s_prefix + "user.purge";
            case Purge_Other:
                return s_prefix + "admin.purge";
            case Purge_All:
                return s_prefix + "admin.purge.all";
            case Jobs_Self:
                return s_prefix + "user.jobs";
            case Jobs_Other:
                return s_prefix + "admin.jobs";
            case Jobs_All:
                return s_prefix + "admin.jobs.all";
            case QueueBypass:
                return s_prefix + "admin.queue-bypass";
            case QueueVip:
                return s_prefix + "user.vip-queue";
            case Mode_Change:
                return s_prefix + "user.mode.change";
            case Mode_Change_Other:
                return s_prefix + "user.admin.change";
            case Mode_On:
                return s_prefix + "user.mode.on";
            case Mode_Off:
                return s_prefix + "user.mode.off";
        }

        return null;
    }
}