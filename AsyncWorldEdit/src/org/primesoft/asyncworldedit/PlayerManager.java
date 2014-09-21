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

import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.permissions.Permission;
import org.primesoft.asyncworldedit.permissions.PermissionManager;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 *
 * @author SBPrime
 */
public class PlayerManager {

    private final AsyncWorldEditMain m_parrent;

    /**
     * List of know players
     */
    private final HashMap<UUID, PlayerWrapper> m_playersUids;

    public PlayerManager(AsyncWorldEditMain parent) {
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

        if (PermissionManager.getPermissionGroup(player).getCleanOnLogout()) {
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
                //TODO: Shuld we get the player from the server?
                return null;
            }

            return result;
        }
    }

    /**
     * Get list of all players
     *
     * @return
     */
    public PlayerWrapper[] getAllPlayers() {
        PlayerWrapper[] result;
        synchronized (m_playersUids) {
            result = m_playersUids.values().toArray(new PlayerWrapper[0]);
        }
        return result;
    }

    /**
     * Get default block placing speed
     *
     * @param player
     * @return
     */
    public static int getMaxSpeed(Player player) {
        if (player == null) {
            return 0;
        }

        return PermissionManager.getPermissionGroup(player).getRendererBlocks();        
    }

    /**
     * Get default user mode
     *
     * @param player
     * @return
     */
    public static boolean getDefaultMode(Player player) {
        if (player == null) {
            return false;
        }

        return PermissionManager.getPermissionGroup(player).isOnByDefault();
    }

    /**
     * PLayer has async mode enabled
     *
     * @param player
     * @return
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
     *
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
