/*
    Foilen Replace PHP Serialize Safe
    https://github.com/foilen/replace-php-serialize-safe
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.replacephpserializesafe;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.ResourceTools;

public class ProcessorTest {

    private static final String REPLACE = "http://newSite.example.com";
    private static final String SEARCH = "http://www.example.com";

    @Test
    public void testReplace() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(ResourceTools.getResourceAsStream("ProcessorTest-testReplace-in.txt", getClass())));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            out.append(Processor.replace(line, SEARCH, REPLACE));
            out.append("\n");
        }

        AssertTools.assertIgnoreLineFeed(ResourceTools.getResourceAsString("ProcessorTest-testReplace-out.txt", getClass()), out.toString());

    }

    @Test
    public void testReplace_begin() {
        String line = SEARCH + "yep";
        String expected = REPLACE + "yep";
        String actual = Processor.replace(line, SEARCH, REPLACE);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplace_end() {
        String line = "yep" + SEARCH;
        String expected = "yep" + REPLACE;
        String actual = Processor.replace(line, SEARCH, REPLACE);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplace_exact() {
        String line = SEARCH;
        String expected = REPLACE;
        String actual = Processor.replace(line, SEARCH, REPLACE);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplace_null() {
        String line = null;
        String expected = null;
        String actual = Processor.replace(line, SEARCH, REPLACE);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplace_twice() {
        String line = SEARCH + SEARCH;
        String expected = REPLACE + REPLACE;
        String actual = Processor.replace(line, SEARCH, REPLACE);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplace_withBogusText_1() {
        String line = "s:40:\"AAABBB" + SEARCH + "AAABBB";
        String expected = "s:40:\"AAABBB" + REPLACE + "AAABBB";
        String actual = Processor.replace(line, SEARCH, REPLACE);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplace_withBogusText_2() {
        String line = "s::\"AAABBB" + SEARCH + "AAABBB";
        String expected = "s::\"AAABBB" + REPLACE + "AAABBB";
        String actual = Processor.replace(line, SEARCH, REPLACE);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplace_withBogusText_3() {
        String line = "here\"AAABBB" + SEARCH + "AAABBB";
        String expected = "here\"AAABBB" + REPLACE + "AAABBB";
        String actual = Processor.replace(line, SEARCH, REPLACE);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplaceLongLine() throws Exception {

        StringBuilder in = new StringBuilder();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < 5000; ++i) {
            in.append("phpSerialized text: s:73:\"AAAhttp://www.example.com/perfectBBB AAAhttp://www.example.com/perfectBBB\";");
            out.append("phpSerialized text: s:81:\"AAAhttp://newSite.example.com/perfectBBB AAAhttp://newSite.example.com/perfectBBB\";");
        }

        String actual = Processor.replace(in.toString(), SEARCH, REPLACE);
        Assert.assertEquals(out.toString(), actual);

    }

}
