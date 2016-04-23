# CLI Beans

[![Build Status](https://travis-ci.org/jbuncle/cli-beans.svg?branch=master)](https://travis-ci.org/jbuncle/cli-beans)
[![codecov.io](https://codecov.io/github/jbuncle/cli-beans/coverage.svg?branch=master)](https://codecov.io/github/jbuncle/cli-beans?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/23160ce3efca439681775cef1072b6e9)](https://www.codacy.com/app/jbuncle/cli-beans)

Java library for creating command line interfaces using objects

Provides the ability to load command line options into Java objects via annotated setter method using annotations.

Features include validation, help text generation & interactive mode.

## Usage
```java

public static class MyClass {

    private String value;

    public String getValue() {
        return value;
    }

    @CLIOption(name = "value", defaultValue = "helloworld")
    public void setValue(String value) {
        this.value = value;
    }

}

public static void main(String[] args) {
    CommandLineParser cliParser = new CommandLineParser<>(MyClass.class);
    if (cliParser.validate(args).isEmpty()) {
        MyClass instance = cliParser.parseArguments(args);
        //Do stuff with instance
        System.out.println(instance.getValue());
    } else {
        //Invalid input, show help text
        cliParser.getHelp().printHelpText();
    }   
}
```

## Licence
The MIT License

Copyright 2016 James Buncle <jbuncle@hotmail.com>.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.