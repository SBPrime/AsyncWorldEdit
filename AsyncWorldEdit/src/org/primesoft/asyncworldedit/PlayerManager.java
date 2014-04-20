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

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 *
 * @author SBPrime
 */
public class PlayerManager {

    private final PluginMain m_parrent;

    /**
     * List of know players
     */
    private final HashMap<UUID, PlayerWrapper> m_playersUids;

    public PlayerManager(PluginMain parent) {
        m_playersUids = new HashMap<UUID, PlayerWrapper>();
        m_parrent = parent;
    }

    public PlayerWrapper addPlayer(Player player) {
        if (player == null) {
            return null;
        }

        UUID uuid = player.getUniqueId();
        String pName = player.getName();
        synchronized (m_playersUids) {
            PlayerWrapper wrapper = m_playersUids.get(uuid);

            if (wrapper != null) {
                return wrapper;
            }

            wrapper = new PlayerWrapper(player, pName, getDefaultMode(player));
            m_playersUids.put(uuid, wrapper);
            return wrapper;
        }
    }

    public void removePlayer(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        synchronized (m_playersUids) {
            m_playersUids.remove(uuid);
        }

        if (ConfigProvider.cleanOnLogoutEnabled()
                && !PermissionManager.isAllowed(player, PermissionManager.Perms.IgnoreCleanup)) {
            m_parrent.getBlockPlacer().purge(uuid);
        }
    }

    /**
     * Get the player wrapper based on UUID
     *
     * @param player
     * @return
     */
    public PlayerWrapper getPlayer(UUID player) {
        if (player == null) {
            return null;
        }

        synchronized (m_playersUids) {
            PlayerWrapper result = m_playersUids.get(player);
            if (result == null) {
                Player cbPlayer = m_parrent.getPlayer(player);
                if (cbPlayer == null) {
                    return null;
                }

                result = addPlayer(cbPlayer);
            }

            return result;
        }
    }

    /**
     * Get default block placing speed
     */
    public static int getMaxSpeed(Player player) {
        if (player == null) {
            return 0;
        }

        boolean isVip = PermissionManager.isAllowed(player, PermissionManager.Perms.QueueVip);
        return ConfigProvider.getBlockCount() + (isVip ? ConfigProvider.getVipBlockCount() : 0);
    }

    /**
     * Get default user mode
     */
    public static boolean getDefaultMode(Player player) {
        if (player == null) {
            return false;
        }

        boolean hasOn = PermissionManager.isAllowed(player, PermissionManager.Perms.Mode_On);
        boolean hasOff = PermissionManager.isAllowed(player, PermissionManager.Perms.Mode_Off);

        if (hasOn && hasOff) {
            return ConfigProvider.getDefaultMode();
        } else if (hasOn) {
            return true;
        } else if (hasOff) {
            return false;
        } else {
            return ConfigProvider.getDefaultMode();
        }
    }

    /**
     * PLayer has async mode enabled
     */
    public boolean hasAsyncMode(UUID player) {
        PlayerWrapper wrapper = getPlayer(player);

        if (wrapper == null) {
            return true;
        }

        return wrapper.getMode();
    }

    /**
     * Set the AWE player mode
     *
     * @param player
     * @param mode
     */
    public void setMode(UUID player, boolean mode) {
        PlayerWrapper wrapper = getPlayer(player);

        if (wrapper == null) {
            return;
        }

        wrapper.setMode(mode);
    }

    public void initalize() {
        Player[] players = m_parrent.getServer().getOnlinePlayers();
        for (Player player : players) {
            addPlayer(player);
        }
    }

    
    /**
     * Gets player UUID from player name
     * @param playerName
     * @return 
     */
    public UUID getPlayerUUID(String playerName) {
        synchronized (m_playersUids) {
            for (PlayerWrapper p : m_playersUids.values()) {
                if (p.getName().equalsIgnoreCase(playerName)) {
                    return p.getUUID();
                }
            }
        }

        try {
            return UUID.fromString(playerName);
        } catch (IllegalArgumentException ex) {
            return ConfigProvider.DEFAULT_USER;
        }
    }
}
