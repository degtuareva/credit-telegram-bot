import { useEffect, useState } from "react";
import ManagerAnalytics from "./ManagerAnalytics.jsx";
import {
    createCalculation,
    loadHistory,
    loadMe,
} from "./api";
import "./App.css";

const moneyFormatter = new Intl.NumberFormat("ru-RU", {
    style: "currency",
    currency: "RUB",
    maximumFractionDigits: 2,
});

function money(value) {
    return moneyFormatter.format(Number(value ?? 0));
}

function paymentTypeLabel(value) {
    const labels = {
        ANNUITY: "Аннуитетный",
        DIFFERENTIAL: "Дифференцированный",
    };

    return labels[value] ?? "Неизвестный тип платежа";
}

export default function App() {
    const [tab, setTab] = useState("calculator");

    const [form, setForm] = useState({
        amount: "500000",
        termMonths: "36",
        annualRate: "18.5",
        paymentType: "ANNUITY",
    });

    const [user, setUser] = useState(null);
    const [schedule, setSchedule] = useState(null);
    const [history, setHistory] = useState([]);

    const [loading, setLoading] = useState(false);
    const [authLoading, setAuthLoading] = useState(true);

    const [error, setError] = useState("");
    const [notice, setNotice] = useState("");

    useEffect(() => {
        const telegram = window.Telegram?.WebApp;

        telegram?.ready();
        telegram?.expand();

        let active = true;

        async function initialize() {
            try {
                const currentUser = await loadMe();

                if (active) {
                    setUser(currentUser);
                }
            } catch (exception) {
                if (active) {
                    setError(exception.message);
                }
            } finally {
                if (active) {
                    setAuthLoading(false);
                }
            }
        }

        initialize();

        return () => {
            active = false;
        };
    }, []);

    function updateField(event) {
        const { name, value } = event.target;

        setForm((current) => ({
            ...current,
            [name]: value,
        }));
    }

    async function calculate(event) {
        event.preventDefault();

        setError("");
        setNotice("");
        setLoading(true);

        try {
            const result = await createCalculation({
                amount: form.amount.replace(",", "."),
                termMonths: Number(form.termMonths),
                annualRate: form.annualRate.replace(",", "."),
                paymentType: form.paymentType,
            });

            setSchedule(result);
            setNotice("Расчёт выполнен и сохранён.");
        } catch (exception) {
            setError(exception.message);
        } finally {
            setLoading(false);
        }
    }

    async function showHistory() {
        setTab("history");
        setError("");
        setNotice("");
        setLoading(true);

        try {
            setHistory(await loadHistory());
        } catch (exception) {
            setError(exception.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <main className="app">
            <header className="app-header">
                <p className="eyebrow">Финансы под контролем</p>
                <h1>Кредитный помощник</h1>

                <p className="muted">
                    {user
                        ? `Здравствуйте, ${user.firstName || "пользователь"}! `
                        : ""}
                    Рассчитайте платёж и оцените переплату до оформления кредита.
                </p>
            </header>

            {authLoading && (
                <p className="status">Проверяем вход через Telegram…</p>
            )}

            {error && (
                <p className="message error" role="alert">
                    {error}
                </p>
            )}

            {notice && (
                <p className="message success" role="status">
                    {notice}
                </p>
            )}

            {!authLoading && !user && (
                <section className="card">
                    <h2>Вход через Telegram</h2>
                    <p>
                        Интерфейс открыт, но защищённые данные недоступны.
                        Запустите приложение через кнопку меню бота.
                    </p>
                </section>
            )}

            <nav className="tabs" aria-label="Разделы приложения">
                <button
                    type="button"
                    className={tab === "calculator" ? "active" : ""}
                    disabled={loading}
                    onClick={() => setTab("calculator")}
                >
                    Калькулятор
                </button>

                <button
                    type="button"
                    className={tab === "history" ? "active" : ""}
                    disabled={!user || loading}
                    onClick={showHistory}
                >
                    История
                </button>
                {user?.manager && (
                    <button
                        type="button"
                        className={tab === "analytics" ? "active" : ""}
                        disabled={loading}
                        onClick={() => {
                            setTab("analytics");
                            setError("");
                            setNotice("");
                        }}
                    >
                        Аналитика
                    </button>
                )}
            </nav>

            {tab === "calculator" && (
                <>
                    <form className="card" onSubmit={calculate}>
                        <h2>Параметры кредита</h2>

                        <label htmlFor="amount">Сумма, ₽</label>
                        <input
                            id="amount"
                            name="amount"
                            type="number"
                            min="0.01"
                            max="1000000000"
                            step="0.01"
                            required
                            value={form.amount}
                            onChange={updateField}
                        />

                        <label htmlFor="termMonths">Срок, месяцев</label>
                        <input
                            id="termMonths"
                            name="termMonths"
                            type="number"
                            min="1"
                            max="360"
                            step="1"
                            required
                            value={form.termMonths}
                            onChange={updateField}
                        />

                        <label htmlFor="annualRate">Годовая ставка, %</label>
                        <input
                            id="annualRate"
                            name="annualRate"
                            type="number"
                            min="0"
                            max="100"
                            step="0.01"
                            required
                            value={form.annualRate}
                            onChange={updateField}
                        />

                        <label htmlFor="paymentType">Тип платежа</label>
                        <select
                            id="paymentType"
                            name="paymentType"
                            value={form.paymentType}
                            onChange={updateField}
                        >
                            <option value="ANNUITY">Аннуитетный</option>
                            <option value="DIFFERENTIAL">
                                Дифференцированный
                            </option>
                        </select>

                        <button
                            className="primary"
                            type="submit"
                            disabled={!user || loading}
                        >
                            {loading ? "Рассчитываем…" : "Рассчитать и сохранить"}
                        </button>
                    </form>

                    {schedule && (
                        <section className="card">
                            <h2>Результат</h2>

                            <div className="summary">
                                <div>
                                    <span>Переплата</span>
                                    <strong>{money(schedule.totalInterest)}</strong>
                                </div>

                                <div>
                                    <span>Общая выплата</span>
                                    <strong>{money(schedule.totalPayment)}</strong>
                                </div>
                            </div>

                            <h3>График платежей</h3>

                            <div className="table-scroll">
                                <table>
                                    <thead>
                                    <tr>
                                        <th>Месяц</th>
                                        <th>Платёж</th>
                                        <th>Тело</th>
                                        <th>Проценты</th>
                                        <th>Остаток</th>
                                    </tr>
                                    </thead>

                                    <tbody>
                                    {(schedule.payments ?? []).map((payment) => (
                                        <tr key={payment.month}>
                                            <td>{payment.month}</td>
                                            <td>{money(payment.totalPayment)}</td>
                                            <td>{money(payment.principal)}</td>
                                            <td>{money(payment.interest)}</td>
                                            <td>{money(payment.remainingDebt)}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        </section>
                    )}
                </>
            )}

            {tab === "history" && (
                <section className="card">
                    <div className="section-heading">
                        <h2>Мои расчёты</h2>

                        <button
                            type="button"
                            disabled={loading || !user}
                            onClick={showHistory}
                        >
                            Обновить
                        </button>
                    </div>

                    <p className="muted">
                        Последние 50 запросов из бота и Mini App.
                    </p>

                    {loading ? (
                        <p>Загружаем…</p>
                    ) : history.length === 0 ? (
                        <p>Сохранённых расчётов пока нет.</p>
                    ) : (
                        <div className="history-list">
                            {history.map((item, index) => (
                                <article
                                    className="history-item"
                                    key={`${item.id}-${index}`}
                                >
                                    <h3>{money(item.amount)}</h3>

                                    <p>
                                        {item.termMonths} мес. · {item.annualRate}%
                                    </p>

                                    <p>{paymentTypeLabel(item.paymentType)}</p>

                                    <p className="muted">
                                        {String(item.createdAt ?? "").replace("T", " ")}
                                    </p>
                                </article>
                            ))}
                        </div>
                    )}
                </section>
            )}
            {tab === "analytics" && user?.manager && (
                <ManagerAnalytics />
            )}

            <footer className="muted">
                Расчёт носит справочный характер и не является
                предложением банка.
            </footer>
        </main>
    );
}