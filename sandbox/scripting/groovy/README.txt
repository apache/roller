
Some experiments with using Groovy

A) Some scripts that run against the Roller API

   bin/groovy - Runs Groovy with Roller jars in path. Edit it to set paths.
   bin/roller-custom.properties - Database properties for bin/groovy
   bin/listusers.gy - Lists users and roles in Roller system
   bin/createuser.gy - Create a user
   bin/createblog.gy - Create a blog


B) A Roller GroovletRenderer that can evaluate a Roller template as Groovy code 
   with Groovlet-style "out" and "html" bindings.

   The implementation:

        org.apache.roller.scripting.GroovletRenderer
        org.apache.roller.scripting.GroovletRendererFactory   
        org.apache.roller.scripting.GroovyRollerBinding

    Here's an example Hello World template:

        println "<html><body>Hello World</body></html>"

    Here's an example template that displays recent entries from a weblog:

        html.html { // html is implicitly bound to new MarkupBuilder(out)
            head {
                title(model.weblog.name)
            }
            body {
                h1(model.weblog.name)
                i(model.weblog.description)
                map = model.getWeblogEntriesPager().getEntries();
                map.keySet().each() {
                   map.get(it).each() {
                      h2(it.title)
                      p(it.text)
                      br()
                   }
                }
            }
        }

C) A Roller GSPRenderer that can evaluate a Roller template as a Groovy Template
   with GSP-style "out" and "html" bindings.

    The implementation:

        org.apache.roller.scripting.GSPRenderer
        org.apache.roller.scripting.GSPRendererFactory   
        org.apache.roller.scripting.GSPRollerBinding

    Here's an example Hello World template:

        <html>
           <body>
              <%= "Hello World" %>
           </body>
        </html>

    Here's an example template that displays recent entries from a weblog:

        <html>
           <head>
              <title>${model.weblog.name}</title>
           </head>
           <body>
           <h1>${model.weblog.name}</h1>
           <i>${model.weblog.description}</i>
           <% map = model.getWeblogEntriesPager().getEntries();
              map.keySet().each() { %>
                 <% map.get(it).each() { %>
                    <h2>${it.title}</h2>
                    <p>${it.text}</p><br />
                 <% } 
              }%> 
           </body>
        </html>


To use renderers (B) and (C) in Roller:

1) Put roller-groovy.jar and groovy-all-1.0.jar in WEB-INF/lib

3) In your roller-custom.properies file add this override:

   rendering.rollerRendererFactories=\
      org.apache.roller.ui.rendering.velocity.VelocityRendererFactory,\
      org.apache.roller.scripting.GroovletRendererFactory,\
      org.apache.roller.scripting.GSPRendererFactory,\

4) Restart Roller

5) Create a page template and set Template Language to either "groovlet" or
   "gsp". Currently you'll have to do that via SQL.

6) In your page template you'll have access to all normal Roller models
   plus the name "out" will be bound to a Writer that you can use for output.
   Also, the name "html" will be bound to a markup builder. Here's an 
   example Groovy Roller page:



