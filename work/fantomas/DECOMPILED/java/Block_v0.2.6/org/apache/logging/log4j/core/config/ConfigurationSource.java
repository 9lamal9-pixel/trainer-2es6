/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.net.UrlConnectionFactory;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.util.Source;
import org.apache.logging.log4j.util.LoaderUtil;

public class ConfigurationSource {
    public static final ConfigurationSource NULL_SOURCE = new ConfigurationSource(new byte[0], null, 0L);
    public static final ConfigurationSource COMPOSITE_SOURCE = new ConfigurationSource(new byte[0], null, 0L);
    private static final String HTTPS = "https";
    private static final String HTTP = "http";
    private final File file;
    private final URL url;
    private final String location;
    private final InputStream stream;
    private volatile byte[] data;
    private volatile Source source = null;
    private final long lastModified;
    private volatile long modifiedMillis;

    public ConfigurationSource(InputStream stream, File file) {
        this.stream = Objects.requireNonNull(stream, "stream is null");
        this.file = Objects.requireNonNull(file, "file is null");
        this.location = file.getAbsolutePath();
        this.url = null;
        this.data = null;
        long modified = 0L;
        try {
            modified = file.lastModified();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.lastModified = modified;
    }

    public ConfigurationSource(InputStream stream, URL url) {
        this.stream = Objects.requireNonNull(stream, "stream is null");
        this.url = Objects.requireNonNull(url, "URL is null");
        this.location = url.toString();
        this.file = null;
        this.data = null;
        this.lastModified = 0L;
    }

    public ConfigurationSource(InputStream stream, URL url, long lastModified) {
        this.stream = Objects.requireNonNull(stream, "stream is null");
        this.url = Objects.requireNonNull(url, "URL is null");
        this.location = url.toString();
        this.file = null;
        this.data = null;
        this.lastModified = lastModified;
    }

    public ConfigurationSource(InputStream stream) throws IOException {
        this(ConfigurationSource.toByteArray(stream), null, 0L);
    }

    public ConfigurationSource(Source source, byte[] data, long lastModified) throws IOException {
        Objects.requireNonNull(source, "source is null");
        this.data = Objects.requireNonNull(data, "data is null");
        this.stream = new ByteArrayInputStream(data);
        this.file = source.getFile();
        this.url = source.getURI().toURL();
        this.location = source.getLocation();
        this.lastModified = lastModified;
    }

    private ConfigurationSource(byte[] data, URL url, long lastModified) {
        Objects.requireNonNull(data, "data is null");
        this.stream = new ByteArrayInputStream(data);
        this.file = null;
        this.url = url;
        this.location = null;
        this.lastModified = lastModified;
    }

    private static byte[] toByteArray(InputStream inputStream) throws IOException {
        int buffSize = Math.max(4096, inputStream.available());
        ByteArrayOutputStream contents = new ByteArrayOutputStream(buffSize);
        byte[] buff = new byte[buffSize];
        int length = inputStream.read(buff);
        while (length > 0) {
            contents.write(buff, 0, length);
            length = inputStream.read(buff);
        }
        return contents.toByteArray();
    }

    public File getFile() {
        return this.file;
    }

    public URL getURL() {
        return this.url;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setModifiedMillis(long modifiedMillis) {
        this.modifiedMillis = modifiedMillis;
    }

    public URI getURI() {
        URI sourceURI = null;
        if (this.url != null) {
            try {
                sourceURI = this.url.toURI();
            }
            catch (URISyntaxException uRISyntaxException) {
                // empty catch block
            }
        }
        if (sourceURI == null && this.file != null) {
            sourceURI = this.file.toURI();
        }
        if (sourceURI == null && this.location != null) {
            try {
                sourceURI = new URI(this.location);
            }
            catch (URISyntaxException ex) {
                try {
                    sourceURI = new URI("file://" + this.location);
                }
                catch (URISyntaxException uRISyntaxException) {
                    // empty catch block
                }
            }
        }
        return sourceURI;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    public String getLocation() {
        return this.location;
    }

    public InputStream getInputStream() {
        return this.stream;
    }

    public ConfigurationSource resetInputStream() throws IOException {
        if (this.source != null) {
            return new ConfigurationSource(this.source, this.data, this.lastModified);
        }
        if (this.file != null) {
            return new ConfigurationSource((InputStream)new FileInputStream(this.file), this.file);
        }
        if (this.url != null && this.data != null) {
            return new ConfigurationSource(this.data, this.url, this.modifiedMillis == 0L ? this.lastModified : this.modifiedMillis);
        }
        if (this.url != null) {
            return ConfigurationSource.fromUri(this.getURI());
        }
        if (this.data != null) {
            return new ConfigurationSource(this.data, null, this.lastModified);
        }
        return null;
    }

    public String toString() {
        if (this.location != null) {
            return this.location;
        }
        if (this == NULL_SOURCE) {
            return "NULL_SOURCE";
        }
        int length = this.data == null ? -1 : this.data.length;
        return "stream (" + length + " bytes, unknown location)";
    }

    public static ConfigurationSource fromUri(URI configLocation) {
        File configFile = FileUtils.fileFromUri(configLocation);
        if (configFile != null && configFile.exists() && configFile.canRead()) {
            try {
                return new ConfigurationSource((InputStream)new FileInputStream(configFile), configFile);
            }
            catch (FileNotFoundException ex) {
                ConfigurationFactory.LOGGER.error("Cannot locate file {}", (Object)configLocation.getPath(), (Object)ex);
            }
        }
        if (ConfigurationFactory.isClassLoaderUri(configLocation)) {
            ClassLoader loader = LoaderUtil.getThreadContextClassLoader();
            String path = ConfigurationFactory.extractClassLoaderUriPath(configLocation);
            ConfigurationSource source = ConfigurationSource.fromResource(path, loader);
            if (source != null) {
                return source;
            }
        }
        if (!configLocation.isAbsolute()) {
            ConfigurationFactory.LOGGER.error("File not found in file system or classpath: {}", (Object)configLocation.toString());
            return null;
        }
        try {
            URL url = configLocation.toURL();
            URLConnection urlConnection = UrlConnectionFactory.createConnection(url);
            InputStream is = urlConnection.getInputStream();
            long lastModified = urlConnection.getLastModified();
            return new ConfigurationSource(is, configLocation.toURL(), lastModified);
        }
        catch (MalformedURLException ex) {
            ConfigurationFactory.LOGGER.error("Invalid URL {}", (Object)configLocation.toString(), (Object)ex);
        }
        catch (Exception ex) {
            ConfigurationFactory.LOGGER.error("Unable to access {}", (Object)configLocation.toString(), (Object)ex);
        }
        return null;
    }

    public static ConfigurationSource fromResource(String resource, ClassLoader loader) {
        URL url = Loader.getResource(resource, loader);
        if (url == null) {
            return null;
        }
        InputStream is = null;
        try {
            is = url.openStream();
        }
        catch (IOException ioe) {
            ConfigurationFactory.LOGGER.catching(Level.DEBUG, ioe);
            return null;
        }
        if (is == null) {
            return null;
        }
        if (FileUtils.isFile(url)) {
            try {
                return new ConfigurationSource(is, FileUtils.fileFromUri(url.toURI()));
            }
            catch (URISyntaxException ex) {
                ConfigurationFactory.LOGGER.catching(Level.DEBUG, ex);
            }
        }
        return new ConfigurationSource(is, url);
    }
}

