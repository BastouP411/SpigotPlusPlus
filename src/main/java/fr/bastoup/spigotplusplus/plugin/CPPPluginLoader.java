package fr.bastoup.spigotplusplus.plugin;

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.*;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class CPPPluginLoader implements PluginLoader {

    protected final Pattern[] fileFilters = new Pattern[] { Pattern.compile("\\.cplugin\\.zip") };
    protected final Map<String, CPPPluginZip> zips = new HashMap<>();
    private Server server;

    public CPPPluginLoader(Server server) {
        this.server = server;
    }

    @Override
    public Plugin loadPlugin(File file) throws InvalidPluginException{
        CPPPlugin result;
        PluginDescriptionFile description;
        CPPPluginZip zip;

        if (!file.exists()) {
            throw new InvalidPluginException(new FileNotFoundException(String.format("%s does not exist", file.getPath())));
        }
        try {
            zip = new CPPPluginZip(file);
            ZipEntry entry = zip.getEntry("plugin.yml");

            if (entry == null) {
                throw new InvalidPluginException(new FileNotFoundException("Zip does not contain plugin.yml"));
            }

            InputStream stream = zip.getInputStream(entry);

            description = new PluginDescriptionFile(stream);

            stream.close();
            zip.close();
        } catch (IOException ex) {
            throw new InvalidPluginException(ex);
        } catch (InvalidDescriptionException e) {
            throw new InvalidPluginException(e);
        }

        File dataFolder = new File(file.getParentFile(), description.getName());

        if (dataFolder.exists() && !dataFolder.isDirectory()) {
            throw new InvalidPluginException(new Exception(String.format(
                    "Projected datafolder: '%s' for %s (%s) exists and is not a directory",
                    dataFolder,
                    description.getName(),
                    file
            )));
        }

        result = new CPPPlugin();
        result.initialize(this, server, description, dataFolder, zip);

        //zips.put(description.getName(), zip);

        return result;
    }

    @Override
    public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
        PluginDescriptionFile description = null;
        try {
            CPPPluginZip zip = new CPPPluginZip(file);
            ZipEntry entry = zip.getEntry("plugin.yml");

            if (entry == null) {
                throw new InvalidDescriptionException(new FileNotFoundException("Zip does not contain plugin.yml"));
            }

            InputStream stream = zip.getInputStream(entry);

            description = new PluginDescriptionFile(stream);

            stream.close();
            zip.close();
        } catch (IOException e) {
            throw new InvalidDescriptionException(e);
        }
        return description;
    }

    @Override
    public Pattern[] getPluginFileFilters() {
        return fileFilters;
    }

    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) {
        return null;
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        if (!(plugin instanceof CPPPlugin)) {
            throw new IllegalArgumentException("Plugin is not associated with this PluginLoader");
        }

        if (!plugin.isEnabled()) {
            String message = String.format("[%s] Loading %s.", plugin.getDescription().getName(), plugin.getDescription().getFullName());
            server.getLogger().info(message);

            CPPPlugin cppPlugin = (CPPPlugin) plugin;

            String pluginName = cppPlugin.getDescription().getName();

            if (!zips.containsKey(pluginName)) {
                zips.put(pluginName, cppPlugin.getZip());
            }

            try {
                cppPlugin.setEnabled(true);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?): " + ex.getMessage(), ex);
            }

            // Perhaps abort here, rather than continue going, but as it stands,
            // an abort is not possible the way it's currently written
            server.getPluginManager().callEvent(new PluginEnableEvent(plugin));
        }
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        if (!(plugin instanceof CPPPlugin)) {
            throw new IllegalArgumentException("Plugin is not associated with this PluginLoader");
        }

        if (plugin.isEnabled()) {
            String message = String.format("[%s] Unloading %s.", plugin.getDescription().getName(), plugin.getDescription().getFullName());
            server.getLogger().info(message);

            server.getPluginManager().callEvent(new PluginDisableEvent(plugin));

            CPPPlugin cppPlugin = (CPPPlugin) plugin;

            try {
                cppPlugin.setEnabled(false);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?): " + ex.getMessage(), ex);
            }

            zips.remove(cppPlugin.getDescription().getName());
        }
    }
}
