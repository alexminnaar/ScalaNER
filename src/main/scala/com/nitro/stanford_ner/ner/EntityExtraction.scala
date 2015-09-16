package com.nitro.stanford_ner.ner

import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.{CoreAnnotations, CoreLabel}
import scala.collection.JavaConversions._


case class EntityToken(entity: String,
                       token: String,
                       tokenIdx: Int)

case class SentenceEntityTokens(EntityTokens: Seq[EntityToken],
                                sentenceIdx: Int)

object EntityExtraction {

  /*
    Given an NER model and some text, extract the NE tokens within the text.
   */
  def extract(nerModel: CRFClassifier[CoreLabel], text: String): Seq[SentenceEntityTokens] = {

    //classify text
    val sentences = nerModel
      .classify(text)
      .toSeq

    //wrap named entities in appropriate case classes
    sentences.zipWithIndex.map { case (sentence, sIdx) =>

      SentenceEntityTokens(
        sentence.zipWithIndex.map { case (tok, tIdx) =>

          EntityToken(
            tok.get(classOf[CoreAnnotations.AnswerAnnotation]),
            tok.word(),
            tIdx
          )

        }.toSeq,
        sIdx
      )

    }

  }

}
