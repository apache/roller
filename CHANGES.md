# Apache Roller — Changes

## 6.1.6

Initial installation now requires a one-time, cryptographically secure setup token printed to the server log; bootstrap access closes after the first administrator is created.

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
