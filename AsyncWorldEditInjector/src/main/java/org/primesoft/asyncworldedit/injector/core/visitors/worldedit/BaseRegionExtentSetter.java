/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2020, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.injector.core.visitors.worldedit;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.primesoft.asyncworldedit.injector.core.visitors.BaseClassVisitor;
import org.primesoft.asyncworldedit.injector.injected.extent.IExtentFieldSetter;
import org.primesoft.asyncworldedit.injector.injected.regions.IRegionSetter;

public class BaseRegionExtentSetter extends BaseClassVisitor {
    private final static String REGION = "Lcom/sk89q/worldedit/regions/Region;";
    private final static String EXTENT = "Lcom/sk89q/worldedit/extent/Extent;";

    private final List<FieldEntry> m_fields = new LinkedList<>();

    public BaseRegionExtentSetter(final ClassVisitor classVisitor) {
        super(classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, injectInterface(interfaces,
                Type.getInternalName(IExtentFieldSetter.class),
                Type.getInternalName(IRegionSetter.class)));
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        boolean isRegion = REGION.equals(descriptor);
        boolean isExtent = EXTENT.equals(descriptor);

        if (!isExtent && !isRegion) {
            return super.visitField(access, name, descriptor, signature, value);
        }

        m_fields.add(new FieldEntry(name, isExtent));

        return super.visitField(access & (~Opcodes.ACC_FINAL), name, descriptor, signature, value);
    }

    @Override
    public void visitEnd() {
        visitRegionSetter();
        visitExtentSetter();

        super.visitEnd();
    }

    private void visitRegionSetter() {
        MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "setRegionAweInjected", "(" + REGION + ")V", null, null);
        mv.visitCode();

        m_fields.stream().filter(i -> !i.IsExtent).map(i -> i.Name).forEach(n -> {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            visitArgumemt(mv, REGION, 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, m_cls, n, REGION);
        });

        mv.visitInsn(Opcodes.RETURN);

        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    Object f;

    public void set(Object value, String fn) {
        if ("f".equals(fn)) {
            f = value;
        }
    }

    private void visitExtentSetter() {
        MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "setExtentAweInjected",
                "(" + EXTENT + "Ljava/lang/String;)V", null, null);
        mv.visitCode();


        m_fields.stream().filter(i -> i.IsExtent).map(i -> i.Name).forEach(n -> {
            Label labelNext = new Label();

            mv.visitLdcInsn(n);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(String.class), "equals", "(Ljava/lang/Object;)Z", false);
            mv.visitJumpInsn(Opcodes.IFEQ, labelNext);

            mv.visitVarInsn(Opcodes.ALOAD, 0);
            visitArgumemt(mv, EXTENT, 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, m_cls, n, EXTENT);

            mv.visitLabel(labelNext);
        });

        mv.visitInsn(Opcodes.RETURN);

        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    @Override
    public void validate() throws RuntimeException {

    }

    public static class FieldEntry {
        public final String Name;
        public final boolean IsExtent;


        /**
         * Create new instance of the field entry
         * @param name The field name to match
         * @param isExtent Is this an extent field
         */
        public FieldEntry(String name, boolean isExtent) {
            Name = name;
            IsExtent = isExtent;
        }
    }
}
