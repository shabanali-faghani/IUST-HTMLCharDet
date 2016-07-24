#IUST HTMLCharDet

IUST HTMLCharDet is a meta java tool for detecting *Charset Encoding* of HTML web pages. **HTMLCharDet** stands for **HTML** **Char**set **Det**ector and **IUST** stands for **I**ran **U**niversity of **S**cience & **T**echnology.

This tool is in connection with a paper entitled:  
<a href="http://link.springer.com/chapter/10.1007/978-3-319-28940-3_17"><p align=center style="font-size:160%;">
 <b>Charset Encoding Detection of HTML Documents</b></br>
 <b>A Practical Experience</b></br>
</p></a>

which was presented In *[Proceedings of the 11th Asia Information Retrieval Societies Conference][1]* (pp. 215-226), Brisbane, Australia, 2015.

Although we wrote a paper to describe the algorithm, but this tool is not just an academic effort to solve *charset encoding detection* problem for HTML web pages. In fact this tool is an **industrial** product which is now actively used in a large-scale web crawler, under a load of over than **1 billion** web pages. But despite its accuracy in practice, it's very small in size (just has two class!!). Both the small size and the accuracy of this tool are originated from its algorithm. It is small, because its algorithm is so easy to implement, and it is accurate, because in addition to the logic of its algorithm per se; it uses two famous charset detector tools namely _**IBM ICU**_ and _**Mozilla CharDet**_ under the hood.

##Important Warning
The release **1.0.0** is not valid because it has a **FATAL** logical bug. It detects all pages as UTF-8! Before this release I refactored the code but have done a tiny mistake. The code at line 130 of the HTMLCharsetDetector class, i.e [*det.Reset();*][bug-line], should be deleted. If you use the source code of this tool just press *ctrl+d* (in Eclipse) on the mentioned line to fix it! I will fix it and will do another release as soon as possible :)

##Precision (quick view)

In order to determine the precision of IUST HTMLCharDet, we compared it with the two famous charset detector tools, i.e. _**IBM ICU**_ and _**Mozilla CharDet**_, against two test scenario including **Encoding-Wise** and **Language-Wise**. Results of the comparisons are presented in the [paper][paper], but bellow you can have a glance at results. To read more about comparisons, please find the paper inside *wiki* folder.

**Note:** In these images *Hybrid* is the same *IUST HTMLCharDet*, in the paper we called it *Hybrid* because it is actually a hybrid mechanism.

####Encoding-Wise Evaluation
In this test scenario, we compared *IBM ICU*, *Mozilla CharDet* and the *hybrid mechanism* against a corpus of HTML documents. To create this corpus, we wrote a multi-threaded crawler and then we gathered a collection of nearly 2700 HTML pages with various charset encoding types. The code which we wrote for creating this corpus is available in the [*./src/test/java/encodingwise/corpus*][corpus-code] folder of this repository and the created corpus is available via [*./test-data/encoding-wise/corpus.zip*][corpus-data]. Bellow find the comparison results ...

<p align=center>
<img src="https://cloud.githubusercontent.com/assets/14090324/12007482/e31a7330-ac1b-11e5-976b-2d45beb64939.jpg" alt="encoding-wise evaluation image" height="450" width="766">
</img>
</p>
Usually, graphical presentation of the results makes a better sense ...

<p align=center>
<img src="https://cloud.githubusercontent.com/assets/14090324/12007849/cc8f46ca-ac2c-11e5-9600-dd3cd3a39ac1.jpg" alt="encoding-wise evaluation diagram image" height="300" width="645">
</img>
</p>
I know that the results are odd and incredible indeed, specially for Windows-1251 and Windows-1256 (Cyrillic and Arabic specific charset encodings respectively), but it is just as well. To see the reason of this behavior of IUST HTMLCharDet from theoretical point of view please refer to the [paper][paper], but to see it in practice you can find prepared test codes in [*./test/java*][test-codes] and test data in [*./test-data*][test-data] folder.

####Language-Wise Evaluation
In this test scenario, we compared our *hybrid mechanism* with the two others from language point of view. In this connection, we collected a list of URLs that are pointing to various web pages with different languages. The URLs are selected from the **top 1 million websites** visited from all over the world, as reported by [Alexa][Alexa]. In order to collect HTML documents in a specific language, we investigated web pages with the internet domain name of that language. For example, *Japanese* web pages are collected from *.jp* domain. The results of evaluation for eight different languages are shown in details in the following table ...

<p align=center>
<img src="https://cloud.githubusercontent.com/assets/14090324/12007456/6d706dfc-ac1a-11e5-8ec3-1d999820f4a4.jpg" alt="language-wise evaluation image" height="305" width="765">
</img>
</p>
To find more details about this test, you may have a look at: [*./test-data/language-wise/results/*][lang-wise-results]. 

<p align=center>
<img src="https://cloud.githubusercontent.com/assets/14090324/12007852/db79aaf4-ac2c-11e5-883a-006de77d3222.jpg" alt="language-wise evaluation diagram image" height="319" width="645">
</img>
</p>
As you can see from this diagram, in this test scenario the improveness in mean average accuracy of IUST HTMLCharDet aginst two other tools is less that from which in the previous test scenario (i.e. 0.14 and 0.10 in Lang-Wise versus 0.38 and 0.69 in Enc-Wise). It is due to the fact that over than 85 % of the websites use UTF-8 as their charset encoding [[ref][w3techs]]. With considering this fact and recalling [Encoding-Wise Evaluation diagram][ewe-diagram], in which we saw both *IBM ICU* and *Mozilla CharDet* are accurate in dealing with UTF-8, we would have been convinced with this results.

##Installation
 
####Maven
If you use Maven as dependency manager, just place this dependency into your POM's `<dependencies>` section:
```java
<dependency>
    <groupId>ir.ac.iust</groupId>
    <artifactId>htmlchardet</artifactId>
    <version>1.0.0</version>
</dependency>
````
####Scala SBT
````scala
libraryDependencies += "ir.ac.iust" % "htmlchardet" % "1.0.0"
````
####Otherwise!
If you don't use any dependency manager and have a pure java project you can download **htmlchardet-1.0.0.jar** either from inside the [wiki][wiki] folder or from [mvnrepository.org][mvnrepo]. In this case you need also the first 4 dependency; those were mentioned in the [pom.xml][pom] file to get it to work for you.

##Usage

If you trust in the Web Server or trust in the Website that the pages are crawled from, you can use this tool as follows:
```java
String charset = HTMLCharsetDetector.detect(htmlByteArray, true);
```
otherwise, call detect method with `false` value for `lookInMeta` argument as follows:
```java
String charset = HTMLCharsetDetector.detect(htmlByteArray, false);
```
Also, there is another detection method with `#detect(byte[] rawHtmlByteSequence)` signature, but I don't recommend to use it. To see why, please refer to its [javadoc][javadoc].

[1]: http://airs-conference.org/2015/program.html
[paper]: https://github.com/shabanali-faghani/IUST-HTMLCharDet/tree/master/wiki/Charset-Encoding-Detection-of-HTML-Documents.pdf
[bug-line]:https://github.com/shabanali-faghani/IUST-HTMLCharDet/blob/master/src/main/java/ir/ac/iust/htmlchardet/HTMLCharsetDetector.java#L130
[corpus-code]: https://github.com/shabanali-faghani/IUST-HTMLCharDet/tree/master/src/test/java/encodingwise/corpus
[corpus-data]: https://github.com/shabanali-faghani/IUST-HTMLCharDet/tree/master/test-data/encoding-wise/corpus.zip
[test-codes]: https://github.com/shabanali-faghani/IUST-HTMLCharDet/tree/master/src/test/java
[test-data]: https://github.com/shabanali-faghani/IUST-HTMLCharDet/tree/master/test-data
[Alexa]: www.alexa.com
[lang-wise-results]: https://github.com/shabanali-faghani/IUST-HTMLCharDet/tree/master/test-data/language-wise/results
[w3techs]: http://w3techs.com/technologies/history_overview/character_encoding
[pom]: https://github.com/shabanali-faghani/IUST-HTMLCharDet/blob/master/pom.xml
[ewe-diagram]: https://cloud.githubusercontent.com/assets/14090324/12007849/cc8f46ca-ac2c-11e5-9600-dd3cd3a39ac1.jpg
[mvnrepo]: http://mvnrepository.org/artifact/ir.ac.iust/htmlchardet/1.0.0
[wiki]: https://github.com/shabanali-faghani/IUST-HTMLCharDet/tree/master/wiki
[javadoc]: https://github.com/shabanali-faghani/IUST-HTMLCharDet/blob/master/src/main/java/ir/ac/iust/htmlchardet/HTMLCharsetDetector.java#L146
