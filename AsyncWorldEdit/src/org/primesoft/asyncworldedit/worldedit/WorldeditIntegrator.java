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

import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.CommandManager;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.world.World;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.AsyncWorldEditBukkit;
import static org.primesoft.asyncworldedit.AsyncWorldEditBukkit.log;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.IWorldeditIntegrator;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.platform.bukkit.BukkitWorld;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.worldedit.util.command.DispatcherWrapper;

/**
 *
 * @author SBPrime
 */
public class WorldeditIntegrator implements IWorldeditIntegrator {

    /**
     * The original world edit
     */
    private WorldEdit m_worldEdit;

    /**
     * The parent plugin
     */
    private final AsyncWorldEditBukkit m_parent;

    private EditSessionFactory m_oldEditSessionFactory;
    private SessionManager m_oldSessions;
    private PlatformManager m_platformManager;
    private CommandManager m_commandManager;
    private Dispatcher m_oldDispatcher;
    private WorldEditPlugin m_worldEditPlugin;

    /**
     * Create new instance of world edit integration checker and start it
     *
     * @param plugin
     * @param worldEditPlugin
     */
    public WorldeditIntegrator(AsyncWorldEditBukkit plugin, WorldEditPlugin worldEditPlugin) {
        m_parent = plugin;
        if (m_parent == null) {
            return;
        }

        if (worldEditPlugin == null) {
            log("Error initializeing Worldedit integrator");
            return;
        }

        m_worldEditPlugin = worldEditPlugin;
        m_worldEdit = worldEditPlugin.getWorldEdit();

        m_oldEditSessionFactory = m_worldEdit.getEditSessionFactory();
        m_oldSessions = m_worldEdit.getSessionManager();

        Reflection.set(m_worldEdit, "editSessionFactory", new AsyncEditSessionFactory(m_parent, m_worldEdit.getEventBus()),
                "Unable to inject edit session factory");

        m_platformManager = m_worldEdit.getPlatformManager();
        m_commandManager = m_platformManager.getCommandManager();

        m_oldDispatcher = Reflection.get(m_commandManager, Dispatcher.class,
                "dispatcher", "Unable to get the dispatcher");
        if (m_oldDispatcher != null) {
            Reflection.set(m_commandManager, "dispatcher", new DispatcherWrapper(m_oldDispatcher),
                    "Unable to inject new commands manager");
        }
    }

    /**
     * Stop the wrapper
     */
    public void queueStop() {
        Reflection.set(m_commandManager, "dispatcher", m_oldDispatcher,
                "Unable to restore dispatcher");

        Reflection.set(m_worldEdit, "editSessionFactory", m_oldEditSessionFactory, "Unable to restore edit session factory");
        Reflection.set(m_worldEdit, "sessions", m_oldSessions, "Unable to restore sessions");
    }

    @Override
    public com.sk89q.worldedit.entity.Player wrapPlayer(IPlayerEntry player) {
        if (!(player instanceof PlayerEntry)) {
            return null;
        }

        Player bPlayer = ((PlayerEntry) player).getPlayer();
        if (bPlayer == null) {
            return null;
        }

        return m_worldEditPlugin.wrapPlayer(bPlayer);

    }

    @Override
    public World getWorld(IWorld world) {
        if (!(world instanceof BukkitWorld)) {
            return null;
        }

        return new com.sk89q.worldedit.bukkit.BukkitWorld(((BukkitWorld) world).getWorld());
    }

    @Override
    public IWorld getWorld(World world) {
        if (world == null) {
            return null;
        }

        if (!(world instanceof com.sk89q.worldedit.bukkit.BukkitWorld)) {
            return m_parent.getWorld(world.getName());
        }

        com.sk89q.worldedit.bukkit.BukkitWorld bWorld = (com.sk89q.worldedit.bukkit.BukkitWorld) world;

        return new BukkitWorld(bWorld.getWorld());
    }
}
