package edu.iust.selab.htmlchardet;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * 
 * @author <a href="mailto:shabanali.faghani@gmail.com">Shabanali Faghani</a>
 * 
 */
public class HTMLCharsetDetector {

	private static final Logger LOG = Logger.getLogger(HTMLCharsetDetector.class);
	private static final int threshold = 10;

	public static String detect(byte[] content, boolean lookInMeta) {

		String html = new String(content, Charset.forName("ISO-8859-1"));
		Document domTree = Jsoup.parse(html);

		String charset = null;
		if (lookInMeta) {
			charset = lookInMetaTags(domTree);
			if (charset != null) {
				return Charsets.normalize(charset);
			}
		}
		
		charset = mozillaJCharDet(content);
		if (charset.equalsIgnoreCase("UTF-8")) {
			return Charsets.normalize(charset);
		}

		String visibleText = domTree.text();
		byte[] byteSequence = null;
		try {
			byteSequence = visibleText.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// This Exception will never raised, because java supports ISO-8859-1. Anyway, ...
			LOG.warn("could not convert string to byte array using \"ISO-8859-1\" charset. "
					+ "Detection process will use raw html document as input.", e);
		}
		if (byteSequence == null || byteSequence.length < threshold) {
			byteSequence = content;
		}

		charset = ibmICU4j(byteSequence);
		return Charsets.normalize(charset);
	}

	private static String lookInMetaTags(Document document) {
		String charset = null;
		Elements metaElements = document.select("meta");
		for (Element meta : metaElements) {
			charset = meta.attr("charset");
			if (Charsets.isValid(charset)) {
				return charset;
			} else {
				String contentAttr = meta.attr("content");
				if (contentAttr.indexOf("charset") != -1) {
					int charsetStart = contentAttr.indexOf("charset=") + 8;
					int charsetEnd = contentAttr.length();
					charset = contentAttr.substring(charsetStart, charsetEnd).trim();
					if (Charsets.isValid(charset)) {
						return charset;
					}
				}
			}
		}
		return null;
	}

	private static String mozillaJCharDet(byte[] bytes) {
		int lang = nsDetector.ALL;
		nsDetector det = new nsDetector(lang);
		det.Init(new nsICharsetDetectionObserver() {
			public void Notify(String charset) {
				// HtmlCharsetDetector.found = true;
			}
		});
		det.DoIt(bytes, bytes.length, false);
		det.DataEnd();
		String[] charsets = det.getProbableCharsets();
		det.Reset();
		return charsets[0];
		// return det.getProbableCharsets()[0];
	}

	private static String ibmICU4j(byte[] bytes) {
		CharsetDetector charsetDetector = new CharsetDetector();
		charsetDetector.setText(bytes);
		CharsetMatch charsetMatch = charsetDetector.detect();
		String charset = charsetMatch.getName();
		return charset;
	}

	/**
	 * @deprecated
	 * @param content
	 * @return
	 */
	public static String detect(byte[] content) {

		String charset = mozillaJCharDet(content);
		if (charset.equalsIgnoreCase("UTF-8")) {
			return Charsets.normalize(charset);
		}

		String bodyStart = "<body";
		String bodyEnd = "/body>";
		String scriptStart = "<script";
		String scriptEnd = "/script>";
		String styleStart = "<style";
		String styleEnd = "/style>";

		byte[] tempArr = new byte[content.length * 2];
		int tempArrIndex = 0;

		int startIndex = 0;
		int endIndex = 0;
		// Capture body tag contains
		startIndex = findPattern(content, bodyStart, 0);
		while (startIndex != -1) {
			endIndex = findPattern(content, bodyEnd, startIndex);
			for (int i = startIndex + 6; i < endIndex - 1; i++) {
				tempArr[tempArrIndex] = content[i];
				// System.out.println(tempArrIndex);
				tempArrIndex++;
			}
			startIndex = findPattern(tempArr, bodyStart, 0);
		}

		// delete script tags in body, if exists
		startIndex = findPattern(tempArr, scriptStart, 0);
		while (startIndex != -1) {
			endIndex = findPattern(tempArr, scriptEnd, startIndex);
			tempArrIndex = startIndex - 1;
			for (int i = endIndex + 8; i < tempArr.length; i++) {
				tempArr[tempArrIndex] = tempArr[i];
				tempArrIndex++;
			}
			startIndex = findPattern(tempArr, scriptStart, 0);
		}

		// delete style tags in body, if exists
		startIndex = findPattern(tempArr, styleStart, 0);
		while (startIndex != -1) {
			endIndex = findPattern(tempArr, styleEnd, startIndex);
			tempArrIndex = startIndex - 1;
			for (int i = endIndex + 8; i < tempArr.length; i++) {
				tempArr[tempArrIndex] = tempArr[i];
				tempArrIndex++;
			}
			startIndex = findPattern(tempArr, scriptStart, 0);
		}

		// remove other tags without their contents, if exists
		tempArr = removeTags(tempArr);

		charset = ibmICU4j(tempArr);
		return charset;
	}

	private static int findPattern(byte[] content, String pattern, int index) {
		int patternSize = pattern.length();

		char[] patternLowerCaseChars = new char[patternSize];
		pattern.toLowerCase().getChars(0, patternSize, patternLowerCaseChars, 0);
		char[] patternUpperCaseChars = new char[patternSize];
		pattern.toUpperCase().getChars(0, patternSize, patternUpperCaseChars, 0);

		int i = index;
		while (i + patternSize - 1 < content.length) {
			boolean found = true;
			for (int j = 0; j < patternSize; j++) {
				char currentChar = (char) content[i + j];
				if (currentChar != patternLowerCaseChars[j] && currentChar != patternUpperCaseChars[j]) {
					found = false;
					break;
				}
			}
			if (found) {
				return i;
			}
			i++;
		}
		return -1;
	}

	private static byte[] removeTags(byte[] html) {
		int inside = 0;
		byte[] tempHtml = new byte[html.length];
		int j = 0;
		for (int i = 0; i < html.length; i++) {
			switch (html[i]) {
			case '<':
				inside++;
				break;
			case '>':
				inside--;
				break;
			default:
				if (inside == 0) {
					tempHtml[j++] = html[i];
				}
			}
		}
		byte[] removedHtmlByteArray = new byte[j];
		System.arraycopy(tempHtml, 0, removedHtmlByteArray, 0, j);
		return removedHtmlByteArray;
	}

}
