#IUST HTMLCharDet

IUST HTMLCharDet is a java tool for detecting *charset encoding* of HTML web pages. **HTMLCharDet** stands for _**HTML** **Char**set **Dete**ctor_ and **IUST** stands for _**I**ran **U**niversity of **S**cience & **T**echnology_.

This tool is in connection with a paper entitled:  
<p align=center style="font-size:160%;">
 <b>Charset Encoding Detection of HTML Documents</b></br>
 <em><b>A Practical Experience</b></em></br>
</p>

which was presented in the *[11th Asia Information Retrieval Societies Conference][1]*, in *Brisbane*, *Australia*, *2015*.

Although we wrote a paper to describe the algorithm, but this tool is not just an academic effort to solve *charset encoding detection* problem for HTML web pages. In fact this tool is an **industrial** product which is actively used in a large-scale web crawler,  continuously under the load of over than **1 billion** web pages.

##Precision (quick view)

In order to determine how IUST HTMLCharDet precise is, we compared it with the two famous charset detector tools namely _**IBM ICU**_ and _**Mozilla CharDet**_ aginst two test scenario, i.e. **Encoding-Wise** and **Language-Wise**. Results of the comparisons are presented in the [paper][paper], but bellow you can have a glance at results. To read more about comparisons, please find the paper inside the *wiki* folder. 

**Note:** In these images *Hybrid* is the same *IUST HTMLCharDet*, because it is actually a hybrid mechanism.

####Encoding-Wise

<p align=center>
<img src="https://cloud.githubusercontent.com/assets/14090324/12007482/e31a7330-ac1b-11e5-976b-2d45beb64939.jpg" alt="encoding-wise evaluation image" height="450" width="766">
</img>
</br>

<img src="https://cloud.githubusercontent.com/assets/14090324/12007849/cc8f46ca-ac2c-11e5-9600-dd3cd3a39ac1.jpg" alt="encoding-wise evaluation diagram image" height="300" width="645">
</img>
</p>

####Language-Wise

<p align=center>
<img src="https://cloud.githubusercontent.com/assets/14090324/12007456/6d706dfc-ac1a-11e5-8ec3-1d999820f4a4.jpg" alt="language-wise evaluation image" height="305" width="765">
</img>
</br>

<img src="https://cloud.githubusercontent.com/assets/14090324/12007852/db79aaf4-ac2c-11e5-883a-006de77d3222.jpg" alt="language-wise evaluation diagram image" height="329" width="645">
</img>
</p>
##Installation

to be continued ...

[1]: http://airs-conference.org/2015/program.html
[paper]: https://github.com/shabanali-faghani/IUST-HTMLCharDet/tree/master/wiki/paper.pdf
