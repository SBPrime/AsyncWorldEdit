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
package org.primesoft.asyncworldedit.injector.core.visitors;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.collection.LocatedBlockList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.enginehub.piston.CommandManager;
import org.primesoft.asyncworldedit.injector.classfactory.IEditSessionJob;
import org.primesoft.asyncworldedit.injector.classfactory.IJob;
import org.primesoft.asyncworldedit.injector.core.InjectorCore;
import org.primesoft.asyncworldedit.injector.injected.IAsyncWrapper;
import org.primesoft.asyncworldedit.injector.injected.IWrapper;
import org.primesoft.asyncworldedit.injector.injected.commands.ICommandsRegistration;
import org.primesoft.asyncworldedit.injector.injected.commands.ICommandsRegistrationDelegate;
import org.primesoft.asyncworldedit.injector.utils.ExceptionOperationAction;
import org.primesoft.asyncworldedit.injector.utils.MultiArgWorldEditOperationAction;
import org.primesoft.asyncworldedit.injector.utils.OperationAction;

/**
 *
 * @author SBPrime
 */
public final class Helpers {

    public static void executeMethod(Operation op, OperationAction method) {
        if (op == null) {
            return;
        }

        InjectorCore.getInstance().getClassFactory().getOperationProcessor().process(op, (OperationAction) method);
    }

    public static <T extends WorldEditException> void executeMethodEx(Operation op, ExceptionOperationAction<T> method) throws T {
        if (op == null) {
            return;
        }

        InjectorCore.getInstance().getClassFactory().getOperationProcessor().process(op, (ExceptionOperationAction<T>) method);
    }

    public static void executeMultiArgMethod(Object _this,
            String name, MultiArgWorldEditOperationAction method,
            final Object... args) {

        final Player player = Stream.of(args).filter(i -> i instanceof Player).map(i -> (Player) i)
                .findFirst().orElse(null);
        final EditSession es = Stream.of(args).filter(i -> i instanceof EditSession).map(i -> (EditSession) i)
                .findFirst().orElse(null);

        if (es == null) {
            InjectorCore.getInstance().getClassFactory().getJobProcessor().executeJob(player, new IJob() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public void execute() {
                    try {
                        method.execute(_this, args);
                    } catch (WorldEditException ex) {
                        player.printError(String.format("Error while executing %1$s", name));

                        InjectorCore.getInstance().getClassFactory().handleError(ex, name);
                    }
                }

            });
        } else {
            InjectorCore.getInstance().getClassFactory().getJobProcessor().executeJob(player, es, new IEditSessionJob() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public void execute(EditSession es) {
                    try {
                        final Object[] argsNew = new Object[args.length];
                        for (int i = 0; i < args.length; i++) {
                            if (args[i] instanceof EditSession) {
                                argsNew[i] = es;
                            } else {
                                argsNew[i] = args[i];
                            }
                        }

                        method.execute(_this, argsNew);
                    } catch (WorldEditException ex) {
                        player.printError(String.format("Error while executing %1$s", name));

                        InjectorCore.getInstance().getClassFactory().handleError(ex, name);
                    }
                }
            });
        }
    }

    public static Clipboard createClipboard(Clipboard parent, Region region) {
        return InjectorCore.getInstance().getClassFactory().createClipboard(parent, region);
    }

    public static Object wrapResult(Object result, Object sender) {
        if (result == sender) {
            return result;
        }

        if (result instanceof IAsyncWrapper && sender instanceof IAsyncWrapper) {
            IAsyncWrapper iaw = (IAsyncWrapper) sender;
            ((IAsyncWrapper) result).initializeAsyncWrapper(iaw);
        }

        return result;
    }

    public static CommandManager wrapCommandManager(Object sender, CommandManager cm) {
        return InjectorCore.getInstance().getClassFactory().wrapCommandManager(sender, cm);
    }

    public static ICommandsRegistrationDelegate createCommandsRegistrationDelegate(ICommandsRegistration parent) {
        return InjectorCore.getInstance().getClassFactory().createCommandsRegistrationDelegate(parent);
    }

    public static boolean wrapperEquals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }

        if (o1 instanceof IWrapper) {
            o1 = ((IWrapper) o1).getWrappedInstance();
        }
        if (o2 instanceof IWrapper) {
            o2 = ((IWrapper) o2).getWrappedInstance();
        }

        return Objects.equals(o1, o2);
    }
    
    public static void cleanMapValues(Map<Object, Object> map) {
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            final Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            
            final Object key = entry.getKey();
            Object newValue = null;           
            if (value instanceof LocatedBlockList) {
                newValue = new LocatedBlockList();
            }
            if (newValue == null) {
                throw new IllegalArgumentException("Don't know how to clean '" + value.getClass().getName() + "' for key '" + key.toString() + "'");
            }
            
            map.put(key, newValue);
        }
    }
    
    public static Iterator<BlockVector3> getRegionIterator(Region region) {
        return InjectorCore.getInstance().getClassFactory().getRegionIterator(region);
    }
}
