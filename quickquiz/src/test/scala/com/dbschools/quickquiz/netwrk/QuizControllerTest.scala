package com.dbschools.quickquiz.netwrk
import client.taker.{Receiver, TakerReceivedMessageProcessor}
import com.dbschools.quickquiz.client.giver.QuizCreationOptions
import msg.JoiningMsg
import org.jgroups.{Message, Address}
import org.specs._
import org.specs.runner._
import org.specs.mock._

class Options(val name: String, val password: String, val giverName: String) extends QuizCreationOptions {
  def getName = name
  def getPassword = password
  def getGiverName = giverName
}

object quizControllerTest extends Specification with JMocker {
  
  val giverName = "Your Teacher"
  val takerName = "Student 1"
  val qc = new QuizController(QuizController.NetworkStackKey.UDP_STACK_KEY)
  val quiz = qc.createQuiz(new Options("Java Quiz", null, giverName))
  val quizzes = qc.getQuizzes

  "quiz" should {
    "have the correct giver name" in {
      quiz.getGiverName mustEqual(giverName)
    }
  }
  
  "controller" should {
    "have one quiz" in {
      quizzes.size mustEqual(1)
    }
    
    "receive JoiningMsg" in {
      val quizChannel = qc.joinQuizChannel(quiz)
      val rmp = mock[TakerReceivedMessageProcessor]

      expect {
        one(rmp).processReceivedMsg(a[Message])
      }

      quizChannel.setReceiver(new Receiver(rmp))
      quizChannel.send(null, null, new JoiningMsg(new QuizTaker(takerName, 
          quizChannel.getLocalAddress())));
      Thread.sleep(1000L)
    }
  }
  
}

object runner {
  def main(args: Array[String]) {
    new ConsoleRunner(quizControllerTest).main(args)
  }
}
