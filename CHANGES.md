# Apache Roller — Changes

## 6.1.8

### Behaviour changes worth reading before upgrading

- **Only global admins manage Admin on the aggregated front-page weblog.**
  With `site.frontpage.weblog.aggregated=true`, that weblog's templates can
  read the whole site, so granting, removing or inviting an Admin there now
  needs a global admin. Before turning aggregation on, review who is Admin
  on the front-page weblog.

- **OAuth 1.0a needs AtomPub with OAuth authentication.** The OAuth endpoints
  and the OAuth keys page answer only when AtomPub is enabled and its
  authentication is set to OAuth. A request token is issued only for a
  correctly signed request, and each request gets new credentials.

- **Check "Absolute URL to site" (`site.absoluteurl`) after upgrading.**
  Roller no longer takes the site's URL from the first request after a start.
  New installs save it during setup, and on an upgraded site where it's empty,
  the first administrator to open the Roller UI saves it from that request,
  unless it's a local or private address. Check it under Server Admin >
  Configuration, and correct it if the site is reached through a proxy or more
  than one host name. While it's empty, each request's own URL is used for its
  links, cached pages are kept per host name, comment notifications are sent
  without links, account activation and invitation emails aren't sent, and
  background tasks such as weblog update pings and the planet sync wait for
  it. Links keep the request's https scheme and port.
  `WebloggerRuntimeConfig.setAbsoluteContextURL` and
  `InitFilter.getAbsoluteUrl` are removed (#337).

- **Weblog templates have render limits.** A page stops rendering, with a
  logged error, once it writes more than `rendering.maxOutputChars` (10 MB),
  inserts a longer string, or runs longer than `rendering.maxRenderSeconds`
  (30 seconds), and the page isn't cached. A `#foreach` of more than 100,000
  passes fails the same way. `#parse` and `#include` load only the weblog's
  own templates. Error pages no longer show template ids (#366).
- **Macros defined in a template belong to that template.** A macro defined
  inline in a template works in that page and in the templates it
  `#parse`s, and one weblog's macro no longer replaces another's of the same
  name. A custom theme that calls a macro defined in a different template,
  which it doesn't `#parse`, now prints the call as text. Before, such a call
  worked only once the defining template had been rendered since the server
  started. Define the macro in the page that calls it, or `#parse` the
  template that defines it. Library macros (`roller.vm`, `weblog.vm` and a
  site's own `roller-custom.vm`) stay global (#366).

- **Entries with scripts or embeds open as HTML.** If an entry contains
  scripts, frames, embedded objects, event-handler attributes or
  `javascript:` links, the rich-text editor opens it in the HTML textarea
  with a notice, and the stored HTML is kept as is. Entries with forms, some
  Bootstrap data attributes (such as `data-toggle`), a link or URL-like word
  containing `<`, as in some code samples, or an iframe or other tag that the
  editor's code view removes, even inside an HTML comment, also open in the
  textarea (#331).

- **Names are shown as text.** Screen and full names are HTML-escaped where
  templates print them and written as plain text in feeds; commenter names
  and addresses are escaped in HTML notification emails; HTML comments are
  escaped unless the HTMLSubset plugin applies to them (#330).

- **Limits on uploads and fetched feeds.** Images with more pixels than
  `uploads.thumbnail.maxPixels` (50 million) are stored without a thumbnail,
  and so are TIFF images (#327). Planet stops reading a feed past
  `planet.aggregator.fetch.maxBytes` (5 MB) or
  `planet.aggregator.fetch.maxSeconds` (30 seconds), and fetches only http
  and https URLs (#326).

- **AtomPub checks.** Publishing an entry through an AtomPub update needs the
  permission to publish, media uploads over `uploads.file.maxsize` are
  refused with 413 before they're stored, and entries show the creator's
  email address only to the creator (also in MetaWeblog responses) (#328).

- **Search queries are limited to 256 characters**, after spaces around them
  are dropped, and a query the search engine can't run finds nothing (#334).

- **Weblog pages ignore a `skipCache` query parameter** (#325).

- **The Design pages follow "Allow custom themes".** While
  `themes.customtheme.allowed` is off (the default), the theme, stylesheet and
  template pages answer as unavailable, as the hidden Design menu already
  implied, and the XML-RPC `blogger.setTemplate` call is refused. Weblogs that
  already use custom templates keep showing them. Turn the setting on in
  Server Settings to edit templates again.

- **Comments are limited to 10,000 characters.** Longer comments are refused,
  with a message saying so, before they are previewed or checked by the
  comment validators. The commenter's name, e-mail address and URL are limited
  to 255 characters each, the size of the columns that store them. Set `comment.maxLength` in `roller-custom.properties` to
  change the limit. The bundled `bannedwordslist.txt` rules match the same
  domains as before but look at most 63 characters between their parts; a
  copy of the old file in the uploads directory is still used if present.

- **Weblog content is served only at the weblog's own addresses.** Direct
  requests to `/roller-ui/rendering/page/`, `feed/`, `search/` and
  `resources/` now answer 404; Roller reaches those servlets by forwarding the
  weblog's public URLs. Comment posts, RSD and
  `/roller-ui/rendering/media-resources/` links in entries work as before.

- **User details in templates.** The user and visitor objects no longer
  provide an email address. Bundled themes use the existing name properties.
- **Longer passwords on account forms.** Registration, profile and the
  administrator's user form take passwords up to 72 UTF-8 bytes.
- **Request and result limits.** Multipart requests to the Roller UI allow
  five files of the "Maximum file size" set in Site Settings plus 1 MiB of
  form data (11 MiB with the default 2 MB), and the limit follows that setting.
  XML-RPC request limits allow for the configured file size and its base64
  encoding, up to 32 MiB, with a 1 MiB limit when uploads are off. Blogger
  and MetaWeblog return at most 200 recent entries, including when the
  requested count is zero or negative.
- **Configuration and weblog logging.** Configuration debug messages show
  property names without values, and weblog lookup errors print handles on
  one line.

### Bug fixes

- **Media filenames keep their punctuation** when inserted into an entry.
- **Changing a password ends the account's other sessions.** Saving a new
  password in the profile, an administrator's reset, or disabling an account
  now logs out that user's other browsers at their next Roller UI page; the
  session that made the change stays logged in (#333).
- **Later pages of a feed are cached separately.** A feed's `?page=N` now has
  its own cache entry, and page numbers are kept between 0 and 10,000 (#325).
- **"nofollow" is added to links that need it.** `addNofollow` only touched
  links that already had `rel="nofollow"` (#330).
- **Tags of a scheduled entry are counted once, when it goes live** (#328).
- **A weblog's only administrator can't resign from it**, and the main menu
  says why. Resigning now redirects to the main menu, as accepting and
  declining an invitation do. Declining an invitation only removes a pending
  invitation, and declining one you already accepted says you're already a
  member (#334).
- **New passwords can't be the external-login placeholder.** Registration,
  the profile page and user administration refuse
  `users.passwords.externalAuthValue` (by default `<externalAuth>`) as a
  password, and it never works as one. When you change that setting, list its
  earlier values in the new `users.passwords.externalAuthValue.previous`
  (comma-separated) so they're treated the same. A login refused for this
  reason is logged, and an administrator can set a new password for an
  account that used one (#306).
- **The LDAP comment authenticator escapes the commenter's user name** when
  it builds the bind name (#334).
- **Roller UI stylesheets, scripts and images don't start a session** (#332).
- **Requested tags are checked and encoded on every weblog page.** A custom
  page given `tags=` together with an entry now answers 404 for tags the
  weblog doesn't have, as it already did without an entry. The tag names are
  HTML-encoded in the feed link titles and in the Gaurav and Fauxcoly tag
  pages. The page cache keys such pages by page name and tags, so they are no
  longer served in place of each other.
- **Entry titles show as text in the next/previous links and the big
  calendar.** The links on a permalink page and the titles in
  `#showWeblogEntryCalendarBig` now drop markup, decode entities and encode the
  result, the same way the rest of the page shows titles. A title saved with
  `&amp;` no longer turns into `&amp;amp;`, and the calendar cuts long titles
  at 20 visible characters.
- **Deleting the front page weblog brings back the welcome page.** The site
  kept pointing at the deleted weblog's handle, so the front page broke until
  an admin chose another weblog. The setting is now cleared when that weblog is
  deleted.
- **Meta descriptions are always encoded.** A weblog's "about" text or an
  entry's search description containing a quote no longer cuts the
  `<meta name="description">` tag short, whether or not weblog admins are
  trusted.
- **Saving the "Create weblog" form follows the site setting.** When "Allow
  users to create weblogs" is off, saving the form now refuses, as opening it
  already did.
- **Only the site's first account becomes an administrator.** With
  `users.firstUserAdmin` on, a new account was made an administrator whenever
  no enabled account existed. Disabled accounts now count too.

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

- **Weblog update pings no longer use the XML-RPC client library.** Roller
  builds the `weblogUpdates.ping` request and reads the response itself, with a
  connection timeout, a read timeout and a 1 MB cap on the response, and reads
  only the `flerror` and `message` fields the protocol defines. Ping behaviour
  is otherwise unchanged.

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
- **Tag names can no longer contain `"`, `&`, `'`, `<`, `>` or `` ` ``.**
  These characters are removed when a tag is saved, and from existing tag
  names when they are shown.
- **Custom-theme files in the media library follow the media rules.** A weblog
  with a custom theme still serves its stylesheets and scripts from the media
  library as before. Other files there, such as HTML or SVG, are served like
  any other media file: as a download unless they are an image, audio, video,
  PDF or plain text. The media file editor sets a file's original path only
  when `mediafile.originalPathEdit.enabled=true`, as its form already shows.
  AtomPub media uploads no longer set one.
- **The entry editor applies the Limited role to each entry.** A Limited member
  can open, change and remove draft and pending entries only, as in AtomPub and
  XML-RPC. Other entries answer with the access denied page.
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
- **Text with an unclosed tag loses the tag.** Where Roller strips HTML from
  plain-text fields (for example a weblog's description), a tag with attributes
  and no closing `>`, such as `text <img src=x`, is now dropped. Other text with
  a `<`, as in `a < b`, `x<y` or `C<C++ fans`, is kept, and no longer gets a
  space added before the `<`. The Your Weblogs page shows a weblog's
  description as text.
- **OPML import keeps only web links.** An imported bookmark is kept only when
  its link is an absolute `http` or `https` URL; others are skipped and named
  in the log. A feed URL that is not one is dropped. Blogroll links shown on
  weblog pages must be relative or use `http`, `https`, `ftp` or `mailto`;
  other links, and links with `&` before the path, are shown without a
  target. The blogroll shows bookmark names and descriptions as plain text,
  without HTML formatting.
- **The user picker matches user names only, except for global admins.** When
  a user who is not a global admin searches for users (for example to invite a
  member), only user names are matched. Global admins can still search by
  email address. For other users the picker lists enabled users only, at
  most 50 at a time. `%` and `_` in the search text match only themselves.
- **Previews need a role in the weblog.** Previewing a weblog's pages or
  theme resources now requires an enabled user with a role in that weblog
  (Limited, Author or Admin), or a global admin.
- **Disabled accounts cannot sign in with an OpenID URL.** An account that an
  administrator has disabled is now refused whenever its OpenID URL is used to
  sign in, on every sign-in form, as user name and password sign-in already
  does.
- **Search queries can name only the search fields.** A weblog search may
  name entry content, title or comment text (for example `title:roller`). A
  query that names another field is treated like a query that cannot be
  parsed. Author user names and comment author names and email addresses are
  no longer added to the search index. An index built by an earlier version
  is rebuilt in the background at the first startup after the upgrade.
- **Search shows only published entries.** Weblog search returns entries that
  are published at the time of the search, even if the search index still
  holds an entry that has since gone back to draft. Updating an entry through
  AtomPub so that it is no longer published also removes it from the index,
  and so does deleting an entry through AtomPub.
- **Actions that change data need a POST.** A request other than a POST
  that calls an action method which changes data, for example
  `pings!enable.rol`, is refused with `405 Method Not Allowed`. Roller's own
  ping, invitation and installer links are now small forms that POST with
  the page's form token, and return to the page afterwards. Saving a common
  ping target needs a POST. A bookmark or script that calls such a URL with
  a GET is refused.

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
