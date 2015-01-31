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

import java.util.Collection;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.permissions.PermissionManager;
import java.util.HashMap;
import java.util.Iterator;
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
    private final HashMap<UUID, PlayerEntry> m_playersUids;

    public PlayerManager(AsyncWorldEditMain parent) {
        m_playersUids = new HashMap<UUID, PlayerEntry>();
        m_parrent = parent;

        synchronized (m_playersUids) {
            m_playersUids.put(PlayerEntry.UUID_CONSOLE, PlayerEntry.CONSOLE);
            m_playersUids.put(PlayerEntry.UUID_UNKNOWN, PlayerEntry.UNKNOWN);
        }
    }

    /**
     * Initialize the player manager
     */
    public void initalize() {
        Collection<? extends Player> players = m_parrent.getServer().getOnlinePlayers();
        for (Iterator iterator = players.iterator(); iterator.hasNext();) {
            Player player = (Player)iterator.next();
            addPlayer(player);
        }
    }
    
    
    /**
     * Update AWE permission groups
     */
    public void updateGroups()
    {
        synchronized (m_playersUids)
        {
            for (PlayerEntry pe : m_playersUids.values())
            {
                Player player = pe.getPlayer();
                pe.update(player, PermissionManager.getPermissionGroup(player));
            }
        }
    }

    /**
     * Wrap new player
     *
     * @param player
     * @return
     */
    public PlayerEntry addPlayer(Player player) {
        if (player == null) {
            return PlayerEntry.CONSOLE;
        }

        UUID uuid = player.getUniqueId();
        String pName = player.getName();
        synchronized (m_playersUids) {
            PlayerEntry wrapper = m_playersUids.get(uuid);

            if (wrapper != null) {
                wrapper.update(player, PermissionManager.getPermissionGroup(player));
                return wrapper;
            }

            wrapper = new PlayerEntry(player, pName, PermissionManager.getPermissionGroup(player));
            m_playersUids.put(uuid, wrapper);
            return wrapper;
        }
    }

    /**
     * Remove player
     *
     * @param player
     */
    public void removePlayer(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        PlayerEntry entry;
        synchronized (m_playersUids) {
            entry = m_playersUids.remove(uuid);
        }

        if (entry != null && entry.getPermissionGroup().getCleanOnLogout()) {
            m_parrent.getBlockPlacer().purge(entry);
        }
    }

    /**
     * Get the player wrapper based on bukkit player class (null = console)
     *
     * @param player
     * @return
     */
    public PlayerEntry getPlayer(Player player) {
        return getPlayer(player != null ? player.getUniqueId() : PlayerEntry.UUID_CONSOLE);
    }

    /**
     * Get the player wrapper based on UUID
     *
     * @param playerUuid
     * @return NEver returns null
     */
    public PlayerEntry getPlayer(UUID playerUuid) {
        if (playerUuid == null) {
            return PlayerEntry.CONSOLE;
        }

        PlayerEntry result;

        synchronized (m_playersUids) {
            result = m_playersUids.get(playerUuid);
            if (result != null) {
                return result;
            }
        }

        /**
         * Unknown player try to find it
         */
        return findPlayer(null, playerUuid);
    }

    /**
     * Gets player wrapper from player name
     *
     * @param playerName
     * @return never returns null
     */
    public PlayerEntry getPlayer(String playerName) {
        if (playerName == null || playerName.length() == 0) {
            return PlayerEntry.CONSOLE;
        }

        synchronized (m_playersUids) {
            for (PlayerEntry p : m_playersUids.values()) {
                if (p.getName().equalsIgnoreCase(playerName)) {
                    return p;
                }
            }
        }

        /**
         * Player name not found try using it as GUID
         */
        try {
            return getPlayer(UUID.fromString(playerName));
        } catch (IllegalArgumentException ex) {
            //This was not 
        }

        return findPlayer(playerName, null);
    }

    /**
     * Search the block placer queues for player entry
     *
     * @param playerName
     * @param playerUuid
     * @return Never returns null
     */
    private PlayerEntry findPlayer(String playerName, UUID playerUuid) {
        if (playerName == null && playerUuid == null) {
            return PlayerEntry.UNKNOWN;
        }

        BlockPlacer bp = m_parrent.getBlockPlacer();
        PlayerEntry[] queuedEntries = bp.getAllPlayers();
        if (queuedEntries == null) {
            return PlayerEntry.UNKNOWN;
        }

        for (PlayerEntry pe : queuedEntries) {
            if ((playerUuid != null && playerUuid.equals(pe.getUUID()))
                    || (playerName != null && playerName.equalsIgnoreCase(pe.getName()))) {
                return pe;
            }
        }

        return PlayerEntry.UNKNOWN;
    }
}
