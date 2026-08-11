import sbt._

object AppDependencies {

  private val bootstrapVersion = "9.18.0"
  private val hmrcMongoVersion = "2.7.0"

  val compile = Seq[ModuleID](
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"            % "12.8.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "3.3.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"                    % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "domain-play-30"                        % "11.0.0",
    "org.typelevel"     %% "cats-core"                             % "2.13.0",
    "uk.gov.hmrc"       %% "crypto-json-play-30"                   % "8.2.0"
  )

  val test = Seq(
    "uk.gov.hmrc"                %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus"          %% "scalacheck-1-15"         % "3.2.11.0",
    "org.scalatestplus"          %% "mockito-3-4"             % "3.2.10.0",
    "org.mockito"                %% "mockito-scala"           % "2.0.0",
    "org.scalacheck"             %% "scalacheck"              % "1.18.1",
    "org.jsoup"                   % "jsoup"                   % "1.21.1",
    "wolfendale"                 %% "scalacheck-gen-regexp"   % "0.1.2",
    "com.softwaremill.quicklens" %% "quicklens"               % "1.9.12"
  ).map(_ % Test)

  val itDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
