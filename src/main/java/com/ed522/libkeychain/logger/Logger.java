/**
 * Copyright 2024 ed522
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ed522.libkeychain.logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Logger {

    private static final String CLEARMODS = "\u001b[0m";
	private static long startTime;
    static {
        startTime = System.currentTimeMillis();
    }

    private static Logger defaultLogger;
    public static Logger getDefault() {
        if (defaultLogger == null) defaultLogger = new Logger();
        return defaultLogger;
    }

    public static void setDefault(Logger logger) {
        defaultLogger = logger;
    }
    
    private PrintStream out;
    private PrintStream err;
    private Level min = Level.ALL;

    private ArrayList<Matcher> filters;

    public enum Level {
        ALL(-2147483648),
        TRACE(0),
        DEBUG(100),
        CONFIG(200),
        INFO(300),
        WARNING(400),
        ERROR(500),
        SEVERE(900),
        PANIC(1000),
        NONE(2147483647);
        public final int priority;
        private Level(int level) {
            this.priority = level;
        }
        
    }

    public static interface Matcher {
        boolean match(String in);
    }

    public static final class Includer implements Matcher {
        List<String> matches;
        public Includer() {
            matches = new ArrayList<>();
        }
        public void setStrings(String... matches) {
            this.matches = new ArrayList<>();
            add(matches);
        }
        public void add(String... matches) {
            for (String s : matches) this.matches.add(s);
        }
        public boolean match(String in) {
            for (String s : matches) if (s.equals(in)) return true;
            return false;
        }
    }

    public static final class Excluder implements Matcher {
        List<String> matches;
        public Excluder() {
            matches = new ArrayList<>();
        }
        public void setStrings(String... matches) {
            this.matches = new ArrayList<>();
            add(matches);
        }
        public void add(String... matches) {
            for (String s : matches) this.matches.add(s);
        }
        public boolean match(String in) {
            for (String s : matches) if (s.equals(in)) return false;
            return true;
        }
    }

    public static class LoggerOutputStream extends OutputStream {

        private Logger logger;
        private String name;
        private Level level;

        public Logger getLogger() {
            return logger;
        }
        public void setLogger(Logger logger) {
            this.logger = logger;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Level getLevel() {
            return level;
        }
        public void setLevel(Level level) {
            this.level = level;
        }

        public LoggerOutputStream(Logger logger, String name, Level level) {
            this.logger = logger;
            this.name = name;
            this.level = level;
        }
        @Override
        public void write(int b) throws IOException {
            logger.logAppendable(level, new String(new byte[] {(byte) b}), name);
        }
		@Override
		public void write(byte[] data, int offset, int len) {
			logger.logAppendable(level, new String(data, offset, len, StandardCharsets.UTF_8), name);
		}
        
    }

    private boolean printTitle = true;

    private String format(Level level, String msg, String name) {
        StringBuilder builder = new StringBuilder();

        String titleCode = "";
        if (level.equals(Level.PANIC)) titleCode = "\u001b[1;7;91m";
        else if (level.equals(Level.SEVERE)) titleCode = "\u001b[1;3;4;91m";
        else if (level.equals(Level.ERROR)) titleCode = "\u001b[1;31m";
        else if (level.equals(Level.WARNING)) titleCode = "\u001b[1;93m";
        else if (level.equals(Level.INFO)) titleCode = "\u001b[1;96m";
        else if (level.equals(Level.CONFIG)) titleCode = "\u001b[1;94m";
        else if (level.equals(Level.DEBUG)) titleCode = "\u001b[36m";
        else titleCode = "\u001b[2;3;36m";

        String allCode = "";
        if (level.equals(Level.PANIC)) allCode = "\u001b[1;91m";

        // https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797 is very useful
        // open
        if (printTitle) {
            builder.append(allCode);
            builder.append("\u001b[1m");
            builder.append("[");
            
            // italic log name
            builder.append("\u001b[3m");
            builder.append(name);
            builder.append("\u001b[23m");
            builder.append("/");
            
            // level title
            builder.append(CLEARMODS);
            builder.append(titleCode);
            builder.append(level.toString());
            builder.append(CLEARMODS);
            builder.append("\u001b[1m");
            builder.append(allCode);
            builder.append("@");
            builder.append((System.currentTimeMillis() - startTime) / 1000d);
            builder.append("]: ");
            builder.append(CLEARMODS);
            builder.append(allCode);
        }
        
        // message
        builder.append(msg);
        builder.append(CLEARMODS);

        // Markdown (but no color):
        // [*ThreadName*/**WARNING**@1.002]: Message
        // *text* is italic, **text** is bold

        return builder.toString();

    }

    public Logger() {
        this(System.out, System.err); // NOSONAR: stdout (required)
    }

    public Logger(PrintStream out, PrintStream err) {
        this.setOut(out, err);
        this.filters = new ArrayList<>();
    }

    public void setLevel(Level level) {
        this.min = level;
    }

    public void setOut(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }

    public void setFilter(Matcher... matchers) {
        this.filters = new ArrayList<>();
        addFilters(matchers);
    }

    public void addFilters(Matcher... matchers) {
        filters.addAll(Arrays.asList(matchers));
    }

    public void logAppendable(Level level, String msg, String name) {
        
		if (level.priority < min.priority) {return;} // sonarlint being stupid
		
        for (Matcher m : filters) {
			if (!m.match(name)) return;
        }
		
		if (level.priority >= Level.ERROR.priority) this.err.print(this.format(level, msg, name));
        else this.out.print(this.format(level, msg, name));
        
		if (msg.contains(System.lineSeparator())) printTitle = true;
        else printTitle = false;

	}

    public void log(Level level, String msg, String name) {
        this.logAppendable(level, msg + System.lineSeparator(), name);
    }

    public void logFormatted(Level level, String msg, String name, Object... params) {

        String formatted = msg;
        for (int i = 0; i < params.length; i++) {
            formatted = formatted.replaceAll("\\$\\{" + i +"\\}", params[i].toString());
        }
        this.log(level, formatted, name);

    }

}
