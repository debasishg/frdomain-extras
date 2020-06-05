package frdomain.ch6.domain

import zio.ZLayer
import zio.blocking.Blocking

import config._
import service._
import repository.{AccountRepository, DoobieAccountRepository}

object Layers {

  type Layer0Env =
    ConfigProvider with Blocking 

  type Layer1Env =
    Layer0Env with AppConfigProvider with DbConfigProvider

  type Layer2Env =
    Layer1Env with AccountRepository

  type AppEnv = Layer2Env with AccountService with ReportingService

  object live {

    val layer0: ZLayer[Blocking, Throwable, Layer0Env] =
      Blocking.any ++ ConfigProvider.live 

    val layer1: ZLayer[Layer0Env, Throwable, Layer1Env] =
      AppConfigProvider.fromConfig ++ DbConfigProvider.fromConfig ++ ZLayer.identity

    val layer2: ZLayer[Layer1Env, Throwable, Layer2Env] =
      DoobieAccountRepository.layer ++ ZLayer.identity

    val appLayer: ZLayer[Blocking, Throwable, AppEnv] =
      layer0 >+> layer1 >+> layer2 >+> AccountService.live >+> ReportingService.live
  }
}