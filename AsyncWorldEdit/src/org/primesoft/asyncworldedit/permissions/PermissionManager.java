package org.primesoft.asyncworldedit.permissions;

import org.primesoft.asyncworldedit.configuration.PermissionGroup;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.PlayerWrapper;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;

public class PermissionManager {

    /**
     * Plugin permissions top node
     */
    public static final String AWE_PREFIX = "AWE.";

    /**
     * Check if player has a specific permission
     *
     * @param player player
     * @param permission
     * @return True if permission present
     */
    public static boolean isAllowed(Player player, Permission permission) {
        if (player == null || player.isOp()) {
            return true;
        }
        return player.hasPermission(permission.getNode());
    }

    /**
     * Gets the permissions group for player
     *
     * @param playerWrapper
     * @return
     */
    public static PermissionGroup getPermissionGroup(Player player) {
        PermissionGroup defaultGroup = ConfigProvider.getDefaultGroup();
        PermissionGroup[] groups = ConfigProvider.getGroups();

        if (defaultGroup == null) {
            return PermissionGroup.getDefaultGroup();
        }

        if (player == null) {
            return defaultGroup;
        }

        PermissionGroup result = defaultGroup;
        if (groups != null) {
            for (PermissionGroup group : groups) {
                if (player.isOp() || player.hasPermission(group.getPermissionNode())) {
                    result = group;
                }
            }
        }

        return result;
    }
}
