# Gitlet Design Document

**Name**: cyc

## Classes and Data Structures
Include here any class definitions. For each class list the instance variables and 
static variables (if any). Include a brief description of each variable and its 
purpose in the class. Your explanations in this section should be as concise as 
possible. Leave the full explanation to the following sections. You may cut this 
section short if you find your document is too wordy.

### <font color = DarkSeaGreen>Class 1: Main</font>
This is the entry point to our program. It takes in arguments from the command line and
based on the command (the first element fo the `args` array) calls the corresponding
command in `Repository` which will actually execute the logic of the command. [Not yet] It also
validates the arguments based on the command to ensure that enough arguments were
passes in.
#### Fields
This class has no fields and hence no associated state: it simply validates arguments
and defers the execution to the `Repository` class.


### <font color = DarkSeaGreen>Class 2: Repository</font>

This is where the main logic of our program will live. This file will handle all 
the actual gitlet commands.


#### Fields

1. Field 1 `public static final File CWD = new File(System.getProperty("user.dir"));`<br>
The Current Working Directory. 


2. Field 2 `public static final File GITLET_DIR = join(CWD, ".gitlet");` <br>
The .gitlet directory.


3. Field 3 `public static final File blobs = join(GITLET_DIR, "blobs");`


4. Field 4 `public static final File commits = join(GITLET_DIR, "commits");`
5. Field 5 `public static final File Staging_Area = join(GITLET_DIR, "index");` <br>
The Staging Area File. And I want to make it a HashMap for holding later indexes.
In the HashMap, file name is the key, and the sha1 value of the file content is the
corresponding value.
   

6. Field 6 `public static final File HEAD = join(GITLET_DIR, "HEAD");` <br>
The HEAD pointer File. The content in this file is the branch name of the currently
active branch. For example, if now master is the active branch, then HEAD contains
the String "master".


7. Field 7 `public static final File BRANCH = join(GITLET_DIR, "Branch_heads");` <br>
The branch directory. Branch objects will be placed there.Each branch object points to
the most front commit in this branch.

      


   
   
   
   
   



### Class 3: Blob

#### Fields

1. Field 1
2. Field 2


### Class 4: Index

An index object records the filename and the corresponding blob SHA1-id.
#### Fields

1. Field 1
2. Field 2


### <font color = DarkSeaGreen>Class 5: Branch</font>
This class provide a way to create a Branch object.

#### Fields

<font color = LightSkyBlue>**1. Field 1**</font> `private final String branchName`
<br> The name of a branch, e.g. master as the default branch name.

<font color = LightSkyBlue>**2. Field 1**</font> `private String branchName`
<br> The commit's sha1 value that this branch is currently pointing at.

### Class 6: Commit

#### Fields

<font color = LightSkyBlue>**1. Field 1**</font> `private final String message` <br>
The message for this commit from the user.

<font color = LightSkyBlue>**2. Field 1**</font> `private final Date timestamp` <br>
The time at which this commit is created.

<font color = LightSkyBlue>**3. Field 1**</font> `private final HashMap<String, String> fileToBlob` <br>
The mapping of file names to blob references for this commit.

<font color = LightSkyBlue>**4. Field 1**</font>  `private final String parent` <br>
The sha1 value of the parent commit of this commit.



## Algorithms
This is where you tell us how your code works. For each class, include a high-level 
description of the methods in that class. That is, do not include a line-by-line 
breakdown of your code, but something you would write in a javadoc comment above a 
method, including any edge cases you are accounting for. We have read the project 
spec too, so make sure you do not repeat or rephrase what is stated there. This 
should be a description of how your code accomplishes what is stated in the spec.

The length of this section depends on the complexity of the task and the complexity 
of your design. However, simple explanations are preferred. Here are some formatting 
tips: 

For complex tasks, like determining merge conflicts, we recommend that you split the 
task into parts. Describe your algorithm for each part in a separate section. Start 
with the simplest component and build up your design, one piece at a time. For 
example, your algorithms section for Merge Conflicts could have sections for:
- Checking if a merge is necessary.
- Determining which files (if any) have a conflict.
- Representing the conflict in the file.

Try to clearly mark titles or names of classes with white space or some other symbols.

### <font color = DarkSeaGreen>Class 1: Main</font>

### <font color = DarkSeaGreen>Class 2: Repository</font>
<font color = LightSkyBlue>**1. Method 1**</font> `public static void initCommand()` <br>
First note that it's a static method because We need to call this method without 
newing an object of Repository.
The function of this method is threefold:
- Checking if there's already a gitlet system.If so, fail.
- Actually creating all those directories and files that's need in the .gitlet/.
- Creating the initial commit by using constructor from the [Commit class](#Commit) and serialize it.

<font color = LightSkyBlue>**2. Method 2**</font> `public static void addCommand(String addFile)` <br>
In this method:
- Checking if the addFile exists in CWD. <br>
 The two cases here: 1. addFile has never existed in CWD. 2. user wants to stage this file for removal. <br>
 If there's no addFile in CWD and in HEAD commit's tracking HashMap there's also no<br>
 addFile as a key in it, then it's case 1, send a message "File does not exist.". Else, it's case 2.
- Handling the removal case. <br>
  Since in this case, addFile is staged for removal, we don't have to change the blobs/
  directory at all. All we need to do is to properly modify the Staging Area. We first
  create a removal Index object, and add it to Staging Area (or overwrite the same name
  one is there is already an addFile in Staging Area).

- Handle the addition case. <br>
  This can then be separated into three parts:
  1. Checking if current commit has a same tracking as addFile by comparing their sha1 value.
  2. Making a corresponding Blob object into the .gitlet/blobs directory.
  3. Making an addition Index object into the Staging Area HashMap. If the current Staging
  Area is already having an entry whose key is addFile, then just overwrites it with the
  new sha1 value as the key's value.

<font color = LightSkyBlue>**3. Method 3**</font> `public static void commitCommand(String message)` <br>
By default, tracking for new commit is from the current(parent) commit's tracking.<br>
This method:
- Correctly merging the default tracking with the Staging Area's indexes to form a 
correct tracking for this new commit. This is done by a helper method `buildIndexes`.
- Creating a new Commit object with the new made tracking.
- Clearing the Staging Area after each commit.
- Moving the HEAD and master pointers


<font color = LightSkyBlue>**4. Method 4**</font> `private static HashMap<String, String> buildIndexes(HashMap<String, String> ParentHM, HashMap<String, String> SA)` <br>
This method iterates the current indexes(which contains filename as key and content sha1 as value) in Staging Area:
- if file does not exist in CWD, it means it's the removal case. Just remove this entry from the parentHM
- Else, add or replace this entry into the parentHM
- return the modified parentHM, this is the new tracking HashMap.

<font color = LightSkyBlue>**5. Method 5**</font> `public static void logCommand()`<br>
This method has a `while` loop to iterate from the current commit to the initial commit.
<br>In each iteration, the block inside `while` print out the information about this
commit, and the update this current to its parent commit.

<font color = LightSkyBlue>**6. Method 6**</font> `public static void checkout(String filename)`
<br> This is the first usage of the checkout command.
<br> Get the current commit, and get the corresponding sha1 of [filename] from it.
<br> Then get the corresponding Blob object and get the content of [filename].
<br> Overwrite the content to CWD.

<font color = LightSkyBlue>**7. Method 7**</font> `public static void checkout(String commitId, String filename)`
<br> This method is almost the same as Method6, except that it depends on the specified
commit.

<font color = LightSkyBlue>**8. Method 8**</font> `private static String getCurCommitSha1()`
<br> This helper method gets the sha1 of the current commit.
- Read in the content of HEAD, which is the name of the currently active branch.
- Get the currently active Branch object.
- From the Branch object get the sha1 of the commit that is currently pointed at.


### <font color = DarkSeaGreen>Class 5: Branch</font>
#### <font color = LightSkyBlue><strong>Constructors</strong></font>
<font color = LightSkyBlue><strong>1. Constructor 1</strong></font> `public Branch(String initialCommitSha1)`
<br> This constructor is to create the default master branch object. So the branch name is
just "master" and has the initial commit's sha1. In `initCommand`, this default master
branch is created in .gitlet/Branch_heads/master.

#### <font color = LightSkyBlue><strong>Methods</strong></font>

<font color = LightSkyBlue><strong>1. Method 1</strong></font> `public void move(String newCommitSha1)`
<br> This method change the branch's `branchCommitSha1` to `newCommitSha1` when a new 
commit was made. In other words, it moves the branch pointer after a commit.


### <span id = "Commit"><font color = DarkSeaGreen>Class 6: Commit</font> </span>
#### <font color = LightSkyBlue><strong>Constructors</strong></font>

<font color = LightSkyBlue><strong>1. Constructor 1</strong></font> `public Commit()` <br>
First of all, this is public because I want to new an object of this outside the Commit
Class, such as in the Repository file. But when I refactor this project, I may want to
make this constructor private and provide a public method to new an object out of this.

This constructor is to create an initial commit, which has an empty tracking HashMap 
and no parent.

#### <font color = LightSkyBlue><strong>Methods</strong></font>

<font color = LightSkyBlue><strong>1. Method 1</strong></font>


## Persistence

## Helper
### <font color = DarkSeaGreen>Note 1: Common Paradigms</font>
<font color = pink>Read - Modify - Write</font> <br>
Remember the haveBirthday command from Lab6:
1. <font color = pink>Read</font> in the Dog object.
2. <font color = pink>Modify</font> the Dog object.
3. <font color = pink>Write</font> the Dog object back to its file.

Consider helper functions since you'll do this a lot.

EXTRA: `add-remote`, `rm-remote`, `push`, `fetch`, `pull`. All the commands are significantly simplified
from their git equivalents.

