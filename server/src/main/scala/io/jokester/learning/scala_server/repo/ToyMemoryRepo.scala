package io.jokester.learning.scala_server.repo

import scala.concurrent.Future

class ToyRaceMemoryRepo extends ToyRepo {
  var state = new ToyState(0, 0)

  override def getS: Future[ToyState] = Future.successful(state)

  override def reduceS(a: ToyAction): Future[ToyState] = Future.successful {
    val newState = state.reduce(a)
    state = newState
    newState
  }
}

class ToySynchronizedMemoryRepo extends ToyRepo {
  var state = new ToyState(0, 0)

  override def getS: Future[ToyState] = Future.successful {
    synchronized {
      state
    }
  }

  override def reduceS(action: ToyAction): Future[ToyState] = Future.successful {
    synchronized {
      val newState = state.reduce(action)
      state = newState
      newState
    }
  }
}