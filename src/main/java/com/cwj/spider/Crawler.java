package com.cwj.spider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @author root 主要有两大步 
 * 1.通过第一个url下载一个html 对html进行解析 将里面的图片和链接提取出来 
 * 2.链接和图片的过滤
 *         把符合格式的图片下载下来 把符合格式的链接存到队列中（先进先出） 
 *         不断的对队列中的连接进行这两步处理，形成循环，不断下载图片
 */
public class Crawler {
	// 图片默认最大值
	private final static int MAX_NUM = 500;
	// 站点名称
	private final String siteDomain;
	// 下载图片的存放目录目录
	private final String downloadDir;
	// 图片计数
	private static int PIC_COUNT = 0;
	// 访问过的链接，确保不会再次访问
	private static final Set<String> visitedUrl = Collections.synchronizedSet(new HashSet<String>());
	// 已下载的图片，不会再次下载
	private static final Set<String> visitedImg = Collections.synchronizedSet(new HashSet<String>());
	// 要下载的图片格式
	private final PicFormat[] picFormats;

	public Crawler(String siteDomain, String downloadDir, PicFormat[] picFormats) {
		this.siteDomain = siteDomain;
		this.downloadDir = downloadDir;
		this.picFormats = picFormats;
		checkFileExists(downloadDir);
	}

	public Crawler(String siteDomain, String downloadDir) {
		this(siteDomain, downloadDir, new PicFormat[] { PicFormat.JPG, PicFormat.PNG });
	}

	public void start() {
		// 图片过滤器
		start(MAX_NUM, new LinkFilter(), new ImageFilter());
	}

	public void start(int max) {
		// 图片过滤器
		start(max, new LinkFilter(), new ImageFilter());
	}

	public void start(int max, Filter<String> urlFilter, Filter<BufferedImage> imageFilter) {
		new HandlerUrl(urlFilter, imageFilter).start(max);
	}

	/**
	 * 检查目录是否存在
	 * 
	 * @param downLoadDir
	 */
	private void checkFileExists(String downLoadDir) {
		File src = new File(downLoadDir);
		if (!src.exists())
			src.mkdirs();
	}

	/**
	 * @author root 该类用于下载图片
	 */
	class HandleImage {
		private final Logger LOG = Logger.getLogger(HandleImage.class);
		// 处理下载的线程池
		private final ExecutorService executor = Executors.newFixedThreadPool(3);
		// 图片过滤器
		private final Filter<BufferedImage> filterImage;

		public HandleImage(Filter<BufferedImage> imageFilter) {
			filterImage = imageFilter;
		}

		public void stop(){
			if(!executor.isShutdown())
				executor.shutdown();
		}
		
		/**
		 * 处理图片下载
		 * 
		 * @param imgSRC
		 *            该图片的url
		 */
		public void handleImage(String imgSRC) {
			int postfix = imgSRC.lastIndexOf(".");
			// 检查图片是否已下载
			if (postfix == -1 || visitedImg.contains(imgSRC))
				return;
			String imagePostfix = imgSRC.substring(postfix + 1);
			// 检查图片是否符合格式
			if (!ensurePicFormat(imagePostfix))
				return;
			try {
				InputStream murl = new URL(imgSRC).openStream();
//				LOG.info("这里的图片已经被过滤的很多了,还要进行最后的图片大小过滤," + imgSRC);
				BufferedImage sourceImg = ImageIO.read(murl);
				// 通过图片过滤器再次过滤图片，该过滤器可以根据需求定义
				if (filterImage.accept(sourceImg)) {
					// 开启一个线程下载图片
					executor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								visitedImg.add(imgSRC);
LOG.info("download pictrue : " + imgSRC);//注释这句话，就不会打印，提示下载的图片地址了
								// 将图片下载到该目录下
								ImageIO.write(sourceImg, imagePostfix,
										new File(downloadDir + "/" + PIC_COUNT++ + "." + imagePostfix));
							} catch (IOException e) {
								LOG.error("image write error ", e);
							}
						}
					});
				}
			} catch (MalformedURLException e) {
				LOG.error("url isn't valid! ", e);
			} catch (IOException e) {
				LOG.error("image write error ", e);
			}
		}

		/**
		 * 下载默认格式的图片
		 * 
		 * @param url
		 * @return
		 */
		private boolean ensurePicFormat(String postfix) {
			for (PicFormat format : picFormats) {
				if (postfix.endsWith(format.toString()))
					return true;
			}
			return false;
		}

	}

	/**
	 * @author root 这个类用于存放url过滤的连接
	 */
	private class HandlerUrl {
		// url存放的最大容量
		private final int MAX_CAP = 500;
		private final Logger LOG = Logger.getLogger(HandlerUrl.class);
		// 图片过滤器
		private final Filter<String> filter;
		// 下载图片
		private final HandleImage downloadImage;
		// frame的过滤器
		private final NodeFilter frameFilter = new NodeFilter() {
			private static final long serialVersionUID = 1L;

			public boolean accept(Node node) {
				if (node.getText().startsWith("frame src=")) {
					return true;
				} else {
					return false;
				}
			}
		};
		// 三种标签的过滤器
		private final NodeFilter[] filters = new NodeFilter[] { new NodeClassFilter(LinkTag.class), frameFilter,
				new NodeClassFilter(ImageTag.class)

		};

		// OrFilter 来设置过滤 <a> 标签，<frame> 标签 和 image标签
		private final OrFilter linkFilter = new OrFilter(filters);
		// 存放链接的队列
		private final BlockingQueue<String> links;

		public HandlerUrl(Filter<String> urlFilter, Filter<BufferedImage> imageFilter) {
			filter = urlFilter;
			this.links = new LinkedBlockingDeque<>(MAX_CAP);
			this.downloadImage = new HandleImage(imageFilter);
			try {
				links.put(siteDomain);
			} catch (InterruptedException e) {
				LOG.error("add first error!", e);
				Thread.currentThread().interrupt();
			}
		}

		public void start(int max) {
			// 如果是图片已达最大值不在下载
			while (!links.isEmpty() && PIC_COUNT < max) {
				try {
					String url = links.take();
					if (visitedUrl.contains(url))
						continue;
					extractLinks(url); // 抽取html中的url和图片地址
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
//			停止下载的线程池
			downloadImage.stop();
		}

		// 获取一个网站上的链接,filter 用来过滤链接
		private void extractLinks(String url) {
			try {
				visitedUrl.add(url);
				Parser parser = new Parser(url);
				// 得到所有经过过滤的标签
				NodeList list = parser.extractAllNodesThatMatch(linkFilter);
				for (int i = 0; i < list.size(); i++) {
					Node tag = list.elementAt(i);
					if (tag instanceof LinkTag) {
						if (links.size() == MAX_CAP)
							continue;
						LinkTag link = (LinkTag) tag;
						String linkUrl = link.getLink();
						if (filter.accept(linkUrl))
							links.add(linkUrl);
						// 如果是图片就获取url，进行下载
					} else if (tag instanceof ImageTag) {
						ImageTag img = (ImageTag) tag;
						String imgSRC = img.getImageURL();
						// 通过url字符串下载该图片
						downloadImage.handleImage(imgSRC);
					} else {
						// 提取 frame 里 src 属性的链接如 <frame src="test.html"/>
						// 如果连接到了最大值，就不要存链接了
						if (links.size() == MAX_CAP)
							continue;
						String frame = tag.getText();
						int start = frame.indexOf("src=");
						frame = frame.substring(start);
						int end = frame.indexOf(" ");
						if (end == -1)
							end = frame.indexOf(">");
						String frameUrl = frame.substring(5, end - 1);
						if (filter.accept(frameUrl))
							links.add(frameUrl);
					}
				}
			} catch (ParserException e) {
				LOG.error("the url is " + url);
			}
		}

	}
}
