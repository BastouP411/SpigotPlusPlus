package fr.bastoup.spigotplusplus.link;

import java.io.File;

public class CPPLink {

    public static void loadPlugin(File binary) {
        if(!binary.exists() || !binary.isFile())
            throw new IllegalArgumentException("The provided binary does not exist.");
        loadPlugin(binary.getAbsolutePath());
    }

    private static native void loadPlugin(String path);
}
