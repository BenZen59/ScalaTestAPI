import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import play.api.libs.json._

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

            // Analyser le contenu JSON
            val json = Json.parse(responseBody)
            val latitude = (json \ "data" \\ "latitude").headOption.flatMap(_.asOpt[Double])
            val longitude = (json \ "data" \\ "longitude").headOption.flatMap(_.asOpt[Double])

            // Vérifier si les valeurs de latitude et de longitude sont présentes
            (latitude, longitude) match {
              case (Some(lat), Some(lon)) =>
                // Utilisation des valeurs de latitude et de longitude
                val distance = calculateDistance(lat, lon)
                println(s"Distance: $distance")

              case _ =>
                println("Erreur lors de l'extraction des données de latitude et de longitude")
            }

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

    def calculateDistance(latitude: Double, longitude: Double): Double = {
      // Exemple de calcul de distance factice
      // Vous pouvez implémenter votre propre logique de calcul de distance ici
      val distance = Math.sqrt(latitude * latitude + longitude * longitude)
      distance
    }

  }}