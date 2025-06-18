package com.example.expensemanager.controller;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.expensemanager.dao.SummaryDao;
import com.example.expensemanager.dto.ExpenseDto;
import com.example.expensemanager.model.Expense;
import com.example.expensemanager.model.User;
import com.example.expensemanager.service.BudgetService;
import com.example.expensemanager.service.ExpenseService;
import com.example.expensemanager.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {
    private final ExpenseService expSvc;
    private final UserService    userSvc;
    private final BudgetService  budSvc;
    private final SummaryDao     summaryDao;
    private final KafkaTemplate<String,String> kafka;       
    private final ObjectMapper objectMapper;                

    public ExpenseController(
        BudgetService budSvc,
        ExpenseService expSvc,
        UserService userSvc,
        SummaryDao summaryDao,
        KafkaTemplate<String,String> kafka,               
        ObjectMapper objectMapper                         
    ) {
        this.budSvc = budSvc;
        this.expSvc = expSvc;
        this.userSvc = userSvc;
        this.summaryDao = summaryDao;
        this.kafka = kafka;
        this.objectMapper = objectMapper;

    }

    @PostMapping
    public ExpenseDto create(
        @RequestHeader("User-Id") Long uid,
        @Valid @RequestBody ExpenseDto dto
    ) throws JsonProcessingException {
        User u = userSvc.findById(uid).orElseThrow();
        Expense e = dto.toEntity();
        e.setUser(u);
        Expense saved = expSvc.create(e);

        // fire Kafka event
        String payload = objectMapper.writeValueAsString(ExpenseDto.fromEntity(saved));
        kafka.send("expenses", payload);

        if (budSvc.isOverBudget(u, saved.getAmount())) {
            // TODO: alert user
        }
        return ExpenseDto.fromEntity(saved);
    }


    @GetMapping
    public Page<ExpenseDto> list(
        @RequestHeader("User-Id") Long uid,
        @RequestParam(required=false) String category,
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate end,
        Pageable pg
    ) {
        User u = userSvc.findById(uid).orElseThrow();
        return expSvc.list(u, category, start, end, pg)
            .map(ExpenseDto::fromEntity);
    }

    @GetMapping("/{id}")
    public ExpenseDto getOne(@PathVariable Long id) {
        Expense e = expSvc.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ExpenseDto.fromEntity(e);
    }

    @PutMapping("/{id}")
    public ExpenseDto update(
        @PathVariable Long id,
        @Valid @RequestBody ExpenseDto dto
    ) {
        Expense existing = expSvc.findById(id).orElseThrow();
        existing.setTitle(dto.getTitle());
        existing.setAmount(dto.getAmount());
        existing.setCategory(dto.getCategory());
        existing.setDate(dto.getDate());
        existing.setTags(dto.getTags());
        existing.setNote(dto.getNote());
        Expense updated = expSvc.update(existing);
        return ExpenseDto.fromEntity(updated);
    }


    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        expSvc.delete(id);
    }

    @GetMapping("/stats/summary")
    public Map<String,Object> getSummary(
        @RequestHeader("User-Id") Long uid,
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate end
    ) throws JsonProcessingException{
        BigDecimal total = summaryDao.getTotalSpent(uid, start, end);
        Long count      = summaryDao.getExpenseCount(uid, start, end);
        BigDecimal avg  = summaryDao.getAverageSpent(uid, start, end);

        Map<String,Object> summary = Map.of(
            "totalSpent",   total,
            "expenseCount", count,
            "averageSpent", avg
        );


        // publish summary to Kafka
        String summaryJson = objectMapper.writeValueAsString(summary);
        kafka.send("expense-summaries", summaryJson);
        return summary;
    }

     @GetMapping("/export")
    public void exportCsv(
        @RequestHeader("User-Id") Long uid,
        HttpServletResponse resp
    ) throws Exception {
        resp.setContentType("text/csv");
        resp.setHeader("Content-Disposition", "attachment; filename=expenses.csv");
        PrintWriter pw = resp.getWriter();
        pw.println("Title,Amount,Category,Date,Tags,Note");
        LocalDate start = LocalDate.of(1970, 1, 1);
        LocalDate end   = LocalDate.now();
        expSvc.list(
            userSvc.findById(uid).orElseThrow(),
            null,
            start,
            end,
            Pageable.unpaged()
        )
        .map(ExpenseDto::fromEntity)
        .forEach(dto -> pw.printf(
            "%s,%s,%s,%s,%s,%s%n",
            dto.getTitle(),
            dto.getAmount(),
            dto.getCategory(),
            dto.getDate(),
            String.join("|", dto.getTags()),
            dto.getNote()
        ));
    }
}
