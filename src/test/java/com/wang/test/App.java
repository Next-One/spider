package com.wang.test;

import java.awt.image.BufferedImage;

import com.cwj.spider.Crawler;
import com.cwj.spider.Filter;

/**
 * 控制台会打印下载的图片的url 我用的log 你可以注释掉
 *
 */
public class App {
	public static void main(String[] args) {
		/*if (args.length < 2) {
			System.err.println("Usage: App <startUrl>  <localDir>");
			System.exit(1);
		}
		// 这里是本机存储位置
		String startURL = args[0];
		String localDir = args[1];*/

		 String startURL ="http://www.quanjing.com/search.aspx?q=%E9%A9%AC%E5%B0%94%E4%BB%A3%E5%A4%AB||1|200|1|2||||#马尔代夫||1|200|1|2||||";
		// // 开始爬图片的网站
		// String startURL = "http://www.win4000.com/meinvtag10141.html"; //
		// 开始爬图片的网站
		 String localDir = "g:/a/pic"; // 本地目录
		Crawler crawler = new Crawler(startURL, localDir);
		// 这里是要爬取图片的网站，上面几个都可以进行爬取，你也可以找网址测试
		// http://www.tooopen.com/img/88_282.aspx
		// http://699pic.com/?sem=1
		// http://www.nipic.com/index.html
		// http://588ku.com/?hd=129
		// https://pixabay.com/
		// http://bizhi.4493.com/image/23170.html
		// crawler.crawling();
		// https://sale.jd.com/act/cmYEkZyJ6z.html
		// http://www.enterdesk.com/special/meizi/
		// https://image.baidu.com
		// https://www.jd.com
		// http://4493bz.1985t.com/uploads/allimg/160405/5-160405144I9.jpg
		// https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=2539922263,2810970709&fm=200&gp=0.jpg

		// url过滤
		crawler.start(
				// 下载500张
				500, 
//				连接过滤
				new Filter<String>() {

					@Override
					public boolean accept(String url) {
						// 这里表示爬的连接的开始部分，不是以这个开始的链接，都不爬
						if (url.startsWith("http://www.quanjing.com"))
							return true;
						return false;
					}
					// 图片大小过滤
				}, new Filter<BufferedImage>() {

					@Override
					public boolean accept(BufferedImage img) {
						if (img == null)
							return false;
						// 宽高都要大于100像素
						if (img.getWidth() > 100 && img.getHeight() > 100)
							return true;
						return false;
					}
				});
	}

}
