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

import static com.jbuncle.clibeans.Utils.parseCliOptions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Maps command line arguments to a given object annotated methods. CLI arguments should be prefixed with a dash (-)
 * symbol
 *
 * @author James Buncle
 * @param <T>
 */
public class CommandLineParser<T extends Object> {

    private final Class<T> targetClass;
    private final Map<String, Method> annotatedMethods;
    private final Map<String, CLIOption> annotations;
    private final Map<Class, PropertyEditor> propertyEditors;
    private final Map<String, String> aliases;

    public CommandLineParser(final Class<T> targetClass) {
        this.targetClass = targetClass;
        this.annotatedMethods = new LinkedHashMap<>();
        this.annotations = new LinkedHashMap<>();
        this.propertyEditors = new LinkedHashMap<>();
        this.aliases = new LinkedHashMap<>();

        for (final Method method : this.targetClass.getMethods()) {
            if (method.isAnnotationPresent(CLIOption.class)) {
                final CLIOption annotation = getAnnotation(method);
                this.annotatedMethods.put(annotation.name(), method);
                this.annotations.put(annotation.name(), annotation);
                if (annotation.alias().length > 0) {
                    for (String alias : annotation.alias()) {
                        this.aliases.put(alias, annotation.name());
                    }
                }
            }
        }
    }

    public Help getHelp() {
        final Help help = new Help();
        for (CLIOption cli : annotations.values()) {
            help.add(new HelpItem(cli));
        }
        return help;
    }

    public Set<String> validate(final String[] args) {
        final Set<String> invalidOptions = new LinkedHashSet<>();
        final Map<String, String> cliOptions = getOptionsMap(args);

        for (Entry<String, CLIOption> entry : annotations.entrySet()) {
            //Loop annotations and do checks, maps annotation -> args
            final CLIOption annotation = entry.getValue();
            final String cliOption = entry.getKey();
            final String cliOptionValue = cliOptions.get(cliOption);
            final boolean hasCliOption = cliOptions.containsKey(cliOption);

            if (annotation.required() && !hasCliOption) {
                invalidOptions.add(cliOption);
            } else if (hasCliOption && cliOptionValue != null) {
                if (!validateValue(annotation, cliOptionValue)) {
                    invalidOptions.add(cliOption);
                }
            }

        }
        return invalidOptions;
    }

    private boolean validateValue(final CLIOption annotation, final String cliOptionValue) {
        //Check regex
        if (annotation.required() && (cliOptionValue == null || cliOptionValue.isEmpty())) {
            return false;
        } else if (cliOptionValue != null && !cliOptionValue.isEmpty()) {
            final String regex = annotation.regex();
            return cliOptionValue.matches(regex);
        }
        return true;
    }

    public T interactive() {
        try {
            //Create new instance to load options into
            final T targetInstance = targetClass.newInstance();
            //Load system in
            //Loop annotations and request from text entry
            for (final CLIOption cliOption : annotations.values()) {
                //Print description
                boolean isValid = false;
                while (!isValid) {
                    String value = getValueFromConsole(cliOption);
                    isValid = validateValue(cliOption, value);
                    if (isValid) {
                        if (!value.isEmpty()) {
                            invokeAnnotatedMethod(cliOption.name(), targetInstance, value);
                        } else {
                            invokeAnnotationDefault(cliOption.name(), targetInstance);
                        }
                    }
                }

            }
            return targetInstance;
        } catch (ReflectiveOperationException | IllegalArgumentException | IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private String getValueFromConsole(final CLIOption cliOption) throws IOException {
        final String description;
        if (cliOption.description().isEmpty()) {
            description = cliOption.name();
        } else {
            description = cliOption.description();
        }
        final String defaultText;
        if (!cliOption.defaultValue().isEmpty()) {
            defaultText = " (default " + cliOption.defaultValue() + ")";
        } else {
            defaultText = "";
        }
        if (cliOption.name().trim().toLowerCase().contains("password")
                || cliOption.name().trim().toLowerCase().contains("secret")) {
            return new String(Utils.readPassword(description + defaultText + ": "));
        } else {
            return Utils.readLine(description + defaultText + ": ");
        }
    }

    private String getValueFromSystemIn(final CLIOption cliOption) throws IOException {
        final String description;
        if (cliOption.description().isEmpty()) {
            description = cliOption.name();
        } else {
            description = cliOption.description();
        }
        final String defaultText;
        if (!cliOption.defaultValue().isEmpty()) {
            defaultText = " (default " + cliOption.defaultValue() + ")";
        } else {
            defaultText = "";
        }
        System.out.print(description + defaultText + ": ");
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        final String value = br.readLine();
        return value;
    }

    /**
     * Map the given argument array to a new instance of the target class
     *
     * @param args
     * @return a new object instance of the target class, with property set based on the arguments provided
     */
    public T parseArguments(String[] args) {
        try {
            //Load annotated methods into a Map keyed by the option name
            final T targetInstance = targetClass.newInstance();

            final Map<String, String> argsMap = getOptionsMap(args);

            for (Entry<String, Method> annotatedMethodEntry : annotatedMethods.entrySet()) {
                final String optionName = annotatedMethodEntry.getKey();
                if (argsMap.containsKey(optionName)) {
                    final String value = argsMap.get(optionName);
                    invokeAnnotatedMethod(optionName, targetInstance, value);
                } else if (!hasDefault(optionName)) {
                    invokeAnnotationDefault(optionName, targetInstance);
                }
            }

            return targetInstance;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException(Arrays.toString(args), ex);
        }
    }

    public void invokeAnnotationDefault(String optionName, T targetInstance) throws ReflectiveOperationException {
        invokeAnnotatedMethod(optionName, targetInstance, getDefault(optionName));
    }

    private boolean hasDefault(final String optionName) {
        return getAnnotation(optionName).defaultValue().isEmpty();
    }

    private String getDefault(final String optionName) {
        return getAnnotation(optionName).defaultValue();
    }

    private CLIOption getAnnotation(final String optionName) {
        return getAnnotation(annotatedMethods.get(optionName));
    }

    private void invokeAnnotatedMethod(String optionName, final T targetInstance, final String value) throws ReflectiveOperationException {
        //found an annotation for the option
        final Method method = annotatedMethods.get(optionName);
        if (getAnnotation(method).flag()) {
            //Flag, so treat as 'true'
            method.invoke(targetInstance, true);
        } else {
            /*
             * Convert the given option value (a string) to the
             * required basic type (as determined by the methods
             * argument type)
             */
            final Class<?> parameterType = method.getParameterTypes()[0];
            final Object valueObject;
            if (this.propertyEditors.containsKey(parameterType)) {
                valueObject = this.propertyEditors.get(parameterType).getObject(value);
            } else {
                valueObject = Utils.stringToType(parameterType, value);
            }
            //Invoke the method on the Object instance using the converted value
            method.invoke(targetInstance, valueObject);
        }
    }

    private static CLIOption getAnnotation(final Method method) {
        return method.getAnnotation(CLIOption.class);
    }

    /**
     * Parse CLI arguments into key-value pairs.
     *
     * @param args
     * @return
     */
    private Map<String, String> getOptionsMap(final String[] args) {
        final Map<String, String> optionsMap = parseCliOptions(args);
        // Cleanup aliased
        for (final Entry<String, String> alias : aliases.entrySet()) {
            if (optionsMap.containsKey(alias.getKey())) {
                final String value = optionsMap.remove(alias.getKey());
                optionsMap.put(alias.getValue(), value);
            }
        }
        return optionsMap;
    }

    public final <T> void registerPropertyEditor(final Class<T> clazz, final PropertyEditor<T> propertyEditor) {
        this.propertyEditors.put(clazz, propertyEditor);
    }

}
