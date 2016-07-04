package org.apache.poi.util;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * Default implementation of the {@link TempFileCreationStrategy} used by {@link TempFile}:
 * Files are collected into one directory and by default are deleted on exit from the VM.
 * Files may be manually deleted by user prior to JVM exit.
 * Files can be kept by defining the system property {@link #KEEP_FILES}.
 * 
 * Each file is registered for deletion with the JVM and the temporary directory is not deleted
 * after the JVM exits. Files that are created in the poifiles directory outside
 * the control of DefaultTempFileCreationStrategy are not deleted.
 * See {@link TempFileCreationStrategy} for better strategies for long-running
 * processes or limited temporary storage.
 */
public class DefaultTempFileCreationStrategy implements TempFileCreationStrategy {
    /** Define a constant for this property as it is sometimes mistypes as "tempdir" otherwise
     * This is private to avoid having two public-visible constants ({@link TempFile#JAVA_IO_TMPDIR}). */
    private static final String JAVA_IO_TMPDIR = TempFile.JAVA_IO_TMPDIR;
    /*package*/ static final String POIFILES = "poifiles";
    
    /** To keep files after JVM exit, set the <code>-Dpoi.keep.tmp.files</code> JVM property */
    public static final String KEEP_FILES = "poi.keep.tmp.files";
    
    /** random number generator to generate unique filenames */
    private static final SecureRandom random = new SecureRandom();
    
    /** The directory where the temporary files will be created (<code>null</code> to use the default directory). */
    private File dir;
    
    /**
     * Creates the strategy so that it creates the temporary files in the default directory.
     * 
     * @see File#createTempFile(String, String)
     */
    public DefaultTempFileCreationStrategy() {
        this(null);
    }
    
    /**
     * Creates the strategy allowing to set the  
     *
     * @param dir The directory where the temporary files will be created (<code>null</code> to use the default directory).
     * 
     * @see File#createTempFile(String, String, File)
     */
    public DefaultTempFileCreationStrategy(File dir) {
        this.dir = dir;
    }
    
    private void createPOIFilesDirectory() throws IOException {
        // Identify and create our temp dir, if needed
        // The directory is not deleted, even if it was created by this TempFileCreationStrategy
        if (dir == null) {
            String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
            if (tmpDir == null) {
                throw new IOException("Systems temporary directory not defined - set the -D"+JAVA_IO_TMPDIR+" jvm property!");
            }
            dir = new File(tmpDir, POIFILES);
        }
        
        createTempDirectory(dir);
    }
    
    /**
     * Attempt to create a directory
     *
     * @param directory
     * @throws IOException
     */
    private void createTempDirectory(File directory) throws IOException {
        if (!(directory.exists() || directory.mkdirs()) || !directory.isDirectory()) {
            throw new IOException("Could not create temporary directory '" + directory + "'");
        }
    }
    
    @Override
    public File createTempFile(String prefix, String suffix) throws IOException {
        // Identify and create our temp dir, if needed
        createPOIFilesDirectory();
        
        // Generate a unique new filename 
        File newFile = File.createTempFile(prefix, suffix, dir);

        // Set the delete on exit flag, unless explicitly disabled
        if (System.getProperty(KEEP_FILES) == null) {
            newFile.deleteOnExit();
        }

        // All done
        return newFile;
    }

    /* (non-JavaDoc) Created directory path is <JAVA_IO_TMPDIR>/poifiles/prefix0123456789 */
    @Override
    public File createTempDirectory(String prefix) throws IOException {
        // Identify and create our temp dir, if needed
        createPOIFilesDirectory();
        
        // Generate a unique new filename
        // FIXME: Java 7+: use java.nio.Files#createTempDirectory
        final long n = random.nextLong();
        File newDirectory = new File(dir, prefix + Long.toString(n));
        createTempDirectory(newDirectory);

        // Set the delete on exit flag, unless explicitly disabled
        if (System.getProperty(KEEP_FILES) == null) {
            newDirectory.deleteOnExit();
        }

        // All done
        return newDirectory;
    }
}