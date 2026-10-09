# Apache Roller — Changes

## 6.1.7

### Improvements

- **Pasted entry images are kept when you publish**
  ([ROL-2184](https://issues.apache.org/jira/browse/ROL-2184)). PNG, JPEG and
  GIF images pasted or dragged into the rich text editor are saved as media
  files when the author can upload. Otherwise they are kept inline.
  - `weblog.inlineImages.preferInline=true` keeps images inline even when
    uploads are available.
  - `weblog.inlineImages.maxFieldBytes` (default 60000) limits a content or
    summary field that has inline images. On MySQL, change the entry columns to
    `MEDIUMTEXT` before you raise it.
  - Bundled themes allow `data:` images. Custom themes with their own Content
    Security Policy need `data:` in `img-src`.
  - Pasted images travel in the form POST as base64. Tomcat's `maxPostSize`
    defaults to 2 MB, so larger pastes are refused with a "form is too large"
    message; raise `maxPostSize` to accept them.

- **The one-time setup token is also printed to the console.** It still goes
  to Roller's log, and now also appears on standard output (for example
  `catalina.out`), so it is easy to find at first start.
    
### Behaviour changes worth reading before upgrading

- **AtomPub honours `webservices.enableAtomPub` on every request.** While the
  setting is off, every AtomPub URL answers 404, not only the service document.
- **AtomPub entry bodies default to a 1 MiB limit.** A larger entry is refused
  with 413. Set `webservices.atomPubMaxEntrySize` in Server Settings to change
  the limit in bytes (default 1048576). Media uploads use the existing file
  upload limits.
- **Templates can no longer reach the objects behind the template wrappers.**
  `$weblog.pojo`, `$entry.pojo` and `getPojo()` no longer resolve in weblog
  templates. A custom theme that uses them will print the reference text
  as-is, without an error. Use the wrapper's own properties instead, for
  example `$weblog.handle` or `$entry.title`.
- **AtomPub tags are normalized like editor tags.** Each character in a tag
  that is not a letter or digit becomes a space, so an AtomPub client that
  sends `foo-bar` gets two tags, `foo` and `bar`, as it would in the editor.
  Bundled themes and the `#showEntryTags` macro also HTML-escape tag names.
- **Planet is off by default.** `planet.aggregator.enabled` now defaults to
  `false`. While it is off, the Planet admin pages, `/planetrss` and the
  Planet background tasks (`RefreshRollerPlanetTask`, `SyncWebsitesTask`) do
  nothing. A site that uses Planet must set `planet.aggregator.enabled=true`
  in `roller-custom.properties` before upgrading.
- **Planet admin changes require POST.** Saving or deleting Planet groups and
  subscriptions is refused unless the request is a POST from the admin form.
- **XML parsing uses Apache Commons Secure XML.** Roller now bundles
  `commons-secure-xml` 1.0.0 and builds all of its XML parsers through it.
- **Startup fails if the XML-RPC parser cannot be configured.** Roller used to
  log an error and continue. It now stops at startup, so check the log if a
  custom XML parser is on the classpath.

### Bug fixes

- **Blogroll, category and ping target dialogs work again after a save.**
  Adding or renaming a blogroll, saving a bookmark, or saving a ping target
  then refreshing the page failed with an error page. So did retrying after a
  "name already in use" message. The page now picks up a new form token after
  each save.
- **"Switch to blogroll" lets you pick a blogroll.** The page no longer reloads
  as soon as you open the list.
- **Renaming a blogroll no longer reports a system error.** The rename was
  saved, but the page showed "System error - check logs".
- **The blogroll, bookmark, category and ping target dialogs show why a save
  was refused.** Before, only a duplicate name was reported. Any other error,
  and on the blogroll dialogs even a duplicate name, closed the dialog as if the
  save had worked, or ended on an error page.
- **AtomPub media collections work.** Listing a media collection by the URL in
  the service document (`/resources/default`) failed with a server error. So
  did uploading media with no `Slug` header and no title, uploading to
  `/resources` itself, or uploading with a very short `Slug`. Unknown media
  directories now answer 404.
- **The Planet feed has a title before Planet Config is first saved.** On a new
  site, `/planetrss` printed `$utils.escapeXML($siteName)` as its title and
  description, and logged a warning for each. Unsaved Planet settings now use
  their defaults.
- **Decimal settings can be saved on the configuration page.** The maximum
  upload file and directory sizes accepted only whole numbers in the browser,
  although they are measured in megabytes with decimals (default `2.00`).

## 6.1.6

Initial installation now requires a one-time, cryptographically secure setup token printed to the server log. 
Bootstrap access closes as soon as setup finishes — when the first administrator is created on a new site, 
or when the database upgrade completes on an existing one.

A maintenance release. Users of 6.1.5 and earlier are encouraged to upgrade.

### Behaviour changes worth reading before upgrading

Three features are retired in this release. Each was optional or long obsolete,
but if you rely on one, plan for it:

- **Incoming Trackback support is removed.** The Trackback endpoint no longer
  exists and is unmapped from `web.xml`. Sites that accepted Trackback pings
  will stop accepting them. `TrackbackLinkbackCommentValidator` is retained as
  an inert validator so an existing configuration that names it still starts.
- **Outbound Trackback is removed.** The entry editor no longer sends
  Trackbacks, and the associated action and screen are gone.
- **WSSE AtomPub authentication is retired.** `authentication.method` now
  accepts `basic` and `oauth`. An installation configured for `wsse` will fail
  closed on startup rather than silently falling back — change the setting
  before upgrading.

Two more changes are visible in normal use:

- **Media file content types are derived from file content** rather than the
  upload request, so a file whose declared type disagrees with its contents is
  now stored and served by what it actually is.
- **Enclosure metadata is stored as submitted.** Adding an enclosure no longer
  fetches the remote URL to discover its type and length; both are taken from
  the media file or the submitted values.

### Improvements

- Authoring resource lookups are scoped to the weblog the action is operating
  on.
- XML-RPC Blogger and MetaWeblog handlers check the caller's weblog permission
  per method, and answer a disabled endpoint before doing any authentication
  work.
- Vendor extension types are disabled on the XML-RPC servlet.
- OAuth authorization is bound to the current Roller session, and approval is
  one-shot.
- The CSRF filters keep the submitted salt and the salt issued for the response
  separate.
- Weblog template resources resolve within the active theme.
- Front-page selection moved into the administrator setup workflow, and
  front-page directory parameters are normalized before the bundled theme
  renders them.
- Authoring UI event handlers moved from inline JavaScript onto data
  attributes.
- Bookmark and configuration parsing share a single JDOM builder that treats a
  document strictly as data.
- Table detection during installation is scoped to the database the connection
  points at. Installing into an empty schema on a server that also hosts
  another Roller database previously found that database's tables, skipped
  table creation, and then failed at startup looking for tables it had never
  created. MySQL installations are the ones affected, because Connector/J 8
  changed `nullCatalogMeansCurrent` to default to false.

### Build and packaging

- `assembly-release/sign-release.sh` takes the version and optional release
  candidate suffix as arguments, requires the signing key to be named through
  `ROLLER_SIGNING_KEY`, refuses a key that is not RSA-4096 or stronger, and
  writes SHA-512 and SHA-256 in `shasum(1)` format.
- Four third-party jars are no longer shipped in the source distribution under
  `docs/examples/scripting/`. The examples' README files say where to obtain
  them.

## Earlier releases

Release notes for 6.1.5 and earlier are in the announcements archived at
<https://lists.apache.org/list.html?user@roller.apache.org>.
