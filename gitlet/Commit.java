package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Formatter;
import java.util.LinkedHashMap;

/** Used to represent the metadata of how the CWD looked
 * at a certain point.
 * @author Izaac Ruiz */
public class Commit implements Serializable {

    /** Object that essentially takes a screenshot of the CWD at a certain point
     * and time, including the MSG, HEAD, and CONTENTS associated with it. */
    public Commit(String msg, File head, LinkedHashMap<File, String> contents) {
        if (msg.equals("initial commit")) {
            _time = new Date(0);
            _parentHash = null;
        } else {
            _time = new Date();
            _parentHash = Utils.readObject(head, Commit.class).getHash();
        }
        _contents = contents;
        _message = msg;
        _hash = Utils.sha1(Utils.serialize(contents),
                Utils.serialize(_parentHash), Utils.serialize(_time));
        _otherParent = null;
    }

    @Override
    public String toString() {
        Formatter str = new Formatter();
        str.format("===\ncommit %1$s\nDate: %2$ta "
                + "%2$tb %2$td %2$tH:%2$tM:%2$tS "
                + "%2$tY %2$tz\n%3$s\n\n", getHash(), _time, _message);
        return str.toString();
    }

    /** Returns a hash of the Commit. */
    public String getHash() {
        return _hash;
    }

    /** Returns LinkedHashMap representing contents
     * of the Commit. */
    public LinkedHashMap<File, String> getContents() {
        return _contents;
    }

    /** Returns the hash of the Commit that came before it. */
    public String getParent() {
        return _parentHash;
    }

    /** Returns message associated with the Commit. */
    public String getMessage() {
        return _message;
    }

    /** Returns the possible other parent of the Commit. */
    public String getMergeParent() {
        return _otherParent;
    }

    /** Hash of the Commit that came before it. */
    private final String _parentHash;

    /** Mapping of which File objects belong to
     * the SHA-ID of their blobs. */
    private final LinkedHashMap<File, String> _contents;

    /** Time that the Commit was made.*/
    private final Date _time;

    /** Unique hash that represents the Commit object. */
    private final String _hash;

    /** Message associated with the Commit object. */
    private final String _message;

    /** Holds SHA-1 id of possible other Commit parent. */
    private String _otherParent;



}


