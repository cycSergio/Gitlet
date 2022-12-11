# Gitlet Design Record

**Name**: cyc

## Checkpoint
**Time**: 12/6

**Status**: Today is Tuesday. Finish Checkpoint part before this week ends! I have 6 commands to implement.
### * 1. init

 **init** creates a new Gitlet version-control system in the current directory. This system will automatically start with one commit(with the message 'initial commit'').
1. I want to write this init method, but where should I write it?

2. Since init will create the initial commit, ~~maybe I should let init be an instance method in the Commit Class?~~

According to 'Get started' video2, init method should be in the Repository Class. And it's reasonable.
3. So let me try writing Commit first.


### * 2. add
### * 3. commit

1. Commit should be an object.
2. A commit should be serialized, but where? In the constructor or in other method that calls this constructor?


### * 4. checkout -- [file name]
### * 5. checkout [commid id] -- [file name]
### * 6. log


