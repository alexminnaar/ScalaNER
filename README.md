#stanford-ner

Train, evaluate, run, and annotate using the [Stanford NER tool](http://nlp.stanford.edu/software/CRF-NER.shtml) in Scala.

##Annotation
Use the ```InteractiveTraining``` command line annotation app for named entity annotation. During annotation, the results are constantly flushed to a text file that records all annotations in a tab-separated format that can the be read by the NER tool to train a model.

##Training
Given some training data (possible obtained by using the above annotation tool), you can train a new NER model using ```CrfTrainer```.  For example,

```scala
CrfTrainer.trainClassifier("training/data/file.txt","model/save/location.ser.gz")
```

Where the first argument is your training data location and the second argument is where you want to save your trained model.

##Running
So you have trained your model and now you want to run it on some text and extract the named entities within.  This can be done with ```EntityExtraction```.  For example,

```scala
val entities=EntityExtraction.extract(myNerModel, myText)
```

where the first argument is your trained ner model and the second argument is the text.  The result is a ```Seq[SentenceEntityTokens]``` which splits ```myText``` into sentences.  Each sentence is a sequence of tokens and corresponding predicted entity types.

##Evaluating
So you have your training data but you want to know how well the resulting NER model will perform in general.  In order to do this you can perform k-fold cross-validation using ```CrossValidation```.  For example,

```scala
val xValResults=CrossValidation.runXVal(numFolds, "training/data/file.txt", "fold/write/location")
```

Where ```numFolds``` is the number of cross-validation folds, ```"training/data/file.txt"``` is the training data file, and ```"fold/write/location"``` is the directory where the training and validation sets will be written.  Note:  This only works if for the case of 2 entity types right now.
