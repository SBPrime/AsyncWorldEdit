/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
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
package org.primesoft.asyncworldedit.injector.scanner;

import java.lang.reflect.Field;
import java.util.regex.Pattern;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;

/**
 *
 * @author SBPrime
 */
public class ClassScannerEntry {

    private final Class<?> m_cls;
    private final Pattern[] m_fields;

    private static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            AsyncWorldEditMain.log("Warning: Unable to get class " + name);
            return null;
        }
    }

    public ClassScannerEntry(Class<?> cls, String[] fields) {
        m_cls = cls;
        if (fields == null) {
            fields = new String[0];
        }

        m_fields = new Pattern[fields.length];
        for (int i = 0; i < fields.length; i++) {
            m_fields[i] = Pattern.compile(fields[i]);
        }
    }

    public ClassScannerEntry(Class<?> cls, String field) {
        this(cls, new String[]{field});
    }

    public ClassScannerEntry(Class<?> cls) {
        this(cls, (String[]) null);
    }

    public ClassScannerEntry(String cls, String[] fields) {
        this(getClass(cls), fields);
    }

    public ClassScannerEntry(String cls, String field) {
        this(cls, new String[]{field});
    }

    public ClassScannerEntry(String cls) {
        this(cls, (String[]) null);
    }

    public boolean isMatch(Class<?> c) {
        return isMatch(c, null);
    }

    public boolean isMatch(Class<?> c, Field f) {
        if (c == null || m_cls == null) {
            return false;
        }

        if (!m_cls.isAssignableFrom(c)) {
            return false;
        }

        if (f == null) {
            if (m_fields == null || m_fields.length == 0) {
                return true;
            } else {
                return false;
            }
        }

        String fName = f.getName();
        for (Pattern p : m_fields) {            
            if (p.matcher(fName).matches()) {
                return true;
            }
        }

        return false;
    }
}
