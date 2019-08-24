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

    public static String replace(String line, String search, String replace) {
        if (line == null) {
            return null;
        }

        // Search for text
        Tuple2<Integer, String> result = new Tuple2<>(0, line);
        replacedOne(line, result, search, replace);
        line = result.getB();
        while (result.getA() != -1) {
            replacedOne(line, result, search, replace);
            line = result.getB();
        }

        return line;
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
