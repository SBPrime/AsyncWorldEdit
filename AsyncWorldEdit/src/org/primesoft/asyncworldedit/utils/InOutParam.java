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
 * This is a helper class that allows you to add output (and input) 
 * parameters to java functions
 * @author SBPrime
 */
public class InOutParam<T> {       
    /**
     * Initialize reference parame (in and out value)
     * @param <T>
     * @param value
     * @return 
     */
    public static <T> InOutParam<T> Ref(T value)
    {
        return new InOutParam<T>(value);
    }
    
    /**
     * Initialize output param (out only)
     * @param <T>
     * @return 
     */
    public static <T> InOutParam<T> Out() {
        return new InOutParam<T>();
    }
    
    /**
     * Is the value set
     */
    private boolean m_isSet;
    
    /**
     * The parameter value
     */
    private T m_value;
    
    
    /**
     * Create new instance of ref param
     * @param value 
     */
    private InOutParam(T value)
    {
        m_value = value;
        m_isSet = true;
    }
    
    /**
     * Create new instance of out param
     */
    private InOutParam(){
        m_isSet = false;
    }
    
    
    /**
     * Get the parameter value
     * @return 
     */
    public T getValue()
    {        
        if (m_isSet) {
            return m_value;
        }
        
        throw new IllegalStateException("Output parameter not set");
    }
    
    
    public void setValue(T value)
    {
        m_isSet = true;
        m_value = value;
    }
}
