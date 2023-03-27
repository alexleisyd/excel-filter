package org.example.model;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvIgnore;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MasterRecord implements CsvBean{

    @CsvBindByPosition(position = 0)
    private String dateString;

    @CsvBindByPosition(position = 1)
    private String description;

    @CsvIgnore
    private LocalDate date;

    @CsvBindByPosition(position = 2)
    private String reference;

    @CsvBindByPosition(position = 3)
    private Double debit;

    @CsvBindByPosition(position = 4)
    private String credit;

}
