import { useState } from "react";
import { loadManagerAnalytics } from "./api";

const moneyFormatter = new Intl.NumberFormat("ru-RU", {
    style: "currency",
    currency: "RUB",
    maximumFractionDigits: 2,
});

function localDateString(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

function initialFrom() {
    const date = new Date();
    date.setDate(date.getDate() - 6);
    return localDateString(date);
}

export default function ManagerAnalytics() {
    const [from, setFrom] = useState(initialFrom);
    const [to, setTo] = useState(() => localDateString(new Date()));
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    async function refresh(event) {
        event.preventDefault();

        setLoading(true);
        setError("");
        setData(null);

        try {
            const result = await loadManagerAnalytics(from, to);
            setData(result);
        } catch (exception) {
            setError(exception.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <section className="card">
            <h2>Менеджерская аналитика</h2>

            <p className="muted">
                Перед загрузкой статистики войдите как менеджер
                в чате бота. После этого нажмите «Показать».
            </p>

            <form onSubmit={refresh}>
                <label htmlFor="analytics-from">С даты</label>
                <input
                    id="analytics-from"
                    type="date"
                    required
                    value={from}
                    disabled={loading}
                    onChange={(event) => setFrom(event.target.value)}
                />

                <label htmlFor="analytics-to">По дату включительно</label>
                <input
                    id="analytics-to"
                    type="date"
                    required
                    min={from}
                    value={to}
                    disabled={loading}
                    onChange={(event) => setTo(event.target.value)}
                />

                <button
                    type="submit"
                    className="primary"
                    disabled={loading}
                >
                    {loading ? "Загружаем…" : "Показать"}
                </button>
            </form>

            {error && (
                <p className="message error" role="alert">
                    {error}
                </p>
            )}

            {data && (
                <>
                    <h3>Сводка за период</h3>

                    <div className="summary">
                        <div>
                            <span>Расчётов</span>
                            <strong>{data.totalRequests}</strong>
                        </div>

                        <div>
                            <span>Уникальных пользователей</span>
                            <strong>{data.uniqueUsers}</strong>
                        </div>

                        <div>
                            <span>Средняя сумма расчёта</span>
                            <strong>
                                {data.totalRequests === 0
                                    ? "Нет данных"
                                    : moneyFormatter.format(
                                        Number(data.averageAmount)
                                    )}
                            </strong>
                        </div>

                        <div>
                            <span>Средний срок</span>
                            <strong>
                                {data.totalRequests === 0
                                    ? "Нет данных"
                                    : `${data.averageTermMonths} мес.`}
                            </strong>
                        </div>
                    </div>

                    <h3>Типы платежей</h3>

                    <p>
                        Аннуитетные: {data.paymentTypes?.ANNUITY ?? 0}
                    </p>

                    <p>
                        Дифференцированные:{" "}
                        {data.paymentTypes?.DIFFERENTIAL ?? 0}
                    </p>

                    <h3>Расчёты по дням</h3>

                    <div className="table-scroll">
                        <table>
                            <thead>
                            <tr>
                                <th>Дата</th>
                                <th>Количество</th>
                            </tr>
                            </thead>

                            <tbody>
                            {Object.entries(data.dailyCounts ?? {})
                                .sort(([a], [b]) => a.localeCompare(b))
                                .map(([date, count]) => (
                                    <tr key={date}>
                                        <td>{date}</td>
                                        <td>{count}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </>
            )}
        </section>
    );
}