package fr.bastoup.spigotplusplus.link;

import fr.bastoup.spigotplusplus.SpigotPlusPlus;
import fr.bastoup.spigotplusplus.util.CPPNativeLoader;

import java.io.File;

public class CPPLink {

    static {
        CPPNativeLoader.loadLinker(SpigotPlusPlus.getPlugin(SpigotPlusPlus.class).getDataFolder());
    }

    public static void loadPlugin(File binary) {
        //if(!binary.exists() || !binary.isFile())
        //    throw new IllegalArgumentException("The provided binary does not exist.");
        loadPlugin("Test de ouf!");
    }

    private static native void loadPlugin(String path);
}
