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

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author James Buncle
 */
public class Help {

    private final List<HelpItem> required;
    private final List<HelpItem> optional;

    public Help() {
        this.required = new LinkedList<>();
        this.optional = new LinkedList<>();
    }

    public void add(HelpItem helpItem) {
        if (helpItem.getCli().required()) {
            this.required.add(helpItem);
        } else {
            this.optional.add(helpItem);
        }
    }

    public Iterable<HelpItem> getRequired() {
        return required;
    }

    public Iterable<HelpItem> getOptional() {
        return optional;
    }

    public void printHelpText() {
        final int maxLength = getMaxKeyLength();
        final Iterable<HelpItem> requiredOptions = this.getRequired();
        if (requiredOptions.iterator().hasNext()) {
            System.out.println("Usage: ");
            for (HelpItem helpItem : requiredOptions) {
                if (helpItem.getCli().required()) {
                    System.out.println('\t' + padRight(helpItem.getUsage(), maxLength) + '\t' + helpItem.getHelp());
                }
            }
        }
        final Iterable<HelpItem> optionalOptions = this.getOptional();
        if (optionalOptions.iterator().hasNext()) {
            System.out.println("Optional: ");
            for (HelpItem helpItem : optionalOptions) {
                if (!helpItem.getCli().required()) {
                    System.out.println('\t' + padRight(helpItem.getUsage(), maxLength) + '\t' + helpItem.getHelp());
                }
            }
        }
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    private int getMaxKeyLength() {
        int maxLength = 0;
        for (HelpItem key : this.required) {
            int keyLength = key.getUsage().length();
            if (keyLength > maxLength) {
                maxLength = keyLength;
            }
        }
        for (HelpItem key : this.optional) {
            int keyLength = key.getUsage().length();
            if (keyLength > maxLength) {
                maxLength = keyLength;
            }
        }
        return maxLength;
    }

}
