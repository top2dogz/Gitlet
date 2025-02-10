package gitlet;

import java.io.File;
import java.io.Serializable;

/** Contains all the data needed to represent
 * the version of the file.
 * @author Izaac Ruiz */
public class Blob implements Serializable {

    /** Object containing metadata from the specified FILE. */
    public Blob(File file) {
        _contents = Utils.readContentsAsString(file);
        _hash = Utils.sha1(_contents);
        _parentFile = file;
    }

    @Override
    public String toString() {
        return _contents;
    }

    /** Returns the hash of the given blob. */
    public String getHash() {
        return _hash;
    }

    /** Returns the File object in which the Blob
     * was made out of. */
    public File getParentFile() {
        return _parentFile;
    }

    /** Returns a STRING representing the name in which
     * the Blob object was made out of. */
    public String parentFileName() {
        return _parentFile.getName();
    }

    /** Contains the text of the parent file. */
    private String _contents;

    /** SHA-1 id that represents the Blob object. */
    private String _hash;

    /** File object representing the file in which
     * the Blob was made out of. */
    private File _parentFile;
}
