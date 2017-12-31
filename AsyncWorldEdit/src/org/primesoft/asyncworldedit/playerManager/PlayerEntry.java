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

import java.util.HashMap;
import java.util.Map;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import java.util.UUID;
import static org.primesoft.asyncworldedit.LoggerProvider.sayConsole;
import org.primesoft.asyncworldedit.api.MessageSystem;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.permissions.IPermission;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.configuration.PermissionGroup;
import org.primesoft.asyncworldedit.core.AwePlatform;
import org.primesoft.asyncworldedit.events.BlockRenderCountEvent;
import org.primesoft.asyncworldedit.strings.MessageType;

/**
 *
 * @author SBPrime
 */
public abstract class PlayerEntry implements IPlayerEntry {
    private String m_name;
    private final UUID m_uuid;
    private boolean m_mode;
    private boolean m_undoDisabled;
    private boolean m_isDisposed;
    private IPermissionGroup m_group;
    private final boolean m_canTalk;
    private long m_lastMessageTime = 0;
    private String m_lastMessage = null;
    private Integer m_rendererBlocks = null;
    private final Map<MessageSystem, Boolean> m_messageSystemOverride = new HashMap<MessageSystem, Boolean>();

    /**
     * The wait mutex
     */
    private final Object m_waitMutex = new Object();


    protected PlayerEntry(String name, UUID uuid, IPermissionGroup group) {
        this(name, uuid, group, false);
    }
    
    protected PlayerEntry(String name, UUID uuid) {
        this(name, uuid, PermissionGroup.getDefaultGroup(), false);
    }

    protected PlayerEntry(String name, UUID uuid,
            IPermissionGroup group, boolean canTalk) {
        m_canTalk = canTalk;
        m_group = group;
        m_uuid = uuid;
        m_name = name;
        m_mode = group.isOnByDefault();
        m_undoDisabled = group.isUndoDisabled();
    }

    /**
     * Set the permission group
     * @param permissionGroup 
     */
    protected void setPermissionGroup(IPermissionGroup permissionGroup) {
        m_group = permissionGroup;
    }
    
    /**
     * Is the entry disposed
     *
     * @return
     */
    @Override
    public boolean isDisposed() {
        return m_isDisposed;
    }

    /**
     * DIspose the player entry
     */
    @Override
    public void dispose() {
        m_isDisposed = true;
    }

    @Override
    public void say(String msg) {
        if (msg == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - m_lastMessageTime < ConfigProvider.renderer().getQueueTalkCooldown()
                && msg.equals(m_lastMessage)) {
            return;
        }

        m_lastMessage = msg;
        m_lastMessageTime = now;

        if (sendRawMessage(msg)) {
            return;
        }
        

        if (m_canTalk) {
            sayConsole(msg);
        }
    }
    
    
    /**
     * Send raw message to player
     * @param msg
     * @return 
     */
    protected abstract boolean sendRawMessage(String msg);

    @Override
    public UUID getUUID() {
        return m_uuid;
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public boolean getAweMode() {
        return m_mode;
    }
    
    
    @Override
    public boolean isUndoOff() {
        return m_undoDisabled;
    }

    @Override
    public void setAweMode(boolean mode) {
        if (mode == m_mode) {
            return;
        }

        m_mode = mode;

        say(MessageType.CMD_TOGGLE_MODE_CHANGED.format(mode
                ? MessageType.CMD_TOGGLE_MODE_ON.format() : MessageType.CMD_TOGGLE_MODE_OFF.format()));

    }

    @Override
    public void setUndoMode(boolean mode) {
        mode = !mode;
        if (mode == m_undoDisabled) {
            return;
        }

        m_undoDisabled = mode;

        say(MessageType.CMD_UNDO_MODE_CHANGED.format(!mode
                ? MessageType.CMD_UNDO_MODE_ON.format() : MessageType.CMD_UNDO_MODE_OFF.format()));

    }

    @Override
    public abstract boolean isAllowed(IPermission permission);

    /**
     * Is this player the console
     *
     * @return
     */
    @Override
    public boolean isConsole() {
        return PlayerManager.UUID_CONSOLE.equals(m_uuid);
    }

    @Override
    public boolean isUnknown() {
        return PlayerManager.UUID_UNKNOWN.equals(m_uuid);
    }

    @Override
    public abstract boolean isPlayer();

    @Override
    public abstract boolean isInGame();

    @Override
    public int hashCode() {
        return m_uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlayerEntry other = (PlayerEntry) obj;

        if (this.m_uuid != other.m_uuid && (this.m_uuid == null || !this.m_uuid.equals(other.m_uuid))) {
            return false;
        }
        return true;
    }

    @Override
    public IPermissionGroup getPermissionGroup() {
        return m_group;
    }

    /**
     * Update the player after relogin
     *
     * @param player
     */
    @Override
    public abstract void update(IPlayerEntry player);

    @Override
    public abstract void updatePermissionGroup();
    

    /**
     * Get the wait mutex
     *
     * @return
     */
    @Override
    public Object getWaitMutex() {
        return m_waitMutex;
    }

    @Override
    public void setMessaging(MessageSystem system, boolean state) {
        m_messageSystemOverride.put(system, state);
    }

    @Override
    public boolean getMessaging(MessageSystem system) {
        if (m_messageSystemOverride.containsKey(system)) {
            return m_messageSystemOverride.get(system);
        }

        switch (system) {
            default:
                return false;
            case BAR:
                return m_group.isBarApiProgressEnabled();
            case CHAT:
                return m_group.isChatProgressEnabled();
            case TALKATIVE:
                return m_group.isTalkative();
        }
    }

    @Override
    public int getRenderBlocks() {
        Integer tmp = m_rendererBlocks;
        if (tmp == null) {
            return m_group.getRendererBlocks();
        }

        return tmp;
    }

    @Override
    public void setRenderBlocks(Integer b) {
        Integer old = m_rendererBlocks;
        
        if (b == null || b > m_group.getRendererBlocks()) {
            m_rendererBlocks = null;
        } else {
            m_rendererBlocks = b;
        }
        
        AwePlatform.getInstance().getCore().getEventBus().post(new BlockRenderCountEvent(this, old, b));
    }

    @Override
    public abstract boolean isFake();    
    
}
