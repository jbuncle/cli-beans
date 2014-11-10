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

/**
 *
 * @author James Buncle
 */
public class HelpItem {

    private final CLIOption cli;
    private final String usage;
    private final String help;

    protected HelpItem(CLIOption cli) {
        this.cli = cli;
        final StringBuilder sb = new StringBuilder();
        sb.append("-");
        sb.append(cli.name());
        if (cli.alias().length > 0) {
            for (String alias : cli.alias()) {
                sb.append(", -");
                sb.append(alias);
            }
        }
        sb.append(" ");
        if (!cli.flag()) {
            sb.append("<argument>");
        }
        this.usage = sb.toString();
        this.help = cli.description();
    }

    public String getUsage() {
        return usage;
    }

    public String getHelp() {
        return help;
    }

    public CLIOption getCli() {
        return cli;
    }

}
