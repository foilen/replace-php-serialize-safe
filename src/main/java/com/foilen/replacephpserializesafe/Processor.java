package com.foilen.replacephpserializesafe;

public class Processor {

    public static String replace(String line, String search, String replace) {
        if (line == null) {
            return null;
        }

        int searchLen = search.length();
        int replaceLen = replace.length();
        int lengthDiff = replaceLen - searchLen;

        StringBuilder result = new StringBuilder(line.length());
        int currentPos = 0;
        int searchPos;

        // Cache the last serialization context to avoid redundant backward searches
        SerializationContext lastContext = null;

        while ((searchPos = line.indexOf(search, currentPos)) != -1) {
            // Check if this match is within the cached serialization context
            SerializationContext context;
            if (lastContext != null && searchPos < lastContext.endQuotePos) {
                // Still within the same serialization - skip it as it's already processed
                currentPos = searchPos + searchLen;
                continue;
            } else {
                // Need to find a new serialization context
                context = findSerializationContext(line, searchPos, searchLen);
                lastContext = context;
            }

            if (context != null) {
                // This is inside a serialized string
                // We need to handle all replacements within this serialized string
                // Append everything up to the start of "s:"
                result.append(line, currentPos, context.sPos);

                // Count how many replacements are in this serialized string
                int startPos = context.colonQuotePos + context.colonQuoteLen;
                int endPos = context.endQuotePos;
                int count = 0;
                int pos = startPos;
                while ((pos = line.indexOf(search, pos)) != -1 && pos + searchLen <= endPos) {
                    count++;
                    pos += searchLen;
                }

                // Append "s:" and the updated length
                result.append("s:");
                result.append(context.length + (count * lengthDiff));

                // Append the content with replacements
                result.append(line, context.colonQuotePos, context.colonQuotePos + context.colonQuoteLen);
                String content = line.substring(startPos, endPos);
                content = content.replace(search, replace);
                result.append(content);

                // Append the closing quote
                if (context.escapedQuote) {
                    result.append("\\\"");
                    currentPos = context.endQuotePos + 2;
                } else {
                    result.append("\"");
                    currentPos = context.endQuotePos + 1;
                }
            } else {
                // Not in a serialized string - simple replacement
                result.append(line, currentPos, searchPos);
                result.append(replace);
                currentPos = searchPos + searchLen;
            }
        }

        // Append remaining content
        result.append(line.substring(currentPos));

        return result.toString();
    }

    private static SerializationContext findSerializationContext(String line, int searchPosition, int searchLen) {
        // If at beginning or end, it's not serialized
        if (searchPosition == 0 || searchPosition + searchLen == line.length()) {
            return null;
        }

        // Limit backward search to avoid scanning entire line for very long lines
        // PHP serialized strings larger than 1MB are extremely rare
        int searchLimit = Math.max(0, searchPosition - 1048576);

        // Search for serialized string pattern before this position
        int lastStartFoundPos = searchPosition;
        while (lastStartFoundPos > searchLimit) {
            int colonAndQuotePos = line.lastIndexOf(":\"", lastStartFoundPos - 1);
            int colonAndEscapedQuotePos = line.lastIndexOf(":\\\"", lastStartFoundPos - 1);

            // Stop if we've searched past our limit
            if (colonAndQuotePos < searchLimit && colonAndEscapedQuotePos < searchLimit) {
                break;
            }

            boolean escapedQuote = false;
            int colonAndQuoteLen = 2;

            if (colonAndQuotePos < colonAndEscapedQuotePos) {
                escapedQuote = true;
                colonAndQuotePos = colonAndEscapedQuotePos;
                colonAndQuoteLen = 3;
            }

            lastStartFoundPos = colonAndQuotePos;
            if (colonAndQuotePos > searchLimit) {
                int sAndColon = line.lastIndexOf("s:", colonAndQuotePos);
                if (sAndColon >= searchLimit) {
                    // Check if there's a valid integer length
                    Integer len = null;
                    try {
                        len = Integer.valueOf(line.substring(sAndColon + 2, colonAndQuotePos));
                    } catch (Exception e) {
                        // Not a valid integer, continue searching
                    }

                    // Check if it ends at the correct quote position
                    if (len != null) {
                        int expectedEndPos = colonAndQuotePos + colonAndQuoteLen + len;
                        if (expectedEndPos < line.length() && expectedEndPos > searchPosition + searchLen) {
                            boolean validEnd = false;
                            if (escapedQuote) {
                                if (expectedEndPos + 1 < line.length() &&
                                        line.charAt(expectedEndPos) == '\\' &&
                                        line.charAt(expectedEndPos + 1) == '"') {
                                    validEnd = true;
                                }
                            } else {
                                if (line.charAt(expectedEndPos) == '"') {
                                    validEnd = true;
                                }
                            }

                            if (validEnd) {
                                return new SerializationContext(sAndColon, colonAndQuotePos, colonAndQuoteLen, len, escapedQuote, expectedEndPos);
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

}
