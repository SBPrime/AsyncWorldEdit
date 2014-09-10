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
package org.primesoft.asyncworldedit.injector.validators;

import java.util.regex.Pattern;

/**
 * Stack trace validator entry
 *
 * @author SBPrime
 */
public class StackValidatorEntry {
    /**
     * Regexp for whitelisted methods
     */
    private final Pattern[] m_methodWhiteRegexp;
    
    /**
     * Regexp for blacklisted methods
     */
    private final Pattern[] m_methodBlackRegexp;
    
    /**
     * The class regexp
     */
    private final Pattern m_classRegexp;

    public Pattern[] getMethodWhiteList() {
        return m_methodWhiteRegexp;
    }

    public Pattern[] getMethodBlackList() {
        return m_methodBlackRegexp;
    }

    
    public Pattern getClassPattern() {
        return m_classRegexp;
    }

    public StackValidatorEntry(String classRegexp,
            String[] methodWhiteList, String[] methodBlackList) {
        m_methodWhiteRegexp = new Pattern[methodWhiteList.length];
        m_methodBlackRegexp = new Pattern[methodBlackList.length];

        for (int i = 0; i < methodWhiteList.length; i++) {
            m_methodWhiteRegexp[i] = Pattern.compile(methodWhiteList[i]);
        }

        for (int i = 0; i < methodBlackList.length; i++) {
            m_methodBlackRegexp[i] = Pattern.compile(methodBlackList[i]);
        }

        m_classRegexp = Pattern.compile(classRegexp);
    }

    public StackValidatorEntry(String classRegexp, String methodAllow, String methodDeny) {
        this(classRegexp, new String[]{methodAllow}, new String[]{methodDeny});
    }

    public StackValidatorEntry(String classRegexp, String[] methodWhiteList, String methodDeny) {
        this(classRegexp, methodWhiteList, new String[]{methodDeny});
    }

    public StackValidatorEntry(String classRegexp, String methodAllow, String[] methodBlackList) {
        this(classRegexp, new String[]{methodAllow}, methodBlackList);
    }

    public StackValidatorEntry(String classRegexp, String methodAllow) {
        this(classRegexp, new String[]{methodAllow}, new String[0]);
    }

    public StackValidatorEntry(String classRegexp) {
        this(classRegexp, new String[0], new String[0]);
    }
}
