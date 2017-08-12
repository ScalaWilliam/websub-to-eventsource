import controllers.Main
import org.jsoup.Jsoup
import org.scalatest._
import org.scalatest.Matchers._

class LinkParseTest extends FreeSpec {
  "It parses" in {
    val link =
      """<https://websub.rocks/blog/100/sLBtEg4RffXAsPQURzzh/hub>; rel="hub""""
    val Main.matchLink(a, b) = link

    a shouldBe "https://websub.rocks/blog/100/sLBtEg4RffXAsPQURzzh/hub"
    b shouldBe "hub"
  }
  "It parses document" in {
    val doc =
      """
        |<head>
        |      <link rel="self" href="https://git.digitalocean.scalawilliam.com/"/>
        |      <link rel="hub" href="https://switchboard.p3k.io/"/>
        |</head>
      """.stripMargin
    Main
      .extractFromDocument(Jsoup.parse(doc))
      .get shouldBe ("https://switchboard.p3k.io/" -> "https://git.digitalocean.scalawilliam.com/")
  }
}
