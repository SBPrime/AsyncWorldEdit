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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.World;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;

/**
 *
 * @author SBPrime
 */
public class AsyncEditSessionFactory extends EditSessionFactory {

    private final AsyncWorldEditMain m_parent;
    private final EventBus m_eventBus;
    private final IPlayerManager m_playerManager;
    private final WorldEditPlugin m_worldEdit;

    /**
     * Get the player UUID
     *
     * @param player
     * @return
     */
    private PlayerEntry getPlayerEntry(com.sk89q.worldedit.entity.Player player) {
        return m_playerManager.getPlayer(player.getName());
    }

    private BukkitPlayer getPlayer(PlayerEntry playerEntry) {
        if (playerEntry == null) {
            return null;
        }

        org.bukkit.entity.Player player = playerEntry.getPlayer();

        if (player == null) {
            return null;
        }

        return m_worldEdit.wrapPlayer(player);
    }

    public AsyncEditSessionFactory(WorldEditPlugin worldEdit, AsyncWorldEditMain parent, EventBus eventBus) {
        m_worldEdit = worldEdit;
        m_parent = parent;
        m_eventBus = eventBus;
        m_playerManager = parent.getPlayerManager();
    }

    @Override
    public EditSession getEditSession(World world, int maxBlocks) {
        return new AsyncEditSession(m_parent, PlayerEntry.UNKNOWN,
                m_eventBus, world, maxBlocks, null,
                new EditSessionEvent(world, null, maxBlocks, null));
    }

    @Override
    public EditSession getEditSession(World world, int maxBlocks, Player player) {
        PlayerEntry entry = getPlayerEntry(player);
        AsyncEditSession result = new AsyncEditSession(m_parent, entry,
                m_eventBus, world, maxBlocks, null,
                new EditSessionEvent(world, player, maxBlocks, null));

        m_parent.getPlotMeFix().setMask(entry.getPlayer());

        return result;
    }

    @Override
    public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag) {
        return new AsyncEditSession(m_parent, PlayerEntry.UNKNOWN,
                m_eventBus, world, maxBlocks, blockBag,
                new EditSessionEvent(world, null, maxBlocks, null));
    }

    @Override
    public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag, Player player) {
        PlayerEntry entry = getPlayerEntry(player);

        AsyncEditSession result = new AsyncEditSession(m_parent, entry,
                m_eventBus, world, maxBlocks, blockBag,
                new EditSessionEvent(world, player, maxBlocks, null));

        m_parent.getPlotMeFix().setMask(entry.getPlayer());

        return result;
    }

    public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag, PlayerEntry playerEntry) {
        AsyncEditSession result = new AsyncEditSession(m_parent, playerEntry,
                m_eventBus, world, maxBlocks, blockBag,
                new EditSessionEvent(world, getPlayer(playerEntry), maxBlocks, null));

        m_parent.getPlotMeFix().setMask(playerEntry.getPlayer());

        return result;
    }

    public ThreadSafeEditSession getThreadSafeEditSession(World world, int maxBlocks) {
        return new ThreadSafeEditSession(m_parent, PlayerEntry.UNKNOWN,
                m_eventBus, world, maxBlocks, null,
                new EditSessionEvent(world, null, maxBlocks, null));
    }

    public ThreadSafeEditSession getThreadSafeEditSession(World world, int maxBlocks,
            Player player) {
        PlayerEntry entry = getPlayerEntry(player);
        ThreadSafeEditSession result = new ThreadSafeEditSession(m_parent, entry,
                m_eventBus, world, maxBlocks, null,
                new EditSessionEvent(world, player, maxBlocks, null));
        m_parent.getPlotMeFix().setMask(entry.getPlayer());

        return result;
    }

    public ThreadSafeEditSession getThreadSafeEditSession(World world, int maxBlocks,
            BlockBag blockBag) {
        return new ThreadSafeEditSession(m_parent, PlayerEntry.UNKNOWN,
                m_eventBus, world, maxBlocks, blockBag,
                new EditSessionEvent(world, null, maxBlocks, null));
    }

    public ThreadSafeEditSession getThreadSafeEditSession(World world, int maxBlocks,
            BlockBag blockBag, Player player) {
        PlayerEntry entry = getPlayerEntry(player);
        ThreadSafeEditSession result = new ThreadSafeEditSession(m_parent, entry,
                m_eventBus, world, maxBlocks, blockBag,
                new EditSessionEvent(world, player, maxBlocks, null));
        m_parent.getPlotMeFix().setMask(entry.getPlayer());

        return result;
    }

    public ThreadSafeEditSession getThreadSafeEditSession(World world, int maxBlocks,
            BlockBag blockBag, PlayerEntry playerEntry) {
        
        ThreadSafeEditSession result = new ThreadSafeEditSession(m_parent, playerEntry,
                m_eventBus, world, maxBlocks, blockBag,
                new EditSessionEvent(world, getPlayer(playerEntry), maxBlocks, null));

        m_parent.getPlotMeFix().setMask(playerEntry.getPlayer());

        return result;
    }
}
