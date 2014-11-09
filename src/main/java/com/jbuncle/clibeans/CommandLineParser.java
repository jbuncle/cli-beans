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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Maps command line arguments to a given object annotated methods. CLI
 * arguments should be prefixed with a dash (-) symbol
 *
 * @author James Buncle
 * @param <T>
 */
public class CommandLineParser<T extends Object> {

    private final Class<T> targetClass;
    private final Map<String, Method> annotatedMethods;
    private final Map<String, CLI> annotations;
    private final Map<Class, PropertyEditor> propertyEditors;
    private final Map<String, String> aliases;

    public CommandLineParser(Class<T> targetClass) {
        this.targetClass = targetClass;
        this.annotatedMethods = new LinkedHashMap<>();
        this.annotations = new LinkedHashMap<>();
        this.propertyEditors = new LinkedHashMap<>();
        this.aliases = new LinkedHashMap<>();

        for (final Method method : this.targetClass.getMethods()) {
            if (method.isAnnotationPresent(CLI.class)) {
                final CLI annotation = method.getAnnotation(CLI.class);
                this.annotatedMethods.put(annotation.name(), method);
                this.annotations.put(annotation.name(), annotation);
                if (!annotation.alias().isEmpty()) {
                    this.aliases.put(annotation.alias(), annotation.name());
                }
            }
        }
    }

    public Help getHelp() {
        final Help help = new Help();
        for (CLI cli : annotations.values()) {
            help.add(new HelpItem(cli));
        }
        return help;
    }

    public Set<String> validate(final String[] args) {
        final Set<String> invalidOptions = new LinkedHashSet<>();
        final Map<String, String> optionsMap = getOptionsMap(args);
        for (Entry<String, CLI> entry : annotations.entrySet()) {
            //Loop annotations and do checks, maps annotation -> args
            final CLI annotation = entry.getValue();
            final String annotationName = entry.getKey();
            if (annotation.required() && !optionsMap.containsKey(annotationName)) {
                invalidOptions.add(annotationName);
            } else if (optionsMap.containsKey(annotationName) && optionsMap.get(annotationName) != null) {
                //Check regex
                final String regex = annotation.regex();
                if (optionsMap.containsKey(annotationName) && !optionsMap.get(annotationName).matches(regex)) {
                    invalidOptions.add(entry.getKey());
                }
            }

        }
        return invalidOptions;
    }

    /**
     * Map the given argument array to a new instance of the target class
     *
     * @param args
     * @return a new object instance of the target class, with property set
     * based on the arguments provided
     */
    public T parseArguments(String[] args) {
        try {
            //Load annotated methods into a Map keyed by the option name
            final T targetInstance = targetClass.newInstance();

            final Map<String, String> argsMap = getOptionsMap(args);
            for (Entry<String, String> entry : argsMap.entrySet()) {
                String optionName = entry.getKey();
                //Lookup for an annotation with the same option name
                if (annotatedMethods.containsKey(optionName)) {
                    //found an annotation for the option
                    final Method method = annotatedMethods.get(optionName);
                    if (method.getAnnotation(CLI.class).flag()) {
                        //Flag, so treat as 'true'
                        method.invoke(targetInstance, true);
                    } else {
                        /* 
                         * Convert the given option value (a string) to the 
                         * required basic type (as determined by the methods 
                         * argument type)
                         */
                        final String value = entry.getValue();
                        final Class<?> parameterType = method.getParameterTypes()[0];
                        final Object valueObject;
                        if (this.propertyEditors.containsKey(parameterType)) {
                            valueObject = this.propertyEditors.get(parameterType).getObject(value);
                        } else {
                            valueObject = stringToType(parameterType, value);

                        }
                        //Invoke the method on the Object instance using the converted value
                        method.invoke(targetInstance, valueObject);
                    }

                }
            }
            return targetInstance;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException(Arrays.toString(args), ex);
        }
    }

    private Map<String, String> getOptionsMap(String[] args) {
        final Map<String, String> optionsMap = new LinkedHashMap<>();
        for (int index = 0; index < args.length; index++) {
            //Loop through and find options (which are followed by values)
            final String arg = args[index];
            if (arg.startsWith("-")) {
                //Remove the preceeding dash to get the option name
                String optionName = arg.substring(1);
                //Convert alias to real name
                if (aliases.containsKey(optionName)) {
                    optionName = aliases.get(optionName);
                }
                if (index < args.length - 1 && !args[index + 1].startsWith("-")) {
                    optionsMap.put(optionName, args[index + 1]);
                } else {
                    optionsMap.put(optionName, null);
                }

            }
        }
        return optionsMap;
    }

    public final <T> void registerPropertyEditor(Class<T> clazz, PropertyEditor<T> propertyEditor) {
        this.propertyEditors.put(clazz, propertyEditor);
    }

    private static Object stringToType(
            final Class<?> targetType,
            final String value) {
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
        return value;
    }
}