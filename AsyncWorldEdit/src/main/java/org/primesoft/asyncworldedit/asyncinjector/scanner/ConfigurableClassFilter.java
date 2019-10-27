/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2019, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.asyncinjector.scanner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.primesoft.asyncworldedit.LoggerProvider;
import org.primesoft.asyncworldedit.api.classScanner.IClassFilter;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;

/**
 *
 * @author Sławomir Belter
 */
class ConfigurableClassFilter implements IClassFilter {
    private static final CacheEntry ALWAYS_FALSE = i -> false;
    
    private static final CacheEntry ALWAYS_TRUE = i -> true;
    
    private volatile Map<String, CacheEntry> m_cachedClasses = new ConcurrentHashMap();
    private volatile Map<Pattern, Pattern[]> m_configuration = Collections.EMPTY_MAP;

    public ConfigurableClassFilter() {
    }

    void loadConfig() {
        Map<String, String[]> blackList = ConfigProvider.classScanner().getBlackList();
        Map<Pattern, Pattern[]> newPatterns = new HashMap<>();
        for (Map.Entry<String, String[]> entry : blackList.entrySet()) {
            try {
                Pattern clsPattern = Pattern.compile(entry.getKey());
                String[] entries = entry.getValue();
                Pattern[] entriesPattern = new Pattern[entries.length];
                
                for (int j = 0; j < entries.length; j++) {
                    entriesPattern[j] = Pattern.compile(entries[j]);
                }
                
                newPatterns.put(clsPattern, entriesPattern);
            }
            catch (Exception ex) {
                LoggerProvider.log("Error: Unable to parse class scanner entry: {'" + entry.getKey() + "', [" + 
                        Stream.of(entry.getValue()).map(i -> "'" + i + "'").collect(Collectors.joining(", ")) + "]}");
            }
        }
        m_configuration = newPatterns;
        m_cachedClasses = new ConcurrentHashMap<>();
    }

    @Override
    public boolean accept(Class<?> cls, Field field) {
        if (cls == null) {
            return true;
        }
        
        return m_cachedClasses.computeIfAbsent(cls.getName(), this::createCache).accept(field);
    }

    private CacheEntry createCache(String className) {
        Map<Pattern, Pattern[]> config = m_configuration;
        for (Pattern key : config.keySet()) {
            if (key.matcher(className).matches()) {
                Pattern[] patterns = config.get(key);
                return patterns == null || patterns.length == 0 ? ALWAYS_FALSE : new CachingEntry(patterns);
            }
        }
        
        return ALWAYS_TRUE;
    }
    
    @FunctionalInterface
    private static interface CacheEntry {
        boolean accept(Field field);
    }

    private static class CachingEntry implements CacheEntry {

        private final Map<String, Boolean> m_matchedFields = new ConcurrentHashMap<>();
        private final Pattern[] m_patterns;

        public CachingEntry(Pattern[] patterns) {
            m_patterns = patterns;
        }

        @Override
        public boolean accept(Field field) {
            if (field == null) {
                return false;
            }
            
            String name = field.getName();
            return m_matchedFields.computeIfAbsent(name, this::calculateCache);
        }

        private boolean calculateCache(String fieldName) {
            for (Pattern p : m_patterns) {
                if (p.matcher(fieldName).matches()) {
                    return false;
                }
            }
            
            return true;
        }
    }
}
