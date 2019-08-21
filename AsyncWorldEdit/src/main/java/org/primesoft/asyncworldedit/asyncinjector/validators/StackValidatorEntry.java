/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.asyncinjector.validators;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.primesoft.asyncworldedit.LoggerProvider.log;

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
    
    public String getOperationName(String name) {
        return name;
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
    
    public Boolean process(
            Supplier<String> className, 
            Supplier<String> methodName,
            Consumer<String> log) {
        
        final String cName = className.get();
        final Matcher m = getClassPattern().matcher(cName);
        if (!m.matches()) {
            //No class match
            return null;
        }

        String mName = methodName.get();
        for (ProcessList entry : getLists()) {
            Boolean r = entry.process(cName, mName, log);
            if (r != null) {
                return r;
            }
        }
        
        return null;
        
    }
    
    protected Boolean processMethodBlackList(
            String className, 
            String methodName, 
            Consumer<String> l) {
        return processMethodList(getMethodBlackList(), "Found on blacklist", false, 
                className, methodName, l);
    }
    
    protected Boolean processMethodWhiteList(
            String className, 
            String methodName, 
            Consumer<String> l) {
        return processMethodList(getMethodWhiteList(), "Found on whitelist", true, 
                className, methodName, l);
    }
    
    protected Boolean processMethodList(
            Pattern[] patterns, 
            String debugMsg, 
            boolean result, 
            String className,
            String methodName,
            Consumer<String> l) {
        
        for (Pattern pattern : patterns) {
            Matcher m = pattern.matcher(methodName);
            if (m.matches()) {
                if (l != null) {
                    l.accept("*");
                    l.accept("* " + debugMsg);
                    l.accept("* Class:\t\t" + className);
                    l.accept("* Method:\t\t" + methodName);
                    l.accept("* Class pattern:\t" + getClassPattern().pattern());
                    l.accept("* Method pattern:\t" + pattern.pattern());
                }
                return result;
            }
        }
        return null;
    }

    protected ProcessList[] getLists() {
        return new ProcessList[]{ this::processMethodBlackList, this::processMethodWhiteList };
    }
    
    @FunctionalInterface
    public interface ProcessList {
        Boolean process(String className, String methodName, Consumer<String> log);
    }
}
