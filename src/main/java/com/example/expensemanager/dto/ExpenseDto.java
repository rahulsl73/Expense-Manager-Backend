package com.example.expensemanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.example.expensemanager.model.Expense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDto {
    private Long id;

    @NotBlank 
    private String title;

    @NotNull @Positive
    private BigDecimal amount;

    @NotBlank 
    private String category;
    
    @NotNull
    private LocalDate date;
    private List<String> tags;
    private String note;
    private Long userId; // Just ID reference, no full User object


    public static ExpenseDto fromEntity(Expense e) {
        return ExpenseDto.builder()
            .id(e.getId())
            .title(e.getTitle())
            .amount(e.getAmount())
            .category(e.getCategory())
            .date(e.getDate())
            .tags(e.getTags())
            .note(e.getNote())
            .userId(e.getUser() != null ? e.getUser().getId() : null)
            .build();
    }


    public Expense toEntity() {
        Expense e = new Expense();
        e.setId(this.id);
        e.setTitle(this.title);
        e.setAmount(this.amount);
        e.setCategory(this.category);
        e.setDate(this.date);
        e.setTags(this.tags);
        e.setNote(this.note);
        return e;
    }

}

