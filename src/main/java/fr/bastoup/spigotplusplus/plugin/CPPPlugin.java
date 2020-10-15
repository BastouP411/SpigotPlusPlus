package fr.bastoup.spigotplusplus.plugin;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.*;
import org.yaml.snakeyaml.reader.StreamReader;

import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

public class CPPPlugin implements Plugin {

    private File dataFolder;
    private PluginLoader loader;
    private Server server;
    private boolean enabled;
    private PluginDescriptionFile description;
    private FileConfiguration newConfig;
    private File configFile;
    private boolean initialized;
    private CPPPluginZip pluginZip;
    private PluginLogger logger;

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public PluginDescriptionFile getDescription() {
        return description;
    }

    @Override
    public FileConfiguration getConfig() {
        if (newConfig == null) {
            reloadConfig();
        }
        return newConfig;
    }

    @Override
    public InputStream getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        try {
            ZipEntry entry = pluginZip.getEntry(filename);

            if(entry == null) {
                return null;
            }

            return pluginZip.getInputStream(entry);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void saveConfig() {
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            Logger.getLogger(CPPPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    @Override
    public void saveDefaultConfig() {
        saveResource("config.yml", false);
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + getFile());
        }

        File outFile = new File(getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                Logger.getLogger(CPPPlugin.class.getName()).log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }
        } catch (IOException ex) {
            Logger.getLogger(CPPPlugin.class.getName()).log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }

    protected File getFile() {
        return pluginZip.getFile();
    }

    protected CPPPluginZip getZip() {
        return pluginZip;
    }

    @Override
    public void reloadConfig() {
        newConfig = YamlConfiguration.loadConfiguration(configFile);

        InputStream defConfigStream = getResource("config.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));

            newConfig.setDefaults(defConfig);
        }
    }

    @Override
    public PluginLoader getPluginLoader() {
        return loader;
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public boolean isNaggable() {
        return false;
    }

    @Override
    public void setNaggable(boolean canNag) {

    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        getServer().getLogger().severe("Plugin " + getDescription().getFullName() + " does not contain any generators that may be used in the default world!");
        return null;
    }

    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = new PluginLogger(this);
        }
        return logger;
    }

    @Override
    public String getName() {
        return description.getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    protected void setEnabled(final boolean enabled) throws IOException {
        if (this.enabled != enabled) {
            this.enabled = enabled;

            if (this.enabled) {
                pluginZip.reload();
                onEnable();
            } else {
                pluginZip.close();
                onDisable();
            }
        }
    }

    protected final void initialize(PluginLoader loader, Server server, PluginDescriptionFile description, File dataFolder, CPPPluginZip pluginZip){
        if (!initialized) {
            this.initialized = true;
            this.loader = loader;
            this.server = server;
            this.pluginZip = pluginZip;
            this.description = description;
            this.dataFolder = dataFolder;
            this.configFile = new File(dataFolder, "config.yml");
        }
    }
}
