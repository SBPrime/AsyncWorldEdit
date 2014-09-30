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
