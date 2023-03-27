package org.example.model;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class FilterRecord implements CsvBean {

    public FilterRecord(String id, String date, String reference, String type, String description, Double debit, Double credit) {
        this.id = id;
        this.dateString = date;
        this.reference = reference;
        this.type = type;
        this.description = description;
        this.debit = debit == null ? 0 : debit;
        this.credit = credit == null ? 0 : credit;
    }

    @CsvBindByPosition(position = 0)
    private String id;

    @CsvBindByPosition(position = 1)
    private String dateString;

    @CsvIgnore
    private LocalDate date;

    @CsvBindByPosition(position = 2)
    private String reference;

    @CsvBindByPosition(position = 3)
    private String type;

    @CsvBindByPosition(position = 4)
    private String description;

    @CsvBindByPosition(position = 5)
    private Double debit;

    @CsvBindByPosition(position = 6)
    private Double credit;

}
