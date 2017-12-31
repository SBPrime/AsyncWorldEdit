/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.worldedit.session;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.session.SessionOwner;
import java.lang.reflect.Field;
import java.util.Map;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.configuration.IWorldEditConfig;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.changesetSerializer.SerializableSessionList;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.worldedit.WrappedLocalSession;

/**
 *
 * @author SBPrime
 */
public class AsyncSessionManager extends SessionManager {

    /**
     * The history field
     */
    private static final Field s_fieldHistory = Reflection.findField(LocalSession.class, "history", "Unable to get LocalSession history field");
    
    /**
     * The stored local sessions
     */
    private static final Field s_fieldSessions = Reflection.findField(SessionManager.class, "sessions", "Unable to get SessionManager sessions field");
    
    /**
     * The player manager
     */
    private final IPlayerManager m_playerManager;
    
    //private final  m_userManager;

    public AsyncSessionManager(WorldEdit worldEdit, IAsyncWorldEdit api) {
        super(worldEdit);
        
        m_playerManager = api.getPlayerManager();
    }

    @Override
    public synchronized LocalSession findByName(String name) {
        return initializeSession(super.findByName(name), m_playerManager.getPlayer(name));
    }

    @Override
    public synchronized LocalSession getIfPresent(SessionOwner owner) {
        return initializeSession(super.getIfPresent(owner), getPlayer(owner));
    }

    @Override
    public synchronized LocalSession get(SessionOwner owner) {        
        LocalSession session = super.getIfPresent(owner);

        boolean isNew = false;
        
        if (session == null) {
            isNew = true;
            session = super.get(owner);
        }
        
        IPlayerEntry playerEntry = getPlayer(owner);

        if (playerEntry == null) {
            return initializeSession(session, m_playerManager.getUnknownPlayer());
        }
        
        IPermissionGroup perms = playerEntry.getPermissionGroup();
        IWorldEditConfig weConfig = perms != null ? perms.getWorldEditConfig() : null;
        if (weConfig == null) {
            return initializeSession(session, playerEntry);
        }

        if (isNew || owner.hasPermission("worldedit.limit.unrestricted")) {
            if (weConfig.getBlockChangeLimit() >= 0) {
                session.setBlockChangeLimit(weConfig.getBlockChangeLimit());
            }            
        }
        
        return initializeSession(session, playerEntry);
    }

    @Override
    public synchronized void remove(SessionOwner owner) {
        LocalSession current = getIfPresent(owner);
        
        super.remove(owner);
        
        if (current != null) {
            cleanupSession(current);
        }
    }

    @Override
    public synchronized void clear() {
        Map map = Reflection.get(this, Map.class, s_fieldSessions, "Unable to get SessionManager storage");
        Object[] values;
        
        if (map != null) {
            values = map.values().toArray();
        } else {
            values = new Object[0];
        }
        
        super.clear();
        
        for (Object o : values) {
            if (o instanceof LocalSession) {
                cleanupSession((LocalSession)o);
            }
        }
    }
        

    /**
     * Initialize the localSession
     *
     * @param localSession
     * @param owner The session owner
     * @return
     */
    private LocalSession initializeSession(LocalSession localSession, IPlayerEntry owner) {
        if (localSession == null) {
            return null;
        }
        
        LocalSession result = localSession;
        
        if (localSession instanceof WrappedLocalSession) {
            localSession = ((WrappedLocalSession)localSession).getParrent();
        }
        
        if (!(result instanceof WrappedLocalSession)) {
            WrappedLocalSession newSession = WrappedLocalSession.wrap(localSession);
            newSession.setOwner(owner);
            result = newSession;
        }
        
        final Object current = Reflection.get(localSession, s_fieldHistory, "Unable to get history value");

        if (current instanceof SerializableSessionList) {
            return result;
        }

        Reflection.set(localSession, s_fieldHistory, new SerializableSessionList(), "Unable to inject history serializer");
        
        return result;
    }

    
    /**
     * Cleanup the removed local session data
     * @param localSession 
     */
    private void cleanupSession(LocalSession localSession) {
        if (localSession == null) {
            return;
        }
        
        if (localSession instanceof WrappedLocalSession) {
            localSession = ((WrappedLocalSession)localSession).getParrent();
        }
        
        final Object current = Reflection.get(localSession, s_fieldHistory, "Unable to get history value");

        if (!(current instanceof SerializableSessionList)) {
            return;
        }
        
        ((SerializableSessionList)current).clear();
    }

    
    /**
     * Get player from session owner
     * @param owner
     * @return 
     */
    private IPlayerEntry getPlayer(SessionOwner owner) {
        if (owner == null) {
            return m_playerManager.getUnknownPlayer();
        }
        
        SessionKey sessionKey = owner.getSessionKey();
        if (sessionKey == null) {
            return m_playerManager.getUnknownPlayer();
        }

        return m_playerManager.getPlayer(sessionKey.getUniqueId());
    }
}