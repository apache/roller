import com.sun.syndication.propono.atom.client.*
import com.sun.syndication.feed.atom.*

try {

    def authStrategy = new OAuthStrategy(
        "joe", // username
        "xxxxx", // consumer key
        "yyyyy", // consumer secret
        "HMAC-SHA1", // key type
        "http://localhost:8080/roller/roller-services/oauth/requestToken",
        "http://localhost:8080/roller/roller-services/oauth/authorize",
        "http://localhost:8080/roller/roller-services/oauth/accessToken")

    // get the AtomPub service
    def appService = AtomClientFactory.getAtomService(
        "http://localhost:8080/roller/roller-services/app", authStrategy)

    // find workspace of blog
    def blog = appService.findWorkspace("Joe's test blog") // find collecton that will accept entries
    def entries = blog.findCollection(null, "application/atom+xml;type=entry")

    // create and post an entry
    def entry = entries.createEntry()
    entry.title = "TestPost"
    def content = new Content()
    content.setValue("This is a test post. w00t!")
    entry.setContent([content])
    entries.addEntry(entry)

} catch (Exception e) {
    e.printStackTrace();
}