package io.jokester.learning.scala_server.db

import io.jokester.learning.scala_server.repo.ToyState
import org.slf4j.{ Logger, LoggerFactory }
import scalikejdbc._
import scalikejdbc.config.DBs

trait DBFacade {

  def logger: Logger

  DBs.setupAll()

  def tryConn(): Unit = {
    using(ConnectionPool.borrow()) { conn =>
      logger.debug(s"got connection {}", conn)
    }

    DB readOnly { implicit session =>
      val t = ToyState.syntax("t")
      val result = sql"select ${t.result.*} from ${ToyState.as(t)}"
        .map(rs => ToyState(t.resultName)(rs))
        .list.apply()
      logger.debug("select 1: {}", result: Any)
    }

    DB readOnly { implicit session =>
      val t = ToyState.syntax("t")

      val results = withSQL {
        select.from(ToyState as t)
          .orderBy(ToyState.column.revision).desc
          .limit(1)
      }.map(ToyState(t.resultName)).first().apply()

      logger.debug("select 2: {}", results: Any)
    }

    DB localTx { implicit session =>
      val t = ToyState.syntax("t")
      val c = ToyState.column

      val results = withSQL {
        insertInto(ToyState)
          .namedValues(
            c.revision -> 555,
            c.value -> 1)
      }.update().apply()

      logger.debug("insert 1: {}", results: Any)
    }
  }
}

object DbF extends DBFacade {
  lazy val logger = LoggerFactory.getLogger(getClass)
}

//case class Group(id: Long, name: String)
//case class GroupMember(id: Long, name: String,
//  groupId: Option[Long] = None, group: Option[Group] = None)
//
//object Group extends SQLSyntaxSupport[Group] {
//
//  // If you need to specify schema name, override this
//  // def table will return sqls"public.groups" in this case
//  // Of course, schemaName doesn't work with MySQL
//  override val schemaName = Some("public")
//
//  // If the table name is same as snake_case'd name of this companion object,
//  // you don't need to specify tableName explicitly.
//  override val tableName = "groups"
//
//  // If you use NamedDB for this entity, override connectionPoolName
//  //override val connectionPoolName = 'anotherdb
//
//  def apply(g: ResultName[Group])(rs: WrappedResultSet) =
//    new Group(rs.long(g.id), rs.string(g.name))
//}
//
//object GroupMember extends SQLSyntaxSupport[GroupMember] {
//  def apply(m: ResultName[GroupMember])(rs: WrappedResultSet) =
//    new GroupMember(rs.long(m.id), rs.string(m.name), rs.longOpt(m.groupId))
//
//  def apply(m: ResultName[GroupMember], g: ResultName[Group])(rs: WrappedResultSet) = {
//    apply(m)(rs).copy(group = rs.longOpt(g.id).map(_ => Group(g)(rs)))
//  }
//}