package io.jokester.scala_server_playground.toy

import com.typesafe.scalalogging.LazyLogging
import scalikejdbc._

import scala.concurrent.Future

object ToyStateDB {

  object ToyState extends SQLSyntaxSupport[ToyState] {
    override val tableName = "toystate"

    def apply(g: ResultName[ToyState])(rs: WrappedResultSet) =
      new ToyState(value = rs.int(g.value), revision = rs.int(g.revision))

    // NOT scalike-jdbc way: but works
    def apply(rs: WrappedResultSet) =
      new ToyState(value = rs.int("value"), revision = rs.int("revision"))
  }

}

class ToyDBRepoBasic(db: () => DB) extends ToyRepo with LazyLogging {

  import ToyStateDB._

  private def syncGetState(): ToyState = db() readOnly { implicit session =>
    val t = ToyState.syntax("t")
    sql"""
     SELECT ${t.result.*} FROM ${ToyState as t} ORDER BY revision DESC LIMIT 1 ;
      """.map(ToyState(t.resultName)).first().apply().get
  }

  private def syncInsert(s: ToyState): Unit = db() localTx { implicit session =>
    val t = ToyState.syntax("t")
    sql"""
    INSERT INTO toystate VALUES (${s.revision}, ${s.value});
    """.update().apply()
    s
  }

  override def getState: Future[ToyState] = Future.successful(syncGetState())

  override def mutateState(a: ToyAction): Future[ToyState] = db() localTx {
    implicit session =>
      val s = syncGetState()
      val newState = s.reduce(a)
      syncInsert(newState)
      Future.successful(newState)
  }
}

class ToyDBRepoNolock(db: () => DB)
    extends ToyDBRepoBasic(db)
    with LazyLogging {

  import ToyStateDB._

  /**
    * OrigSQL:
    *
    * @param a
    * @return
    */
  private def syncMutate(a: ToyAction): ToyState = db() localTx {
    implicit session =>
      val t = ToyState.syntax("t")

      sql"""
    INSERT INTO toystate
        (SELECT (1 + revision) AS revision, (${a.delta} + value) AS value FROM toystate ORDER BY revision DESC LIMIT 1)
    RETURNING *;
    """.map(ToyState(_)).first().apply().get
  }

  override def mutateState(a: ToyAction): Future[ToyState] = db() localTx {
    implicit session =>
      val newState = syncMutate(a)
      Future.successful(newState)
  }
}
