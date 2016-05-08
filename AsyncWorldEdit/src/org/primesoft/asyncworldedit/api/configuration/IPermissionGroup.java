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
package org.primesoft.asyncworldedit.api.configuration;

/**
 *
 * @author SBPrime
 */
public interface IPermissionGroup {

    /**
     * Kill all player jobs on logout
     *
     * @return
     */
    boolean getCleanOnLogout();

    /**
     * Maximum number of concurrent jobs
     *
     * @return
     */
    int getMaxJobs();

    /**
     * The permission node
     *
     * @return
     */
    String getPermissionNode();

    /**
     * maximum size of the player block queue
     *
     * @return
     */
    int getQueueHardLimit();

    /**
     * number of blocks on the player queue when to stop placing blocks
     *
     * @return
     */
    int getQueueSoftLimit();

    /**
     * Number of blocks placed in each run
     *
     * @return
     */
    int getRendererBlocks();

    /**
     * Maximum number of miliseconds spend on placing blocks
     *
     * @return
     */
    int getRendererTime();

    /**
     * The minimum number of blocks to show the progress bar
     *
     * @return
     */
    int getBarApiProgresMinBlocks();

    /**
     * Use the bar api to display progress
     *
     * @return
     */
    boolean isBarApiProgressEnabled();

    /**
     * Is the undo disabled
     *
     * @return
     */
    boolean isUndoDisabled();

    /**
     * Use chat to display progress
     *
     * @return
     */
    boolean isChatProgressEnabled();

    /**
     * Is the group default
     *
     * @return
     */
    boolean isDefault();

    /**
     * The AWE mode when player logins
     *
     * @return
     */
    boolean isOnByDefault();

    /**
     * is async world edit talkative
     *
     * @return
     */
    boolean isTalkative();
    
    /**
     * The WorldEdit config
     * @return 
     */
    IWorldEditConfig getWorldEditConfig();
}
