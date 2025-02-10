# Gitlet Design Document

**Name**: Izaac Salvador Ruiz

## Classes and Data Structures

## Blobs

Store contents of the Files 

**Fields**
1. `str HashCode` Unique code representing the blob.

## Tree

Structure that keeps track of staging area, `Blob` objects, and current branch.

**Fields**
1. `T head`: Current node.
2. `HashMap<T> node`: Structures trees and blobs.

## Commits

**Fields**
1. `Date time`: Keeps track of time and date of the commit.
2. `String msg`: Message inputted by the user.
3. `boolean isHead`: Indicates if `this` is the head commit of the branch
4. `final str hashCode`: Hashcode representing a certain commit.

## Main

**Fields**
1. `static final File CWD`: Path of the current working directory
2. `static final File CURRENT_BRANCH`: Path of the directory containing
Files regarding our current branch
3. `static final File COMMITS` Path referencing a text file
containing our `Commit` objects.


## Algorithms

### Blob Class
1. `Blob(File file)` Blob constructor. Allows for files to be referenced
later on in trees by providing Files with their own unique hashcode.

### Commit Class
1. `Commit(Tree branch)` Constructs a commit. If no commits exist, then the
constructor creates a commit with no parent. `Commit` objects will keep
track of the date, inputted message, and branch.
2. 

### Main

1. `addFile(File file)`: Creates a `Blob` for staging. This will involve creating a new 
Blob class, serializing the result, and adding it into the Tree that keeps track
of our current branch.
2. `removeFile()`: Reads through the file containing the
current branch and deletes it from the branch at hand. Will require
the use of readObject that will
3. `find()`: Deserializes the contents of the
file containing the list of the commits made
and loops through to return the ids of the commits
with the same id. In the case that the function loops
and finds no commit message with the specified id, then it
prints `Found no commit with that message`
4. `commit(Tree branch)`: Creates a new `Commit()` class with the current branch.
References to parent commit for any issues
with files not being modified.
5. `checkout(File name)`: Searches for the Tree with `isHead` and
overwrites the file in our `CWD` with the one
referenced.





## Persistence

To be able to create persistence that supports our version
control system, we need to be able to keep track of all
the commits that were made. As a result, we need to
1. Create a file that contains serialized versions of our commits
2. Create a separate folder that only keeps track of the branch (`Tree`)
that we are currently dealing with.  
 
In this folder, there will be many Files that will keep track of how we
modify our version control system. These files will include:
* Branches: A list of all the different pointers
that have been created with the method `checkout()`
in Main
* Staged Files: `Blob` objects that have been placed for being
committed. This will be a `Tree` or `Blob` objects,
as well as other `Trees` that are getting
ready to be implemented within a `Commit` object.
* Removed Files: `Blob` objects that have been removed
from `Staged Files`
* Modifications that aren't staged: Files that have not been 
found in the current directory that can't
be found in our `Staged Files`
* Untracked Files: `Blob` objects that can't
be found in the Parent `Commit` object



