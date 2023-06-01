import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.math._
import scala.util.{Failure, Success}
import scala.util.Random



object APICaller {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("position-stack-example")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    // Générer une liste de 5 kilomètres de trajets aléatoires
    val random = new Random()
    val trajets = List.fill(5)(random.nextDouble() * 5) // Génère une liste de 5 nombres aléatoires entre 0 et 5

    // Trouver la plus petite valeur dans la liste des trajets
    val plusPetitTrajet = trajets.min

    // Afficher la plus petite valeur
    println(s"Le plus petit trajet est de : $plusPetitTrajet kilomètres")

    // Définir une liste de villes
    val villes = List("Paris", "Londres", "New York", "Tokyo", "Sydney", "Berlin", "Rome", "Barcelone")

    // Générer une liste de 5 villes aléatoires
    val randomVille = new Random()
    val villesAleatoires = List.fill(5)(villes(random.nextInt(villes.length)))

    // Afficher les villes aléatoires
    println("Liste des villes qui serviront de stop :")
    villesAleatoires.foreach(println)

    case class VilleStop(ville: String, stop: String)

    // Créer un tableau d'objets avec 5 villes et attribuer "oui" à deux objets pour le champ "stop"
    val tableauObjets = Array(
      VilleStop("Paris", "non"),
      VilleStop("Londres", "non"),
      VilleStop("New York", "oui"),
      VilleStop("Tokyo", "non"),
      VilleStop("Sydney", "oui")
    )

    // Filtrer les objets qui ont "oui" dans le champ "stop"
    val objetsAvecStopOui = tableauObjets.filter(_.stop == "oui")

    // Afficher les objets avec "oui" dans le champ "stop"
    println("Objets avec stop = oui :")
    objetsAvecStopOui.foreach(println)

    // Afficher tous les objets du tableau
    println("Tous les objets :")
    tableauObjets.foreach(println)

    case class Trajet(villeDepart: String, villeArrivee: String, distance: Double)

    // Créer un tableau d'objets avec 3 trajets
    val tableauObjetsTrajets = Array(
      Trajet("Paris", "Londres", 500.0),
      Trajet("New York", "San Francisco", 4000.0),
      Trajet("Tokyo", "Sydney", 6000.0)
    )

    // Trouver l'objet avec la distance la plus courte
    val trajetDistanceMin = tableauObjetsTrajets.minBy(_.distance)

    // Afficher l'objet avec la distance la plus courte
    println(s"Trajet avec la distance la plus courte : ${trajetDistanceMin.villeDepart} - ${trajetDistanceMin.villeArrivee}, Distance : ${trajetDistanceMin.distance} km")

    // Définir votre clé d'API PositionStack
    val apiKey = "c071e4936018043b17d463c556194f93"
    // Effectuer une requête pour obtenir la géolocalisation d'une adresse
    val address = "1600 Amphitheatre Parkway, Mountain View, CA"
    val address2 = "Champ de Mars, 5 Av. Anatole France, 75007 Paris"

    val requestUri = Uri(s"http://api.positionstack.com/v1/forward")
      .withQuery(
        Uri.Query(
          "access_key" -> apiKey,
          "query" -> address
        )
      )

    val requestUri2 = Uri(s"http://api.positionstack.com/v1/forward")
      .withQuery(
        Uri.Query(
          "access_key" -> apiKey,
          "query" -> address2
        )
      )

    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = requestUri))
    val responseFuture2: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = requestUri2))

    val futureResult1: Future[Double] = responseFuture.flatMap { response =>
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
                val length = calculateLengthPath(lat, lon)
                println(s"Length: $length")
                Future.successful(calculateDistance(lat, lon))


              case _ =>
                println("Erreur lors de l'extraction des données de latitude et de longitude")
                Future.failed(new RuntimeException("Erreur lors de l'extraction des données de latitude et de longitude"))
            }
          }

        case _ =>
          // Gestion des erreurs de requête
          println(s"Request failed with status code ${response.status}")
          response.discardEntityBytes()
          Future.failed(new RuntimeException(s"Request failed with status code ${response.status}"))
      }
    }

    val futureResult2: Future[Double] = responseFuture2.flatMap { response =>
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
                val length = calculateLengthPath(lat, lon)
                println(s"Length: $length")
                Future.successful(calculateDistance(lat, lon))

              case _ =>
                println("Erreur lors de l'extraction des données de latitude et de longitude")
                Future.failed(new RuntimeException("Erreur lors de l'extraction des données de latitude et de longitude"))
            }
          }

        case _ =>
          // Gestion des erreurs de requête
          println(s"Request failed with status code ${response.status}")
          response.discardEntityBytes()
          Future.failed(new RuntimeException(s"Request failed with status code ${response.status}"))
      }
    }

    val futureCalculations: Future[Double] = for {
      result1 <- futureResult1
      result2 <- futureResult2
    } yield {
      result1 + result2
    }

    futureCalculations.onComplete {
      case Success(distance) =>
        println(s"Distance: $distance")
        system.terminate()
      case Failure(ex) =>
        println(s"Erreur lors du calcul de la distance : ${ex.getMessage}")
        system.terminate()
    }
  }



  def calculateDistance(latitude: Double, longitude: Double): Double = {
    // Exemple de calcul de distance factice
    // Vous pouvez implémenter votre propre logique de calcul de distance ici
    val distance = Math.sqrt(latitude * latitude + longitude * longitude)
    distance
  }

  def calculateLengthPath(latitude: Double, longitude: Double): Double = {
    val earthRadiusKm = 6371.0

    val dLat = toRadians(latitude)
    val dLon = toRadians(longitude)

    val a = pow(sin(dLat / 2), 2) + cos(toRadians(latitude)) * cos(toRadians(latitude)) * pow(sin(dLon / 2), 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    val distanceInKilometers = earthRadiusKm * c
    distanceInKilometers

  }
}