Version control system that supports the following commands.

init

    Usage: java gitlet.Main init

    Description: Creates a new Gitlet version-control system in the current directory. This system will automatically start with one commit: a commit that contains no files and has the commit message initial commit (just like that, with no punctuation). It will have a single branch: master, which initially points to this initial commit, and master will be the current branch. The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 in whatever format you choose for dates (this is called “The (Unix) Epoch”, represented internally by the time 0.) Since the initial commit in all repositories created by Gitlet will have exactly the same content, it follows that all repositories will automatically share this commit (they will all have the same UID) and all commits in all repositories will trace back to it.


add

    Usage: java gitlet.Main add [file name]

    Description: Adds a copy of the file as it currently exists to the staging area (see the description of the commit command). For this reason, adding a file is also called staging the file for addition. Staging an already-staged file overwrites the previous entry in the staging area with the new contents. The staging area should be somewhere in .gitlet. If the current working version of the file is identical to the version in the current commit, do not stage it to be added, and remove it from the staging area if it is already there (as can happen when a file is changed, added, and then changed back to it’s original version). The file will no longer be staged for removal (see gitlet rm), if it was at the time of the command.


commit

    Usage: java gitlet.Main commit [message]

    Description: Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit. The commit is said to be tracking the saved files. By default, each commit’s snapshot of files will be exactly the same as its parent commit’s snapshot of files; it will keep versions of files exactly as they are, and not update them. A commit will only update the contents of files it is tracking that have been staged for addition at the time of commit, in which case the commit will now include the version of the file that was staged instead of the version it got from its parent. A commit will save and start tracking any files that were staged for addition but weren’t tracked by its parent. Finally, files tracked in the current commit may be untracked in the new commit as a result being staged for removal by the rm command (below).

rm

    Usage: java gitlet.Main rm [file name]

    Description: Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so (do not remove it unless it is tracked in the current commit).


log

    Usage: java gitlet.Main log

    Description: Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits. (In regular Git, this is what you get with git log --first-parent). This set of commit nodes is called the commit’s history. For every node in this history, the information it should display is the commit id, the time the commit was made, and the commit message. Here is an example of the exact format it should follow:


global-log

    Usage: java gitlet.Main global-log

    Description: Like log, except displays information about all commits ever made. The order of the commits does not matter. Hint: there is a useful method in gitlet.Utils that will help you iterate over files within a directory.


find

    Usage: java gitlet.Main find [commit message]

    Description: Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword message, put the operand in quotation marks, as for the commit command below. Hint: the hint for this command is the same as the one for global-log.


status

    Usage: java gitlet.Main status

    Description: Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged for addition or removal. An example of the exact format it should follow is as follows.


checkout


    Usages:

        java gitlet.Main checkout -- [file name]

        java gitlet.Main checkout [commit id] -- [file name]

        java gitlet.Main checkout [branch name]

    Descriptions:

        Takes the version of the file as it exists in the head commit and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.

        Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.

        Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch (see Failure cases below).

   



branch

    Usage: java gitlet.Main branch [branch name]

    Description: Creates a new branch with the given name, and points it at the current head commit. A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node. This command does NOT immediately switch to the newly created branch (just as in real Git). Before you ever call branch, your code should be running with a default branch called “master”.


rm-branch

    Usage: java gitlet.Main rm-branch [branch name]

    Description: Deletes the branch with the given name. This only means to delete the pointer associated with the branch; it does not mean to delete all commits that were created under the branch, or anything like that.


reset

    Usage: java gitlet.Main reset [commit id]

    Description: Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch’s head to that commit node. See the intro for an example of what happens to the head pointer after using reset. The [commit id] may be abbreviated as for checkout. The staging area is cleared. The command is essentially checkout of an arbitrary commit that also changes the current branch head.


merge

    Usage: java gitlet.Main merge [branch name]

    Description: Merges files from the given branch into the current branch. This method is a bit complicated, so here’s a more detailed description:
        First consider what we call the split point of the current branch and the given branch. For example, if master is the current branch and branch is the given branch: Split point The split point is a latest common ancestor of the current and given branch heads: - A common ancestor is a commit to which there is a path (of 0 or more parent pointers) from both branch heads. - A latest common ancestor is a common ancestor that is not an ancestor of any other common ancestor. For example, although the leftmost commit in the diagram above is a common ancestor of master and branch, it is also an ancestor of the commit immediately to its right, so it is not a latest common ancestor. If the split point is the same commit as the given branch, then we do nothing; the merge is complete, and the operation ends with the message Given branch is an ancestor of the current branch. If the split point is the current branch, then the effect is to check out the given branch, and the operation ends after printing the message Current branch fast-forwarded. Otherwise, we continue with the steps below.

        Any files that have been modified in the given branch since the split point, but not modified in the current branch since the split point should be changed to their versions in the given branch (checked out from the commit at the front of the given branch). These files should then all be automatically staged. To clarify, if a file is “modified in the given branch since the split point” this means the version of the file as it exists in the commit at the front of the given branch has different content from the version of the file at the split point. Remember: blobs are content addressable!

        Any files that have been modified in the current branch but not in the given branch since the split point should stay as they are.

        Any files that have been modified in both the current and given branch in the same way (i.e., both files now have the same content or were both removed) are left unchanged by the merge. If a file was removed from both the current and given branch, but a file of the same name is present in the working directory, it is left alone and continues to be absent (not tracked nor staged) in the merge.

        Any files that were not present at the split point and are present only in the current branch should remain as they are.

        Any files that were not present at the split point and are present only in the given branch should be checked out and staged.

        Any files present at the split point, unmodified in the current branch, and absent in the given branch should be removed (and untracked).

        Any files present at the split point, unmodified in the given branch, and absent in the current branch should remain absent.

        Any files modified in different ways in the current and given branches are in conflict. “Modified in different ways” can mean that the contents of both are changed and different from other, or the contents of one are changed and the other file is deleted, or the file was absent at the split point and has different contents in the given and current branches. In this case, replace the contents of the conflicted file with


