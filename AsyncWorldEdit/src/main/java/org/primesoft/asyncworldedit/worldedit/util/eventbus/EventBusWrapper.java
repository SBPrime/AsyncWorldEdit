/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2018, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.worldedit.util.eventbus;

import com.google.common.collect.Multimap;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import org.primesoft.asyncworldedit.injector.injected.util.eventbus.IDispatchableEventBus;
import org.primesoft.asyncworldedit.injector.injected.util.eventbus.IEventBus;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
public class EventBusWrapper implements IEventBus {
    private final IDispatchableEventBus m_target;
    
    public EventBusWrapper(IDispatchableEventBus target) {
        m_target = target;
    }

    @Override
    public void post(Object event) {
        m_target.nonwrapped_post(event);
    }

    @Override
    public void register(Object object) {
        m_target.nonwrapped_register(object);
    }

    @Override
    public synchronized void subscribe(Class<?> clazz, EventHandler handler) {
        m_target.nonwrapped_subscribe(clazz, handler);
    }

    @Override
    public synchronized void subscribeAll(Multimap<Class<?>, EventHandler> handlers) {
        m_target.nonwrapped_subscribeAll(handlers);
    }

    @Override
    public void unregister(Object object) {
        m_target.nonwrapped_unregister(object);
    }

    @Override
    public synchronized void unsubscribe(Class<?> clazz, EventHandler handler) {
        m_target.nonwrapped_unsubscribe(clazz, handler);
    }

    @Override
    public synchronized void unsubscribeAll(Multimap<Class<?>, EventHandler> handlers) {
        m_target.nonwrapped_unsubscribeAll(handlers);
    }

    @Override
    public void dispatch(Object event, EventHandler handler) {
        try {
            m_target.nonwrapped_dispatch(event, handler);
        } catch (Throwable ex) {
            ExceptionHelper.printException(ex, "Unable to dispatch evetn: " + event + " to handler " + handler);
        }
    }        
}
