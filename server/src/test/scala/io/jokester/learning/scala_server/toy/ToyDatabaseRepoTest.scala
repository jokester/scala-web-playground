package io.jokester.learning.scala_server.toy

import io.jokester.scala_server_playground.hello.repo.ToyAction
import io.jokester.scala_server_playground.toy.ToyDatabaseRepo
import org.scalatest.{AsyncWordSpec, Matchers}
import scalikejdbc.config.DBs
import scalikejdbc.{ConnectionPool, DB}

class ToyDatabaseRepoTest extends AsyncWordSpec with Matchers {

  DBs.setupAll

  "ToyDatabaseRepo" should {
    val cpool = ConnectionPool('default)

    val repo = new ToyDatabaseRepo(() => DB(cpool.borrow))

    "return last" in {
      val futureS1 = repo.getState
      val futureS2 = repo.mutateState(ToyAction(5))
      val futureS3 = repo.getState

      for (s1 <- futureS1; s2 <- futureS2; s3 <- futureS3) yield {
        s2.value shouldBe (s1.value + 5)
        s3 shouldEqual s2
      }
    }

  }
}
