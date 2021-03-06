/*
 *  Copyright (c) 2014 James Buncle
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 * 
 */
package com.jbuncle.clibeans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author James Buncle
 */
public class CommandLineParserTest {

    /**
     * Test of cliToBean method, of class CliToObjectConverter.
     */
    @Test
    public void testParseArguments() {
        System.out.println("parseArguments");
        String[] args = new String[]{"-myproperty", "Hello world", "-requiredProperty"};
        CommandLineParser<TestClass> instance = new CommandLineParser<>(TestClass.class);
        instance.getHelp().printHelpText();

        TestClass result = instance.parseArguments(args);
        assertEquals("Hello world", result.getMyProperty());
        assertFalse(false);

        args = new String[]{"-myproperty", "Hello World 2", "-uppercase", "-requiredProperty"};
        result = instance.parseArguments(args);
        assertEquals("Hello World 2", result.getMyProperty());
        assertTrue(result.isUppercase());

        args = new String[]{"-number", "1"};
        result = instance.parseArguments(args);
        assertEquals(1, result.getNumericProperty());

        args = new String[]{"-myproperty=value"};
        result = instance.parseArguments(args);
        assertEquals("value", result.getMyProperty());

    }

    @Test
    public void testPropertyEditor() {
        System.out.println("propertyEditor");

        CommandLineParser<TestClass> instance = new CommandLineParser<>(TestClass.class);
        instance.registerPropertyEditor(Date.class, new PropertyEditor<Date>() {

            @Override
            public Date getObject(String string) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    return format.parse(string);
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        final String[] args = new String[]{"-date", "01/01/1999"};

        final TestClass result = instance.parseArguments(args);
        assertEquals(result.getDate().getTime(), 915148800000L);
        assertEquals(result.isUppercase(), false);

    }

    @Test
    public void testAliases() {
        System.out.println("aliases");
        CommandLineParser<TestClass> instance = new CommandLineParser<>(TestClass.class);
        //Required property fail
        Set<String> result = instance.validate(new String[]{"-requiredProperty", "-a", "value"});
        assertEquals(0, result.size());
        result = instance.validate(new String[]{"-requiredProperty", "-aliased", "value"});
        assertEquals(0, result.size());
        result = instance.validate(new String[]{"-requiredProperty"});
        assertEquals(1, result.size());

        TestClass testClass = instance.parseArguments(new String[]{"-requiredProperty", "-aliased", "value"});
        assertEquals("value", testClass.getAliased());

        testClass = instance.parseArguments(new String[]{"-requiredProperty", "-a", "value"});
        assertEquals("value", testClass.getAliased());

        CommandLineParser<AliasObject> testInstance = new CommandLineParser<>(AliasObject.class);
        AliasObject testResult = testInstance.parseArguments(new String[]{"-als", "value"});
        assertEquals("value", testResult.getAliases());
        testResult = testInstance.parseArguments(new String[]{"-ali", "value"});
        assertEquals("value", testResult.getAliases());
    }

    @Test
    public void testValidate() {
        System.out.println("validate");
        CommandLineParser<TestClass> instance = new CommandLineParser<>(TestClass.class);
        //Required property fail
        Set<String> result = instance.validate(new String[]{"-myproperty", "Hello world", "-a"});
        assertEquals(1, result.size());
        //Required property success
        result = instance.validate(new String[]{"-myproperty", "Hello world", "-requiredProperty", "-a"});
        assertEquals(0, result.size());
        //Regex Test fail
        result = instance.validate(new String[]{"-requiredProperty", "-a", "-number", "NaN"});
        assertEquals(1, result.size());
        //Regex Test success
        result = instance.validate(new String[]{"-requiredProperty", "-a", "-number", "1"});
        assertEquals(0, result.size());

    }

    @Test
    public void testDefault() {
        System.out.println("default");
        CommandLineParser<DefaultObject> instance = new CommandLineParser<>(DefaultObject.class);
        DefaultObject objectInstance = instance.parseArguments(new String[]{""});
        assertEquals("test", objectInstance.getDefaultedValue());
        objectInstance = instance.parseArguments(new String[]{"-defaultedValue", "test2"});
        assertEquals("test2", objectInstance.getDefaultedValue());

    }

    public static class DefaultObject {

        private String defaultedValue;

        public String getDefaultedValue() {
            return defaultedValue;
        }

        @CLIOption(name = "defaultedValue", defaultValue = "test")
        public void setDefaultedValue(String defaultedValue) {
            this.defaultedValue = defaultedValue;
        }

    }

    public static class AliasObject {

        private String aliases;

        public String getAliases() {
            return aliases;
        }

        @CLIOption(name = "aliases", alias = {"als", "ali"})
        public void setAliases(String aliases) {
            this.aliases = aliases;
        }
    }

    public static class TestClass {

        private String myProperty;
        private int numericProperty;
        private boolean requiredProperty;
        private boolean uppercase;
        private Date date;
        private String aliased;

        public String getMyProperty() {
            return myProperty;
        }

        @CLIOption(name = "myproperty", description = "Basic property")
        public void setMyProperty(String myProperty) {
            this.myProperty = myProperty;
        }

        public boolean isUppercase() {
            return uppercase;
        }

        @CLIOption(name = "uppercase", description = "Basic flag", flag = true)
        public void setUppercase(boolean uppercase) {
            this.uppercase = uppercase;
        }

        public boolean getRequiredProperty() {
            return requiredProperty;
        }

        @CLIOption(name = "requiredProperty", description = "Basic required property", flag = true, required = true)
        public void setRequiredProperty(boolean requiredProperty) {
            this.requiredProperty = requiredProperty;
        }

        public int getNumericProperty() {
            return numericProperty;
        }

        @CLIOption(name = "number", regex = "[0-9]*")
        public void setNumericProperty(int numericProperty) {
            this.numericProperty = numericProperty;
        }

        public Date getDate() {
            return date;
        }

        @CLIOption(name = "date")
        public void setDate(Date date) {
            this.date = date;
        }

        public String getAliased() {
            return aliased;
        }

        @CLIOption(name = "aliased", alias = "a", required = true)
        public void setAliased(String aliased) {
            this.aliased = aliased;
        }

        public void print() {
            if (this.isUppercase()) {
                System.out.println(this.getMyProperty().toUpperCase());
            } else {
                System.out.println(this.getMyProperty());
            }
        }

    }
}
