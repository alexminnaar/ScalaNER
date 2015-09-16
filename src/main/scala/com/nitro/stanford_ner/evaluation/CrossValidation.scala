package com.nitro.stanford_ner.evaluation

import java.io.{File, PrintWriter}

import com.nitro.stanford_ner.evaluation.CrossValidation.Example
import com.nitro.stanford_ner.ner.{EntityToken, EntityExtraction}
import com.nitro.stanford_ner.training.CrfTrainer
import edu.stanford.nlp.ie.crf.CRFClassifier

import scala.io.Source

case class CvFold(examples: Seq[Example])

case class ClassifierPerformance(precision: Double, recall: Double)

/*
    N-fold cross-validation for the Stanford CoreNLP NER tool.  Currently only works for the 2 entity class case.
 */
object CrossValidation {

  type Example = String

  private[this] def writeFolds(writeLocationDirectory: String, folds: Seq[CvFold]): Unit = {

    folds.zipWithIndex.foreach { case (fd, idx) =>

      val trainingSet = folds
        .diff(Seq(fd))
        .flatMap(_.examples)

      //write validation set for this fold
      val valSetPr = new PrintWriter(new File(writeLocationDirectory + s"/val_set_${idx}.txt"))
      println(s"printing validation set ${idx}")
      fd.examples.foreach(valSetPr.println)
      valSetPr.close()


      //write training set for this fold
      val trainingSetPr = new PrintWriter(new File(writeLocationDirectory + s"/training_set_${idx}.txt"))
      println(s"printing training set ${idx}")
      trainingSet.foreach(trainingSetPr.println)
      trainingSetPr.close()

    }

  }


  def runXVal(numFolds: Int
              , trainingDataLocation: String
              , foldWriteLocation: String): Seq[ClassifierPerformance] = {

    val trainingData: Seq[Example] = Source.fromFile(trainingDataLocation)
      .getLines()
      .toSeq

    val foldSize = Math.ceil(trainingData.size.toDouble / numFolds)

    val folds = trainingData
      .grouped(foldSize.toInt)
      .map(CvFold)
      .toSeq

    //write folds that we are going to read from during x-validation
    writeFolds(foldWriteLocation, folds)

    //train on training sets, test on validation sets
    (0 to numFolds - 1).map { foldNum =>

      //train + serialize CRF model as side effect
      CrfTrainer.trainClassifier(
        foldWriteLocation + s"/training_set_${foldNum}.txt",
        foldWriteLocation + s"ner_model_${foldNum}.ser.gz"
      )

      //test on validation set to return precision/recall performance
      testModel(
        foldWriteLocation + s"ner_model_${foldNum}.ser.gz",
        foldWriteLocation + s"/val_set_${foldNum}.txt"
      )

    }

  }


  def testModel(modelLocation: String, testSetLocation: String): ClassifierPerformance = {

    val crfClassifier = CRFClassifier.getClassifier(modelLocation)

    val testData = Source.fromFile(testSetLocation)
      .getLines()
      .map(l => l.split("\t"))
      .toSeq

    //Here I am assuming that concatenating tokens with whitespace delimiters preserves the original tokenization.
    val tokenPredictions = EntityExtraction.extract(crfClassifier, testData.map(_.head).mkString(" "))
      .flatMap(x => x.EntityTokens)

    val predictionError = testData
      .zip(tokenPredictions)
      .foldLeft((0.0, 0.0, 0.0)) { (stats: (Double, Double, Double), errorEg: (Array[String], EntityToken)) =>

      //make sure we are comparing predicted class and actual for the same token
      assert(errorEg._1.head == errorEg._2.token)

      val target = errorEg._1.last
      val prediction = errorEg._2.entity

      var tp = stats._1
      var fp = stats._2
      var fn = stats._3

      //We don't really care about true negatives at the moment
      if ((target == prediction) && target != "O") {
        tp += 1
      } else if (target == "O" && prediction != "O") {
        fp += 1
      } else fn += 1

      (tp, fp, fn)
    }

    ClassifierPerformance(
      predictionError._1 / (predictionError._1 + predictionError._2),
      predictionError._1 / (predictionError._1 + predictionError._3)
    )

  }


}

