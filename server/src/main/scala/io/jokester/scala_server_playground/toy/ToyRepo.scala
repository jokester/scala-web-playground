package io.jokester.scala_server_playground.toy

import scala.concurrent.Future

trait ToyRepo {
  def getState: Future[ToyState]

  def mutateState(a: ToyAction): Future[ToyState]
}
