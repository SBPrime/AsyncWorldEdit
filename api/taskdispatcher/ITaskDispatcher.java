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
package org.primesoft.asyncworldedit.api.taskdispatcher;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.World;
import org.primesoft.asyncworldedit.utils.Action;
import org.primesoft.asyncworldedit.utils.Func;

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
    void performSafeChunk(Object mutex, Action action, World world, Vector2D pos);

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
    <T> T performSafeChunk(Object mutex, Func<T> action, World world, Vector2D pos);

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param mutex
     * @param action
     * @param world
     * @param pos
     */
    void performSafe(Object mutex, Action action, World world, Vector pos);

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param mutex
     * @param action
     * @param world
     * @param region
     */
    void performSafe(Object mutex, Action action, World world, Region region);

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
    <T> T performSafe(Object mutex, Func<T> action, World world, Region region);

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
    <T> T performSafe(Object mutex, Func<T> action, World world, Vector pos);

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param mutex
     * @param action
     */
    void performSafe(Object mutex, Action action);

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param <T>
     * @param mutex
     * @param action
     * @return
     */
    <T> T performSafe(Object mutex, Func<T> action);

    /**
     * Set pause on task dispatcher placer
     *
     * @param pause
     */
    void setPause(boolean pause);

}
