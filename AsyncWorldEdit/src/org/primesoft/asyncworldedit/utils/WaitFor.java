/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primesoft.asyncworldedit.utils;

/**
 *
 * @author SBPrime
 */
public class WaitFor {

    /**
     * Waiting object state
     */
    private Object m_waitingObject = null;

    /**
     * MTA mutex
     */
    private final Object m_mutex = new Object();

    /**
     * The wait mutex
     */
    private final Object m_waitMutex = new Object();

    /**
     * Set waiting state
     * @param o
     */
    public void setWait(Object o, boolean state) {
        synchronized (m_waitMutex) {
            checkAndWait(o);
            
            synchronized (m_mutex) {
                m_waitingObject = state ? o : null;
                if (state) {
                    return;
                }
            }
            
            m_waitMutex.notifyAll();
        }
    }
    
    /**
     * Test if I shuld wait
     * @param o
     */
    public void checkAndWait(Object o) {
        if (m_waitingObject == null || m_waitingObject == o) {
            return;
        }

        synchronized (m_waitMutex) {
            synchronized (m_mutex) {
                if (m_waitingObject == null || m_waitingObject == o) {
                    return;
                }
            }

            try {
                m_waitMutex.wait();
            } catch (InterruptedException ex) {
            }
        }
    }
}
