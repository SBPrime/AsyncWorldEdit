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
package org.primesoft.asyncworldedit.platform.bukkit;

import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.api.permissions.IPermission;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.permissions.PermissionManager;
import org.primesoft.asyncworldedit.platform.api.IPermissionProvider;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;

/**
 *
 * @author SBPrime
 */
public class BukkitPlayerEntry extends PlayerEntry {
    private Player m_player;
    private final IPermissionProvider m_permissionProvider;

    BukkitPlayerEntry(Player player) {
        this(player, new BukkitPermissionChecker(player));
    }
    
    private BukkitPlayerEntry(Player player, IPermissionProvider permissionProvider) {
        super(player.getName(), player.getUniqueId(), PermissionManager.getPermissionGroup(permissionProvider));
        
        m_permissionProvider = permissionProvider;
        m_player = player;
    }

    @Override
    public boolean isFake() {
        return false;
    }

    /**
     * Get the wrapped player
     *
     * @return
     */
    public Player getPlayer() {
        return m_player;
    }

    @Override
    protected boolean sendRawMessage(String msg) {
        if (m_player == null) {
            return false;
        }

        if (m_player.isOnline()) {
            m_player.sendRawMessage(msg);
        }

        return true;
    }

    @Override
    public boolean isAllowed(IPermission permission) {
        return m_permissionProvider.hasPermission(permission.getNode());
    }


    @Override
    public boolean isPlayer() {
        return m_player != null;
    }

    @Override
    public boolean isInGame() {
        return m_player != null && m_player.isOnline();
    }

    /**
     * Update the player after relogin
     *
     * @param player
     */
    @Override
    public void update(IPlayerEntry player) {
        if (!(player instanceof BukkitPlayerEntry)) {
            return;
        }

        setPermissionGroup(player.getPermissionGroup());
        m_player = ((BukkitPlayerEntry) player).getPlayer();
    }

    @Override
    public void updatePermissionGroup() {
        setPermissionGroup(PermissionManager.getPermissionGroup(m_permissionProvider));
    }
}
