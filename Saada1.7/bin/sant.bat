@ echo off
rem Shell script calling the embedded ant

java -Dant.home="..\jtools" -classpath "..\jtools\ant-launcher.jar" -Dant.library.dir="..\jtools" org.apache.tools.ant.launch.Launcher %*
