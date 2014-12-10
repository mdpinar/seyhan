import play.Project._

name := "seyhan"

version := "1.0.0"

libraryDependencies ++= Seq(
  jdbc,
  javaEbean,
  cache,
  "mysql" % "mysql-connector-java" % "5.1.27",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "net.sf.jasperreports" % "jasperreports" % "4.0.1",
  "net.sf.jasperreports" % "jasperreports-fonts" % "4.0.0",
  "commons-digester" % "commons-digester" % "1.7",
  "com.lowagie" % "itext" % "2.1.7",
  "net.sourceforge.jexcelapi" % "jxl" % "2.6.10",
  "com.google.code.gson" % "gson" % "1.7.1",
  "com.googlecode.flyway" % "flyway-core" % "2.2"
)     

playJavaSettings