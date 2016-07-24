package ir.ac.iust.htmlchardet;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.intl.chardet.nsDetector;

import com.ibm.icu.text.CharsetDetector;

/**
 * The logic behind the codes of this class is described in details in a paper entitled: </br>
 * <p align="center"> <a href="http://link.springer.com/chapter/10.1007/978-3-319-28940-3_17"> Charset
 * Encoding Detection of HTML Documents</br> A Practical Experience</a>
 * </p>
 * which was presented <i> In Proceedings of the 11th Asia Information Retrieval Societies Conference </i>(pp. 215-226). Brisbane, Australia, 2015.
 * 
 * @author <a href="mailto:shabanali.faghani@gmail.com">Shabanali Faghani</a>
 * 
 */
public class HTMLCharsetDetector {

	private static final Logger LOG = Logger.getLogger(HTMLCharsetDetector.class);

	private static final int threshold = 40;

	// prevent from instantiating
	private HTMLCharsetDetector() {
	}

	/**
	 * This method uses <b>ISO-8859-1 Decoding-Encoding</b> approach for Markup Elimination.
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
	 * Just to avoiding from code duplication.
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
	 * Looks for charset inside Meta tag. 
	 * 
	 * @param domTree
	 * @return found charset if exists, null otherwise
	 */
	private static String lookInMetaTags(Document domTree) {
		String charset = null;
		Elements metaElements = domTree.select("meta");
		for (Element meta : metaElements) {
			charset = meta.attr("charset");  //charset attribute is supported in html5
			if (Charsets.isValid(charset)) {
				return charset;
			} else {
				String contentAttr = meta.attr("content");
				if (contentAttr.contains("charset")) {
					int charsetBegin = contentAttr.indexOf("charset=") + 8;
					int charsetEnd = contentAttr.length();
					charset = contentAttr.substring(charsetBegin, charsetEnd).trim();
					if (Charsets.isValid(charset)) {
						return charset;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Detects charset using Mozilla JCharDet.
	 * 
	 * @param bytes
	 * @return detected charset if could to, "nomatch" otherwise
	 */
	private static String mozillaJCharDet(byte[] bytes) {
		nsDetector det = new nsDetector(nsDetector.ALL);
		det.DoIt(bytes, bytes.length, false);
		det.DataEnd();
		return det.getProbableCharsets()[0];
	}

	/**
	 * Detects charset using IBM ICU4J. 
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

		String bodyBegin = "<body";
		String bodyEnd = "/body>";
		String scriptBegin = "<script";
		String scriptEnd = "/script>";
		String styleBegin = "<style";
		String styleEnd = "/style>";

		byte[] tempArr = new byte[rawHtmlByteSequence.length * 2];
		int tempArrIndex = 0;

		int beginIndex = 0;
		int endIndex = 0;
		// capture body tag contents
		beginIndex = findPattern(rawHtmlByteSequence, bodyBegin, 0);
		while (beginIndex != -1) {
			endIndex = findPattern(rawHtmlByteSequence, bodyEnd, beginIndex);
			for (int i = beginIndex + 6; i < endIndex - 1; i++) {
				tempArr[tempArrIndex] = rawHtmlByteSequence[i];
				tempArrIndex++;
			}
			beginIndex = findPattern(tempArr, bodyBegin, 0);
		}

		// delete script tags in body, if exists
		beginIndex = findPattern(tempArr, scriptBegin, 0);
		while (beginIndex != -1) {
			endIndex = findPattern(tempArr, scriptEnd, beginIndex);
			tempArrIndex = beginIndex - 1;
			for (int i = endIndex + 8; i < tempArr.length; i++) {
				tempArr[tempArrIndex] = tempArr[i];
				tempArrIndex++;
			}
			beginIndex = findPattern(tempArr, scriptBegin, 0);
		}

		// delete style tags in body, if exists
		beginIndex = findPattern(tempArr, styleBegin, 0);
		while (beginIndex != -1) {
			endIndex = findPattern(tempArr, styleEnd, beginIndex);
			tempArrIndex = beginIndex - 1;
			for (int i = endIndex + 8; i < tempArr.length; i++) {
				tempArr[tempArrIndex] = tempArr[i];
				tempArrIndex++;
			}
			beginIndex = findPattern(tempArr, scriptBegin, 0);
		}

		// remove other tags without their contents, if exists
		tempArr = removeTags(tempArr);

		charset = ibmICU4j(tempArr);
		return charset;
	}

	/**
	 * Returns begin index of the given pattern, if exists, -1 otherwise.
	 * 
	 * @param content
	 * @param pattern
	 * @param index
	 * @return begin index of the pattern
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
	 * Removes HTML tags. 
	 * 
	 * @param html structure
	 * @return pure text (without tags) of html
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
