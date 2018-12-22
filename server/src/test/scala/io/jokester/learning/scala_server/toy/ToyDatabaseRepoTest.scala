package io.jokester.learning.scala_server.toy

import io.jokester.learning.scala_server.repo.{ ToyAction, ToyDatabaseRepo }
import org.scalatest.{ AsyncWordSpec, Matchers }
import scalikejdbc.config.DBs
import scalikejdbc.{ ConnectionPool, DB }

class ToyDatabaseRepoTest extends AsyncWordSpec with Matchers {

  DBs.setupAll

  "ToyDatabaseRepo" should {
    val cpool = ConnectionPool('default)

    val repo = new ToyDatabaseRepo(() => DB(cpool.borrow))

    "return last" in {
      val futureS1 = repo.getS
      val futureS2 = repo.reduceS(ToyAction(5))
      val futureS3 = repo.getS

      for (s1 <- futureS1; s2 <- futureS2; s3 <- futureS3) yield {
        s2.value shouldBe (s1.value + 5)
        s3 shouldEqual s2
      }
    }

  }
}
