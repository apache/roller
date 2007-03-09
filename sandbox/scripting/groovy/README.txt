
Some experiments with using Groovy


A) Some scripts that run against the Roller API:
   bin/groovy - Runs Groovy with Roller jars in path. Edit it to set paths.
   bin/roller-custom.properties - Database properties for bin/groovy
   bin/listusers.gy - Lists users and roles in Roller system
   bin/createuser.gy - Create a user
   bin/createblog.gy - Create a blog


B) A Roller Renderer that allows use of Groovy in Roller templates:
   org.apache.roller.scripting.GroovyRenderer
   org.apache.roller.scripting.GroovyRendererFactory
   org.apache.roller.scripting.GroovyRollerBinding

To use it in Roller:

1) Put roller-groovy.jar and groovy-all-1.0.jar in WEB-INF/lib

3) In your roller-custom.properies file add this override:

   rendering.rollerRendererFactories=\
      org.apache.roller.ui.rendering.velocity.VelocityRendererFactory,\
      org.apache.roller.scripting.GroovyRendererFactory

4) Restart Roller

5) Create a page template and set Template Language to either "groovy" but
   currently you'll have to do that via SQL.

6) In your page template you'll have access to all normal Roller models
   plus the name "out" will be bound to a Writer that you can use for output.
   Also, the name "html" will be bound to a markup builder. Here's an 
   example Groovy Roller page:

      html.html {  // html is implicitly bound to new MarkupBuilder(out)
          head {
              title("Groovy Roller page")
          }
          body {
             p("Hello World, my blog is named ${model.weblog.name}")
          }
      }

