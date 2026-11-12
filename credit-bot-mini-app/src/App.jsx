import {useEffect, useState} from "react";

import ManagerAnalytics from "./ManagerAnalytics.jsx";
import EarlyRepayment from "./EarlyRepayment.jsx";
import Login from "./Login.jsx";
import LoanComparison from "./LoanComparison.jsx"

import {clearToken, createCalculation, deleteCalculation, getToken, loadHistory, loadMe,} from "./api";

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
    const isTelegramApp = Boolean(
        window.Telegram?.WebApp?.initData
    );

    useEffect(() => {
        const telegram = window.Telegram?.WebApp;

        telegram?.ready();
        telegram?.expand();

        let active = true;

        async function initialize() {
            const hasTelegramData = Boolean(
                telegram?.initData
            );

            if (!hasTelegramData && !getToken()) {
                if (active) {
                    setAuthLoading(false);
                }

                return;
            }
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

    useEffect(() => {
        console.info("[App] Состояние", {
            tab,
            authenticated: Boolean(user),
            loading,
            authLoading,
            calculationDisabled:
                !user || loading || authLoading,
        });
    }, [tab, user, loading, authLoading]);

    function handleLoginSuccess(currentUser) {
        setUser(currentUser);
        setSchedule(null);
        setHistory([]);
        setTab("calculator");

        setError("");
        setNotice("Вы вошли в приложение.");
        console.info("[Auth] Вход выполнен.");
    }

    function logout() {
        clearToken();

        setUser(null);
        setSchedule(null);
        setHistory([]);
        setTab("calculator");

        setError("");
        setNotice(
            isTelegramApp
                ? "Локальное состояние очищено. При повторном открытии Mini App возможен автоматический вход через Telegram."
                : "Вы вышли из аккаунта."
        );

    console.info("[Auth] Выполнен выход")
}


function updateField(event) {
    const {name, value} = event.target;

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

async function removeCalculation(id) {
    setError("");
    setNotice("");
    setLoading(true);

    try {
        await deleteCalculation(id);

        setHistory((current) =>
            current.filter((item) => item.id !== id)
        );

        setNotice("Расчёт удалён.");
    } catch (exception) {
        setError(exception.message);
    } finally {
        setLoading(false);
    }
}

function repeatCalculation(item) {
    setForm({
        amount: String(item.amount),
        termMonths: String(item.termMonths),
        annualRate: String(item.annualRate),
        paymentType: item.paymentType,
    });

    setTab("calculator");
    setSchedule(null);
    setNotice("Параметры прошлого расчёта подставлены.");
}

async function exportHistory() {
    setError("");
    setNotice("");
    setLoading(true);

    try {
        const token = getToken();

        if (!token) {
            throw new Error("Требуется вход в аккаунт.");
        }

        const response = await fetch(
            "/api/mini-app/history/export",
            {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            }
        );

        if (!response.ok) {
            throw new Error(
                `Не удалось скачать файл: ${response.status}`
            );
        }

        const blob = await response.blob();
        const url = URL.createObjectURL(blob);

        const link = document.createElement("a");
        link.href = url;
        link.download = "credit-history.csv";
        link.click();

        URL.revokeObjectURL(url);

        setNotice("История сохранена в CSV.");
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
            {user && !window.Telegram?.WebApp?.initData && (
                <button
                    type="button"
                    disabled={loading}
                    onClick={logout}
                >
                    Выйти из аккаунта
                </button>
            )}
        </header>

        {authLoading && (
            <p className="status">Проверяем вход…</p>
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
            <Login
                onSuccess={(currentUser) => {
                    setUser(currentUser);
                    setError("");
                    setNotice("Вы вошли в приложение.");
                }}
            />
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
                className={tab === "comparison" ? "active" : ""}
                disabled={loading}
                onClick={() => {
                    setTab("comparison");
                    setError("");
                    setNotice("");
                }}
            >
                Сравнение
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

            <button
                type="button"
                className={tab === "early" ? "active" : ""}
                disabled={loading}
                onClick={() => {
                    setTab("early");
                    setError("");
                    setNotice("");
                }}
            >
                Досрочно
            </button>
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
                        {loading
                            ? "Рассчитываем…"
                            : "Рассчитать и сохранить"}
                    </button>
                </form>

                {schedule && (
                    <section className="card">
                        <h2>Результат</h2>

                        <div className="summary">
                            <div>
                                <span>Переплата</span>
                                <strong>
                                    {money(schedule.totalInterest)}
                                </strong>
                            </div>

                            <div>
                                <span>Общая выплата</span>
                                <strong>
                                    {money(schedule.totalPayment)}
                                </strong>
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
                                {(schedule.payments ?? []).map(
                                    (payment) => (
                                        <tr key={payment.month}>
                                            <td>
                                                {payment.month}
                                            </td>
                                            <td>
                                                {money(
                                                    payment.totalPayment
                                                )}
                                            </td>
                                            <td>
                                                {money(
                                                    payment.principal
                                                )}
                                            </td>
                                            <td>
                                                {money(
                                                    payment.interest
                                                )}
                                            </td>
                                            <td>
                                                {money(
                                                    payment.remainingDebt
                                                )}
                                            </td>
                                        </tr>
                                    )
                                )}
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
                                    {item.termMonths} мес. ·{" "}
                                    {item.annualRate}%
                                </p>

                                <p>
                                    {paymentTypeLabel(
                                        item.paymentType
                                    )}
                                </p>

                                <p className="muted">
                                    {String(item.createdAt ?? "")
                                        .replace("T", " ")}
                                </p>

                                <div className="history-actions">
                                    <button
                                        type="button"
                                        disabled={loading || !user}
                                        onClick={() =>
                                            repeatCalculation(item)
                                        }
                                    >
                                        Повторить
                                    </button>

                                    <button
                                        type="button"
                                        disabled={loading || !user}
                                        onClick={() =>
                                            removeCalculation(item.id)
                                        }
                                    >
                                        Удалить
                                    </button>
                                </div>
                            </article>
                        ))}
                    </div>
                )}

                <button
                    className="primary"
                    type="button"
                    disabled={loading || !user}
                    onClick={exportHistory}
                >
                    Скачать CSV
                </button>
            </section>
        )}

        {tab === "comparison" && (
            <LoanComparison/>
        )}

        {tab === "analytics" && user?.manager && (
            <ManagerAnalytics/>
        )}

        {tab === "early" && (
            <EarlyRepayment/>
        )}

        <footer className="muted">
            Расчёт носит справочный характер и не является
            предложением банка.
        </footer>
    </main>
);
}