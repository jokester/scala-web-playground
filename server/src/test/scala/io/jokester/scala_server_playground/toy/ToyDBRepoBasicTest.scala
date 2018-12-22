package io.jokester.scala_server_playground.toy

import org.scalatest.{ AsyncWordSpec, Matchers }
import scalikejdbc.config.DBs
import scalikejdbc.{ ConnectionPool, DB }

class ToyDBRepoBasicTest extends AsyncWordSpec with Matchers {

  DBs.setupAll

  "ToyDatabaseRepo" should {
    val cpool = ConnectionPool('default)

    testRepo(new ToyRepoMemoryUnsafe)
    testRepo(new ToyRepoMemorySynced)
    testRepo(new ToyDBRepoBasic(() => DB(cpool.borrow)))
    testRepo(new ToyDBRepoNolock(() => DB(cpool.borrow)))
  }

  def testRepo(repo: ToyRepo): Unit = {
    s"return updated state for ${repo.getClass}" in {
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
