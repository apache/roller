
Some experiments with using BSF to enable scripting in Roller templates.


To use it in Roller:

1) Put roller-scripting.jar and bsf.jar in WEB-INF/lib

2) Add the jars necessary JavaScript, Groovy or JRuby to WEB-INF/lib

3) In your roller-custom.properies file add this override:

   rendering.rollerRendererFactories=\
      org.apache.roller.ui.rendering.velocity.VelocityRendererFactory,\
      org.apache.roller.scripting.BSFRendererFactory

4) Restart Roller

5) Create a new Weblog Template and set the Template Language to either 
   "groovy", "jruby" or "javascript"

6) In your page template you'll have access to all normal Roller models
   plus the name "out" will be bound to a Writer that you can use for output.
   For example, Here's Groovy for a simple Hello World template:

        import java.io.PrintWriter;
        pw = new PrintWriter(out);
        pw.println("Hello World, my blog is named ${model.weblog.name}");

