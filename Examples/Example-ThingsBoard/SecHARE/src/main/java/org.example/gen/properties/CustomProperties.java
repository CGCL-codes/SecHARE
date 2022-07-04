package org.example.gen.properties;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class CustomProperties extends Properties {
    private static final long serialVersionUID = 5011694856722313621L;

    private static final String KEY_VALUE_SEPARATORS = "=: \t\r\n\f";

    private static final String STRICT_KEY_VALUE_SEPARATORS = "=:";

    private static final String SPECIAL_SAVE_CHARS = "=: \t\r\n\f#!";

    private static final String WHITE_SPACE_CHARS = " \t\r\n\f";

    private final PropertiesContext context = new PropertiesContext();

    public PropertiesContext getContext() {
        return context;
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {

        BufferedReader in;
        in = new BufferedReader(new InputStreamReader(inStream, "8859_1"));
        while (true) {
            String line = in.readLine();
            String intactLine = line;
            if (line == null) {
                return;
            }

            if (line.length() > 0) {

                int len = line.length();
                int keyStart;
                for (keyStart = 0; keyStart < len; keyStart++) {
                    if (WHITE_SPACE_CHARS.indexOf(line.charAt(keyStart)) == -1) {
                        break;
                    }
                }

                if (keyStart == len) {
                    continue;
                }

                char firstChar = line.charAt(keyStart);

                if ((firstChar != '#') && (firstChar != '!')) {
                    StringBuilder sb = new StringBuilder(intactLine);
                    while (continueLine(line)) {
                        String nextLine = in.readLine();
                        sb.append("\n").append(nextLine);
                        if (nextLine == null) {
                            nextLine = "";
                        }
                        String loppedLine = line.substring(0, len - 1);
                        int startIndex;
                        for (startIndex = 0; startIndex < nextLine.length(); startIndex++) {
                            if (WHITE_SPACE_CHARS.indexOf(nextLine.charAt(startIndex)) == -1) {
                                break;
                            }
                        }
                        nextLine = nextLine.substring(startIndex);
                        line = loppedLine + nextLine;
                        len = line.length();
                    }
                    intactLine = sb.toString();

                    int separatorIndex;
                    for (separatorIndex = keyStart; separatorIndex < len; separatorIndex++) {
                        char currentChar = line.charAt(separatorIndex);
                        if (currentChar == '\\') {
                            separatorIndex++;
                        } else if (KEY_VALUE_SEPARATORS.indexOf(currentChar) != -1) {
                            break;
                        }
                    }

                    int valueIndex;
                    for (valueIndex = separatorIndex; valueIndex < len; valueIndex++) {
                        if (WHITE_SPACE_CHARS.indexOf(line.charAt(valueIndex)) == -1) {
                            break;
                        }
                    }

                    if (valueIndex < len) {
                        if (STRICT_KEY_VALUE_SEPARATORS.indexOf(line.charAt(valueIndex)) != -1) {
                            valueIndex++;
                        }
                    }

                    while (valueIndex < len) {
                        if (WHITE_SPACE_CHARS.indexOf(line.charAt(valueIndex)) == -1) {
                            break;
                        }
                        valueIndex++;
                    }
                    String key = line.substring(keyStart, separatorIndex);
                    String value = (separatorIndex < len) ? line.substring(valueIndex, len) : "";

                    key = loadConvert(key);
                    value = loadConvert(value);
                    put(key, value, intactLine);
                } else {
                    context.addCommentLine(intactLine);
                }
            } else {
                context.addCommentLine(intactLine);
            }
        }
    }


    private String loadConvert(String theString) {
        char aChar;
        int len = theString.length();
        StringBuilder outBuffer = new StringBuilder(len);

        for (int x = 0; x < len;) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0': case '1': case '2': case '3': case '4':
                            case '5': case '6': case '7': case '8': case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a': case 'b': case 'c':
                            case 'd': case 'e': case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A': case 'B': case 'C':
                            case 'D': case 'E': case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    switch (aChar) {
                        case 't':
                            outBuffer.append('\t');
                            break;
                        case 'r':
                            outBuffer.append('\r');
                            break;
                        case 'n':
                            outBuffer.append('\n');
                            break;
                        case 'f':
                            outBuffer.append('\f');
                            break;
                        default:
                            outBuffer.append(aChar);
                            break;
                    }
                }
            } else {
                outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    @Override
    public synchronized void store(OutputStream out, String header) throws IOException {
        BufferedWriter aWriter;
        aWriter = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
        if (header == null) {
            header = new Date().toString();
        }
        writeln(aWriter, "# " + header);
 
        List<Object> entries = context.getCommentOrEntries();
        for (Object obj : entries) {
            if (obj.toString() != null) {
                writeln(aWriter, obj.toString());
            }
        }
        aWriter.flush();
    }

    private static void writeln(BufferedWriter bw, String s) throws IOException {
        bw.write(s);
        bw.newLine();
    }

    private boolean continueLine(String line) {
        int slashCount = 0;
        int index = line.length() - 1;
        while ((index >= 0) && (line.charAt(index--) == '\\')) {
            slashCount++;
        }
        return (slashCount % 2 == 1);
    }

    private String saveConvert(String theString, boolean escapeSpace) {
        int len = theString.length();
        StringBuilder outBuilder = new StringBuilder(len * 2);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace) {
                        outBuilder.append('\\');
                    }

                    outBuilder.append(' ');
                    break;
                case '\\':
                    outBuilder.append('\\');
                    outBuilder.append('\\');
                    break;
                case '\t':
                    outBuilder.append('\\');
                    outBuilder.append('t');
                    break;
                case '\n':
                    outBuilder.append('\\');
                    outBuilder.append('n');
                    break;
                case '\r':
                    outBuilder.append('\\');
                    outBuilder.append('r');
                    break;
                case '\f':
                    outBuilder.append('\\');
                    outBuilder.append('f');
                    break;
                default:
                    if ((aChar < 0x0020) || (aChar > 0x007e)) {
                        outBuilder.append('\\');
                        outBuilder.append('u');
                        outBuilder.append(toHex((aChar >> 12) & 0xF));
                        outBuilder.append(toHex((aChar >> 8) & 0xF));
                        outBuilder.append(toHex((aChar >> 4) & 0xF));
                        outBuilder.append(toHex(aChar & 0xF));
                    } else {
                        if (SPECIAL_SAVE_CHARS.indexOf(aChar) != -1) {
                            outBuilder.append('\\');
                        }
                        outBuilder.append(aChar);
                    }
            }
        }
        return outBuilder.toString();
    }

    /**
     * Convert a nibble to a hex character
     *
     * @param nibble
     *            the nibble to convert.
     */
    private static char toHex(int nibble) {
        return HEX_DIGIT[(nibble & 0xF)];
    }

    /** A table of hex digits */
    private static final char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
            'F' };

    @Override
    public synchronized Object put(Object key, Object value) {
        context.putOrUpdate(key.toString(), value.toString());
        return super.put(key, value);
    }

    public synchronized Object put(Object key, Object value, String line) {
        context.putOrUpdate(key.toString(), value.toString(), line);
        return super.put(key, value);
    }


    @Override
    public synchronized Object remove(Object key) {
        context.remove(key.toString());
        return super.remove(key);
    }



    class PropertiesContext {
        private final List<Object> commentOrEntries = new ArrayList<>();

        public List<Object> getCommentOrEntries() {
            return commentOrEntries;
        }

        public void addCommentLine(String line) {
            commentOrEntries.add(line);
        }

        public void putOrUpdate(PropertyEntry pe) {
            remove(pe.getKey());
            commentOrEntries.add(pe);
        }

        public void putOrUpdate(String key, String value, String line) {
            PropertyEntry pe = new PropertyEntry(key, value, line);
            remove(key);
            commentOrEntries.add(pe);
        }

        public void putOrUpdate(String key, String value) {
            PropertyEntry pe = new PropertyEntry(key, value);
            int index = remove(key);
            commentOrEntries.add(index, pe);
        }

        public int remove(String key) {
            for (int index = 0; index < commentOrEntries.size(); index++) {
                Object obj = commentOrEntries.get(index);
                if (obj instanceof PropertyEntry) {
                    if (key.equals(((PropertyEntry) obj).getKey())) {
                        commentOrEntries.remove(obj);
                        return index;
                    }
                }
            }
            return commentOrEntries.size();
        }

        class PropertyEntry {
            private String key;

            private String value;

            private String line;

            public String getLine() {
                return line;
            }

            public void setLine(String line) {
                this.line = line;
            }

            public PropertyEntry(String key, String value) {
                this.key = key;
                this.value = value;
            }

            public PropertyEntry(String key, String value, String line) {
                this(key, value);
                this.line = line;
            }

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            @Override
            public String toString() {
                if (line != null) {
                    return line;
                }
                if (key != null && value != null) {
                    String k = saveConvert(key, true);
                    String v = saveConvert(value, false);
                    return k + "=" + v;
                }
                return null;
            }
        }
    }


    public void addComment(String comment) {
        if (comment != null) {
            context.addCommentLine("#" + comment);
        }
    }

}