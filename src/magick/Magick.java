package magick;


import java.awt.*;
import java.io.*;


/**
 * The sole purpose of this class is to cause the native
 * library to be loaded whenever a concrete class is used
 * and provide utility methods.
 *
 * @author Eric Yeo
 * @author Max Kollegov &lt;virtual_max@geocities.com&gt;
 */
public class Magick {


    static {
        // code from https://github.com/kwhat/jnativehook/blob/master/src/java/org/jnativehook/NativeSystem.java

        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        if (osName.equalsIgnoreCase("linux")) {
            loadLibraryFromJar("/lib/libJMagick.so");
        } else if (osName.equalsIgnoreCase("mac os x")) {
            loadLibraryFromJar("/lib/libJMagick.jnilib");
        } else throw new RuntimeException("Operating system not supported");

        init();
    }

    public static void loadLibraryFromJar(String path) {
        // Code from: https://github.com/adamheinrich/native-utils/blob/master/NativeUtils.java
        // and
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("The path has to be absolute (start with '/').");
        }

        // Obtain filename from path
        String[] parts = path.split("/");
        String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

        // Split filename to prexif and suffix (extension)
        String prefix = "";
        String suffix = null;
        if (filename != null) {
            parts = filename.split("\\.", 2);
            prefix = parts[0];
            suffix = (parts.length > 1) ? "."+parts[parts.length - 1] : null;
        }

        // Check if the filename is okay
        if (filename == null || prefix.length() < 3) {
            throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
        }

        // Prepare temporary file
        File temp = null;
        try {
            temp = File.createTempFile(prefix, suffix);

            temp.deleteOnExit();

            if (!temp.exists()) {
                throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");
            }

            // Prepare buffer for data copying
            byte[] buffer = new byte[1024];
            int readBytes;

            // Open and check input stream
            InputStream is = Magick.class.getResourceAsStream(path);
            if (is == null) {
                throw new FileNotFoundException("File " + path + " was not found inside JAR.");
            }

            // Open output stream and copy data between source file in JAR and the temporary file
            OutputStream os = new FileOutputStream(temp);
            try {
                while ((readBytes = is.read(buffer)) != -1) {
                    os.write(buffer, 0, readBytes);
                }
            } finally {
                // If read/write fails, close streams safely before throwing an exception
                os.close();
                is.close();
            }

            // Finally, load the library
            System.load(temp.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading library from jar.");
        }
    }

    /**
     * Initializes the ImageMagic system
     */
    private static native void init();


    /**
     * Parses a geometry specification and returns the
     * width, height, x, and y values in the rectangle.
     * It also returns flags that indicates which of the
     * four values (width, height, xoffset, yoffset) were
     * located in the string, and whether the x and y values
     * are negative.  In addition, there are flags to report
     * any meta characters (%, !, <, and >).
     * @param geometry String containing the geometry specifications
     * @param rect The rectangle of values x, y, width and height
     * @return bitmask indicating the values in the geometry string
     * @see magick.GeometryFlags
     */
    public static native int parseImageGeometry(String geometry,
                                                Rectangle rect);

}
