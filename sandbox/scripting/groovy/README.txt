
Some experiments with using Groovy


Scripts that run against the Roller API.

   bin/groovy - Runs Groovy with Roller jars in path. Edit it to set paths.

   bin/roller-custom.properties - Database properties for bin/groovy

   bin/listusers.gy - Lists users and roles in Roller system

   bin/createuser.gy - Create a user

   bin/createblog.gy - Create a blog


A Roller Renderer that allows use of Groovy in Roller templates:

   org.apache.roller.scripting.GroovyRenderer
   org.apache.roller.scripting.GroovyRendererFactory
   org.apache.roller.scripting.GroovyRollerBinding
