# IUST HTMLCharDet

IUST HTMLCharDet is a meta java tool for detecting *Charset Encoding* of HTML web pages. **HTMLCharDet** stands for **HTML** **Char**set **Det**ector and **IUST** stands for **I**ran **U**niversity of **S**cience & **T**echnology.

This tool is in connection with a paper entitled:  
<a href="http://link.springer.com/chapter/10.1007/978-3-319-28940-3_17"><p align=center style="font-size:160%;">
 <b>Charset Encoding Detection of HTML Documents</b></br>
 <b>A Practical Experience</b></br>
</p></a>

which was presented In *[Proceedings of the 11th Asia Information Retrieval Societies Conference][1]* (pp. 215-226), Brisbane, Australia, 2015.

Although we wrote a paper to describe the algorithm, but this tool is not just an academic effort to solve *charset encoding detection* problem for HTML web pages. In fact this tool is an **industrial** product which is now actively used in a large-scale web crawler, under a load of over than **1 billion** web pages.

## Precision (quick view)

In order to determine the precision of IUST HTMLCharDet, we compared it with the two famous charset detector tools, i.e. _**IBM ICU**_ and _**Mozilla CharDet**_, against two test scenarios including **Encoding-Wise** and **Language-Wise**. Results of the comparisons were presented in the [paper][paper], but you can take a glance at them, below. To read more about comparisons, you can find the paper inside the *wiki* folder.

**Note:** In these images *Hybrid* is the same *IUST HTMLCharDet*, we called it *Hybrid* in the paper because it is actually a hybrid mechanism.

#### Encoding-Wise Evaluation
In this test scenario, we compared *IBM ICU*, *Mozilla CharDet* and the *Hybrid mechanism* against a corpus of HTML documents. To create this corpus, we wrote a multi-threaded crawler and then we gathered a collection of nearly 2700 HTML pages with various charset encoding types. The code which we wrote for creating this corpus is available in the [*./eval/src/main/java/encodingwise/corpus*][corpus-code] folder of this repository and the created corpus is available via [*./eval/test-data/encoding-wise/corpus.zip*][corpus-data]. Below find the comparison results ...

<p align=center>
<img src="https://cloud.githubusercontent.com/assets/14090324/12007482/e31a7330-ac1b-11e5-976b-2d45beb64939.jpg" alt="encoding-wise evaluation image" height="450" width="766">
</img>
</p>

Usually, graphical presentation of results makes a better sense ...

<p align=center>
<img src="https://cloud.githubusercontent.com/assets/14090324/12007849/cc8f46ca-ac2c-11e5-9600-dd3cd3a39ac1.jpg" alt="encoding-wise evaluation diagram image" height="300" width="645">
</img>
</p>

I know that the results are odd and incredible indeed, specially for Windows-1251 and Windows-1256 (Cyrillic and Arabic specific charset encodings respectively), but it is just as well. Refer to the [paper][paper] to know why IUST HTMLCharDet is so accurate from theoretical point of view and to see its accuracy in practice yourself you can find a prepared evaluation project inside the [*./eval*][eval] folder.

#### Language-Wise Evaluation
In this test scenario, we compared our *hybrid mechanism* with the two others from language point of view. In this connection, we gathered a collection of URLs of web pages with different languages. The URLs were selected from the URLs of [Alexa][Alexa]'s **top 1 million websites** visited from throughout the world. In order to get URLs in the list of a specific language, we investigated URLs with the Internet Top Level Domain (TLD) name of that language/country. For example, the URLs of *Japanese* web pages were collected from *.jp* TLD. The results of the evaluation for eight different languages are shown in details in the following table ...

<p align=center>
<img src="https://cloud.githubusercontent.com/assets/14090324/12007456/6d706dfc-ac1a-11e5-8ec3-1d999820f4a4.jpg" alt="language-wise evaluation image" height="305" width="765">
</img>
</p>

Take a look at [*./eval/test-data/language-wise/results/*][lang-wise-results] to find more details about this test.

<p align=center>
<img src="https://cloud.githubusercontent.com/assets/14090324/12007852/db79aaf4-ac2c-11e5-883a-006de77d3222.jpg" alt="language-wise evaluation diagram image" height="319" width="645">
</img>
</p>

As you can see from this diagram, in this test scenario the improveness in mean average accuracy of IUST HTMLCharDet aginst two other tools is less that from which in the previous test scenario (i.e. 0.14 and 0.10 in Lang-Wise versus 0.38 and 0.69 in Enc-Wise). It is because over than 85% of the websites use UTF-8 as their charset encoding ([ref][w3techs]) and as we know from [Encoding-Wise Evaluation diagram][ewe-diagram], compared to other charsets, both *IBM ICU* and *Mozilla CharDet* are more accurate when the charset is UTF-8.

## Installation
 
#### Maven
```java
<dependency>
    <groupId>ir.ac.iust</groupId>
    <artifactId>htmlchardet</artifactId>
    <version>1.0.1</version>
</dependency>
````
#### Scala SBT
````scala
libraryDependencies += "ir.ac.iust" % "htmlchardet" % "1.0.1"
````

## Usage

```java
HTMLCharsetDetector htmlCharsetDetector = new HTMLCharsetDetector();
String charset = htmlCharsetDetector.detect(htmlInputStream);
// or
String charset = htmlCharsetDetector.detect(htmlInputStream, true); // to look into meta tags
```

[1]: http://airs-conference.org/2015/program.html
[paper]: http://link.springer.com/chapter/10.1007/978-3-319-28940-3_17
[corpus-code]: https://github.com/shabanali-faghani/IUST-HTMLCharDet/tree/master/eval/src/main/java/encodingwise/corpus
[corpus-data]: https://github.com/shabanali-faghani/IUST-HTMLCharDet/tree/master/eval/test-data/encoding-wise/corpus.zip
[eval]: https://github.com/shabanali-faghani/IUST-HTMLCharDet/tree/master/eval
[Alexa]: www.alexa.com
[lang-wise-results]: https://github.com/shabanali-faghani/IUST-HTMLCharDet/tree/master/eval/test-data/language-wise/results
[w3techs]: http://w3techs.com/technologies/history_overview/character_encoding
[ewe-diagram]: https://cloud.githubusercontent.com/assets/14090324/12007849/cc8f46ca-ac2c-11e5-9600-dd3cd3a39ac1.jpg
