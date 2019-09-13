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
package org.primesoft.asyncworldedit.injector.core.visitors.worldedit.extent.reorder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.primesoft.asyncworldedit.injector.core.visitors.BaseClassVisitor;
import org.primesoft.asyncworldedit.injector.injected.extent.reorder.IResetable;

/**
 *
 * @author SBPrime
 */
public class ResetableExtentVisitor extends BaseClassVisitor {

    private final static String INTERFACE = Type.getInternalName(IResetable.class);

    private String m_className;
    private Set<String> m_fields = new HashSet<>();

    public ResetableExtentVisitor(ClassVisitor classVisitor) {
        super(classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        m_className = name;

        super.visit(version, access, name, signature, superName, 
                injectInterface(interfaces, INTERFACE));
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        m_fields.add(name);
        
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public void visitEnd() {
        MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "reset", "()V", null, null);
        mv.visitCode();

        if (m_fields.contains("containedBlocks")) {
            // containedBlocks.clear();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, m_className, "containedBlocks", Type.getDescriptor(Set.class));
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(Set.class), "clear", "()V", true);
        }
        
        if (m_fields.contains("stages")) {
            // Helpers.cleanMapValues(stages);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            final String mapDescriptor = Type.getDescriptor(Map.class);
            mv.visitFieldInsn(Opcodes.GETFIELD, m_className, "stages", mapDescriptor);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, CLASS_HELPERS_DESCRIPTOR, "cleanMapValues", "(" + mapDescriptor + ")V", false);
        }
        
        // return;
        mv.visitInsn(Opcodes.RETURN);

        mv.visitMaxs(1, 1);
        mv.visitEnd();

        super.visitEnd();
    }

    @Override
    public void validate() throws RuntimeException {
    }

}
