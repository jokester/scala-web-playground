package io.jokester.learning.scala_server.repo

import scalikejdbc._

import scala.concurrent.Future

trait ToyRepo {
  def getS: Future[ToyState]

  def reduceS(a: ToyAction): Future[ToyState]
}

case class ToyAction(delta: Int)

case class ToyState(value: Int, revision: Int) {

  def reduce(action: ToyAction) = new ToyState(value = action.delta + value, revision = 1 + revision)
}

object ToyState extends SQLSyntaxSupport[ToyState] {

  override val tableName = "toystate"

  def apply(g: ResultName[ToyState])(rs: WrappedResultSet) =
    new ToyState(rs.int(g.value), rs.int(g.revision))
}