package fr.bastoup.spigotplusplus;

import fr.bastoup.spigotplusplus.link.CPPLink;
import fr.bastoup.spigotplusplus.util.CPPNativeLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotPlusPlus extends JavaPlugin {
    @Override
    public void onEnable() {
        super.onEnable();
        CPPLink.loadPlugin(null);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
