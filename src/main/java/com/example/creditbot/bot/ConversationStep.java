package com.example.creditbot.bot;

/**
 * Шаг пошагового диалога с пользователем.
 */
public enum ConversationStep {

    /**
     * Диалог не запущен.
     */
    NONE,

    /**
     * Ожидается сумма кредита.
     */
    WAITING_AMOUNT,

    /**
     * Ожидается срок кредита.
     */
    WAITING_TERM,

    /**
     * Ожидается годовая процентная ставка.
     */
    WAITING_RATE,

    /**
     * Ожидается выбор типа платежа.
     */
    WAITING_PAYMENT_TYPE,

    /**
     * Ожидается пароль менеджера.
     */
    WAITING_MANAGER_PASSWORD

}