package com.cwj.spider;

/**
 * @author root
 *图片的格式
 */
public enum PicFormat {
	// bmp,jpg,png,tiff,gif,pcx,tga,exif,fpx,svg,psd,cdr,pcd,dxf,ufo,eps,ai,raw,WMF
	JPG, PNG, GIF, SVG, PSD, TIFF, AI, WMF, RAW, UFO, EPS, PCX, BMP;

	@Override
	public String toString() {
		if (this == JPG)
			return "jpg";
		if (this == PNG)
			return "png";
		if (this == GIF)
			return "gif";
		if (this == SVG)
			return "svg";
		if (this == PSD)
			return "psd";
		if (this == TIFF)
			return "tiff";
		if (this == AI)
			return "ai";
		if (this == WMF)
			return "wmf";
		if (this == RAW)
			return "raw";
		if (this == UFO)
			return "ufo";
		if (this == EPS)
			return "eps";
		if (this == PCX)
			return "pcx";
		if (this == BMP)
			return "bmp";
		return "";
	}

}
