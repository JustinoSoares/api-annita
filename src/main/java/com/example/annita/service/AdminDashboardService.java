package com.example.annita.service;

import com.example.annita.dto.AdminDashboardResponse;
import com.example.annita.repository.CategoryRepository;
import com.example.annita.repository.EventRepository;
import com.example.annita.repository.NewsletterSubscriptionRepository;
import com.example.annita.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class AdminDashboardService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;

    public AdminDashboardService(EventRepository eventRepository, UserRepository userRepository, CategoryRepository categoryRepository, NewsletterSubscriptionRepository newsletterSubscriptionRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.newsletterSubscriptionRepository = newsletterSubscriptionRepository;
    }

    public AdminDashboardResponse getDashboard() {
        LocalDate now = LocalDate.now();
        LocalDate startOfCurrentMonth = now.withDayOfMonth(1);
        LocalDate startOfLastMonth = startOfCurrentMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfCurrentMonth.minusDays(1);

        LocalDateTime startCurrentMonth = startOfCurrentMonth.atStartOfDay();
        LocalDateTime startLastMonth = startOfLastMonth.atStartOfDay();
        LocalDateTime endLastMonth = endOfLastMonth.atTime(LocalTime.MAX);

        long totalEvents = eventRepository.count();
        long currentMonthEvents = eventRepository.countByCreatedAtBetween(startCurrentMonth, LocalDateTime.now());
        long lastMonthEvents = eventRepository.countByCreatedAtBetween(startLastMonth, endLastMonth);

        long totalUsers = userRepository.count();
        long currentMonthUsers = userRepository.countByCreatedAtBetween(startCurrentMonth, LocalDateTime.now());
        long lastMonthUsers = userRepository.countByCreatedAtBetween(startLastMonth, endLastMonth);

        long totalCategories = categoryRepository.count();
        long currentMonthCategories = categoryRepository.countByCreatedAtBetween(startCurrentMonth, LocalDateTime.now());
        long lastMonthCategories = categoryRepository.countByCreatedAtBetween(startLastMonth, endLastMonth);

        long totalSubscribers = newsletterSubscriptionRepository.count();
        long currentMonthSubscribers = newsletterSubscriptionRepository.countByCreatedAtBetween(startCurrentMonth, LocalDateTime.now());
        long lastMonthSubscribers = newsletterSubscriptionRepository.countByCreatedAtBetween(startLastMonth, endLastMonth);

        AdminDashboardResponse response = new AdminDashboardResponse();
        response.setEvents(buildMetric(totalEvents, currentMonthEvents, lastMonthEvents));
        response.setUsers(buildMetric(totalUsers, currentMonthUsers, lastMonthUsers));
        response.setCategories(buildMetric(totalCategories, currentMonthCategories, lastMonthCategories));
        response.setSubscribers(buildMetric(totalSubscribers, currentMonthSubscribers, lastMonthSubscribers));

        return response;
    }

    private AdminDashboardResponse.MetricItem buildMetric(long total, long currentMonth, long lastMonth) {
        double changePercentage;
        if (lastMonth == 0) {
            changePercentage = currentMonth > 0 ? 100.0 : 0.0;
        } else {
            changePercentage = ((double) (currentMonth - lastMonth) / lastMonth) * 100.0;
        }
        changePercentage = Math.round(changePercentage * 100.0) / 100.0;
        return new AdminDashboardResponse.MetricItem(total, currentMonth, lastMonth, changePercentage);
    }
}
