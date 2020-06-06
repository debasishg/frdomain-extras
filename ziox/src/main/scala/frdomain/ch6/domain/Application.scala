package frdomain.ch6.domain

import zio.ZLayer
import zio.blocking.Blocking

import config._
import service._
import repository.{AccountRepository, DoobieAccountRepository}

object Application {

  // define all layers of the architecture
  type InfraLayerEnv =
    ConfigProvider with Blocking 

  type ConfigLayerEnv =
    InfraLayerEnv with AppConfigProvider with DbConfigProvider

  type RepositoryLayerEnv =
    ConfigLayerEnv with AccountRepository

  type ServiceLayerEnv = 
    RepositoryLayerEnv with AccountService with ReportingService

  type AppEnv = ServiceLayerEnv

  // compose layers for prod
  object prod {

    val infraLayer: ZLayer[Blocking, Throwable, InfraLayerEnv] =
      Blocking.any ++ ConfigProvider.live 

    val configLayer: ZLayer[InfraLayerEnv, Throwable, ConfigLayerEnv] =
      AppConfigProvider.fromConfig ++ DbConfigProvider.fromConfig ++ ZLayer.identity

    val repositoryLayer: ZLayer[ConfigLayerEnv, Throwable, RepositoryLayerEnv] =
      DoobieAccountRepository.layer ++ ZLayer.identity

    val serviceLayer: ZLayer[RepositoryLayerEnv, Throwable, ServiceLayerEnv] =
      AccountService.live ++ ReportingService.live ++ ZLayer.identity

    // final application layer for prod
    val appLayer: ZLayer[Blocking, Throwable, AppEnv] =
      infraLayer >+> 
      configLayer >+> 
      repositoryLayer >+> 
      serviceLayer
  }
}