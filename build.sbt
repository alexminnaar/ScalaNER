name := "stanford-ner"

version := "1.0"

scalaVersion := "2.11.7"


libraryDependencies++=Seq(
"edu.stanford.nlp" % "stanford-corenlp" % "3.5.2",
"org.scalatest" %% "scalatest" % "3.0.0-M9"
)
    