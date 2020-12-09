package fr.bastoup.spigotplusplus.util;

import fr.bastoup.spigotplusplus.SpigotPlusPlus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class CPPNativeLoader {

    public static boolean loadLinker(File pluginFolder) {
        String libPath = getLibPath("cpplink");

        InputStream stream = SpigotPlusPlus.class.getResourceAsStream(libPath);

        if(stream == null)
            return false;

        return loadStreamLib(pluginFolder, "cpplink", stream);
    }

    public static String getLibPath(String name) {
        return OSInfo.getNativeLibFolderPathForCurrentOS() + "/" + getLibName(name);
    }

    public static String getLibName(String name) {
        return name + (OSInfo.getOSName().equalsIgnoreCase("windows") ? ".dll" : ".so");
    }

    public static boolean loadStreamLib(File pluginFolder, String name, InputStream in) {
        File file = new File(pluginFolder, getLibPath(name));

        if(file.exists()) {
            file.delete();
        }

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();

            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            in.close();

            FileOutputStream os = new FileOutputStream(file);
            os.write(buffer);
            os.flush();
            os.close();

            System.load(file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static Set<String> getLoadedLibraries(final ClassLoader loader) throws IllegalAccessException {

        Method libraries;
        try {
            libraries = loader.getClass().getDeclaredMethod("nativeLibraries");
            libraries.setAccessible(true);
            return ((Map<String, Object>) libraries.invoke(null)).keySet();
        } catch (NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }

    }


}
