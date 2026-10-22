package com.example.creditbot.message;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.domain.Payment;
import com.example.creditbot.domain.PaymentType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Формирует текстовые ответы Telegram-бота.
 */
@Component
public class BotMessageFormatter {

    public String startMessage() {
        return """
                👋 Привет! Я бот для расчёта графика погашения кредита.

                Выберите действие:
                """;
    }

    public String helpMessage() {
        return """
                📖 Как пользоваться ботом:

                1. Нажмите «Рассчитать кредит» или отправьте /calculate.
                2. Введите сумму кредита.
                3. Введите срок в месяцах.
                4. Введите годовую ставку.
                5. Выберите тип платежа с помощью кнопок.

                В любой момент можно отменить действие командой /cancel.
                """;
    }

    public String formatHistory(List<CreditRequest> history) {
        if (history.isEmpty()) {
            return "📭 У вас пока нет сохранённых запросов.";
        }

        StringBuilder result = new StringBuilder(
                "📜 История запросов:\n\n"
        );

        for (int index = 0; index < history.size(); index++) {
            CreditRequest request = history.get(index);

            result.append(index + 1)
                    .append(". ")
                    .append(request.amount())
                    .append(" руб., срок: ")
                    .append(request.termMonths())
                    .append(" мес., ставка: ")
                    .append(request.annualRate())
                    .append("%, тип: ")
                    .append(formatPaymentType(request.paymentType()))
                    .append(", дата: ")
                    .append(request.createdAt())
                    .append("\n");
        }

        return result.toString();
    }

    public String formatAnalytics(
            long totalRequests,
            Map<PaymentType, Long> paymentTypes,
            Map<Integer, Long> terms
    ) {
        String popularTerm = terms.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(entry ->
                        entry.getKey()
                                + " мес. ("
                                + entry.getValue()
                                + " запросов)"
                )
                .orElse("нет данных");

        return """
                📊 Аналитика по кредитным запросам:

                Всего запросов: %d

                Популярный срок: %s

                Аннуитетный тип: %d
                Дифференцированный тип: %d
                """.formatted(
                totalRequests,
                popularTerm,
                paymentTypes.getOrDefault(
                        PaymentType.ANNUITY,
                        0L
                ),
                paymentTypes.getOrDefault(
                        PaymentType.DIFFERENTIAL,
                        0L
                )
        );
    }

    public String formatSchedule(CreditSchedule schedule) {
        StringBuilder result = new StringBuilder();

        result.append("📋 График платежей\n\n")
                .append("Переплата: ")
                .append(schedule.totalInterest())
                .append(" руб.\n")
                .append("Общая сумма выплат: ")
                .append(schedule.totalPayment())
                .append(" руб.\n\n");

        for (Payment payment : schedule.payments()) {
            result.append("Месяц ")
                    .append(payment.month())
                    .append(": ")
                    .append(payment.totalPayment())
                    .append(" руб. ")
                    .append("(тело: ")
                    .append(payment.principal())
                    .append(", проценты: ")
                    .append(payment.interest())
                    .append(", остаток: ")
                    .append(payment.remainingDebt())
                    .append(")\n");
        }

        return result.toString();
    }

    private String formatPaymentType(PaymentType paymentType) {
        return switch (paymentType) {
            case ANNUITY -> "аннуитетный";
            case DIFFERENTIAL -> "дифференцированный";
        };
    }
}