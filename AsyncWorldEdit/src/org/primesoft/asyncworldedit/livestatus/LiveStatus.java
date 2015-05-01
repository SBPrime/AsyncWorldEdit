package org.primesoft.asyncworldedit.livestatus;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Weby@we-bb.com <Nicolas Glassey>
 * @version 1.0.0
 * @since 29/04/15
 */
public interface LiveStatus
{
    class LiveStatusSelector
    {
        public static List<LiveStatus> checkJars(JavaPlugin plugin, List<LiveStatus> loaded) {
            List<LiveStatus> existing = new ArrayList<LiveStatus>();
            Plugin[] plugins = plugin.getServer().getPluginManager().getPlugins();
            List<String> pluginsNames = new ArrayList<String>();
            for(Plugin p : plugins)
                pluginsNames.add(p.getName().toLowerCase());

            for(LiveStatus implementation : loaded)
            {
                String cName = implementation.getClass().getSimpleName().replace("Integrator", "");
                if(pluginsNames.contains(cName.toLowerCase()))
                {
                    existing.add(implementation);
                }
            }
            return existing;
        }
        public static List<LiveStatus> loadStatusAPIs(JavaPlugin plugin, ClassLoader classLoader)
        {
            List<LiveStatus> implementations = new ArrayList<LiveStatus>();
            if(classLoader!=null)
            {
                Package p = LiveStatus.class.getPackage();
                ArrayList<Class> classes = new ArrayList<Class>();

                File directory = null;
                String fullPath;
                String pkgname = p.getName();
                String relPath = pkgname.replace(".", "/");

                URL resource = classLoader.getResource(relPath);
                if(resource!=null)
                {

                    fullPath = resource.getFile();

                    try
                    {
                        directory = new File(resource.toURI());
                    } catch (URISyntaxException e)
                    {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e)
                    {
                        directory = null;
                    }

                    if (directory != null && directory.exists())
                    {
                        String[] files = directory.list();
                        for (String file : files)
                        {
                            if (file.endsWith(".class"))
                            {
                                String className = pkgname + "." + file.substring(0, file.length() - 6);
                                try
                                {
                                    classes.add(Class.forName(className));
                                } catch (ClassNotFoundException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else
                    {
                        try
                        {
                            String jarPath = fullPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
                            JarFile jarFile = new JarFile(jarPath);
                            Enumeration<JarEntry> entries = jarFile.entries();
                            while (entries.hasMoreElements())
                            {
                                JarEntry entry = entries.nextElement();
                                String entryName = entry.getName();
                                if (entryName.startsWith(relPath) && entryName.length() > (relPath.length() + "/".length()))
                                {
                                    String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
                                    try
                                    {
                                        classes.add(Class.forName(className));
                                    } catch (ClassNotFoundException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                for(Class clazz : classes)
                {
                    if(LiveStatus.class.isAssignableFrom(clazz) && !clazz.isInterface())
                    {
                        try
                        {
                            implementations.add((LiveStatus) clazz.newInstance());
                        } catch (InstantiationException e)
                        {
                            e.printStackTrace();
                        } catch (IllegalAccessException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return implementations;
        }
        public static LiveStatus getStatusAPI(PlayerEntry player)
        {
            LiveStatus ls=null;
            if(player!=null)
            {
                List<LiveStatus> available = AsyncWorldEditMain.getAvailableStatusAPIs();
                if(available.size()>0)
                {
                    String sapi = player.getPermissionGroup().getStatusAPI();
                    if(sapi.equalsIgnoreCase("auto"))
                    {
                        ls = available.get(0);
                    }
                    for(LiveStatus l : available)
                    {
                        if(l.getClass().getSimpleName().replace("Integrator","").equalsIgnoreCase(sapi))
                        {
                            return l;
                        }
                    }
                }
            }
            return ls;
        }
    }

    void disableMessage(@Nonnull PlayerEntry player);

    void setMessage(@Nonnull PlayerEntry player, int blocks, int maxBlocks, int jobs, double speed, double time, double percentage);
}
