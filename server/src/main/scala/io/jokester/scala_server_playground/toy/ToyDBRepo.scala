package io.jokester.scala_server_playground.toy

import com.typesafe.scalalogging.LazyLogging
import scalikejdbc._

import scala.concurrent.Future

object ToyStateDB {

  object ToyState extends SQLSyntaxSupport[ToyState] {
    override val tableName = "toystate"

    def apply(g: ResultName[ToyState])(rs: WrappedResultSet) =
      new ToyState(rs.int(g.value), rs.int(g.revision))
  }

  val t: scalikejdbc.QuerySQLSyntaxProvider[scalikejdbc.SQLSyntaxSupport[ToyState], ToyState] = ToyState.syntax("t")
  val c: scalikejdbc.ColumnName[ToyState] = ToyState.column
}

class ToyDBRepoBasic(db: () => DB) extends ToyRepo with LazyLogging {

  import ToyStateDB._

  private def syncGetState(): ToyState = db() readOnly { implicit session =>
    sql"""
     SELECT * FROM toystate ORDER BY revision DESC LIMIT 1 ;
      """.map(ToyState(t.resultName)).first().apply().get
  }

  private def syncInsert(s: ToyState): ToyState = db() localTx { implicit session =>
    sql"""
    INSERT INTO ${ToyState as t} VALUE (${s.revision}, ${s.value})
    RETURNING *;
    """.map(ToyState(t.resultName)).first().apply().get
  }

  override def getState: Future[ToyState] = Future.successful(syncGetState())

  override def mutateState(a: ToyAction): Future[ToyState] = db() localTx { implicit session =>
    val s = syncGetState()
    val newState = s.reduce(a)
    val inserted = syncInsert(newState)
    Future.successful(inserted)
  }
}

class ToyDBRepoNolock(db: () => DB) extends ToyDBRepoBasic(db) with LazyLogging {

  import ToyStateDB._

  /**
   * OrigSQL:
   *
   * @param a
   * @return
   */
  private def syncMutate(a: ToyAction): ToyState = db() localTx { implicit session =>
    sql"""
    INSERT INTO ${ToyState as t}
        (SELECT (1 + revision) revision, (${a.delta} + value) value FROM toystate ORDER BY revision DESC LIMIT 1)
    RETURNING *;
    """.map(ToyState(t.resultName)).first().apply().get
  }

  override def mutateState(a: ToyAction): Future[ToyState] = db() localTx { implicit session =>
    val newState = syncMutate(a)
    Future.successful(newState)
  }
}