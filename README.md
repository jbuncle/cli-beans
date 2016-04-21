# CLI Beans

[![Build Status](https://travis-ci.org/jbuncle/cli-beans.svg?branch=master)](https://travis-ci.org/jbuncle/cli-beans)
[![codecov.io](https://codecov.io/github/jbuncle/cli-beans/coverage.svg?branch=master)](https://codecov.io/github/jbuncle/cli-beans?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/23160ce3efca439681775cef1072b6e9)](https://www.codacy.com/app/jbuncle/cli-beans)

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
