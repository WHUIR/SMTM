# SMTM
This is the java implementation of paper [Multi-label Dataless Text Classification with Topic Modeling](https://arxiv.org/abs/1711.01563)
# Requirements
- Java 1.8
- Java Libraries (download `lib.zip`)

# Data Preparation
Dataset directory should look like this: (The code uses both training and testing documents for learning but only computes metrics based on testing data)
```
├──dataset
   ├──train
      ├──[document 1]
      ├──[document 2]
      ├──...
   ├──test
      ├──[document 1]
      ├──[document 2]
      ├──...
```
#### categories file: 
```
[train/test]/[document] [category 1] [category 2] ...
```
For example,
```
train/0001 C1 C2 C3
test/1001 C3 C4
```
says there is a training document `0001` in `train/` with categories `C1` `C2` `C3`, and there is a testing document `1001` in `test/` with categories `C3` `C4`

#### seedwords file:
```
[category 1] [seedword 1] [seedword 2] ...
[category 2] [seedword 1] [seedword 2] ...
...
```
#### document:
```
[token 1] [token 2] ...
```

# How to Run
The main entry is in `src/BiasedGPU.java`. Please specify some paths and the category number before running the code:

- `catsFilePath`: the path of the categories file.
- `dataRootPath`: the root directory of the dataset.
- `seedwordPath`: the path of the seedwords.
- `catNum`: the number of categories.
