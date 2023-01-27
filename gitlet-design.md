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
The HEAD pointer File. The content in this file is the sha1 value of the HEAD commit.


7. Field 7 `public static final File master = join(GITLET_DIR, "master");` <br>
The master pointer File. The content in this file is the sha1 value of the HEAD commit.

      


   
   
   
   
   



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

### <span id = "Commit"><font color = DarkSeaGreen>Class 3: Commit</font> </span>
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