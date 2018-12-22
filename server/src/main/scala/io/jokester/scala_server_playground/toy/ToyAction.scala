package io.jokester.scala_server_playground.toy

case class ToyAction(delta: Int)

case class ToyState(value: Int, revision: Int) {
  def reduce(action: ToyAction) = ToyState(value = action.delta + value, revision = 1 + revision)
}
