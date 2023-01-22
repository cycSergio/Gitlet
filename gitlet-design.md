# Gitlet Design Document

**Name**: cyc

## Classes and Data Structures

### Class 1: Main

#### Fields

1. Field 1
2. Field 2


### Class 2: Repository

This is where the main logic of our program will live. ? I just copy it from Capers Sample.

#### Fields

1. Field 1
2. Field 2
3. Field 3 `public static void initCommand()` When user enter "java gitlet.Main init", this method will be called. 
This is `static` because we want to call this method without newing a Repository object. This method first checks whether
there is already a .gitlet in CWD, if not, then create one, and finally create the initial commit and serialize it into 
the right place.
in .gitlet.



### Class 3: Blob

#### Fields

1. Field 1
2. Field 2


### Class 4: Index

An index object records the filename and the corresponding blob SHA1-id.
#### Fields

1. Field 1
2. Field 2


### Class 5: Branch

#### Fields

1. Field 1
2. Field 2

### Class 6: Commit

#### Fields

1. Field 1
2. Field 2



## Algorithms

## Persistence

