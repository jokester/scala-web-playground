package io.jokester.scala_server_playground.toy

import scala.concurrent.Future

class ToyRaceMemoryRepo extends ToyRepo {
  var state = ToyState(0, 0)

  override def getState: Future[ToyState] = Future.successful(state)

  override def mutateState(a: ToyAction): Future[ToyState] = Future.successful {
    val newState = state.reduce(a)
    state = newState
    newState
  }
}

class ToySynchronizedMemoryRepo extends ToyRepo {
  var state = ToyState(0, 0)

  override def getState: Future[ToyState] = Future.successful {
    synchronized {
      state
    }
  }

  override def mutateState(action: ToyAction): Future[ToyState] = Future.successful {
    synchronized {
      val newState = state.reduce(action)
      state = newState
      newState
    }
  }
}