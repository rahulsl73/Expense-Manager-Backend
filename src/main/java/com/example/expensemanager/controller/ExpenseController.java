package com.example.expensemanager.controller;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import com.example.expensemanager.dto.TimeSeriesPoint;
import com.example.expensemanager.model.Expense;
import com.example.expensemanager.model.User;
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
    private final SummaryDao     summaryDao;
         

    public ExpenseController(
        ExpenseService expSvc,
        UserService userSvc,
        SummaryDao summaryDao,
        
        ObjectMapper objectMapper                         
    ) {
        this.expSvc = expSvc;
        this.userSvc = userSvc;
        this.summaryDao = summaryDao;
       

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
        if (start == null) {
        start = LocalDate.of(1970, 1, 1);
        }
        if (end == null) {
            end = LocalDate.now();
        }
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


        
        return summary;
    }



    // controller for donut data
    @GetMapping("/stats/category")
    public Map<String, BigDecimal> getCategoryBreakdown(
        @RequestHeader("User-Id") Long uid,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        User user = userSvc.findById(uid)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return expSvc.getSpendByCategory(user, start, end);
    }


    // For Horizontal Bar Chart
    @GetMapping("/stats/top")
    public List<ExpenseDto> getTopExpenses(
        @RequestHeader("User-Id") Long uid,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
        @RequestParam int n
    ) {
        User user = userSvc.findById(uid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        // ask service for top n
        return expSvc.findTopByAmount(user, start, end, n)
                     .stream()
                     .map(ExpenseDto::fromEntity)
                     .toList();
    }


    @GetMapping("/stats/timeseries")
    public List<TimeSeriesPoint> getTimeSeries(
        @RequestHeader("User-Id") Long uid,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
        @RequestParam String interval  // "day", "week", or "month"
    ) {
        User user = userSvc.findById(uid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return expSvc.getTimeSeries(user, start, end, interval);
    }



    @GetMapping("/export")
    public void exportCsv(
        @RequestHeader("User-Id") Long uid,

        // default to 1970-01-01 if the client omits the “start” param
        @RequestParam(
        name = "start",
        defaultValue = "1970-01-01"
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate start,

        // default to today if the client omits the “end” param
        @RequestParam(
        name = "end",
        defaultValue = "#{T(java.time.LocalDate).now().toString()}"
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate end,

        @RequestParam(
        name = "category",
        defaultValue = ""
        )
        String category,

        HttpServletResponse resp
    ) throws Exception {
       

        resp.setContentType("text/csv");
        resp.setHeader(
        HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=expenses.csv"
        );

        try (PrintWriter pw = resp.getWriter()) {
            pw.println("Title,Amount,Category,Date,Tags,Note");

            expSvc.list(
                userSvc.findById(uid)
                    .orElseThrow(() -> 
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
                    ),
                category.isBlank() ? null : category,
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
            dto.getNote() == null ? "" : dto.getNote().replace("\n"," ").replace(","," ")
            ));
        }
    }



}
