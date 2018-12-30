package ir.ac.iust.htmlchardet;

import ir.ac.iust.icu.CharsetDetector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rypt.f8.Utf8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * The logic behind the code of this class is described in details in a paper entitled:
 * </br>
 * <p align="center">
 *     <a href="http://link.springer.com/chapter/10.1007/978-3-319-28940-3_17">
 *         Charset Encoding Detection of HTML Documents A Practical Experience
 *     </a>
 * </p>
 * which was presented <i> In Proceedings of the 11th Asia Information Retrieval Societies Conference </i>(pp. 215-226).
 * Brisbane, Australia, 2015.
 *
 * @author <a href="mailto:shabanali.faghani@gmail.com">Shabanali Faghani</a>
 */
public class HTMLCharsetDetector {

    private static final int threshold = 40;

    /**
     * Detects charset encoding of the given html input stream
     *
     * @param htmlStream
     * @param lookInMeta
     * @return detected charset
     */
    public String detect(InputStream htmlStream, boolean... lookInMeta) throws IOException {
        Document domTree = null;
        if (lookInMeta.length > 0 && lookInMeta[0]) {
            domTree = createDomTree(toByteArray(htmlStream), Charsets.ISO_8859_1.toString());
            String charset = lookInMetaTags(domTree);
            if (Charsets.isValid(charset)) {
                return Charsets.normalize(charset);
            }
        }

        boolean isUTF8 = Utf8.isValidUpToTruncation(htmlStream);
        if (isUTF8 == true) {
            return Charsets.UTF_8.toString();
        }

        if (domTree == null) {
            domTree = createDomTree(toByteArray(htmlStream), Charsets.ISO_8859_1.toString());
        }
        String visibleText = domTree.text();
        InputStream visibleTextStream;
        if (visibleText.length() < threshold) {
            visibleTextStream = htmlStream;
        } else {
            visibleTextStream = new ByteArrayInputStream(visibleText.getBytes(Charsets.ISO_8859_1.toString()));
        }

        CharsetDetector charsetDetector = new CharsetDetector();
        charsetDetector.setText(visibleTextStream);
        return Charsets.normalize(charsetDetector.detect().getName());
    }

    /**
     * Reads the given inputStream into a byteArray
     *
     * @param is
     * @return byte array
     * @throws IOException
     */
    public byte[] toByteArray(InputStream is) throws IOException {
        byte[] byteArray = {};
        int length = Integer.MAX_VALUE;
        int pos = 0;
        while (pos < length) {
            int bytesToRead;
            if (pos >= byteArray.length) {
                bytesToRead = Math.min(length - pos, byteArray.length + 1024);
                if (byteArray.length < pos + bytesToRead) {
                    byteArray = Arrays.copyOf(byteArray, pos + bytesToRead);
                }
            } else {
                bytesToRead = byteArray.length - pos;
            }
            int cc = is.read(byteArray, pos, bytesToRead);
            if (cc < 0 && byteArray.length != pos) {
                byteArray = Arrays.copyOf(byteArray, pos);
                break;
            }
            pos += cc;
        }
        return byteArray;
    }

    /**
     * To avoid from code duplication
     *
     * @param rawHtmlByteSequence
     * @param charset
     * @return DOM tree
     */
    private Document createDomTree(byte[] rawHtmlByteSequence, String charset) {
        String trueHtmlStructure = new String(rawHtmlByteSequence, Charset.forName(charset));
        return Jsoup.parse(trueHtmlStructure);
    }

    /**
     * Looks for charset inside Meta tag
     *
     * @param domTree
     * @return charset if exists, null otherwise
     */
    private String lookInMetaTags(Document domTree) {
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

}
