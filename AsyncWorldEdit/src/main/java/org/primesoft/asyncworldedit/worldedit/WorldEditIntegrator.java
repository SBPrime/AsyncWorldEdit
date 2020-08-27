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
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.session.SessionOwner;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.World;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.inner.IWorldeditIntegratorInner;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.injector.injected.IWorldEdit;
import org.primesoft.asyncworldedit.worldedit.extension.factory.ExtendedBlockFactory;
import org.primesoft.asyncworldedit.worldedit.session.AsyncSessionManager;

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
    private BlockFactory m_oldBlockFactory;
    
    /**
     * Get the world edit event bus
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

        IWorldEdit we = (IWorldEdit)(Object)m_worldEdit; // Force caste

        we.setBlockFactory(new ExtendedBlockFactory(m_aweCore.getPlatform(), m_worldEdit, m_aweCore.getPlayerManager()));
        we.setEditSessionFactory(new AsyncEditSessionFactory(m_aweCore));
        we.setSessionManager(new AsyncSessionManager(m_worldEdit, m_aweCore));
    }
    
    /**
     * Stop the wrapper
     */
    @Override
    public void queueStop() {
        IWorldEdit we = (IWorldEdit) (Object) m_worldEdit; // Force caste

        we.setEditSessionFactory(m_oldEditSessionFactory);
        we.setSessionManager(m_oldSessions);
        we.setBlockFactory(m_oldBlockFactory);
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
    
    protected final void initializationDone() {
        m_aweCore.onWorldEditEnabled();
    }

    @Override
    public WorldEdit getWE() {
        return m_worldEdit;
    }
}
