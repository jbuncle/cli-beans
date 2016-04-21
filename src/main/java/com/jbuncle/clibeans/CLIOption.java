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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author James Buncle
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface CLIOption {

    /**
     * The name of the CLI option, which will be prefixed with a dash (-) symbol.
     *
     * @return the name of the option
     */
    public String name();

    /**
     * Whether or not the option is a flag, that is it doesn't take an option argument.
     * <br />
     * If false the CLI option will expect an argument in the format of -<i>option</i> <i>option_argument</i>, seperated
     * by a space
     *
     * @return whether or not the CLI option is a flag
     */
    public boolean flag() default false;

    /**
     * Used for validation to check if the CLI option is required
     *
     * @return whether or not the CLI option is required
     */
    public boolean required() default false;

    /**
     * A description of the CLI option, used for creating help text
     *
     * @return a short description of the CLI option
     */
    public String description() default "";

    /**
     * A regular expression used to validate the CLI option argument against.
     *
     * @return a regular expression to match valid CLI option arguments
     */
    public String regex() default ".*";

    /**
     * An alternative name for the CLI Option, which will be prefixed with a dash (-) symbol. This could be a short or
     * long name for the option.
     *
     * @return the CLI Option alias
     */
    public String[] alias() default {};

    /**
     * Default value for CLI Option, expressed as a string
     *
     * @return the default string value
     */
    public String defaultValue() default "";
}
