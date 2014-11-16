package controllers

import java.net.URLEncoder
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import play.Logger
import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.libs.json.JsPath
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.ws.WS
import play.api.libs.ws.WSRequestHolder
import play.api.libs.ws.WSRequestHolder
import play.api.mvc.Action
import play.api.mvc.Controller
import scala.util.Failure
import scala.util.Success
import utils.CustomHandyUtils

object MovieController extends Controller {

  case class Movies(movies: Seq[Movie])
  case class Movie(movieHeader: MovieHeader, movieDetails: MovieDetails)
  case class MovieHeader(id: String, name: String, trailerURL: String, eventURL: String)
  case class RegionalMovies(rgn: Seq[MovieHeader])
  case class MovieDetails(Title: String //      ,year: String
  //      ,rated: String
  //      ,released: String
  , Runtime: String, Genre: String, Director: String, Writer: String, Actors: String, Plot: String //      ,language: String
  //      ,country: String
  //      ,awards: String
  , Poster: String //      ,metascore: String
  , imdbRating: String)
  //      ,imdbVotes: String
  //      ,imdbID: String
  //      ,Type: String
  //      ,response: String)

  implicit val movieHeaderReads: Reads[MovieHeader] = ((JsPath \ "EventCode").read[String] and
    (JsPath \ "EventName").read[String] and
    (JsPath \ "TrailerURL").read[String] and
    (JsPath \ "EventUrl").read[String])(MovieHeader.apply _)
  implicit val regionalMovieReads = Json.reads[RegionalMovies]
  /*implicit val movieDetailsReads: Reads[MovieDetails] = ((JsPath\"Title").read[String] and
      (JsPath\"imdbRating").read[String])(MovieDetails.apply _)*/
  implicit val movieHeaderWrites: Writes[MovieHeader] = Json.writes[MovieHeader]
  implicit val movieDetailsReads: Reads[MovieDetails] = Json.reads[MovieDetails]
  implicit val regionalMovieWrites = Json.writes[RegionalMovies]
  implicit val movieDetailsWrites = Json.writes[MovieDetails]
  implicit val movieWrites = Json.writes[Movie]
  implicit val moviesWrites = Json.writes[Movies]

  def getMoviesPlayingByArea(region: Option[String]) = Action.async {
    val reqionalMoviesRequest: WSRequestHolder = WS.url("http://in.bookmyshow.com/getHTML.bms?cmd=BMSTOP&mvcnt=20&rgn=" + region.getOrElse("BANG"))
    val regionalMovies: Future[RegionalMovies] = reqionalMoviesRequest.get().map(x => Json.parse(x.body).as[RegionalMovies])
    val regionalMovieWithDetails: Future[Seq[Future[Movie]]] = regionalMovies.map {
      s =>
        s.rgn.map { x =>
          fetchMovieDetails(x.eventURL).map(movieDetail =>
            Movie(x, movieDetail))
        }
    }
    def futureToFutureTry[T](f: Future[T]): Future[Try[T]] =
      f.map(Success(_)).recover({ case x => Failure(x) })

    val myAwesomeFuture: Future[Future[Seq[Movie]]] = regionalMovieWithDetails.map(Future.sequence(_))
    myAwesomeFuture.flatMap(_.map { y => Ok(Json.toJson(y.sortBy { z => -CustomHandyUtils.toDefiniteDouble(z.movieDetails.imdbRating) })) withHeaders ("Access-Control-Allow-Origin" -> "*") })
  }

  def getMovieDetails(name: String) = Action.async {
    fetchMovieDetails(name).map { movies =>
      Ok(Json.toJson(movies)).withHeaders(("Access-Control-Allow-Origin" -> "*"))
    }
  }

  def fetchMovieDetails(name: String): Future[MovieDetails] = {
    val trimmedName = trimMovieName(name)
    Logger.debug("\n\n-------------------Trimmed Name: " + trimmedName)
    WS.url("http://www.omdbapi.com/?t=" + trimmedName + "&y=&plot=short&r=json").get().map {
      ratingsResponse =>
        val movieDetails: Option[MovieDetails] = ratingsResponse.json.asOpt[MovieDetails] //Need to have as option to handle errors
        movieDetails match {
          case Some(x) => x
          case None    => MovieDetails("", "", "", "", "", "", "", "", "") 
        }
    }
  }

  def trimMovieName(name: String): String = {
    val trimmed = name.replaceAll("hindi", "")
      .replaceAll("punjabi", "")
      .replaceAll("3d", "")
      .replaceAll("2d", "")
    java.net.URLEncoder.encode(trimmed, "UTF-8")
  }

  def getTweets(query: String) = {
    WS.url("http://twitter-search-proxy.herokuapp.com/search/tweets?q=" + query).get().flatMap {
      response => Future { Ok(response.body) }
    }
  }
}
