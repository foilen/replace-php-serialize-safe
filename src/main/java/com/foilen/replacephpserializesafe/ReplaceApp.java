/*
    Foilen Replace PHP Serialize Safe
    https://github.com/foilen/replace-php-serialize-safe
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.replacephpserializesafe;

import java.io.File;
import java.io.PrintWriter;

import com.foilen.smalltools.tools.FileTools;
import com.google.common.util.concurrent.RateLimiter;

public class ReplaceApp {

    public static void main(String[] args) throws Exception {

        // Check arguments
        if (args.length != 4) {
            System.out.println("Usage: inFile outFile search replace");
            System.exit(1);
        }

        int arg = 0;
        File inFile = new File(args[arg++]);
        File outFile = new File(args[arg++]);
        String search = args[arg++];
        String replace = args[arg++];

        if (!inFile.exists()) {
            System.out.println("File [" + inFile.getAbsolutePath() + "] does not exists");
            System.exit(1);
        }

        // Start processing
        RateLimiter rateLimiter = RateLimiter.create(1);
        System.out.println("Start processing " + inFile.getAbsolutePath());
        PrintWriter out = new PrintWriter(outFile);
        long linesCount = 0;
        for (String line : FileTools.readFileLinesIteration(inFile)) {
            ++linesCount;
            out.println(Processor.replace(line, search, replace));
            if (rateLimiter.tryAcquire()) {
                System.out.println("Processed " + linesCount + " lines");
            }
        }
        out.close();

        System.out.println("Processed " + linesCount + " lines");
        System.out.println("Processing completed. File in " + outFile.getAbsolutePath());

    }

}
