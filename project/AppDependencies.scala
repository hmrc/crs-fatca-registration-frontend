import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.6.0"
  private val hmrcMongoVersion = "1.9.0"

  val compile = Seq[ModuleID](
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"     % "9.10.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30"  % "2.0.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"     % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"             % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "domain-play-30"                 % "9.0.0",
    "org.typelevel"     %% "cats-core"                      % "2.10.0",
    "uk.gov.hmrc"       %% "crypto-json-play-30"            % "8.0.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus"       %% "scalacheck-1-15"         % "3.2.11.0",
    "org.scalatestplus"       %% "mockito-3-4"             % "3.2.10.0",
    "org.mockito"             %% "mockito-scala"           % "1.17.31",
    "org.scalacheck"          %% "scalacheck"              % "1.18.0",
    "org.pegdown"             %  "pegdown"                 % "1.6.0",
    "org.jsoup"               %  "jsoup"                   % "1.17.2",
    "wolfendale"             %%  "scalacheck-gen-regexp"   % "0.1.2",
    "com.softwaremill.quicklens" %% "quicklens"               % "1.9.7"
  ).map(_ % Test)

  val itDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
