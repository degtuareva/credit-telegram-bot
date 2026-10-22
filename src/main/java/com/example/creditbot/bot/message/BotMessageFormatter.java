package com.example.creditbot.bot.message;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.domain.Payment;
import com.example.creditbot.domain.PaymentType;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Формирует текст сообщений.
 */
@Component
public class BotMessageFormatter {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public String startMessage() {
        return """
                👋 Привет! Я бот для расчёта графика погашения кредита.

                Выберите действие:
                """;
    }

    public String helpMessage() {
        return """
                📖 Как пользоваться ботом:

                1. Отправьте /calculate или нажмите «Рассчитать кредит».
                2. Введите сумму кредита.
                3. Введите срок в месяцах.
                4. Введите годовую ставку.
                5. Выберите тип платежа.

                Команды:
                /start — главное меню
                /calculate — новый расчёт
                /history — история запросов
                /cancel — отменить текущий диалог
                /help — помощь

                Для менеджера:
                /manager_login — вход
                /analytics — аналитика
                /manager_logout — выход
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
                    .append(". Сумма: ")
                    .append(request.amount().toPlainString())
                    .append(" руб.\n")
                    .append("Срок: ")
                    .append(request.termMonths())
                    .append(" мес.\n")
                    .append("Ставка: ")
                    .append(request.annualRate().toPlainString())
                    .append("%\n")
                    .append("Тип: ")
                    .append(formatPaymentType(request.paymentType()))
                    .append("\n")
                    .append("Дата: ")
                    .append(request.createdAt().format(DATE_FORMAT))
                    .append("\n\n");
        }

        return result.toString();
    }

    public String formatAnalytics(
            long totalRequests,
            Map<PaymentType, Long> paymentTypes,
            Map<Integer, Long> terms
    ) {
        // При одинаковой популярности выбираем меньший срок.
        Comparator<Map.Entry<Integer, Long>> termComparator =
                Comparator
                        .<Map.Entry<Integer, Long>>comparingLong(
                                entry -> entry.getValue()
                        )
                        .thenComparing(
                                entry -> entry.getKey(),
                                Comparator.reverseOrder()
                        );

        String popularTerm = terms.entrySet()
                .stream()
                .max(termComparator)
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
                .append(schedule.totalInterest().toPlainString())
                .append(" руб.\n")
                .append("Общая сумма выплат: ")
                .append(schedule.totalPayment().toPlainString())
                .append(" руб.\n\n");

        for (Payment payment : schedule.payments()) {
            result.append("Месяц ")
                    .append(payment.month())
                    .append(": ")
                    .append(payment.totalPayment().toPlainString())
                    .append(" руб.\n")
                    .append("Тело: ")
                    .append(payment.principal().toPlainString())
                    .append(", проценты: ")
                    .append(payment.interest().toPlainString())
                    .append(", остаток: ")
                    .append(payment.remainingDebt().toPlainString())
                    .append("\n\n");
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