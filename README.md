# GithubVCS
A version control system used to control local file versions.


## Rationale
In GithubVCS, I try to implement a version-control system, which is essentially a backup system for files. I use Commit to backup directories of files whose contents are kept as Blobs. I use tree as the structure of directory for the convenience of mapping names to references to blobs and other trees(subdirectories). 
I store everything in the Hashmaps as a String instead of pointers to objects because I plan to serialize all the Blob and Commit objects and retrieving them using the String names. This way, I can simply store 1 copy of every unique version of each file, and one copy of each commit in spite of the possibility of multiple branches.
I considered serializing Command, but since I am only storing Hashmaps / Hashsets of Strings, it would be inefficient to serialize and deserialize the hashmaps just to access a single / few elements. Instead I only store hashmaps.


## Data Structures and Functions

## Blob

Store the files in a wrapper class that keeps track of important information such as its sha-1 hash, filename, directory, and content.


## Commit {

Store serialized commits which keeps track of its sha-1 hash, a mapping of filename to blobs, the id of the parent commit, log, and timestamp.

## Control {    

The actual functionality of the version control system. 

**Functionality include:**
* initialize Git environment
* add
* commit
* remove
* log
* global log
* find
* status
* checkout file
* checkout commit
* checkout branch
* create branch
* remove branch
* reset to previous commit
* merge branches

## Persistence
In order to control versions of the files, I need to persist gitlet to disk. I can achieve this by:
Creating a .gitlet directory 
Writing Commit to disk, writing staging area, other hashmaps to disk
Saving contents of file, filename to blob and writing blob to disk’
I can do the above by saving this as a string in a text file or use serialize to convert the Java object into bytes that I can eventually write to a file with sha-1 unique id on disk. 

In order to checkout, I need to search for the saved Commit objects and files. 
To do this, I need to:
Look for the unique SHA-1 id of the Commit.
Loop through the Hashmap contents of the commit and get the SHA-1 of all the files I need. 
Use the SHA-1 of all the files to get access to all files.
Deserialize to convert files into original ones. 
