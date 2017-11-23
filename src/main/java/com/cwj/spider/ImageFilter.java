package com.cwj.spider;

import java.awt.image.BufferedImage;

/**
 * @author root
 *图像过滤器
 *默认过滤只有宽高大于300px的图片
 */
public class ImageFilter implements Filter<BufferedImage>{

	@Override
	public boolean accept(BufferedImage image) {
		if(image == null ) return false;
		if(image.getHeight() >= 200 && image.getWidth() >= 200)
			return true;
		return false;
	}

}
