package com.foilen.replacephpserializesafe;

class SerializationContext {
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
