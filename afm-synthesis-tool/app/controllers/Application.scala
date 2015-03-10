package controllers

import java.io.File

import com.github.tototoshi.csv._
import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.step0_load())
  }

  def step0 = Action {
    Ok(views.html.step0_load())
  }

  def step1 = Action(parse.multipartFormData) { request =>
    request.body.file("configuration_matrix").map { file =>

      // Create a temporary file to read the uploaded matrix
      val uploadedFile = File.createTempFile("afm_", ".csv")
      file.ref.moveTo(uploadedFile, replace = true)

      // Read the CSV file
      val reader = CSVReader.open(uploadedFile)
      val matrix = reader.all()
      reader.close()

      // Delete the temporary file
      uploadedFile.delete()

      Ok(views.html.step1_features_attributes(matrix))
    }.getOrElse {
      Redirect(routes.Application.step0()).flashing(
        "error" -> "Missing file"
      )
    }

  }

  def step2 = Action { request =>
    val variableTypes = request.body.asFormUrlEncoded.get.map(t => (t._1, t._2.head))


    for ((variable, variableType) <- variableTypes) {
      println(variable + " -> " + variableType)
    }

    Ok(views.html.step2_hierarchy())
  }

  def step3 = Action {
    Ok(views.html.step3_feature_groups())
  }

  def step4 = Action {
    Ok(views.html.step4_constraints())
  }

  def step5 = Action {
    Ok(views.html.step5_afm())
  }

}