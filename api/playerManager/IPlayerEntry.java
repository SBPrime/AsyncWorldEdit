/*
 * AsyncWorldEdit Premium is a commercial version of AsyncWorldEdit. This software 
 * has been sublicensed by the software original author according to p7 of
 * AsyncWorldEdit license.
 *
 * AsyncWorldEdit Premium - donation version of AsyncWorldEdit, a performance 
 * improvement plugin for Minecraft WorldEdit plugin.
 *
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
 *
 * All rights reserved.
 *
 * 1. You may: 
 *    install and use AsyncWorldEdit in accordance with the Software documentation
 *    and pursuant to the terms and conditions of this license
 * 2. You may not:
 *    sell, redistribute, encumber, give, lend, rent, lease, sublicense, or otherwise
 *    transfer Software, or any portions of Software, to anyone without the prior 
 *    written consent of Licensor
 * 3. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 4. The original author of the software is allowed to sublicense the software 
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
package org.primesoft.asyncworldedit.api.playerManager;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.configuration.PermissionGroup;
import org.primesoft.asyncworldedit.permissions.Permission;

/**
 *
 * @author SBPrime
 */
public interface IPlayerEntry {

    /**
     * DIspose the player entry
     */
    void dispose();

    /**
     * Is AWE enabled
     * @return 
     */
    boolean getAweMode();

    /**
     * Get the player name
     * @return 
     */
    String getName();

    /**
     * Get the permission group
     * @return 
     */
    PermissionGroup getPermissionGroup();

    /**
     * Get the wrapped player
     * @return 
     */
    Player getPlayer();

    /**
     * Get the player UUID
     * @return 
     */
    UUID getUUID();

    /**
     * Get the wait mutex
     *
     * @return
     */
    Object getWaitMutex();

    /**
     * Get the 
     * @param permission
     * @return 
     */
    boolean isAllowed(Permission permission);

    /**
     * Is this the console
     *
     * @return
     */
    boolean isConsole();

    /**
     * Is the entry disposed
     *
     * @return
     */
    boolean isDisposed();

    /**
     * Is the player in game
     * @return 
     */
    boolean isInGame();

    /**
     * Is this a player
     * @return 
     */
    boolean isPlayer();

    /**
     * Is this unknown
     * @return 
     */
    boolean isUnknown();

    /**
     * Sand message to the player chat
     * @param msg 
     */
    void say(String msg);

    /**
     * Set player AWE mode
     * @param mode 
     */
    void setAweMode(boolean mode);

    /**
     * Update the player after relogin
     *
     * @param player
     * @param permissionGroup
     */
    void update(Player player, PermissionGroup permissionGroup);
    
}
