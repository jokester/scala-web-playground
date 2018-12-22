package io.jokester.scala_server_playground.toy

import scala.concurrent.Future

trait ToyRepo {
  def getState: Future[ToyState]
  def mutateState(a: ToyAction): Future[ToyState]
}

case class ToyAction(delta: Int)

case class ToyState(value: Int, revision: Int) {
  def reduce(action: ToyAction) = ToyState(value = action.delta + value, revision = 1 + revision)
}
