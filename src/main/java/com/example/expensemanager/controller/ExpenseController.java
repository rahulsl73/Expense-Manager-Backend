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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.expensemanager.constant.Constants;
import com.example.expensemanager.dao.SummaryDao;
import com.example.expensemanager.dto.ExpenseDto;
import com.example.expensemanager.dto.TimeSeriesPoint;
import com.example.expensemanager.dto.UserDto;
import com.example.expensemanager.model.Expense;
import com.example.expensemanager.model.User;
import com.example.expensemanager.service.ExchangeRateService;
import com.example.expensemanager.service.ExpenseService;
import com.example.expensemanager.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/user/{userId}/expenses")
public class ExpenseController {
    private final ExpenseService expSvc;
    private final UserService    userSvc;
    private final SummaryDao     summaryDao;
    private final ExchangeRateService rateSvc;  

    public ExpenseController(
        ExpenseService expSvc,
        UserService userSvc,
        SummaryDao summaryDao,
        ExchangeRateService rateSvc
    ) {
        this.expSvc = expSvc;
        this.userSvc = userSvc;
        this.summaryDao = summaryDao;
        this.rateSvc = rateSvc;

    }

    @PostMapping
    public ExpenseDto create(
        @PathVariable Long userId,
        @Valid @RequestBody ExpenseDto dto
    ) throws JsonProcessingException {
        User u = userSvc.findByIdOrThrow(userId);
        Expense e = dto.toEntity();
        e.setUser(u);
        Expense saved = expSvc.create(e);
        return ExpenseDto.fromEntity(saved);
    }


    @GetMapping
    public Page<ExpenseDto> list(
        @PathVariable Long userId,
        @RequestParam(required=false) String category,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate end,
        Pageable pg
    ) {
        User u = userSvc.findByIdOrThrow(userId);
        return expSvc.list(u, category, start, end, pg)
            .map(ExpenseDto::fromEntity);
    }

     // ——— Currency convert to all for monthly budget ———

    public static class ConvertPayload {
        public String fromCurrency;
        public String toCurrency;
    }

    
    @PutMapping(Constants.CONVERT)
    public UserDto convertAllExpenses(
        @PathVariable Long userId,
        @RequestBody ConvertPayload payload
    ) {
        User u = userSvc.findByIdOrThrow(userId);

        BigDecimal rate = rateSvc.getRate(payload.fromCurrency, payload.toCurrency);

        List<Expense> all = expSvc
            .list(u, null, LocalDate.of(1970,1,1), LocalDate.now(), Pageable.unpaged())
            .getContent();

        all.forEach(exp -> {
            exp.setAmount(exp.getAmount().multiply(rate));
            expSvc.update(exp);
        });

        BigDecimal newBudget = u.getMonthlyBudget().multiply(rate);
        User updatedUser = userSvc.updateProfile(userId, u.getEmail(), newBudget);

        return UserDto.from(updatedUser);
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


   

    @GetMapping(Constants.STATS_SUMMARY)
    public Map<String,Object> getSummary(
        @PathVariable Long userId,
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate end
    ) throws JsonProcessingException{
        BigDecimal total = summaryDao.getTotalSpent(userId, start, end);
        Long count      = summaryDao.getExpenseCount(userId, start, end);
        BigDecimal avg  = summaryDao.getAverageSpent(userId, start, end);

        Map<String,Object> summary = Map.of(
            "totalSpent",   total,
            "expenseCount", count,
            "averageSpent", avg
        );
        
        return summary;
    }



    // controller for donut data
    @GetMapping(Constants.STATS_CATEGORY)
    public Map<String, BigDecimal> getCategoryBreakdown(
        @PathVariable Long userId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        User user = userSvc.findByIdOrThrow(userId);
    
        return expSvc.getSpendByCategory(user, start, end);
    }


    // For Horizontal Bar Chart
    @GetMapping(Constants.STATS_TOP)
    public List<ExpenseDto> getTopExpenses(
        @PathVariable Long userId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
        @RequestParam int n
    ) {
        User user = userSvc.findByIdOrThrow(userId);
            
        return expSvc.findTopByAmount(user, start, end, n)
                     .stream()
                     .map(ExpenseDto::fromEntity)
                     .toList();
    }


    // for line graph
    @GetMapping(Constants.STATS_TIMESERIES)
    public List<TimeSeriesPoint> getTimeSeries(
        @PathVariable Long userId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
        @RequestParam String interval  // "day", "week", or "month"
    ) {
        User user = userSvc.findByIdOrThrow(userId);

        return expSvc.getTimeSeries(user, start, end, interval);
    }



    @GetMapping(Constants.EXPORT)
    public void exportCsv(
        @PathVariable Long userId,
        @RequestParam(
        name = "start",
        defaultValue = "1970-01-01"
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate start,

        @RequestParam(
        name = "end",
        defaultValue = "#{T(LocalDate).now().toString()}"
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
       

        resp.setContentType(Constants.CSV_CONTENT_TYPE);
        resp.setHeader(
        HttpHeaders.CONTENT_DISPOSITION,
        Constants.CSV_DISPOSITION
        );

        try (PrintWriter pw = resp.getWriter()) {
            pw.println("Title,Amount,Category,Date,Tags,Note");

            expSvc.list(
                userSvc.findByIdOrThrow(userId),
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
