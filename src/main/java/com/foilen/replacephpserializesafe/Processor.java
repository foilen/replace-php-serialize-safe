/*
    Foilen Replace PHP Serialize Safe
    https://github.com/foilen/replace-php-serialize-safe
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.replacephpserializesafe;

import com.foilen.smalltools.tuple.Tuple2;

public class Processor {

    private static class SerializationContext {
        int sPos;
        int colonQuotePos;
        int colonQuoteLen;
        int length;
        boolean escapedQuote;
        int endQuotePos;
        
        SerializationContext(int sPos, int colonQuotePos, int colonQuoteLen, int length, boolean escapedQuote, int endQuotePos) {
            this.sPos = sPos;
            this.colonQuotePos = colonQuotePos;
            this.colonQuoteLen = colonQuoteLen;
            this.length = length;
            this.escapedQuote = escapedQuote;
            this.endQuotePos = endQuotePos;
        }
    }

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
        
        while ((searchPos = line.indexOf(search, currentPos)) != -1) {
            // Check if this match is within a PHP serialized string
            SerializationContext context = findSerializationContext(line, searchPos, searchLen);
            
            if (context != null) {
                // This is inside a serialized string
                // We need to handle all replacements within this serialized string
                // Append everything up to the start of "s:"
                result.append(line.substring(currentPos, context.sPos));
                
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
                result.append(line.substring(context.colonQuotePos, context.colonQuotePos + context.colonQuoteLen));
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
                result.append(line.substring(currentPos, searchPos));
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
        
        // Search for serialized string pattern before this position
        int lastStartFoundPos = searchPosition;
        while (lastStartFoundPos > 0) {
            int colonAndQuotePos = line.lastIndexOf(":\"", lastStartFoundPos - 1);
            int colonAndEscapedQuotePos = line.lastIndexOf(":\\\"", lastStartFoundPos - 1);
            boolean escapedQuote = false;
            int colonAndQuoteLen = 2;
            
            if (colonAndQuotePos < colonAndEscapedQuotePos) {
                escapedQuote = true;
                colonAndQuotePos = colonAndEscapedQuotePos;
                colonAndQuoteLen = 3;
            }
            
            lastStartFoundPos = colonAndQuotePos;
            if (colonAndQuotePos > 0) {
                int sAndColon = line.lastIndexOf("s:", colonAndQuotePos);
                if (sAndColon >= 0) {
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

    private static void replacedOne(String line, Tuple2<Integer, String> fromIndexAndFinalLine, String search, String replace) {

        // Find the text if present
        int searchPosition = line.indexOf(search, fromIndexAndFinalLine.getA());
        if (searchPosition == -1) {
            fromIndexAndFinalLine.setA(searchPosition);
            fromIndexAndFinalLine.setB(line);
            return;
        }

        // If beginning or end, it is not serialized
        int searchLen = search.length();
        if (searchPosition == 0 || searchPosition + searchLen == line.length()) {
            fromIndexAndFinalLine.setA(searchPosition);
            fromIndexAndFinalLine.setB(replaceOne(line, searchPosition, replace, searchLen));
            return;
        }

        // Search serialized string before
        int lastStartFoundPos = searchPosition;
        while (lastStartFoundPos > 0) {
            int colonAndQuotePos = line.lastIndexOf(":\"", lastStartFoundPos - 1);
            int colonAndEscapedQuotePos = line.lastIndexOf(":\\\"", lastStartFoundPos - 1);
            boolean escapedQuote = false;
            int colonAndQuoteLen = 2;
            if (colonAndQuotePos < colonAndEscapedQuotePos) {
                escapedQuote = true;
                colonAndQuotePos = colonAndEscapedQuotePos;
                colonAndQuoteLen = 3;
            }
            lastStartFoundPos = colonAndQuotePos;
            if (colonAndQuotePos > 0) {
                int sAndColon = line.lastIndexOf("s:", colonAndQuotePos);
                if (sAndColon >= 0) {
                    // Check is an integer
                    Integer len = null;
                    try {
                        len = Integer.valueOf(line.substring(sAndColon + 2, colonAndQuotePos));
                    } catch (Exception e) {
                    }

                    // Check it ends to the other quote
                    if (len != null) {
                        int expectedEndPos = colonAndQuotePos + colonAndQuoteLen + len;
                        if (expectedEndPos < line.length() && expectedEndPos > searchPosition + searchLen) {
                            boolean replaceIt = false;
                            if (escapedQuote) {
                                if (line.charAt(expectedEndPos) == '\\' && line.charAt(expectedEndPos + 1) == '"') {
                                    replaceIt = true;
                                }
                            } else {
                                if (line.charAt(expectedEndPos) == '"') {
                                    replaceIt = true;
                                }
                            }

                            if (replaceIt) {
                                // Replace len
                                StringBuilder replacedLine = new StringBuilder();
                                replacedLine.append(line.substring(0, sAndColon + 2));
                                replacedLine.append((len + replace.length() - searchLen));

                                // Replace text
                                replacedLine.append(line.substring(colonAndQuotePos, searchPosition));
                                replacedLine.append(replace);
                                replacedLine.append(line.substring(searchPosition + searchLen));
                                fromIndexAndFinalLine.setA(searchPosition);
                                fromIndexAndFinalLine.setB(replacedLine.toString());
                                return;
                            }
                        }
                    }
                }
            }
        }

        // Replace
        fromIndexAndFinalLine.setA(searchPosition);
        fromIndexAndFinalLine.setB(replaceOne(line, searchPosition, replace, searchLen));
    }

    private static String replaceOne(String line, int begin, String replace, int searchLen) {
        StringBuilder replacedLine = new StringBuilder();
        replacedLine.append(line.substring(0, begin));
        replacedLine.append(replace);
        replacedLine.append(line.substring(begin + searchLen));
        return replacedLine.toString();
    }

}
