package com.learning.demo;

import htsjdk.samtools.SAMRecord;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.Callable;

import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;

public class Application implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final String APPLICATION = "LearningApp";
    public static final String VERSION = Application.class.getPackage().getImplementationVersion();

    @Option(names = {"-b", "--bam"}, description = "Input BAM", required = true)
    private String inputBam;

    @Override
    public Integer call() throws Exception {

        LOGGER.info("Starting with app {} version {}", APPLICATION, VERSION);
        SamReader samReader = SamReaderFactory.makeDefault().open(new File(inputBam));
        LOGGER.info(String.valueOf(samReader.getFileHeader().getReadGroups()));
        Iterator<SAMRecord> readIterator = samReader.iterator();
        SAMRecord read = readIterator.next();
        String sequence = read.getReadString();
        LOGGER.info("Sequence: {}", sequence);
//        while(readIterator.hasNext()) {
//            Object element = readIterator.next();
//            System.out.println(element);
//        }

        samReader.close();
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

}
