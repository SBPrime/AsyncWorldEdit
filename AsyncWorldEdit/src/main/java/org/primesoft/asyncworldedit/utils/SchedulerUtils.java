/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import org.primesoft.asyncworldedit.platform.api.IScheduler;
import org.primesoft.asyncworldedit.platform.api.ITask;

/**
 * Class helping in task starting
 *
 * @author SBPrime
 */
public class SchedulerUtils {

    /**
     * Run task asynchronously
     *
     * @param scheduler the scheduler
     * @param task the task to be run
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalArgumentException if task is null
     * @throws IllegalArgumentException if scheduler is null
     */
    public static void runTaskAsynchronously(final IScheduler scheduler,
            final BukkitRunnable task) {
        if (scheduler == null) {
            throw new IllegalArgumentException("scheduler");
        }
        if (task == null) {
            throw new IllegalArgumentException("task");
        }

        ITask bTask = scheduler.runTaskAsynchronously(task);
        task.setTask(bTask);
    }

    
    /**
     * The task queue
     */
    private static final HashMap<Object, Queue<BukkitRunnable>> s_taskQueue = new LinkedHashMap<Object, Queue<BukkitRunnable>>();
    
    /**
     * Run task asynchronously one after another
     *
     * @param scheduler the scheduler
     * @param task the task to be run
     * @param sequenceKey
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalArgumentException if task is null
     * @throws IllegalArgumentException if scheduler is null
     */
    public static void runTaskAsynchronouslyInSequence(IScheduler scheduler,
            final BukkitRunnable task, final Object sequenceKey) {
        if (sequenceKey == null) {
            runTaskAsynchronously(scheduler, task);
            return;
        }

        if (scheduler == null) {
            throw new IllegalArgumentException("scheduler");
        }
        if (task == null) {
            throw new IllegalArgumentException("task");
        }
        
        synchronized (s_taskQueue) {
            if (s_taskQueue.containsKey(sequenceKey)) {
                final Queue<BukkitRunnable> queue = s_taskQueue.get(sequenceKey);
                synchronized(queue) {
                    queue.add(task);
                }
            } else {
                final Queue<BukkitRunnable> queue = new LinkedList<BukkitRunnable>();
                queue.add(task);
                
                s_taskQueue.put(sequenceKey, queue);
                
                final BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        SchedulerUtils.processTaskQueue(sequenceKey);                        
                        super.run(); 
                    }                  
                };
                
                runTaskAsynchronously(scheduler, runnable);                
            }
        }
    }

    /**
     * Process the task queue
     * @param sequenceKey 
     */
    private static void processTaskQueue(Object sequenceKey) {
        final Queue<BukkitRunnable> queue;
        synchronized (sequenceKey) {
            if (!s_taskQueue.containsKey(sequenceKey)) {
                return;
            }
            
            queue = s_taskQueue.get(sequenceKey);
        }
        
        do {
            BukkitRunnable task;
            synchronized (s_taskQueue) {
                synchronized (queue) {
                    if (queue.isEmpty()) {
                        s_taskQueue.remove(sequenceKey);
                        return;
                    }
                    
                    task = queue.poll();
                }
            }
            
            task.run();
        } while (true);
    }
}