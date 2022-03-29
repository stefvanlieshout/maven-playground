package com.learning.demo;

import htsjdk.samtools.SAMRecord;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.learning.demo.samplesheet.IlluminaSample;
import com.learning.demo.samplesheet.SampleSheet;

public class Application implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final String APPLICATION = "LearningApp";
    public static final String VERSION = Application.class.getPackage().getImplementationVersion();

    @Option(names = {"-b", "--bam"}, description = "Input BAM", required = true)
    private String inputBam;

    @Option(names = {"-s", "--sheet"}, description = "Input SampleSheet.csv")
    private String sampleSheetCsv;

    @Override
    public Integer call() throws Exception {
        LOGGER.info("Starting with app {} version {}", APPLICATION, VERSION);
//        processBam(inputBam);
//        processSampleSheet(sampleSheetCsv);
        SampleSheet sampleSheet = readSampleSheet(sampleSheetCsv);
        List<IlluminaSample> samples = sampleSheet.samples();
        List<IlluminaSample> uniqueSamples = sampleSheet.samples().stream().distinct().collect(Collectors.toList());
        LOGGER.info("Processing samples non-unique-way");
        for (IlluminaSample sample : samples) {
            LOGGER.info("  {} {}", sample.barcode(), sample.sample());
        }
        LOGGER.info("Processing samples unique-way");
        for (IlluminaSample sample : uniqueSamples) {
            LOGGER.info("  {} {}", sample.barcode(), sample.sample());
        }
        return 0;
    }

    public SampleSheet readSampleSheet(String sheet) throws Exception {
        LOGGER.info("  Reading SampleSheet {}", sheet);
        String entireFile = Files.readString(Path.of(sheet));
        String experimentName = entireFile.substring(entireFile.indexOf("ExperimentName")).split(",")[1];
        return SampleSheet.builder().experimentName(experimentName).addAllSamples(samples(entireFile)).build();
    }

    private List<IlluminaSample> samples(String entireFile) {
        try {
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            CsvMapper mapper = new CsvMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readerFor(IlluminaSample.class).with(schema).<IlluminaSample>readValues(entireFile.substring(entireFile.indexOf(
                    "Sample_ID"))).readAll().stream().filter(s -> !s.barcode().isEmpty()).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processSampleSheet(String sheet) throws Exception {
        LOGGER.info("  Processing SampleSheet {}", sheet);
        Path sheetPath = Path.of(sheet);
        String entireFile = Files.readString(sheetPath);

        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        CsvMapper mapper = new CsvMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<IlluminaSample> samples = mapper.readerFor(IlluminaSample.class).with(schema).<IlluminaSample>readValues(entireFile.substring(entireFile.indexOf(
                "Sample_ID"))).readAll().stream().filter(s -> !s.barcode().isEmpty()).collect(Collectors.toList());
        List<IlluminaSample> distinctSamples = mapper.readerFor(IlluminaSample.class).with(schema).<IlluminaSample>readValues(entireFile.substring(entireFile.indexOf(
                "Sample_ID"))).readAll().stream().filter(s -> !s.barcode().isEmpty()).distinct().collect(Collectors.toList());
        LOGGER.info("Samples:");
        System.out.println(samples);
        LOGGER.info("DistinctSamples:");
        System.out.println(distinctSamples);
    }

    private static void processBam(String bam) throws Exception {
        LOGGER.info("  Processing BAM {}", bam);
        SamReader samReader = SamReaderFactory.makeDefault().open(new File(bam));
        LOGGER.info(String.valueOf(samReader.getFileHeader().getReadGroups()));
        Iterator<SAMRecord> readIterator = samReader.iterator();
        SAMRecord read = readIterator.next();
        String sequence = read.getReadString();
        LOGGER.info("Sequence: {}", sequence);
        samReader.close();
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

}
