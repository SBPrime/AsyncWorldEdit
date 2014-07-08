/*
 * The MIT License
 *
 * Copyright 2013 SBPrime.
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
package org.primesoft.asyncworldedit.taskdispatcher;

import org.primesoft.asyncworldedit.utils.Action;

/**
 *
 * @author Prime
 */
public class ActionEntry extends BaseDispatcherEntry {
    /**
     * Action to perform
     */
    private final Action m_action;
        
    
    /**
     * Is the task done
     */
    private boolean m_isDone = false;

    
    /**
     * The action
     * @return 
     */
    public Action getAction() {
        return m_action;
    }

    /**
     * Is the operation done
     * @return 
     */
    public boolean isDone() {
        return m_isDone;
    }

    /**
     * Create new instance of class
     * @param action action to perform
     */
    public ActionEntry(Action action) {
        m_action = action;
    }

    @Override
    public void Execute() {
        m_action.Execute();
        m_isDone = true;
    }        
}