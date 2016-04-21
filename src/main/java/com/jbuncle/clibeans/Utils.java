/*
 * The MIT License
 *
 * Copyright 2016 James Buncle <jbuncle@hotmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.jbuncle.clibeans;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author James Buncle <jbuncle@hotmail.com>
 */
public class Utils {

    public static String readLine(String format, Object... args) throws IOException {
        if (System.console() != null) {
            return System.console().readLine(format, args);
        }
        System.out.print(String.format(format, args));
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        return reader.readLine();
    }

    public static char[] readPassword(String format, Object... args)
            throws IOException {
        if (System.console() != null) {
            return System.console().readPassword(format, args);
        }
        return readLine(format, args).toCharArray();
    }

    public static Object stringToType(final Class<?> targetType, final String value) {
        //Adapted from http://stackoverflow.com/questions/13943550/how-to-convert-from-string-to-a-primitive-type-or-standard-java-wrapper-types
        if (Boolean.class == targetType || Boolean.TYPE == targetType) {
            return Boolean.parseBoolean(value);
        }
        if (Byte.class == targetType || Byte.TYPE == targetType) {
            return Byte.parseByte(value);
        }
        if (Short.class == targetType || Short.TYPE == targetType) {
            return Short.parseShort(value);
        }
        if (Integer.class == targetType || Integer.TYPE == targetType) {
            return Integer.parseInt(value);
        }
        if (Long.class == targetType || Long.TYPE == targetType) {
            return Long.parseLong(value);
        }
        if (Float.class == targetType || Float.TYPE == targetType) {
            return Float.parseFloat(value);
        }
        if (Double.class == targetType || Double.TYPE == targetType) {
            return Double.parseDouble(value);
        }
        if (File.class == targetType) {
            return new File(value);
        }
        return value;
    }

    /**
     * Parse CLI Options into a name-value map.
     * 
     * @param args Raw CLI arguments
     * @return parsed map.
     */
    public static Map<String, String> parseCliOptions(String[] args) {
        final Map<String, String> optionsMap = new LinkedHashMap<>();
        for (int index = 0; index < args.length; index++) {
            //Loop through and find options (which may be followed by values)
            final String arg = args[index];
            if (arg.startsWith("-")) {

                //Remove the preceeding dash
                String optionName = arg.substring(1);

                final String value;
                if (arg.contains("=")) {
                    //Handle -option=argument pairs
                    value = optionName.substring(optionName.indexOf("=") + 1);
                    optionName = optionName.substring(0, optionName.indexOf("="));
                } else if (index < args.length - 1 && !args[index + 1].startsWith("-")) {
                    //Handle space separated '-option argument' pairs
                    value = args[index + 1];
                } else {
                    //Treat as flag
                    value = null;
                }

                optionsMap.put(optionName, value);

            }
        }
        return optionsMap;
    }

}
