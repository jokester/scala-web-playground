package io.jokester.learning.scala_server.repo

import io.jokester.learning.scala_server.util.Logging
import scalikejdbc._

import scala.concurrent.Future

class ToyDatabaseRepo(db: () => DB) extends ToyRepo with Logging {

  private val t = ToyState.syntax("t")
  private val c = ToyState.column

  private def syncGetS(): ToyState = db() readOnly { implicit session =>
    val s = withSQL {
      select.from(ToyState as t)
        .orderBy(ToyState.column.revision).desc
        .limit(1)
    }.map(ToyState(t.resultName)).first().apply().get
    s
  }

  private def syncInsertS(newState: ToyState) = db() localTx { implicit session =>
    withSQL {
      insertInto(ToyState)
        .namedValues(c.value -> newState.value, c.revision -> newState.revision)
    }.update().apply()
  }

  /**
   * OrigSQL:
   * @param a
   * @return
   */
  def syncReduceS(a: ToyAction): ToyState = db() localTx { implicit session =>
    """
      |INSERT INTO toystate as FFF (revision, value)
      |    (SELECT 1 + revision revision, 2 + value FROM toystate ORDER BY revision DESC LIMIT 1)
      |RETURNING *;
    """.stripMargin

    sql"""
    INSERT INTO ${ToyState as t}
        (SELECT (1 + revision) revision, (${} + value) value FROM toystate ORDER BY revision DESC LIMIT 1)
    RETURNING *
    """.map(ToyState(t.resultName)).first().apply().get
  }

  override def getS: Future[ToyState] = Future.successful {
    syncGetS()
  }

  override def reduceS(a: ToyAction): Future[ToyState] = Future.successful {
    val s = syncGetS()
    val newState = s.reduce(a)
    val inserted = syncInsertS(newState)
    newState
  }
}
