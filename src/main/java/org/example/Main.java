package org.example;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.lang3.StringUtils;
import org.example.model.FilterRecord;
import org.example.model.MasterRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {

        List<MasterRecord> masterRecords = getMasterRecords();

        List<FilterRecord> filterRecords = getFilterRecords();

        List<MasterRecord> filteredMasterRecords = filterNoMatch(masterRecords, filterRecords);

        log.info("No Match Record size {}", filteredMasterRecords.size());

        try (Writer writer = new FileWriter("master_records_filtered.csv")) {
            StatefulBeanToCsv<MasterRecord> sbc = new StatefulBeanToCsvBuilder<MasterRecord>(writer)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .build();

            sbc.write(filteredMasterRecords);
        } catch (Exception e) {
            log.error("Error writing result file: ", e);
        }
    }

    private static List<MasterRecord> filterNoMatch(List<MasterRecord> masterRecords, List<FilterRecord> filterRecords) {
        //Construct filterMap
        Map<Double, List<FilterRecord>> filterMap = new HashMap<>(filterRecords.size());
        for (FilterRecord line : filterRecords) {
            if (line.getCredit() == null) {
                continue;
            }
            if (filterMap.containsKey(line.getCredit())) {
                filterMap.get(line.getCredit()).add(line);
            } else {
                List<FilterRecord> rs = new ArrayList<>();
                rs.add(line);
                filterMap.put(line.getCredit(), rs);
            }
        }
        List<MasterRecord> noMatchRecords = new ArrayList<>();
        for (MasterRecord line : masterRecords) {
            if (line.getDebit() == null) {
                continue;
            }
            if (filterMap.containsKey(line.getDebit())) {
                //
            } else {
                noMatchRecords.add(line);
            }
        }
        return noMatchRecords;
    }

    private static List<MasterRecord> getMasterRecords() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream is = classloader.getResourceAsStream("master.csv")) {
            if (is == null) {
                throw new RuntimeException("master.csv is null");
            }
            CsvToBean<MasterRecord> masterRecordCsv = new CsvToBeanBuilder<MasterRecord>(new InputStreamReader(is))
                    .withType(MasterRecord.class)
                    .build();
            List<MasterRecord> masterRecords = masterRecordCsv.parse();
            log.info("Master records: {}", masterRecords.size());
            masterRecords.forEach(Main::transformMasterRecord);
            return masterRecords;
        } catch (Exception e) {
            log.error("Error loading master.csv", e);
            return Collections.emptyList();
        }
    }

    private static void transformMasterRecord(MasterRecord masterRecord) {
        if (StringUtils.isNotBlank(masterRecord.getDateString())) {
            String dateString = masterRecord.getDateString().trim().replaceAll("\\s+", "");
            if (StringUtils.length(dateString) == 9) {
                dateString = "0" + dateString;
            }
            try {
                masterRecord.setDate(LocalDate.parse(dateString, formatter));
            } catch (DateTimeParseException e) {
                log.error("Error parsing: {}, string: {}, length: {}", masterRecord, dateString, StringUtils.length(dateString));
            }
        }
    }

    private static List<FilterRecord> transformFilterRecord(List<FilterRecord> filterRecords) {
        List<FilterRecord> records = new ArrayList<>();
        FilterRecord toAdd = null;
        for (FilterRecord line : filterRecords) {
            if (StringUtils.isBlank(line.getId())) {
                if (toAdd != null && StringUtils.isNotBlank(line.getDescription())) {
                    toAdd.setDescription(toAdd.getDescription() + line.getDescription());
                }
            } else {
                toAdd = new FilterRecord(line.getId(), line.getDateString(), line.getReference(), line.getType(), line.getDescription(), line.getDebit(), line.getCredit());
                records.add(toAdd);
            }
        }
        return records;
    }

    private static List<FilterRecord> getFilterRecords() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream is = classloader.getResourceAsStream("filter.csv")) {
            if (is == null) {
                throw new RuntimeException("filter.csv is null");
            }
            CsvToBean<FilterRecord> filterRecordsCsv = new CsvToBeanBuilder<FilterRecord>(new InputStreamReader(is))
                    .withType(FilterRecord.class)
                    .build();
            List<FilterRecord> filterRecords = filterRecordsCsv.parse();
            List<FilterRecord> filterRecordsProcessed = transformFilterRecord(filterRecords);
            log.info("Filter records: {} -> {}", filterRecords.size(), filterRecordsProcessed.size());
            return filterRecordsProcessed;
        } catch (Exception e) {
            log.error("Error loading filter.csv", e);
            return Collections.emptyList();
        }
    }

}