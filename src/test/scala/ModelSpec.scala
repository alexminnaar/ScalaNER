import com.nitro.stanford_ner.ner.EntityExtraction
import edu.stanford.nlp.ie.crf.CRFClassifier
import org.scalatest.{Matchers, WordSpec}

class ModelSpec extends WordSpec with Matchers {


  "Stanford's Pre-trained models" should {

    "find the correct named entities" in {

      val text = "Sarah went to work at the FBI"

      val myCRF = CRFClassifier.getClassifier("stanford_models/english.all.3class.distsim.crf.ser.gz")

      val entities = EntityExtraction.extract(myCRF, text)

      val entityPredictions = entities
        .flatMap(_.EntityTokens.map(_.entity))

      entityPredictions should equal(Seq("PERSON", "O", "O", "O", "O", "O", "ORGANIZATION"))

    }

  }

}
