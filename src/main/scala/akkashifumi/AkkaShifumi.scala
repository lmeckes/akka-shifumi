package akkashifumi

import akka.actor._
import scala.util.Random
import scala.collection._

case object Pierre

case object Feuille

case object Ciseau

case object CoupEnAttente


case object JeuDebut

case object JeuGagne

case object JeuPerd

case object JeuNul

class Arbitre(pl: Seq[ActorRef]) extends Actor {

  var statut: mutable.Map[ActorRef, Object] = mutable.Map()

  def announceResult = {

    val m0 = statut(pl(0))
    val m1 = statut(pl(1))

    if (m0 == m1) {
      pl.foreach(_ ! JeuNul)
    } else if (m0 == Pierre && m1 == Feuille) {
      pl(0) ! JeuPerd
      pl(1) ! JeuGagne
    } else if (m0 == Pierre && m1 == Ciseau) {
      pl(0) ! JeuGagne
      pl(1) ! JeuPerd
    } else if (m0 == Feuille && m1 == Pierre) {
      pl(0) ! JeuGagne
      pl(1) ! JeuPerd
    } else if (m0 == Feuille && m1 == Ciseau) {
      pl(0) ! JeuPerd
      pl(1) ! JeuGagne
    } else if (m0 == Ciseau && m1 == Pierre) {
      pl(0) ! JeuPerd
      pl(1) ! JeuGagne
    } else if (m0 == Ciseau && m1 == Feuille) {
      pl(0) ! JeuGagne
      pl(1) ! JeuPerd
    }

    self ! JeuDebut
  }

  def x(o: Object): Unit = {
    statut += sender -> o
    if (statut.values.filter(_ == CoupEnAttente).isEmpty) {
      announceResult
    }
  }

  def receive = {
    case JeuDebut => {
      println("Nouveau jeu")
      statut = mutable.Map(pl(0) -> CoupEnAttente, pl(1) -> CoupEnAttente)
      pl.foreach(_ ! JeuDebut)
    }
    case Pierre => {
      x(Pierre)
    }
    case Feuille => {
      x(Feuille)
    }
    case Ciseau => {
      x(Ciseau)
    }

  }
}

class RandomBot extends Actor {

  var score = 0

  def receive = {
    case JeuDebut => {
      println(s"Randombot choisit un coup aléatoire (score=$score)")
      sender ! Random.shuffle(Seq(Pierre, Feuille, Ciseau)).head
    }
    case JeuGagne => {
      score += 1
      println("Randombot Gagne!")
    }
  }

}

class FibonnaciBot extends Actor {

  var score = 0
  var moveNumber = 0

  lazy val fib: Stream[BigInt] = Stream.cons(BigInt(0), Stream.cons(BigInt(1), fib.zip(fib.tail).map(a => a._1 + a._2)))

  def receive = {
    case JeuDebut => {
      moveNumber += 1
      println(s"FibonnaciBot choisit un coup basé sur la séquence de Fibonacci (score=$score)")
      sender ! Seq(Pierre, Feuille, Ciseau)((fib(moveNumber) % 3).toInt)
    }
    case JeuGagne => {
      score += 1
      println("FibonnaciBot Gagne!")
    }
  }

}

object ShifumiTest extends App {

  val system = ActorSystem("ShifumiContest")

  val joueur1 = system.actorOf(Props[RandomBot], name = "joueur1")
  val joueur2 = system.actorOf(Props[FibonnaciBot], name = "joueur2")
  val referee = system.actorOf(Props(new Arbitre(Seq(joueur1, joueur2))), name = "arbitre")

  referee ! JeuDebut

}



