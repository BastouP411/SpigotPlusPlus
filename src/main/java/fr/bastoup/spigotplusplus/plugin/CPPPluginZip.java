package fr.bastoup.spigotplusplus.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.plugin.InvalidPluginException;
public class CPPPluginZip {


    private ZipFile zip;
    private File file;
    private boolean closed = true;

    public CPPPluginZip(File file) throws IOException {
        this.file = file;
        this.reload();
    }

    public void reload() throws IOException {
        if (closed) {
            zip = new ZipFile(file);
            closed = false;
        }
    }

    public void close() throws IOException {
        zip.close();
        closed = true;
    }

    public ZipEntry getEntry(String filename) throws IOException {
        return zip.getEntry(filename);
    }

    public InputStream getInputStream(ZipEntry entry) throws IOException {
        return zip.getInputStream(entry);
    }

    public File getFile() {
        return file;
    }
}