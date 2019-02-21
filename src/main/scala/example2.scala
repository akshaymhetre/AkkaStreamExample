import SampleApp.{done, system}
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink}
import example1.{done, system}

import scala.concurrent.Future

object example2 extends App {
  implicit val system = ActorSystem("reactive-tweets")
  implicit val materializer = ActorMaterializer()

  import TweetModel._

  val writeAuthors: Sink[Author, Future[Done]] = Sink.foreach[Author](author => println(s" Author : ${author}"))
  val writeHashtags: Sink[Hashtag, Future[Done]] = Sink.foreach[Hashtag](hashtag => println(s" Hashtag : ${hashtag}"))

  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val bcast = b.add(Broadcast[Tweet](2))
    tweets ~> bcast.in
    bcast.out(0) ~> Flow[Tweet].map(_.author) ~> writeAuthors
    bcast.out(1) ~> Flow[Tweet].mapConcat(_.hashtags.toList) ~> writeHashtags
    ClosedShape
  })
  g.run()

}
