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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import java.util.HashMap;
import java.util.Map;
import org.primesoft.asyncworldedit.PluginMain;

/**
 * This hash map autimaticly injects LocalSessionWrapper for each LocalSession
 * request
 *
 * @author SBPrime
 */
public class SpyHashMap extends HashMap<String, LocalSession> {
    /**
     * World edit configuration
     */
    private final LocalConfiguration m_weLocalConfiguration;

    public SpyHashMap(LocalConfiguration configuration) {
        m_weLocalConfiguration = configuration;
    }

    
    /**
     * This should always returns true because this is the only location
     * to make surre that only LocalSessionWrapper classes are added.
     * @param key
     * @return 
     */
    @Override
    public boolean containsKey(Object key) {
        synchronized (this) {
            if (key == null || !(key instanceof String))
            {
                return false;
            }
            
            String s = (String)key;
            if (!super.containsKey(key)){
                createAndAdd(s, null);
            }
            
            return true;
        }
    }

    @Override
    public boolean containsValue(Object value) {
        synchronized (this) {
            if (super.containsValue(value)) {
                return true;
            }
            
            for (LocalSession ls : values()) {
                LocalSession parrent = ((LocalSessionWrapper)ls).getParrent();
                if (parrent!= null && parrent.equals(value))
                {
                    return true;
                }
            }
            
            return false;
        }
    }

    /**
     * Create a new LocalSessionWrapper and add it to the collection
     *
     * @param key
     * @return
     */
    private LocalSession createAndAdd(String key, LocalSession parent) {
        LocalSessionWrapper result = new LocalSessionWrapper(key, m_weLocalConfiguration, parent);
        return super.put(key, result);
    }

    @Override
    public LocalSession put(String key, LocalSession value) {
        synchronized (this) {
            if ((value instanceof LocalSessionWrapper)) {
                return super.put(key, value);
            }

            PluginMain.log("Warning: put request found on LocalSession hash. The integration might be incorrect.");
            return createAndAdd(key, value);

        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends LocalSession> m) {
        boolean showError = false;
        synchronized (this) {
            for (Map.Entry<? extends String, ? extends LocalSession> entry : m.entrySet()) {
                String key = entry.getKey();
                LocalSession value = entry.getValue();
                boolean isWrapper = value instanceof LocalSession;

                if (isWrapper) {
                    super.put(key, value);
                } else {
                    showError = true;
                    createAndAdd(key, value);
                }
            }
        }

        if (showError) {
            PluginMain.log("Warning: putAll request found on LocalSession hash. The integration might be incorrect.");
        }
    }
}