/*
Script: Language.en.js
	MooTools FileManager - Language Strings in Simplified Chinese

Translation:
	[Ronfey Li] (email: tangdaoke@gmail.com)
*/

FileManager.Language.zh = {
	more: '详细',
	width: '宽:',
	height: '高:',

	ok: '好',
	open: '选择文件',
	upload: '上传',
	create: '新建文件夹',
	createdir: '请指定一个文件夹:',
	cancel: '取消',
	error: '出错',

	information: '详细信息',
	type: '类别:',
	size: '大小:',
	dir: '路径:',
	modified: '最后更改:',
	preview: '预览',
	close: '关闭',
	destroy: '删除',
	destroyfile: '确定删除此文件？',

	rename: '重命名',
	renamefile: '请输入新的文件名:',

	download: '下载',
	nopreview: '<i>不适用</i>',

	title: '标题:',
	artist: '艺术家:',
	album: '专辑:',
	length: '时间:',
	bitrate: '比率:',

	deselect: '取消选择',

	nodestroy: '删除受限.',

	toggle_side_boxes: '缩略图视图',
	toggle_side_list: '列表视图',
	show_dir_thumb_gallery: '预览面板显示文件缩略图',
	drag_n_drop: '此文件夹的拖放功能已开启',
	drag_n_drop_disabled: '此文件夹的拖放功能已暂时关闭',
	goto_page: '转至页码',

	'backend.disabled': '上传受限。',
	'backend.authenticated': '您没有上传的权限。',
	'backend.path': '上传文件夹不存在，请联系网管。',
	'backend.exists': '上传位置已经存在，请联系网管。',
	'backend.mime': '禁止的文件类型。',
	'backend.extension': '上传未知或是禁止的文件类型。',
	'backend.size': '文件过大，请重新上传小些的文件。',
	'backend.partial': '上传的文件不完全，请重新上传。',
	'backend.nofile': '没有指定文件。',
	'backend.default': '上传出错。',
	'backend.path_not_writable': '您没有更改或上传此文件夹的权限。',
	'backend.filename_maybe_too_large': '文件名过长。请重新输入短些的名称。',
	'backend.fmt_not_allowed': '此文件类型禁止上传。',
	'backend.unidentified_error': '与后台服务器通讯发生未知错误。',

	'backend.nonewfile': '找不到待拷贝或移动的文件的新名称.',
	'backend.corrupt_img': '此文件不是图像或者已损坏： ', // path
	'backend.resize_inerr': '出现内部错误此文件不能更改大小.',
	'backend.copy_failed': '复制错误： ', // oldlocalpath : newlocalpath
	'backend.delete_cache_entries_failed': '删除图像缓存（缩略图，元数据）出错',
	'backend.mkdir_failed': '创建文件夹出错：', // path
	'backend.move_failed': '移动/重命名文件或文件夹出错: ', // oldlocalpath : newlocalpath
	'backend.path_tampering': '检测到路径更改.',
	'backend.realpath_failed': '不能保存到有效的路径: ', // $path
	'backend.unlink_failed': '删除文件或文件夹出错： ',  // path

	// Image.class.php:
	'backend.process_nofile': '图像处理器收到无效的文件路径。',
	'backend.imagecreatetruecolor_failed': '图像处理器出错: GD imagecreatetruecolor() 失败.',
	'backend.imagealphablending_failed': '图像处理器出错: 不能执行图像alpha混合.',
	'backend.imageallocalpha50pctgrey_failed': '图像处理器出错: 不能分配空间给alpha通道和50%背景。',
	'backend.imagecolorallocatealpha_failed': '图像处理器出错: 不能分配空间给当前彩色图像的alpha通道。',
	'backend.imagerotate_failed': '图像处理器出错: GD imagerotate() 失败.',
	'backend.imagecopyresampled_failed': '图像处理器出错: GD imagecopyresampled() 失败. 图像分辨率： ', /* x * y */
	'backend.imagecopy_failed': '图像处理器出错: GD imagecopy() 失败.',
	'backend.imageflip_failed': '图像处理器出错: 不能翻转图像.',
	'backend.imagejpeg_failed': '图像处理器出错: GD imagejpeg() 失败.',
	'backend.imagepng_failed': '图像处理器出错: GD imagepng() 失败.',
	'backend.imagegif_failed': '图像处理器出错: GD imagegif() 失败.',
	'backend.imagecreate_failed': '图像处理器出错：GD imagecreate() 失败.',
	'backend.cvt2truecolor_failed': '转换真彩色失败。图像分辨率: ', /* x * y */
	'backend.no_imageinfo': '损坏的图像或者文件不是图像.',
	'backend.img_will_not_fit': '服务器出错: 内存不够；最低要求(估计): ', /* XXX MBytes */
	'backend.unsupported_imgfmt': '不支持的图像格式: ',    /* jpeg/png/gif/... */

	/* FU */
	uploader: {
		unknown: '未知错误',
		sizeLimitMin: '添加 "<em>${name}</em>" (${size})失败, 文件不足<strong>${size_min}</strong>!',
		sizeLimitMax: '添加 "<em>${name}</em>" (${size})失败, 文件超过<strong>${size_max}</strong>!',
		mod_security: '上传器没有反应，很可能是因为“mod_security”是开启的同时它的其中一个规则取消了上传请求。如果您不能解除“mod_security”，请使用非flash上传器。'
	},

	flash: {
		hidden: '为了使上传器生效，取消浏览器阻止，然后刷新 (见Adblock).',
		disabled: '为了使上传器生效, 取消阻止Flash，然后刷新 (见Flashblock).',
		flash: '上传前需安装好<a href="http://www.adobe.com/shockwave/download/download.cgi?P1_Prod_Version=ShockwaveFlash">Adobe Flash</a>.'
	},

	resizeImages: '缩小大尺寸图像',

	serialize: '保存为画廊',
	gallery: {
		text: '图片描述',
		save: '保存',
		remove: '从画廊移除',
		drag: '拖动至此创建画廊...'
	}
};