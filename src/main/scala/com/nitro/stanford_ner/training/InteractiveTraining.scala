package com.nitro.stanford_ner.training

import java.io.{FileWriter, PrintWriter, File}
import scala.util.control.Breaks._

import scala.io.{Source, StdIn}

/*
  An interactive app that presents the user with a document and allows them to annotate each token as a named entity
  type.  By convention, an annotation of "O" means the token is not a named entity.

  Documents are presented to the user line-by-line.  For convenience, hitting "ENTER" indicates the current token is
  not a named entity and is automatically annotated with a "O".  Also for convenience, if the user sees there are no
  named entities on the current line, they may type "next", in which case all remaining tokens on the current line are
  automatically annotated with "O" and the next line is presented.

  All annotations are written to file in a format that can be read by the Stanford NER tool for training.
 */
object InteractiveTraining extends App {

  //File path+name to write annotations
  val annotationsWriteFile = args(0)
  //Location of directory containing documents on which to perform annotations.
  val documentDirectory = args(1)

  val annotationsFile = new FileWriter(annotationsWriteFile, true)

  val pr = new PrintWriter(annotationsFile)

  val docFiles = new File(documentDirectory)
    .listFiles()
    .filter(f => f.getName != ".DS_Store")  //I hate this file


  docFiles.foreach { f =>

    val lines = Source.fromFile(f, "ISO-8859-1").getLines()

    println(s"Begin NER annotation for file ${f.getName}")

    var linesSoFar: Vector[String] = Vector.empty

    while (lines.hasNext) {

      print("\u001b[2J" + "\u001b[H")

      val line = lines.next()

      val tokens = Tokenizer.tokenize(line)

      breakable {
        tokens.zipWithIndex.foreach { case (token, idx) =>

          linesSoFar.foreach(println)

          println(tokens.take(idx).mkString(" ") + " <" + token + "> " + tokens.drop(idx + 1).mkString(" ") + "\n")
          val response = StdIn.readLine(s"Enter entity type of  <${token}>  token >> ")

          response match {
            case "" => pr.println(token + "\t" + "O")
            case "next" => {
              tokens.drop(idx).foreach { tok =>
                pr.println(tok + "\t" + "O")
                pr.flush()
              }
              linesSoFar :+= line
              print("\u001b[2J" + "\u001b[H")
              break()
            }
            case _ => pr.println(token + "\t" + response)
          }

          pr.flush()
          print("\u001b[2J" + "\u001b[H")
        }

        linesSoFar :+= line

      }
    }
  }

  pr.close()

}
