package org.primesoft.asyncworldedit;

import org.bukkit.entity.Player;

public class PermissionManager {

    /**
     * Check if player has a specific permission
     *
     * @param player player
     * @return True if permission present
     */
    public static boolean isAllowed(Player player, Permission permission) {
        if (player == null || player.isOp()) {
            return true;
        }
        return player.hasPermission(permission.getNode());
    }
}
