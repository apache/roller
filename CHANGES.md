# Apache Roller — Changes

## 7.0.0

### Improvements

- **Redesigned media library.** Browsing, search and upload were rebuilt
  around one page per folder (PR #233).
  - One search box with type filters, a search scope (this folder or all
    folders), sorting by date, name or type, and paging that keeps its place.
  - Upload several files at once by choosing or dragging them. Each file
    gets its own result, and only the files that failed are retried.
  - A details page for each file, with a preview, its facts, a copy-URL
    button and the edit form. The description is used as the image's alt text.

- **Redesigned categories page.** Categories are one list you can reorder.
  - Drag a category by its handle, or use the arrow keys on the handle, to
    change the order themes show categories in.
  - Each category shows how many entries it has, linked to those entries.
  - Add and edit in one dialog with field-level checks and an image preview.
    When media uploads are on, the dialog can choose the category image
    from the media library. Deleting a category that has entries asks
    where to move them.
  - The page works on phones, and a short message confirms each change.

- **AtomPub can manage templates and categories.** New collections
  in each weblog's workspace, documented in the user guide:
  - Templates (standard and mobile versions) for weblog administrators,
    when the weblog uses a custom theme.
  - Categories, including an RFC 5023 category document at
    `/{handle}/categories.atomcat`. The entries collection lists its
    categories inline, as before, and also references that document.

- **New AtomPub Guide** (`docs/roller-atompub-guide.adoc`). It documents
  every AtomPub URI, permission, element and status code, with examples
  that the test suite checks against the code.

- **Redesigned blogroll page.** A weblog's blogrolls are listed beside
  their links, and each blogroll has its own address.
  - Drag a link by its handle, or use the arrow keys on the handle, to
    change the order themes show links in.
  - Select links to move them to another blogroll or delete them.
  - Add and edit links in one dialog that starts with the URL, fills in the
    name from it, and checks each field. OPML import is back on the page.
  - The default blogroll can't be renamed, since themes find it by its
    name, and the page works on phones.

- **Form labels sit above their fields.** In the admin forms a label used to
  sit in a column beside its field, so short labels were a long way from
  their inputs and long ones wrapped onto two lines. Labels are now above
  their fields at every width, as they already were on phones. On the entry
  editor, the Permalink and Status rows line up with the rest and the status
  is shown as a badge.

### Bug fixes

- **New passwords can't be the external-login placeholder.** Registration,
  the profile page and user administration refuse
  `users.passwords.externalAuthValue` (by default `<externalAuth>`) as a
  password, and it never works as one. When you change that setting, list its
  earlier values in the new `users.passwords.externalAuthValue.previous`
  (comma-separated) so they're treated the same. A login refused for this
  reason is logged, and an administrator can set a new password for an
  account that used one (#304, #364).
- **Roller UI stylesheets, scripts and images don't start a session** (#310).
- **The LDAP comment authenticator escapes the commenter's user name** when
  it builds the bind name (#322).
- **Changing a password ends the account's other sessions.** Saving a new
  password in the profile, an administrator's reset, or disabling an account
  now logs out that user's other browsers at their next Roller UI page; the
  session that made the change stays logged in (#321).
- **Profiles of OpenID Connect users can be saved again.** Their random
  placeholder password was longer than BCrypt accepts, so saving the profile
  failed (#323).
- **Later pages of a feed are cached separately.** A feed's `?page=N` now has
  its own cache entry, and page numbers are kept between 0 and 10,000 (#313).
- **"nofollow" is added to links that need it.** `addNofollow` only touched
  links that already had `rel="nofollow"`. It now parses the HTML with jsoup
  and adds nofollow to each link's own `rel` value, so its output is jsoup's
  normalized HTML: open tags are closed and attribute values are
  double-quoted (#318).
- **HTML comments are cleaned with jsoup.** The HTMLSubset comment plugin
  and HTML comment e-mails keep the same tags and link schemes, but a tag
  outside them is now removed, keeping its text, instead of being shown as
  markup, and a link keeps its address when it has other attributes (#353).
- **Tags of a scheduled entry are counted once, when it goes live.** Entries
  posted through AtomPub with a future date showed their tags in tag counts
  early and counted them twice (#316).
- **A weblog's only administrator can't resign from it**, and declining an
  invitation only removes a pending invitation (#322).
- **Passwords stored with an older encoding are upgraded at login.** With
  `passwds.encryption.lazyUpgradeFrom` set (`SHA` by default), passwords from
  older Roller versions could be used to log in, but they stayed stored in the
  old encoding. A successful login now stores the password again with
  `passwds.encryption.algorithm`, bcrypt by default. Nothing is rewritten when
  that would store plain text, that is while `passwds.encryption.enabled` is
  off or the algorithm is `noop`.
- **Weblogs no longer advertise XML-RPC while it is switched off.** With
  `webservices.enableXmlRpc` off, each weblog page still linked to an RSD
  document listing the Blogger and MetaWeblog endpoints, which then answered
  404. The link now appears, and the RSD document answers, only while both
  that setting and the weblog's client support are on. Refused XML-RPC
  requests are logged at debug level instead of as a warning each.
- **A search index rebuild that is interrupted is redone at the next start.**
  When Roller replaced the index at startup it also deleted the marker that
  records an unclean shutdown, so if it was stopped during the rebuild, the
  next start kept the incomplete index.
- **The entry editor's "Pinned to main" checkbox shows its tooltip.** The
  attribute was misspelled, so the help text never appeared.
- **A bad date pattern in a theme no longer blanks the page.**
  `$utils.formatDate` logs a warning and uses the locale's standard date and
  time format. Older themes often pass a message key that no longer exists,
  such as `macro.weblog.date.timestampFormat`, which `$text.get` returns as is.
- **Requested tags are checked and encoded on every weblog page.** A custom
  page given `tags=` together with an entry now answers 404 for tags the
  weblog doesn't have, as it already did without an entry. The tag names are
  HTML-encoded in the feed link titles and in the tag pages of the Gaurav,
  Fauxcoly, Hyde, Terminal and PaperMod themes. The page cache, including the
  front page weblog's, keys such pages by page name and tags, so they are no
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
- **A database password made only of hex digits no longer stops Roller from
  starting.** EclipseLink mistook such a password for an encrypted one and
  failed with "Database password was encrypted by deprecated algorithm".
- **Page menus list a weblog's custom pages again.** `#showPageMenu` and the
  menus in the Gaurav and PaperMod themes loop over `$weblog.pages`, which
  had stopped existing, so pages marked "include in page menu" never appeared.
- **Reading a weblog no longer starts a session.** Every page, feed and
  resource request from a visitor or crawler was given an HTTP session and a
  form token it had no use for.
- **Search shows how many entries matched and pages through all of them.**
  The count was the number of results on the current page, so it never went
  above ten and there was no link to a second page. Results past the first
  500 matches can now be reached too: paging goes as far as the newest 5,000
  matches, a limit the new `search.results.max` property sets. The count is
  the number of matches in the search index, so it can be higher than the
  number of entries the pages reach.
- **AtomPub fixes found while running a 7.0.0 snapshot.**
  - Uploaded media files get the usual extension for their type:
    `image/svg+xml` gives `.svg`, parameters such as `charset` are ignored,
    and a `Slug` that already has a matching extension keeps it.
  - The `next` and `previous` links of a named media directory include the
    slash before the offset, so paging through it works.
  - Characters that XML 1.0 does not allow are left out of AtomPub
    responses, so one stored control character no longer makes a whole page
    unreadable.
  - A file uploaded over AtomPub can be read back through its `edit-media`
    URI; it answered 404.
- **The rich text editor's menus show their labels again.** Summernote is
  upgraded to 0.9.1, and the panel chevron no longer appears on the
  editor's menu items.
- **Fixes found while upgrading a 6.1 site to a 7.0.0 snapshot.**
  - The database create and upgrade result pages show a clear status, one
    Start Roller button, and the script output in a panel you can expand.
  - The media library opens, and theme import works, for weblogs that have
    no default media folder; Roller creates the folder instead of failing.
  - A custom template with no content logs one warning line instead of a
    stack trace.
  - The 404 page shows a plain message and a link to the home page, not
    the server's internal error text.
  - The Theme and Create Weblog pages work with a shared theme whose
    `theme.xml` names a preview image that isn't there; they show no preview
    instead of failing to load the theme's description.
  - The Emoticons plugin no longer changes text inside HTML tags or
    comments, so the `:x` in an attribute such as `xmlns:xlink` stays as it
    is; in an embedded SVG it used to become an emoji.

### Behaviour changes worth reading before upgrading

- **Weblog templates have render limits.** A page stops rendering, with a
  logged error, once it writes more than `rendering.maxOutputChars` (10 MB)
  or runs longer than `rendering.maxRenderSeconds` (30 seconds); `#foreach`
  stops at 100,000 passes, and `String.repeat` isn't available. `#parse` and
  `#include` load only the weblog's own templates, and macros defined inline
  in a template stay with the page that defines them and the templates it
  includes, so weblogs can't affect each other's macros.
  Library macros such as `roller.vm` and a site's own `roller-custom.vm` stay
  global. Error pages no longer show template ids (#317).
- **Entries with scripts or embeds open as HTML.** If an entry contains
  scripts, frames, embedded objects, event-handler attributes or
  `javascript:` links, the rich-text editor opens it in the HTML textarea
  with a notice, and the stored HTML is kept as is (#319).
- **Names are shown as text.** Screen and full names are HTML-escaped where
  templates print them and written as plain text in feeds; commenter names
  and addresses are escaped in HTML notification emails; HTML comments are
  escaped unless the HTMLSubset plugin applies to them (#318).
- **Limits on uploads and fetched feeds.** Images with more pixels than
  `uploads.thumbnail.maxPixels` (50 million) are stored without a thumbnail
  (#315). Planet stops reading a feed past `planet.aggregator.fetch.maxBytes`
  (5 MB) or `planet.aggregator.fetch.maxSeconds` (30 seconds), and fetches
  only http and https URLs (#314).
- **AtomPub checks.** Publishing an entry through an AtomPub update needs the
  permission to publish, media uploads over `uploads.file.maxsize` are
  refused with 413 before they're stored, and entries show the creator's
  email address only to the creator (also in MetaWeblog responses) (#316).
- **Search queries are limited to 256 characters**, and a query the search
  engine can't run finds nothing (#322).
- **The OpenID Connect identity can't be changed from the profile or
  registration forms**; only Roller's OpenID Connect login sets it (#322).
- **Weblog pages ignore a `skipCache` query parameter** (#313).
- **Check "Absolute URL to site" (`site.absoluteurl`) after upgrading.**
  Roller no longer takes the site's URL from the first request after a start.
  New installs save it during setup, and on an upgraded site where it's empty,
  the first administrator to open the Roller UI saves it from that request,
  unless it's a local or private address. Check it under Server Admin >
  Configuration, and correct it if the site is reached through a proxy or more
  than one host name. While it's empty, each request's own URL is used for its
  links, cached pages are kept per host name, comment notifications are sent
  without links, account activation and invitation emails aren't sent, and
  background tasks such as the planet sync wait for it. Links keep the
  request's https scheme and port.
  `WebloggerRuntimeConfig.setAbsoluteContextURL` and
  `InitFilter.getAbsoluteUrl` are removed (#320).
- **The Design pages follow "Allow custom themes".** While
  `themes.customtheme.allowed` is off (the default), the theme, stylesheet and
  template pages answer as unavailable, as the hidden Design menu already
  implied, and the XML-RPC `blogger.setTemplate` call is refused. Weblogs that
  already use custom templates keep showing them. Turn the setting on in
  Server Settings to edit templates again.

- **Blogger and MetaWeblog requests have a size limit.** Roller now answers
  XML-RPC itself instead of through Apache XML-RPC. Clients see the same
  methods, faults and fault codes, but a request larger than 1 MiB is
  refused with HTTP 413. While uploads are on, the limit is raised to fit
  the largest allowed upload as base64, up to 32 MiB, so a file larger
  than about 23 MB has to be uploaded in the media library instead of
  through a blog client. The limit also applies to the
  decompressed size of a gzip-encoded request. Only the standard XML-RPC
  value types are accepted.
  Two other differences: characters outside the Basic Multilingual Plane,
  such as emoji, are now sent as valid XML (the old library wrote each
  surrogate half as its own reference, which clients rejected), and stored
  carriage returns are now sent as `&#13;`, so clients receive line breaks
  as stored rather than normalised to `\n`.

- **Themes from Roller 5.0 or earlier may need updating.** Roller 5.1 removed
  the pre-3.0 macros in `WEB-INF/velocity/deprecated/`, such as
  `#showWeblogEntries` and `#showBookmarks`, and referrer macros such as
  `#showReferrersList`. A template that still calls one prints the call as
  plain text. Compare your custom
  templates with the macros in `WEB-INF/velocity/weblog.vm`, or switch to a
  shared theme and customize from there.

- **The obsolete weblog update ping feature is removed.** Public ping services
  have largely shut down following years of ping spam. The 7.0 database upgrade
  drops the three ping tables and removes their settings and task lock. Remove
  `PingQueueTask` from `tasks.enabled`, and remove any `tasks.PingQueueTask.*`
  and `pings.*` settings from `roller-custom.properties` before upgrading.
  If the removed task class is still configured, Roller logs a task-loading
  error and continues startup.

- **Roller now runs on Jakarta EE**
  ([ROL-2183](https://issues.apache.org/jira/browse/ROL-2183)). It needs Java 21
  or later and a Servlet 6.1 container such as Tomcat 11. Tomcat 9 and other
  `javax.servlet` containers can no longer run Roller.
- **The search index is rebuilt when upgrading from Roller 6.1.6 or
  earlier.** Roller 7 uses Lucene 10, which reads the index Roller 6.1.7
  writes. An older index either holds fields Roller no longer indexes or,
  like the Lucene 8 index of Roller 6.0, can't be read at all. Roller
  replaces it at first startup and rebuilds it in the background, so no
  action is needed. Search results are complete again once the log shows
  "Completed rebuilding index".
- **A search word with a hyphen or other punctuation must match all its
  parts.** Search has always required every word, but a word the index splits
  into parts, such as `spring-boot`, `wi-fi` or `AT&T`, used to match entries
  with any one part. With Lucene 10 it needs all of them, so `spring-boot`
  finds the same entries as `spring boot`, and such searches can return far
  fewer results than before.
- **OAuth 1.0a is removed** ([ROL-2183](https://issues.apache.org/jira/browse/ROL-2183)).
  AtomPub accepts only BASIC authentication, and the OAuth credentials page and
  consumer keys are gone. If `webservices.atomPubAuth` is still set to `oauth`,
  AtomPub refuses every request until you set it to `basic`.
- **OpenID 2.0 login is removed** ([ROL-2183](https://issues.apache.org/jira/browse/ROL-2183)).
  Sites with `authentication.method` set to `openid` or `db-openid` must switch
  to another method before upgrading.
- **AtomPub no longer uses ROME Propono.** Roller now has its own AtomPub
  server. The `propono.properties` file and the `RollerAtomHandlerFactory`
  extension point are removed, so a deployment that configured its own
  `AtomHandlerFactory` (for example, for custom authentication) no longer has
  that handler. AtomPub clients see no change.
- **Icons and images without a compatible license are gone.** The Gaurav theme
  no longer ships Font Awesome 3, and the Gaurav and Fauxcoly themes, the
  admin pages and the Emoticons plugin no longer use Silk icons or images of
  unknown origin. Bootstrap Icons replace them, drawn inline so they look the
  same in a customized copy of a theme. The Emoticons plugin now turns `:-)`
  into a Unicode emoji instead of an image from `/images/smileys/`, which is
  removed along with `/images/editor/` and most Silk icons in `/images/`.
  The standard feed icon, the RSS, Atom and OPML badges, the OpenID logo and
  the permalink and launch-link icons in `/images/` are gone too, with feed
  links now drawn as a Bootstrap Icons feed icon. The unused admin colour
  themes under `/roller-ui/theme/` and three unused images in
  `/roller-ui/images/` are removed.
  Custom templates that link to those files, or to `font-awesome.min.css` or
  the theme icons other than `asf-logo.png`, show broken images until they
  are updated. Weblogs already using a customized copy keep their own copies
  of the old files. Search term highlighting in both themes now uses a new
  `js/search-highlight.js`; customized search pages that load `searchhi.js`
  keep working only while their copy of that file remains.

- **Comments are limited to 10,000 characters.** Longer comments are refused,
  with a message saying so, before they are previewed or checked by the
  comment validators. The commenter's name, e-mail address and URL are limited
  to 255 characters each, the size of the columns that store them. Set `comment.maxLength` in `roller-custom.properties` to
  change the limit. The bundled `bannedwordslist.txt` rules match the same
  domains as before but look at most 63 characters between their parts; a
  copy of the old file in the uploads directory is still used if present.

### Build and packaging

- The Docker image starts Tomcat with `-XX:-UseBASE64Intrinsics`, which
  avoids a Base64 decoding flaw in JDKs older than the November 2026 updates
  on x86-64 processors with AVX-512 (CVE-2026-114938). Sites that run Roller
  on their own server should update their JDK, or set the same option until
  they can; the install guide says how. A `CATALINA_OPTS` passed to the
  container is added to this option.
- Apache XML-RPC (`xmlrpc-common`, `xmlrpc-server`) and `ws-commons-util`
  are no longer shipped. Apache XML-RPC is end of life, and 3.1.3 has an
  unfixed denial-of-service issue (CVE-2016-5004).
- Third-party libraries are updated to their latest patch and minor releases,
  among them Struts 7.4.0, Spring 7.0.10 and Spring Security 7.1.2. OGNL is
  pinned to 3.4.17, which fixes CVE-2026-113730.
- **The WAR no longer includes jcl-over-slf4j.** Roller logs through
  commons-logging 1.4, which sends everything straight to Log4j. A deployment
  that added its own commons-logging bridge should remove it.
- The source distribution now extracts to `apache-roller-<version>-source/`,
  matching its file name, instead of `roller-weblogger-<version>-source/`.
- ROME, the feed library Planet uses, is upgraded from 1.19.0 to 2.1.0.
- The admin UI now uses jQuery 4.0.0 and jQuery Validation 1.22.1.

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
