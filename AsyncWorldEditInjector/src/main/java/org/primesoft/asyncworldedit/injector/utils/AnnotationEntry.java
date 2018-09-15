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
package org.primesoft.asyncworldedit.injector.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 *
 * @author SBPrime
 */
public class AnnotationEntry {

    private static interface IEntry {

        public void visit(AnnotationVisitor av);
    }

    private static class EntryVisit implements IEntry {

        private final String m_name;
        private final Object m_value;

        private EntryVisit(String name, Object value) {
            m_name = name;
            m_value = value;
        }

        @Override
        public void visit(AnnotationVisitor av) {
            av.visit(m_name, m_value);
        }
    }

    private static class EntryArray implements IEntry {

        private final String m_name;

        public final List<Object> values = new ArrayList<>();

        private EntryArray(String name) {
            m_name = name;
        }

        @Override
        public void visit(AnnotationVisitor av) {
            AnnotationVisitor sub = av.visitArray(m_name);
            values.forEach(v -> sub.visit(null, v));
            sub.visitEnd();
        }
    }

    private static class EntryEnum implements IEntry {

        private final String m_name;
        private final String m_descriptor;
        private final String m_value;

        private EntryEnum(String name, String descriptor, String value) {
            m_name = name;
            m_descriptor = descriptor;
            m_value = value;
        }

        @Override
        public void visit(AnnotationVisitor av) {
            av.visitEnum(m_name, m_descriptor, m_value);
        }
    }

    public final String descriptor;
    public final boolean visible;

    private final List<IEntry> m_entries = new ArrayList<>();

    public AnnotationEntry(String descriptor, boolean visible) {
        this.descriptor = descriptor;
        this.visible = visible;
    }

    public void addVisit(String name, Object value) {
        m_entries.add(new EntryVisit(name, value));
    }

    public Collection<Object> addArray(String name) {
        EntryArray entry = new EntryArray(name);
        m_entries.add(entry);

        return entry.values;
    }

    public void addEnum(String name, String descriptor, String value) {
        m_entries.add(new EntryEnum(name, descriptor, value));
    }

    public void addAnnotation(String name, String descriptor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(MethodVisitor mv) {
        AnnotationVisitor av = mv.visitAnnotation(descriptor, visible);
        m_entries.forEach(e -> e.visit(av));
        av.visitEnd();
    }
}
