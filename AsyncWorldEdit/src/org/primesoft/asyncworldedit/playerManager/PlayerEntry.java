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
package org.primesoft.asyncworldedit.playerManager;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.AsyncWorldEditBukkit;
import static org.primesoft.asyncworldedit.AsyncWorldEditBukkit.log;
import org.primesoft.asyncworldedit.api.MessageSystem;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.permissions.IPermission;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.configuration.PermissionGroup;
import org.primesoft.asyncworldedit.permissions.PermissionManager;
import org.primesoft.asyncworldedit.strings.MessageType;

/**
 *
 * @author SBPrime
 */
public class PlayerEntry implements IPlayerEntry {

    private Player m_player;
    private String m_name;
    private final UUID m_uuid;
    private boolean m_mode;
    private IPermissionGroup m_group;
    private final boolean m_canTalk;
    private boolean m_isDisposed;

    public PlayerEntry(Player player) {
        this(player, player.getName(), PermissionGroup.getDefaultGroup());
    }
    
    PlayerEntry(Player player, String name, IPermissionGroup group) {
        this(player, name, player.getUniqueId(), group, true);
    }

    protected PlayerEntry(Player player, String name, UUID uuid,
            IPermissionGroup group, boolean canTalk) {
        m_canTalk = canTalk;
        m_group = group;
        m_player = player;
        m_uuid = uuid;
        m_name = name;
        m_mode = group.isOnByDefault();
    }

    @Override
    public void say(String msg) {
        if (msg == null) {
            return;
        }
        if (m_player != null) {
            if (m_player.isOnline()) {
                m_player.sendRawMessage(msg);
            }
            return;
        }

        if (m_canTalk) {
            AsyncWorldEditBukkit.sayConsole(msg);
        }
    }

    public Player getPlayer() {
        return m_player;
    }

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
    public void setAweMode(boolean mode) {
        if (mode == m_mode) {
            return;
        }

        m_mode = mode;

        say(MessageType.CMD_TOGGLE_MODE_CHANGED.format(mode
                ? MessageType.CMD_TOGGLE_MODE_ON.format() : MessageType.CMD_TOGGLE_MODE_OFF.format()));

    }

    @Override
    public boolean isAllowed(IPermission permission) {
        return PermissionManager.isAllowed(m_player, permission);
    }

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
    public boolean isPlayer() {
        return m_player != null
                && !PlayerManager.UUID_CONSOLE.equals(m_uuid)
                && !PlayerManager.UUID_UNKNOWN.equals(m_uuid);
    }

    @Override
    public boolean isInGame() {
        return isPlayer() && m_player.isOnline();
    }

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
     * Set the permission group
     * @param permissionGroup 
     */
    protected void setPermissionGroup(IPermissionGroup permissionGroup) {
        m_group = permissionGroup;
    }

    
    /**
     * Update the player after relogin
     *
     * @param player
     */
    @Override
    public void update(IPlayerEntry player) {
        if (!(player instanceof PlayerEntry)) {
            return;
        }

        setPermissionGroup(player.getPermissionGroup());
        m_player = ((PlayerEntry) player).getPlayer();
    }

    @Override
    public void updatePermissionGroup() {
        setPermissionGroup(PermissionManager.getPermissionGroup(m_player));
    }

    @Override
    public void dispose() {
        m_isDisposed = true;
    }

    @Override
    public boolean isUndoOff() {
        return false;
    }

    @Override
    public Object getWaitMutex() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDisposed() {
        return m_isDisposed;
    }

    @Override
    public void setUndoMode(boolean mode) {
        log("******************************************************************************");
        log("******************************************************************************");
        log("**                                                                          **");
        log("** Undo disable is not available for this version of the plugin             **");
        log("**                                                                          **");
        log("******************************************************************************");
        log("******************************************************************************");
    }

    @Override
    public void setMessaging(MessageSystem system, boolean state) {
        log("******************************************************************************");
        log("******************************************************************************");
        log("**                                                                          **");
        log("** Messagin system changes are not available for this version of the plugin **");
        log("**                                                                          **");
        log("******************************************************************************");
        log("******************************************************************************");
    }

    @Override
    public boolean getMessaging(MessageSystem system) {
        return true;
    }

    @Override
    public int getRenderBlocks() {
        log("******************************************************************************");
        log("******************************************************************************");
        log("**                                                                          **");
        log("** Render block per player are not available for this version of the plugin **");
        log("**                                                                          **");
        log("******************************************************************************");
        log("******************************************************************************");

        return 0;
    }

    @Override
    public void setRenderBlocks(Integer b) {
        log("******************************************************************************");
        log("******************************************************************************");
        log("**                                                                          **");
        log("** Render block per player are not available for this version of the plugin **");
        log("**                                                                          **");
        log("******************************************************************************");
        log("******************************************************************************");
    }

    @Override
    public boolean isFake() {
        return false;
    }
}
