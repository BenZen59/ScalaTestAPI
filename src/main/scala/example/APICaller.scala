import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object APICaller {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("position-stack-example")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    // Définir votre clé d'API PositionStack
    val apiKey = "c071e4936018043b17d463c556194f93"

    // Effectuer une requête pour obtenir la géolocalisation d'une adresse
    val address = "1600 Amphitheatre Parkway, Mountain View, CA"

    val requestUri = Uri(s"http://api.positionstack.com/v1/forward")
      .withQuery(
        Uri.Query(
          "access_key" -> apiKey,
          "query" -> address
        )
      )

    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = requestUri))

    responseFuture.flatMap { response =>
      response.status match {
        case StatusCodes.OK =>
          // Lire la réponse JSON
          response.entity.toStrict(2.seconds).flatMap { entity =>
            val responseBody = entity.data.utf8String
            // Traitement de la réponse JSON ici
            println(responseBody)
            Future.successful(())
          }

        case _ =>
          // Gestion des erreurs de requête
          println(s"Request failed with status code ${response.status}")
          response.discardEntityBytes()
          Future.successful(())
      }
    }.onComplete { _ =>
      // Fermer le système d'acteurs et le matérialiseur
      system.terminate()
    }
  }
}