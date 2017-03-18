package unparsing

import spire.math.Complex

class SpireSuite extends CommonSuite {

  import SpireDefaults._

  test("Complex pretty print") {

    PrettyPrint.pretty(Complex[Int](2, -1)) shouldBe "2-1i"

  }

}
