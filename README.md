# Semantic Test Repair for Web applications

The repository stores our implementations for the approach **SEMTER**  proposed in paper "Semantic Test Repair for Web applications". 

## Requirement

- Java 8, Python 3 as well as necessary packages are installed. 
- Install [Jep](https://github.com/ninia/jep/).
- Use compiler [Ajc](https://github.com/eclipse/org.aspectj/releases).

## How to run

1. Create the root directory, and `git clone https://github.com/Reoke/SEMTER.git` to the directory.
2. Install [Firefox 100.0](http://ftp.mozilla.org/pub/firefox/releases/100.0/win64/en-US/). Download driver [geckodriver 30.0](https://github.com/mozilla/geckodriver/releases/download/v0.30.0/geckodriver-v0.30.0-win64.zip) and put it in directory `resources/browser`.
3. Use class `tracer.TraceRunner` to run and record the execution trace of a test case on a base version. The results are serialized to Java objects on the specified paths.
4. Use class `repairer.RepairRunner` to repair a test case on an updated version, provided that the test case has been traced. The results are serialized to Java objects on the specified paths.

## Datasets

The dataset used to fine-tune the image model is collected from [iconfont](https://www.iconfont.cn/). The URL and label for each icon in the dataset are presented in file [label.csv](resources/label.csv). Note: These icons must be recaptured by `WebElement.getScreenShot()` method before training, to ensure data consistency.

The SNLI corpus used to select the threshold of semantic similarity is available at [The Stanford Natural Language Inference (SNLI) Corpus](https://nlp.stanford.edu/projects/snli/).

