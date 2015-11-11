package ir.ac.iust.selab.htmlchardet;

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

/**
 * 
 * @author <a href="mailto:shabanali.faghani@gmail.com">Shabanali Faghani</a>
 * 
 */
public class HTMLCharsetDetector {

	private static final Logger LOG = Logger.getLogger(HTMLCharsetDetector.class);

	private static final int threshold = 40;

	// preventing from instantiation any instance of this class
	private HTMLCharsetDetector() {
	}

	/**
	 * This method use <b>ISO-8859-1 Decoding-Encoding</b> approach for Markup Elimination
	 * 
	 * @param rawHtmlByteSequence
	 * @param lookInMeta
	 * @return detected charset
	 */
	public static String detect(byte[] rawHtmlByteSequence, boolean... lookInMeta) {

		Document domTree = null;

		String charset = null;
		if (lookInMeta != null && lookInMeta.length > 0 && lookInMeta[0]) {
			domTree = HTMLCharsetDetector.createDomTree(rawHtmlByteSequence, "ISO-8859-1");
			charset = HTMLCharsetDetector.lookInMetaTags(domTree);
			if (Charsets.isValid(charset)) {
				return Charsets.normalize(charset);
			}
		}

		charset = HTMLCharsetDetector.mozillaJCharDet(rawHtmlByteSequence);
		if (charset.equalsIgnoreCase("UTF-8")) {
			return Charsets.normalize(charset);
		}

		if (domTree == null) {
			domTree = HTMLCharsetDetector.createDomTree(rawHtmlByteSequence, "ISO-8859-1");
		}
		String visibleText = domTree.text();
		byte[] visibleTextbyteSequence = null;
		try {
			visibleTextbyteSequence = visibleText.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// I think this Exception will never raised, because using
			// ISO-8859-1 it's OK to decode-encode any byte sequence. Anyway, ...
			LOG.warn("Could not extract byte sequence from visible text of the html document using "
					+ "\"ISO-8859-1\" charset. Detection process will use the raw html byte sequence as input.", e);
		}
		if (visibleTextbyteSequence == null || visibleTextbyteSequence.length < threshold)
			visibleTextbyteSequence = rawHtmlByteSequence;

		charset = HTMLCharsetDetector.ibmICU4j(visibleTextbyteSequence);
		return Charsets.normalize(charset);
	}

	/**
	 * just to avoiding from code duplication
	 * 
	 * @param rawHtmlByteSequence
	 * @param charset 
	 * @return DOM tree
	 */
	private static Document createDomTree(byte[] rawHtmlByteSequence, String charset) {
		String trueHtmlStructure = new String(rawHtmlByteSequence, Charset.forName(charset)); 
		return Jsoup.parse(trueHtmlStructure);
	}

	/**
	 * 
	 * @param domTree
	 * @return found charset if exists, null otherwise
	 */
	private static String lookInMetaTags(Document domTree) {
		String charset = null;
		Elements metaElements = domTree.select("meta");
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

	/**
	 * 
	 * @param bytes
	 * @return detected charset if could to, "nomatch" otherwise
	 */
	private static String mozillaJCharDet(byte[] bytes) {
		int lang = nsDetector.ALL;
		nsDetector det = new nsDetector(lang);
		det.Init(new nsICharsetDetectionObserver() {
			@Override
			public void Notify(String charset) {
			}
		});
		det.DoIt(bytes, bytes.length, false);
		det.DataEnd();
		det.Reset();
		return det.getProbableCharsets()[0];
	}

	/**
	 * 
	 * @param bytes
	 * @return detected charset
	 */
	private static String ibmICU4j(byte[] bytes) {
		CharsetDetector charsetDetector = new CharsetDetector();
		charsetDetector.setText(bytes);
		return charsetDetector.detect().getName();
	}

	/**
	 * This method uses <b>Direct</b> approach for Markup Elimination
	 * 
	 * @param rawHtmlByteSequence
	 * 
	 * @deprecated Due to the frequency of malformed HTML web pages this method is highly error-prone,</br>
	 *             so I strongly recommend to use the {@link #detect(byte[] rawHtmlByteSequence, boolean lookInMeta)} method instead.</br>
	 *             Also of note, even for well-formed HTML web pages there is no guarantee that this method does <b>Markup Elimination</b></br>   
	 *             phase correctly, because there are many odd and special points about HTML documents that where neglected in this method.</br>  
	 *             Furthermore, this method has a lot of duplicate code!</br>
	 *             </br>
	 *             Anyway this method would be useful for those who do not want to add additional dependency, </br>
	 *             i.e. Jsoup, into their project. Hence, any suggestion to complete this method would be welcome :) 
	 *             
	 * @return detected charset
	 */
	@Deprecated
	public static String detect(byte[] rawHtmlByteSequence) {

		String charset = HTMLCharsetDetector.mozillaJCharDet(rawHtmlByteSequence);
		if (charset.equalsIgnoreCase(Charsets.UTF_8.getValue())) {
			return Charsets.UTF_8.getValue();
		}

		String bodyStart = "<body";
		String bodyEnd = "/body>";
		String scriptStart = "<script";
		String scriptEnd = "/script>";
		String styleStart = "<style";
		String styleEnd = "/style>";

		byte[] tempArr = new byte[rawHtmlByteSequence.length * 2];
		int tempArrIndex = 0;

		int startIndex = 0;
		int endIndex = 0;
		// capture body tag contents
		startIndex = findPattern(rawHtmlByteSequence, bodyStart, 0);
		while (startIndex != -1) {
			endIndex = findPattern(rawHtmlByteSequence, bodyEnd, startIndex);
			for (int i = startIndex + 6; i < endIndex - 1; i++) {
				tempArr[tempArrIndex] = rawHtmlByteSequence[i];
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

	/**
	 * 
	 * @param content
	 * @param pattern
	 * @param index
	 * @return
	 */
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

	/**
	 * 
	 * @param html
	 * @return
	 */
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
