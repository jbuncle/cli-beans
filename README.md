# CLI Beans

Provides the ability to load command line options into Java objects via annotated setter method using annotations.

Features include validation, help text generation & interactive mode.

## Usage
```java
CommandLineParser cliParser = new CommandLineParser<>(MyClass.class);
if (cliParser.validate(args).isEmpty()) {
    MyClass instance = cliParser.parseArguments(args);
    //Do stuff with instance
} else {
    //Invalid input, show help text
    cliParser.getHelp().printHelpText();
}
```
