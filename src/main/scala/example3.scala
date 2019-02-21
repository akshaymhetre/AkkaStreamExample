import SampleApp.{done, system}
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, RunnableGraph, Sink}
import example1.{done, system}

import scala.concurrent.Future
/*
* So far we’ve been only processing data using Flows and consuming it into some kind of external Sink -
* be it by printing values or storing them in some external system. However sometimes we may be interested in some value
* that can be obtained from the materialized processing pipeline. For example, we want to know how many tweets we have processed.
* */
object example3 extends App {
  implicit val system = ActorSystem("reactive-tweets")
  implicit val materializer = ActorMaterializer()

  import TweetModel._

  val count: Flow[Tweet, Int, NotUsed] = Flow[Tweet].map(_ ⇒ 1)

  val sumSink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)

  val counterGraph: RunnableGraph[Future[Int]] =
    tweets
      .via(count)
      .toMat(sumSink)(Keep.right)

  val sum: Future[Int] = counterGraph.run()

  implicit val ec = system.dispatcher
  sum.foreach(c ⇒ println(s"Total tweets processed: $c"))
}
