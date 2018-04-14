/*
Script: Language.en.js
	MooTools FileManager - Language Strings in Norwegian

Translation:
	[Eilen Amundsgård]
*/

FileManager.Language.no = {
	more: 'Detaljer',
	width: 'Bredde:',
	height: 'Høyde:',

	ok: 'Ok',
	open: 'Velg fil',
	upload: 'Last opp',
	create: 'Opprett mappe',
	createdir: 'Angi mappenavn:',
	cancel: 'Avbryt',
	error: 'Feil',

	information: 'Informasjon',
	type: 'Type:',
	size: 'Størrelse:',
	dir: 'Plassering:',
	modified: 'Sist endret:',
	preview: 'Forhåndsvisning',
	close: 'Lukk',
	destroy: 'Slett',
	destroyfile: 'Er du sikker på at du vil slette denne filen?',

	rename: 'Endre navn',
	renamefile: 'Angi et nytt filnavn:',

	download: 'Last ned',
	nopreview: '<i>Ingen forhåndsvisning tilgjengelig</i>',

	title: 'Tittel:',
	artist: 'Artist:',
	album: 'Album:',
	length: 'Lengde:',
	bitrate: 'Bitrate:',

	deselect: 'Fjern markering',

	nodestroy: 'Muligheten for å slette filer er deaktivert på denne serveren.',

	toggle_side_boxes: 'Thumbnail visning',
	toggle_side_list: 'Liste visning',
	show_dir_thumb_gallery: 'Vis thumbnails av filene i forhåndsvisningsruten',
	drag_n_drop: 'Drag & drop er aktivert for denne mappen',
	drag_n_drop_disabled: 'Drag & drop er midlertidig deaktivert for denne mappen',
	goto_page: 'Gå til siden',

	'backend.disabled': 'Opplasting av filer er deaktivert på denne serveren.',
	'backend.authorized': 'Du har ikke rettigheter til å laste opp filer.',
	'backend.path': 'Den angitte opplastingsmappen eksisterer ikke. Vennligst kontakt denne sidens administrator.',
	'backend.exists': 'Det angitte opplastingsplasseringen finnes allerede. Vennligst kontakt denne sidens administrator.',
	'backend.mime': 'Den angitte filtypen er ikke lovlig.',
	'backend.extension': 'Den opplastede filen er av en ukjent eller ulovlig filtype.',
	'backend.size': 'Størrelsen på den opplastede filen er for stor til å bli behandlet på denne serveren. Vennligst last opp en mindre fil.',
	'backend.partial': 'Filen du lastet opp ble bare delvis opplastet, vennligst prøv å laste opp filen på nytt.',
	'backend.nofile': 'Ingen fil ble angitt for opplasting.',
	'backend.default': 'Noe gikk galt under opplasting av filen.',
	'backend.path_not_writable': 'Du har ikke skrive-/opplastingrettigheter for denne mappen.',
	'backend.filename_maybe_too_large': 'Filnavnet/stien er sannsynligvis for lang for serverens filsystem. Vennligst forsøk igjen med et kortere filnavn.',
	'backend.fmt_not_allowed': 'Du har ikke rettigheter til å laste opp filer av denne typen eller med dette navnet.',
	'backend.unidentified_error': 'En uidentifiserbar feil oppsto under kontakten med backend (webserveren).',

	'backend.nonewfile': 'Et nytt navn for filen som skal flyttes / kopieres mangler.',
	'backend.corrupt_img': 'Dette er enten ikke en bildefil eller så er den korrupt: ', // path
	'backend.resize_inerr': 'På grunn av en intern feil kan ikke denne filen endre størrelse.',
	'backend.copy_failed': 'En feil oppstod under kopiering av filen/mappen: ', // oldlocalpath : newlocalpath
	'backend.delete_cache_entries_failed': 'En feil oppstod under et forsøk på å slette objektets cache (thumbnails, metadata)',
	'backend.mkdir_failed': 'En feil oppstod under et forsøk på å opprette mappen: ', // path
	'backend.move_failed': 'En feil oppstod under flytting/endring av navn på filen/mappen: ', // oldlocalpath : newlocalpath
	'backend.path_tampering': 'Forsøk på å endre plassering er oppdaget.',
	'backend.realpath_failed': 'Kan ikke oversette den gitte filspesifikasjonen til en gylding lagringsadresse: ', // $path
	'backend.unlink_failed': 'En feil oppstod under et forsøk på å slette filen/mappen: ',  // path

	// Image.class.php:
	'backend.process_nofile': 'Enheten for bildeprosessering fikk ikke en gyldig filplassering å jobbe på.',
	'backend.imagecreatetruecolor_failed': 'Enheten for bildeprosessering feilet: GD imagecreatetruecolor() feilet.',
	'backend.imagealphablending_failed': 'Enheten for bildeprosessering feilet: kan ikke utføre alpha blending på bildet.',
	'backend.imageallocalpha50pctgrey_failed': 'Enheten for bildeprosessering feilet: kan ikke allokere plass for alpha kanalen og 50% bakgrunn.',
	'backend.imagecolorallocatealpha_failed': 'Enheten for bildeprosessering feilet: kan ikke allokere plass for alpha kanalen for denne fargen.',
	'backend.imagerotate_failed': 'Enheten for bildeprosessering feilet: GD imagerotate() feilet.',
	'backend.imagecopyresampled_failed': 'Enheten for bildeprosessering feilet: GD imagecopyresampled() feilet. Bildeoppløsning: ', /* x * y */
	'backend.imagecopy_failed': 'Enheten for bildeprosessering feilet: GD imagecopy() feilet.',
	'backend.imageflip_failed': 'Enheten for bildeprosessering feilet: kan ikke rotere bildet.',
	'backend.imagejpeg_failed': 'Enheten for bildeprosessering feilet: GD imagejpeg() feilet.',
	'backend.imagepng_failed': 'Enheten for bildeprosessering feilet: GD imagepng() feilet.',
	'backend.imagegif_failed': 'Enheten for bildeprosessering feilet: GD imagegif() feilet.',
	'backend.imagecreate_failed': 'Enheten for bildeprosessering feilet: GD imagecreate() feilet.',
	'backend.cvt2truecolor_failed': 'konvertering til True Color feilet. Bildeoppløsning: ', /* x * y */
	'backend.no_imageinfo': 'Korrupt bilde eller ikke en bildefil.',
	'backend.img_will_not_fit': 'Server feil: bildet får ikke plass i tilgjengelig RAM; minimumskrav (estimat): ', /* XXX MBytes */
	'backend.unsupported_imgfmt': 'bildeformat ikke støttet: ',    /* jpeg/png/gif/... */

	/* FU */
	uploader: {
		unknown: 'Ukjent feil',
		sizeLimitMin: 'Du kan ikke laste opp "<em>${name}</em>" (${size}), minste tillatte størrelse er <strong>${size_min}</strong>!',
		sizeLimitMax: 'Du kan ikke laste opp "<em>${name}</em>" (${size}), største tillatte størrelse er <strong>${size_max}</strong>!',
		mod_security: 'Fikk ikke svar fra opplasteren, dette kan bety at "mod_security" er aktivert på serveren og at en av reglene i mod_security har avbrutt denne forespørselen. Hvis du ikke kan deaktivere mod_security, er det mulig du må bruke NoFlash opplasteren.'
	},

	flash: {
		hidden: 'For å aktivere den innebygde opplasteren, fjern blokkeringen i din nettleser og oppdater siden (se Adblock).',
		disabled: 'For å aktivere den innebygde opplasteren, aktiver den blokkerte Flashfilmen og oppdater siden (se Flashblock).',
		flash: 'For å kunne laste opp filene dine må du installere <a href="http://www.adobe.com/shockwave/download/download.cgi?P1_Prod_Version=ShockwaveFlash">Adobe Flash</a>.'
	},

	resizeImages: 'Juster størrelse på store bilder under opplasting',

	serialize: 'Lagre galleri',
	gallery: {
		text: 'Bildetittel',
		save: 'Lagre',
		remove: 'Fjern fra galleri',
		drag: 'Dra filer hit for å lage et galleri...'
	}
};