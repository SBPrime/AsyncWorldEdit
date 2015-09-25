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

/**
 *
 * @author prime
 */
public interface IPlayerManager {
    /**
     * The console player UUID
     * @return 
     */
    UUID getUuidConsole();
    
    /**
     * The unknown player UUID
     * @return 
     */
    UUID getUuidUnknown();
    
    
    /**
     * Get the console player entry
     * @return 
     */
    IPlayerEntry getConsolePlayer();
    
    /**
     * Get the unknown player entry
     * @return 
     */
    IPlayerEntry getUnknownPlayer();
    
    
    /**
     * Create a new player entry (do not add to the manager)
     * @param player
     * @param name
     * @param group
     * @return 
     */
    IPlayerEntry createPlayer(Player player, String name, PermissionGroup group);
    
    /**
     * Create new player entry (do not add to the manager)
     * @param name
     * @param uuid
     * @return 
     */
    IPlayerEntry createPlayer(String name, UUID uuid);
    
    
    /**
     * Get the player wrapper based on bukkit player class (null = console)
     *
     * @param player
     * @return
     */
    IPlayerEntry getPlayer(Player player);

    /**
     * Get the player wrapper based on UUID
     *
     * @param playerUuid
     * @return NEver returns null
     */
    IPlayerEntry getPlayer(UUID playerUuid);

    /**
     * Gets player wrapper from player name
     *
     * @param playerName
     * @return never returns null
     */
    IPlayerEntry getPlayer(String playerName);
    
}
