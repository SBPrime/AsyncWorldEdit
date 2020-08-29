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

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.primesoft.asyncworldedit.api.worldedit.IAsyncEditSessionBuilder;
import org.primesoft.asyncworldedit.injector.core.visitors.BaseFieldAccessorVisitor;
import org.primesoft.asyncworldedit.injector.utils.SimpleValidator;

public class EditSessionBuilderVisitor extends BaseFieldAccessorVisitor {

    private final static String DESCRIPTOR_BUILD = "()Lcom/sk89q/worldedit/EditSession;";
    private final static String NAME_BUILD = "build";

    private final static FieldEntry FIELD_THREAD_SAFE = new FieldEntry(Opcodes.ACC_PRIVATE, "m_threadSafeOnly", "Z", null, null);
    private final static FieldEntry FIELD_PLAYER_ENTITY = new FieldEntry(Opcodes.ACC_PRIVATE, "m_playerEntry", "Lorg/primesoft/asyncworldedit/api/playerManager/IPlayerEntry;", null, null);

    private final Map<String, FieldEntry> m_fields;

    private final SimpleValidator m_methodBuild = new SimpleValidator("Method " + NAME_BUILD + DESCRIPTOR_BUILD + " not found");

    public EditSessionBuilderVisitor(
            final ClassVisitor classVisitor) {
        this(new FieldEntry[]{
                new FieldEntry(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "eventBus", "Lcom/sk89q/worldedit/util/eventbus/EventBus;", null, null),
                new FieldEntry(Opcodes.ACC_PRIVATE, "world", "Lcom/sk89q/worldedit/world/World;", null, null),
                new FieldEntry(Opcodes.ACC_PRIVATE, "maxBlocks", "I", null, null),
                new FieldEntry(Opcodes.ACC_PRIVATE, "actor", "Lcom/sk89q/worldedit/extension/platform/Actor;", null, null),
                new FieldEntry(Opcodes.ACC_PRIVATE, "blockBag", "Lcom/sk89q/worldedit/extent/inventory/BlockBag;", null, null),
                new FieldEntry(Opcodes.ACC_PRIVATE, "tracing", "Z", null, null),
        }, classVisitor);
    }

    private EditSessionBuilderVisitor(
            final FieldEntry[] fieldEntries,
            final ClassVisitor classVisitor) {

        super(IAsyncEditSessionBuilder.class, fieldEntries, classVisitor);

        m_fields = Stream.of(fieldEntries).collect(Collectors.toMap(i -> i.Name, i -> i));
    }

    @Override
    public void validate() throws RuntimeException {
        super.validate();

        m_methodBuild.validate();
    }

    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {

        if (NAME_BUILD.equals(name) &&
            DESCRIPTOR_BUILD.equals(descriptor) &&
            access == Opcodes.ACC_PUBLIC) {

            m_methodBuild.set();
            return null;
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

        super.visitField(Opcodes.ACC_PRIVATE, FIELD_PLAYER_ENTITY.Name, FIELD_PLAYER_ENTITY.Descriptor, null, null);
        super.visitField(Opcodes.ACC_PRIVATE, FIELD_THREAD_SAFE.Name, FIELD_THREAD_SAFE.Descriptor, null, null);

        visitMethodBuild();
        visitMethodTheadSafeEditSession();
        visitMethodSetPlayerEntry();
    }

    private void visitMethodSetPlayerEntry() {
        final MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "setPlayerEntry", "(Lorg/primesoft/asyncworldedit/api/playerManager/IPlayerEntry;)Lcom/sk89q/worldedit/EditSessionBuilder;", null, null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        visitArgumemt(mv, FIELD_PLAYER_ENTITY.Descriptor, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, m_cls, FIELD_PLAYER_ENTITY.Name, FIELD_PLAYER_ENTITY.Descriptor);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void visitMethodTheadSafeEditSession() {
        final MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "theadSafeEditSession", "()Lcom/sk89q/worldedit/EditSessionBuilder;", null, null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitLdcInsn(true);
        mv.visitFieldInsn(Opcodes.PUTFIELD, m_cls, FIELD_THREAD_SAFE.Name, FIELD_THREAD_SAFE.Descriptor);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void visitMethodBuild() {

        final MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, NAME_BUILD, DESCRIPTOR_BUILD, null, null);
        mv.visitCode();

        emmitGet(mv, m_fields.get("eventBus"));
        emmitGet(mv, m_fields.get("world"));
        emmitGet(mv, m_fields.get("maxBlocks"));
        emmitGet(mv, m_fields.get("actor"));
        emmitGet(mv, m_fields.get("blockBag"));
        emmitGet(mv, m_fields.get("tracing"));

        emmitGet(mv, FIELD_THREAD_SAFE);
        emmitGet(mv, FIELD_PLAYER_ENTITY);

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, CLASS_HELPERS_DESCRIPTOR,
                "buildEditSession",
                "(" +
                        m_fields.get("eventBus").Descriptor +
                        m_fields.get("world").Descriptor +
                        m_fields.get("maxBlocks").Descriptor +
                        m_fields.get("actor").Descriptor +
                        m_fields.get("blockBag").Descriptor +
                        m_fields.get("tracing").Descriptor +
                        FIELD_THREAD_SAFE.Descriptor + FIELD_PLAYER_ENTITY.Descriptor +
                        ")Lcom/sk89q/worldedit/EditSession;", false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
}
