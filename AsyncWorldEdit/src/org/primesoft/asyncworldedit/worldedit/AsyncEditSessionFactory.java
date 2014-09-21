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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.World;
import java.util.UUID;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.PlayerManager;
import org.primesoft.asyncworldedit.worldedit.entity.PlayerWrapper;

/**
 *
 * @author SBPrime
 */
public class AsyncEditSessionFactory extends EditSessionFactory {

    private final AsyncWorldEditMain m_parent;
    private final EventBus m_eventBus;
    private final PlayerManager m_playerManager;

    /**
     * Get the player UUID
     *
     * @param player
     * @return
     */
    private UUID getUUID(com.sk89q.worldedit.entity.Player player) {
        if (player instanceof BukkitPlayer) {
            return ((BukkitPlayer) player).getPlayer().getUniqueId();
        } else if (player instanceof PlayerWrapper) {
            return ((PlayerWrapper) player).getUUID();
        }

        return m_playerManager.getPlayerUUID(player.getName());
    }

    public AsyncEditSessionFactory(AsyncWorldEditMain parent, EventBus eventBus) {
        m_parent = parent;
        m_eventBus = eventBus;
        m_playerManager = parent.getPlayerManager();
    }

    @Override
    public EditSession getEditSession(World world, int maxBlocks) {
        return new AsyncEditSession(m_parent, ConfigProvider.DEFAULT_USER,
                m_eventBus, world, maxBlocks, null,
                new EditSessionEvent(world, null, maxBlocks, null));
    }

    @Override
    public EditSession getEditSession(World world, int maxBlocks, Player player) {
        UUID uuid = getUUID(player);
        AsyncEditSession result = new AsyncEditSession(m_parent, uuid,
                m_eventBus, world, maxBlocks, null,
                new EditSessionEvent(world, player, maxBlocks, null));
        m_parent.getPlotMeFix().setMask(AsyncWorldEditMain.getPlayer(uuid));

        return result;
    }

    @Override
    public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag) {
        return new AsyncEditSession(m_parent, ConfigProvider.DEFAULT_USER,
                m_eventBus, world, maxBlocks, blockBag,
                new EditSessionEvent(world, null, maxBlocks, null));
    }

    @Override
    public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag, 
            Player player) {
        UUID uuid = getUUID(player);
        AsyncEditSession result = new AsyncEditSession(m_parent, uuid,
                m_eventBus, world, maxBlocks, blockBag,
                new EditSessionEvent(world, player, maxBlocks, null));
        m_parent.getPlotMeFix().setMask(AsyncWorldEditMain.getPlayer(uuid));

        return result;
    }

    public ThreadSafeEditSession getThreadSafeEditSession(World world, int maxBlocks) {
        return new ThreadSafeEditSession(m_parent, ConfigProvider.DEFAULT_USER,
                m_eventBus, world, maxBlocks, null,
                new EditSessionEvent(world, null, maxBlocks, null));
    }

    public ThreadSafeEditSession getThreadSafeEditSession(World world, int maxBlocks, 
            Player player) {
        UUID uuid = getUUID(player);
        ThreadSafeEditSession result = new ThreadSafeEditSession(m_parent, uuid,
                m_eventBus, world, maxBlocks, null,
                new EditSessionEvent(world, player, maxBlocks, null));
        m_parent.getPlotMeFix().setMask(AsyncWorldEditMain.getPlayer(uuid));

        return result;
    }

    public ThreadSafeEditSession getThreadSafeEditSession(World world, int maxBlocks,
            BlockBag blockBag) {
        return new ThreadSafeEditSession(m_parent, ConfigProvider.DEFAULT_USER,
                m_eventBus, world, maxBlocks, blockBag,
                new EditSessionEvent(world, null, maxBlocks, null));
    }

    public ThreadSafeEditSession getThreadSafeEditSession(World world, int maxBlocks,
            BlockBag blockBag, Player player) {
        UUID uuid = getUUID(player);
        ThreadSafeEditSession result = new ThreadSafeEditSession(m_parent, uuid,
                m_eventBus, world, maxBlocks, blockBag,
                new EditSessionEvent(world, player, maxBlocks, null));
        m_parent.getPlotMeFix().setMask(AsyncWorldEditMain.getPlayer(uuid));

        return result;
    }
}
