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
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.configuration.PermissionGroup;
import org.primesoft.asyncworldedit.permissions.Permission;
import org.primesoft.asyncworldedit.permissions.PermissionManager;
import org.primesoft.asyncworldedit.strings.MessageType;

/**
 *
 * @author SBPrime
 */
public class PlayerEntry {

    public final static UUID UUID_CONSOLE = UUID.randomUUID();
    public final static UUID UUID_UNKNOWN = UUID.randomUUID();

    public final static PlayerEntry CONSOLE = new PlayerEntry(null, "<Console>", UUID_CONSOLE, PermissionGroup.getDefaultGroup(), true);
    public final static PlayerEntry UNKNOWN = new PlayerEntry(null, "<Unknown>", UUID_UNKNOWN, PermissionGroup.getDefaultGroup(), false);

    private Player m_player;
    private String m_name;
    private final UUID m_uuid;
    private boolean m_mode;
    private PermissionGroup m_group;
    private final boolean m_canTalk;

    public PlayerEntry(Player player, String name, PermissionGroup group) {
        this(player, name, player.getUniqueId(), group, true);
    }

    public PlayerEntry(String name, UUID uuid) {
        this(null, name, uuid, PermissionGroup.getDefaultGroup(), false);
    }
    
    private PlayerEntry(Player player, String name, UUID uuid,
            PermissionGroup group, boolean canTalk) {
        m_canTalk = canTalk;
        m_group = group;
        m_player = player;
        m_uuid = uuid;
        m_name = name;
        m_mode = group.isOnByDefault();
    }

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
            AsyncWorldEditMain.sayConsole(msg);
        }
    }

    public Player getPlayer() {
        return m_player;
    }

    public UUID getUUID() {
        return m_uuid;
    }

    public String getName() {
        return m_name;
    }

    public boolean getMode() {
        return m_mode;
    }

    public void setMode(boolean mode) {
        if (mode == m_mode) {
            return;
        }

        m_mode = mode;

        say(MessageType.CMD_TOGGLE_MODE_CHANGED.format(mode ? 
                MessageType.CMD_TOGGLE_MODE_ON.format() : MessageType.CMD_TOGGLE_MODE_OFF.format()));

    }

    public boolean isAllowed(Permission permission) {
        return PermissionManager.isAllowed(m_player, permission);
    }

    /**
     * Is this player the console
     *
     * @return
     */
    public boolean isConsole() {
        return UUID_CONSOLE.equals(m_uuid);
    }

    public boolean isUnknown() {
        return UUID_UNKNOWN.equals(m_uuid);
    }

    public boolean isPlayer() {
        return m_player != null
                && !UUID_CONSOLE.equals(m_uuid)
                && !UUID_UNKNOWN.equals(m_uuid);
    }

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

    public PermissionGroup getPermissionGroup() {
        return m_group;
    }

    /**
     * Update the player after relogin
     *
     * @param player
     * @param permissionGroup
     */
    public void update(Player player, PermissionGroup permissionGroup) {
        m_player = player;
        m_group = permissionGroup;
    }
}
