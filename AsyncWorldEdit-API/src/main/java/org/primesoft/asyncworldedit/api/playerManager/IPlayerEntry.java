/*
 * AsyncWorldEdit API
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit API contributors
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
package org.primesoft.asyncworldedit.api.playerManager;

import java.util.UUID;
import org.primesoft.asyncworldedit.api.MessageSystem;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.permissions.IPermission;

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
     * Is undo disabled
     * @return 
     */
    boolean isUndoOff();

    /**
     * Get the player name
     * @return 
     */
    String getName();

    /**
     * Get the permission group
     * @return
     */
    IPermissionGroup getPermissionGroup();

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
     * @return is the player allowed this permission
     */
    boolean isAllowed(IPermission permission);
    

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
     * Set player Undo mode
     * @param mode 
     */
    void setUndoMode(boolean mode);

    
    /**
     * Update the player after relogin
     * @param player 
     */
    void update(IPlayerEntry player);
    
    /**
     * Update the permission group
     */
    void updatePermissionGroup();
    
    
    /**
     * Update the messaging system
     * @param system
     * @param state 
     */
    void setMessaging(MessageSystem system, boolean state);
    
    /**
     * Get the messaging system status
     * @param system
     * @return 
     */
    boolean getMessaging(MessageSystem system);

    /**
     * Get the number of blocks rendered each run
     * @return 
     */
    int getRenderBlocks();
    
    /**
     * Set the number of rendered blocks
     * @param b The new speed (null for default)
     */
    void setRenderBlocks(Integer b);
    
    
    /**
     * Is this a fake player entry
     * @return 
     */
    boolean isFake();    
}
