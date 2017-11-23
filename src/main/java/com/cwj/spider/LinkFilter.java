package com.cwj.spider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkFilter implements Filter<String>{
	private static final String URLFilter = "(http|https).*\\.html";
	@Override
	public boolean accept(String url) {
		Matcher matcher = Pattern.compile(URLFilter).matcher(url);
		return matcher.find();
	}

}
