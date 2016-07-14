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
package org.primesoft.asyncworldedit.api.taskdispatcher;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.Region;
import java.util.Collection;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.utils.IAction;
import org.primesoft.asyncworldedit.api.utils.IFunc;

/**
 *
 * @author SBPrime
 */
public interface ITaskDispatcher {

    /**
     * Add new fast/simple task (high priority tasks!)
     *
     * @param entry
     */
    void addFastTask(IDispatcherEntry entry);

    /**
     * Is this thread the main bukkit thread
     *
     * @return
     */
    boolean isMainTask();

    /**
     * Is the task dispatcher paused
     *
     * @return
     */
    boolean isPaused();

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param mutex
     * @param action
     * @param world
     * @param pos
     */
    void performSafeChunk(Object mutex, IAction action, IWorld world, Vector2D pos);

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param <T>
     * @param mutex
     * @param action
     * @param world
     * @param pos
     * @return
     */
    <T> T performSafeChunk(Object mutex, IFunc<T> action, IWorld world, Vector2D pos);
    
    
    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param mutex
     * @param action
     * @param world
     * @param chunks
     */
    void performSafeChunk(Object mutex, IAction action, IWorld world, Collection<BlockVector2D> chunks);

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param <T>
     * @param mutex
     * @param action
     * @param world
     * @param chunks
     * @return
     */
    <T> T performSafeChunk(Object mutex, IFunc<T> action, IWorld world, Collection<BlockVector2D> chunks);
    

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param mutex
     * @param action
     * @param world
     * @param pos
     */
    void performSafe(Object mutex, IAction action, IWorld world, Vector pos);

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param mutex
     * @param action
     * @param world
     * @param region
     */
    void performSafe(Object mutex, IAction action, IWorld world, Region region);

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param <T>
     * @param mutex
     * @param action
     * @param world
     * @param region
     * @return
     */
    <T> T performSafe(Object mutex, IFunc<T> action, IWorld world, Region region);

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param <T>
     * @param mutex
     * @param action
     * @param world
     * @param pos
     * @return
     */
    <T> T performSafe(Object mutex, IFunc<T> action, IWorld world, Vector pos);
    
    /**
     * Perform an action on the dispatcher
     * @param action
     */    
    void queueFastOperation(IAction action);
    
    /**
     * Perform an action on the dispatcher
     * @param action
     */    
    <T> T queueFastOperation(IFunc<T> action);
    

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param mutex
     * @param action
     */
    void performSafe(Object mutex, IAction action);

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param <T>
     * @param mutex
     * @param action
     * @return
     */
    <T> T performSafe(Object mutex, IFunc<T> action);

    /**
     * Set pause on task dispatcher placer
     *
     * @param pause
     */
    void setPause(boolean pause);

}
