package controllers

import java.io.File

import play.Logger
import play.api.Play
import play.api.Play.current
import play.api.mvc.{AnyContent, Action, Controller}

/**
 * Created by zhugb on 15-9-17.
 */
object ExternalAssetsController extends Controller {

  val AbsolutePath = """^(/|[a-zA-Z]:\\).*""".r

  /**
   * Generates an `Action` that serves a static resource from an external folder
   *
   * @param rootPath the root folder for searching the static resource files.
   * @param file the file part extracted from the URL
   */
  def at(rootPath: String, file: String): Action[AnyContent] = Action { request =>
    val fileToServe = rootPath match {
      case AbsolutePath(_) => new File(rootPath, file)
      case _ => new File(Play.application.getFile(rootPath), file)
    }

    Logger.info("requesting for file: " + fileToServe.getAbsolutePath);

    if (fileToServe.exists) {
      Ok.sendFile(fileToServe, inline = true)
    } else {
      Logger.error("ExternalAssetsController failed to serve file: " + file)
      NotFound
    }
  }

}
