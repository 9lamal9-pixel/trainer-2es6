/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.web;

import com.sun.javafx.logging.PlatformLogger;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.HashMap;
import java.util.Map;

final class DirectoryLock {
    private static final PlatformLogger logger = PlatformLogger.getLogger(DirectoryLock.class.getName());
    private static final Map<File, Descriptor> descriptors = new HashMap<File, Descriptor>();
    private Descriptor descriptor;

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    DirectoryLock(File file) throws IOException, DirectoryAlreadyInUseException {
        block10: {
            file = DirectoryLock.canonicalize(file);
            this.descriptor = descriptors.get(file);
            if (this.descriptor == null) {
                File file2 = DirectoryLock.lockFile(file);
                RandomAccessFile randomAccessFile = new RandomAccessFile(file2, "rw");
                try {
                    FileLock fileLock = randomAccessFile.getChannel().tryLock();
                    if (fileLock == null) {
                        throw new DirectoryAlreadyInUseException(file.toString(), null);
                    }
                    this.descriptor = new Descriptor(file, randomAccessFile, fileLock);
                    descriptors.put(file, this.descriptor);
                    if (this.descriptor != null) break block10;
                }
                catch (OverlappingFileLockException overlappingFileLockException) {
                    try {
                        throw new DirectoryAlreadyInUseException(file.toString(), overlappingFileLockException);
                    }
                    catch (Throwable throwable) {
                        if (this.descriptor != null) throw throwable;
                        try {
                            randomAccessFile.close();
                            throw throwable;
                        }
                        catch (IOException iOException) {
                            logger.warning(String.format("Error closing [%s]", file2), iOException);
                        }
                        throw throwable;
                    }
                }
                try {
                    randomAccessFile.close();
                }
                catch (IOException iOException) {
                    logger.warning(String.format("Error closing [%s]", file2), iOException);
                }
            }
        }
        this.descriptor.referenceCount++;
    }

    void close() {
        if (this.descriptor == null) {
            return;
        }
        this.descriptor.referenceCount--;
        if (this.descriptor.referenceCount == 0) {
            try {
                this.descriptor.lock.release();
            }
            catch (IOException iOException) {
                logger.warning(String.format("Error releasing lock on [%s]", DirectoryLock.lockFile(this.descriptor.directory)), iOException);
            }
            try {
                this.descriptor.lockRaf.close();
            }
            catch (IOException iOException) {
                logger.warning(String.format("Error closing [%s]", DirectoryLock.lockFile(this.descriptor.directory)), iOException);
            }
            descriptors.remove(this.descriptor.directory);
        }
        this.descriptor = null;
    }

    static int referenceCount(File file) throws IOException {
        Descriptor descriptor = descriptors.get(DirectoryLock.canonicalize(file));
        return descriptor == null ? 0 : descriptor.referenceCount;
    }

    static File canonicalize(File file) throws IOException {
        Object object = file.getCanonicalPath();
        if (((String)object).length() > 0 && ((String)object).charAt(((String)object).length() - 1) != File.separatorChar) {
            object = (String)object + File.separatorChar;
        }
        return new File((String)object);
    }

    private static File lockFile(File file) {
        return new File(file, ".lock");
    }

    final class DirectoryAlreadyInUseException
    extends Exception {
        DirectoryAlreadyInUseException(String string, Throwable throwable) {
            super(string, throwable);
        }
    }

    private static class Descriptor {
        private final File directory;
        private final RandomAccessFile lockRaf;
        private final FileLock lock;
        private int referenceCount;

        private Descriptor(File file, RandomAccessFile randomAccessFile, FileLock fileLock) {
            this.directory = file;
            this.lockRaf = randomAccessFile;
            this.lock = fileLock;
        }
    }
}

