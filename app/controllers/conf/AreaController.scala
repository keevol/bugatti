package controllers.conf

import actor.ActorUtils
import actor.salt.{DeleteArea, UpdateArea, AddArea, RefreshHosts}
import controllers.BaseController
import enums.{ModEnum, FuncEnum}
import models.conf.{AreaInfo, AreaHelper, Area}
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import utils.SaltTools

/**
 * 区域管理
 * @author of557
 */
object AreaController extends BaseController {
  implicit val areaFormat = Json.format[Area]
  implicit val areaInfoFormat = Json.format[AreaInfo]

  def msg(user: String, ip: String, msg: String, data: Area) =
    Json.obj("mod" -> ModEnum.area.toString, "user" -> user, "ip" -> ip, "msg" -> msg, "data" -> Json.toJson(data)).toString

  val areaForm = Form(
    mapping(
      "id" -> optional(number),
      "name" -> nonEmptyText(maxLength = 30),
      "syndicName" -> nonEmptyText(maxLength = 30),
      "syndicIp" -> nonEmptyText(maxLength = 30)
    )(Area.apply)(Area.unapply)
  )

  def all = Action {
    Ok(Json.toJson(AreaHelper.allInfo))
  }

  def get(id: Int) = Action {
    Ok(Json.toJson(AreaHelper.findInfoById(id)))
  }

  def save = AuthAction(FuncEnum.area) { implicit request =>
    areaForm.bindFromRequest.fold(
      formWithErrors => BadRequest(resultFail(formWithErrors.errorsAsJson)),
      area =>
        AreaHelper.findByName(area.name) match {
          case Some(_) => Ok(resultExists)
          case None =>
            ALogger.info(msg(request.user.jobNo, request.remoteAddress, "新增区域", area))
            val areaId = AreaHelper.create(area)
            val newArea = area.copy(id = Option(areaId))
            ActorUtils.areas ! AddArea(newArea)
            Ok(resultSuccess(Json.toJson(areaId)))
        }
    )
  }

  def update = AuthAction(FuncEnum.area) { implicit request =>
    areaForm.bindFromRequest.fold(
      formWithErrors => BadRequest(resultFail(formWithErrors.errorsAsJson)),
      area => {
        ALogger.info(msg(request.user.jobNo, request.remoteAddress, "修改区域", area))
        ActorUtils.areas ! UpdateArea(area)
        Ok(resultSuccess(Json.toJson(AreaHelper.update(area))))
      }
    )
  }

  def delete(id: Int) = AuthAction(FuncEnum.area) { implicit request =>
    AreaHelper.findById(id) match {
      case Some(area) =>
        ALogger.info(msg(request.user.jobNo, request.remoteAddress, "删除区域", area))
        ActorUtils.areas ! DeleteArea(id)
        Ok(resultSuccess(Json.toJson(AreaHelper.delete(id))))
      case None => NotFound(resultNone)
    }
  }

  def refresh(id: Int) = AuthAction(FuncEnum.area) { implicit request =>
    AreaHelper.findById(id) match {
      case Some(area) => {
        ActorUtils.areaRefresh ! RefreshHosts(id)
        ALogger.info(msg(request.user.jobNo, request.remoteAddress, "刷新区域", area))
        Ok(resultSuccess(Json.toJson(AreaHelper.findInfoById(id))))
      }
      case None => NotFound(resultNone)
    }
  }

}
