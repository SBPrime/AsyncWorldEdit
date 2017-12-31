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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.factory.BlockFactory;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.CommandManager;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.session.SessionOwner;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.World;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.inner.IWorldeditIntegratorInner;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.excommands.CommandsInjector;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.worldedit.extension.factory.ExtendedBlockFactory;
import org.primesoft.asyncworldedit.worldedit.session.AsyncSessionManager;
import org.primesoft.asyncworldedit.worldedit.util.command.DispatcherWrapper;

/**
 *
 * @author SBPrime
 */
public abstract class WorldEditIntegrator implements IWorldeditIntegratorInner {
    
    /**
     * The AWE core
     */
    private final IAsyncWorldEditCore m_aweCore;
    
    /**
     * The original world edit
     */
    private WorldEdit m_worldEdit;
    
    private EditSessionFactory m_oldEditSessionFactory;
    private SessionManager m_oldSessions;
    private PlatformManager m_platformManager;
    private CommandManager m_commandManager;
    private Dispatcher m_oldDispatcher;
    private BlockFactory m_oldBlockFactory;
    
    /**
     * Get the world edit event bus
     * @return 
     */
    @Override
    public EventBus getEventBus() {
        WorldEdit we = m_worldEdit;
        if (we == null) {
            return null;
        }
        
        return we.getEventBus();
    }
    
    /**
     * Get the WorldEdit configuration
     * @return 
     */
    @Override
    public LocalConfiguration getConfiguration() {
        return m_worldEdit != null ? m_worldEdit.getConfiguration() : null;
    }
    
    public WorldEditIntegrator(IAsyncWorldEditCore aweCore) {
        m_aweCore = aweCore;
    }
    
    
    protected void initialize(WorldEdit worldEdit) {
        m_worldEdit = worldEdit;
        
        m_oldBlockFactory = m_worldEdit.getBlockFactory();
        m_oldEditSessionFactory = m_worldEdit.getEditSessionFactory();
        m_oldSessions = m_worldEdit.getSessionManager();

        Reflection.set(m_worldEdit, "blockFactory", new ExtendedBlockFactory(m_aweCore.getPlatform(), m_worldEdit, m_aweCore.getPlayerManager()),
                "Unable to set new block factory");

        Reflection.set(m_worldEdit, "editSessionFactory", new AsyncEditSessionFactory(m_aweCore, m_worldEdit.getEventBus()),
                "Unable to inject edit session factory");

        Reflection.set(m_worldEdit, "sessions", new AsyncSessionManager(m_worldEdit, m_aweCore), "Unable to set new sessions manager");

        m_platformManager = m_worldEdit.getPlatformManager();
        m_commandManager = m_platformManager.getCommandManager();

        m_oldDispatcher = Reflection.get(m_commandManager, Dispatcher.class,
                "dispatcher", "Unable to get the dispatcher");
        if (m_oldDispatcher != null) {
            Reflection.set(m_commandManager, "dispatcher", new DispatcherWrapper(m_oldDispatcher),
                    "Unable to inject new commands manager");
        }

        CommandsInjector.injectCommands(m_worldEdit, m_aweCore,
                findMostPreferred(Capability.USER_COMMANDS, m_platformManager.getPlatforms()),
                m_commandManager);
    }
    
    /**
     * Find the most preferred platform for a given capability from the list of
     * platforms. This does not use the map of preferred platforms.
     *
     * @param capability the capability
     * @return the most preferred platform, or null if no platform was found
     */
    private synchronized @Nullable
    Platform findMostPreferred(Capability capability, List<Platform> platforms) {
        Platform preferred = null;
        Preference highest = null;

        for (Platform platform : platforms) {
            Preference preference = platform.getCapabilities().get(capability);
            if (preference != null && (highest == null || preference.isPreferredOver(highest))) {
                preferred = platform;
                highest = preference;
            }
        }

        return preferred;
    }

    
    
    /**
     * Stop the wrapper
     */
    @Override
    public void queueStop() {
        Reflection.set(m_commandManager, "dispatcher", m_oldDispatcher,
                "Unable to restore dispatcher");

        Reflection.set(m_worldEdit, "editSessionFactory", m_oldEditSessionFactory, "Unable to restore edit session factory");
        Reflection.set(m_worldEdit, "sessions", m_oldSessions, "Unable to restore sessions");
        Reflection.set(m_worldEdit, "blockFactory", m_oldBlockFactory, "Unable to restore block factory");
    }

    
    
    @Override
    public void removeSession(final IPlayerEntry player) {
        if (player == null) {
            return;
        }

        final SessionManager sManager = m_worldEdit != null ? m_worldEdit.getSessionManager() : null;

        if (sManager == null) {
            return;
        }

        sManager.remove(new SessionOwner() {

            @Override
            public SessionKey getSessionKey() {
                return new SessionKey() {

                    @Override
                    public String getName() {
                        return player.getName();
                    }

                    @Override
                    public boolean isActive() {
                        return false;
                    }

                    @Override
                    public boolean isPersistent() {
                        return false;
                    }

                    @Override
                    public UUID getUniqueId() {
                        return player.getUUID();
                    }
                };
            }

            @Override
            public String[] getGroups() {
                return new String[0];
            }

            @Override
            public void checkPermission(String permission) throws AuthorizationException {
            }

            @Override
            public boolean hasPermission(String permission) {
                return true;
            }
        });
    }

    @Override
    public abstract Player wrapPlayer(IPlayerEntry player);
    
    @Override
    public abstract World getWorld(IWorld world);

    @Override
    public IWorld getWorld(World world) {
        if (world == null) {
            return null;
        }
        
        return m_aweCore.getWorld(world.getName());
    }
}
