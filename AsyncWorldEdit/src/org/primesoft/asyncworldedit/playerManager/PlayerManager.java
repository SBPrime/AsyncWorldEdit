/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
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
package org.primesoft.asyncworldedit.playerManager;

import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;

import java.util.HashMap;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.inner.IWorldeditIntegratorInner;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.configuration.ConfigUndo;
import org.primesoft.asyncworldedit.platform.api.IPlayerStorage;

/**
 *
 * @author SBPrime
 */
public class PlayerManager implements IPlayerManager, IPlayerStorage {

    final static UUID UUID_CONSOLE = UUID.randomUUID();
    final static UUID UUID_UNKNOWN = UUID.randomUUID();

    final static IPlayerEntry CONSOLE = new NoPlatformPlayerEntry("<Console>", UUID_CONSOLE, true);
    final static IPlayerEntry UNKNOWN = new NoPlatformPlayerEntry("<Unknown>", UUID_UNKNOWN, false);

    private final IAsyncWorldEditCore m_parrent;

    /**
     * List of know players
     */
    private final HashMap<UUID, IPlayerEntry> m_playersUids;

    public PlayerManager(IAsyncWorldEditCore parent) {
        m_playersUids = new HashMap<UUID, IPlayerEntry>();
        m_parrent = parent;

        synchronized (m_playersUids) {
            m_playersUids.put(UUID_CONSOLE, CONSOLE);
            m_playersUids.put(UUID_UNKNOWN, UNKNOWN);
        }
    }

    /**
     * Update AWE permission groups
     */
    public void updateGroups() {
        synchronized (m_playersUids) {
            for (IPlayerEntry pe : m_playersUids.values()) {
                pe.updatePermissionGroup();
            }
        }
    }

    @Override
    public IPlayerEntry addPlayer(IPlayerEntry player) {
        UUID uuid = player.getUUID();

        synchronized (m_playersUids) {
            IPlayerEntry wrapper = m_playersUids.get(uuid);

            if (wrapper == null) {
                IBlockPlacer bp = m_parrent.getBlockPlacer();
                IPlayerEntry[] players = bp.getAllPlayers();
                for (IPlayerEntry entry : players) {
                    if (entry.getUUID().equals(uuid)) {
                        wrapper = entry;
                        break;
                    }
                }
            }

            if (wrapper != null) {
                wrapper.update(player);
                return wrapper;
            }

            m_playersUids.put(uuid, player);

            return player;
        }
    }

    @Override
    public void removePlayer(UUID uuid) {
        IPlayerEntry entry;
        synchronized (m_playersUids) {
            entry = m_playersUids.remove(uuid);
        }

        if (entry != null) {
            if (entry.getPermissionGroup().getCleanOnLogout()) {
                m_parrent.getBlockPlacer().purge(entry);
            }

            entry.dispose();
        }

        ConfigUndo undoConfig = ConfigProvider.undo();
        if (undoConfig != null && undoConfig.cleanOnLogout()) {
            IWorldeditIntegratorInner integrator = m_parrent.getWorldEditIntegrator();
            if (integrator != null) {
                integrator.removeSession(entry);
            }
        }
    }

    /**
     * The console player UUID
     *
     * @return
     */
    @Override
    public UUID getUuidConsole() {
        return UUID_CONSOLE;
    }

    /**
     * The unknown player UUID
     *
     * @return
     */
    @Override
    public UUID getUuidUnknown() {
        return UUID_UNKNOWN;
    }

    /**
     * Get the console player entry
     *
     * @return
     */
    @Override
    public IPlayerEntry getConsolePlayer() {
        return CONSOLE;
    }

    /**
     * Get the unknown player entry
     *
     * @return
     */
    @Override
    public IPlayerEntry getUnknownPlayer() {
        return UNKNOWN;
    }

    /**
     * Create a fake player entry (do not add to the manager)
     *
     * @param uuid
     * @param name
     * @param group
     * @return
     */
    @Override
    public IPlayerEntry createFakePlayer(String name, UUID uuid, IPermissionGroup group) {
        return new NoPlatformPlayerEntry(name, uuid, group, false);
    }

    /**
     * Create a fake player entry (do not add to the manager)
     *
     * @param name
     * @param uuid
     * @return
     */
    @Override
    public IPlayerEntry createFakePlayer(String name, UUID uuid) {
        return new NoPlatformPlayerEntry(name, uuid, false);
    }

    /**
     * Get the player wrapper based on UUID
     *
     * @param playerUuid
     * @return NEver returns null
     */
    @Override
    public IPlayerEntry getPlayer(UUID playerUuid) {
        if (playerUuid == null) {
            return CONSOLE;
        }

        IPlayerEntry result;

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
    @Override
    public IPlayerEntry getPlayer(String playerName) {
        if (playerName == null || playerName.length() == 0) {
            return CONSOLE;
        }

        synchronized (m_playersUids) {
            for (IPlayerEntry p : m_playersUids.values()) {
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
    private IPlayerEntry findPlayer(String playerName, UUID playerUuid) {
        if (playerName == null && playerUuid == null) {
            return UNKNOWN;
        }

        IBlockPlacer bp = m_parrent.getBlockPlacer();
        IPlayerEntry[] queuedEntries = bp.getAllPlayers();
        if (queuedEntries == null) {
            return UNKNOWN;
        }

        for (IPlayerEntry pe : queuedEntries) {
            if ((playerUuid != null && playerUuid.equals(pe.getUUID()))
                    || (playerName != null && playerName.equalsIgnoreCase(pe.getName()))) {
                return pe;
            }
        }

        return UNKNOWN;
    }
}
