import { useState } from "react";
import { compareCalculations } from "./api";
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

const emptyVariant = {
    amount: "500000",
    termMonths: "36",
    annualRate: "18.5",
    paymentType: "ANNUITY",
};

export default function LoanComparison() {
    const [first, setFirst] = useState(emptyVariant);
    const [second, setSecond] = useState({
        ...emptyVariant,
        amount: "700000",
        termMonths: "60",
        annualRate: "15.5",
    });

    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    function updateVariant(variant, event) {
        const { name, value } = event.target;

        const updater = variant === "first" ? setFirst : setSecond;

        updater((current) => ({
            ...current,
            [name]: value,
        }));
    }

    async function compare(event) {
        event.preventDefault();

        setError("");
        setResult(null);
        setLoading(true);

        try {
            const response = await compareCalculations(
                {
                    amount: first.amount.replace(",", "."),
                    termMonths: Number(first.termMonths),
                    annualRate: first.annualRate.replace(",", "."),
                    paymentType: first.paymentType,
                },
                {
                    amount: second.amount.replace(",", "."),
                    termMonths: Number(second.termMonths),
                    annualRate: second.annualRate.replace(",", "."),
                    paymentType: second.paymentType,
                }
            );

            setResult(response);
        } catch (exception) {
            setError(exception.message);
        } finally {
            setLoading(false);
        }
    }

    function variantFields(variant, title) {
        const value = variant === "first" ? first : second;

        return (
            <section className="card">
                <h2>{title}</h2>

                <label>Сумма, ₽</label>
                <input
                    name="amount"
                    type="number"
                    min="0.01"
                    step="0.01"
                    required
                    value={value.amount}
                    onChange={(event) => updateVariant(variant, event)}
                />

                <label>Срок, месяцев</label>
                <input
                    name="termMonths"
                    type="number"
                    min="1"
                    max="360"
                    step="1"
                    required
                    value={value.termMonths}
                    onChange={(event) => updateVariant(variant, event)}
                />

                <label>Годовая ставка, %</label>
                <input
                    name="annualRate"
                    type="number"
                    min="0"
                    max="100"
                    step="0.01"
                    required
                    value={value.annualRate}
                    onChange={(event) => updateVariant(variant, event)}
                />

                <label>Тип платежа</label>
                <select
                    name="paymentType"
                    value={value.paymentType}
                    onChange={(event) => updateVariant(variant, event)}
                >
                    <option value="ANNUITY">Аннуитетный</option>
                    <option value="DIFFERENTIAL">
                        Дифференцированный
                    </option>
                </select>
            </section>
        );
    }

    return (
        <section className="comparison">
            <form onSubmit={compare}>
                {variantFields("first", "Вариант 1")}

                {variantFields("second", "Вариант 2")}

                <button
                    className="primary"
                    type="submit"
                    disabled={loading}
                >
                    {loading ? "Сравниваем…" : "Сравнить кредиты"}
                </button>
            </form>

            {error && (
                <p className="message error" role="alert">
                    {error}
                </p>
            )}

            {result && (
                <section className="card">
                    <h2>Результат сравнения</h2>

                    <div className="table-scroll">
                        <table>
                            <thead>
                            <tr>
                                <th>Показатель</th>
                                <th>Вариант 1</th>
                                <th>Вариант 2</th>
                                <th>Разница</th>
                            </tr>
                            </thead>

                            <tbody>
                            <tr>
                                <td>Ежемесячный платёж</td>
                                <td>
                                    {money(result.firstMonthlyPayment)}
                                </td>
                                <td>
                                    {money(result.secondMonthlyPayment)}
                                </td>
                                <td>
                                    {money(
                                        result.monthlyPaymentDifference
                                    )}
                                </td>
                            </tr>

                            <tr>
                                <td>Переплата</td>
                                <td>
                                    {money(result.firstTotalInterest)}
                                </td>
                                <td>
                                    {money(result.secondTotalInterest)}
                                </td>
                                <td>
                                    {money(
                                        result.totalInterestDifference
                                    )}
                                </td>
                            </tr>

                            <tr>
                                <td>Общая выплата</td>
                                <td>
                                    {money(result.firstTotalPayment)}
                                </td>
                                <td>
                                    {money(result.secondTotalPayment)}
                                </td>
                                <td>
                                    {money(
                                        result.totalPaymentDifference
                                    )}
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>

                    <div className="summary">
                        <div>
                            <span>Дешевле по общей выплате</span>
                            <strong>
                                {result.cheaperByTotalPayment === "FIRST"
                                    ? "Вариант 1"
                                    : result.cheaperByTotalPayment === "SECOND"
                                        ? "Вариант 2"
                                        : "Одинаково"}
                            </strong>
                        </div>

                        <div>
                            <span>Ниже ежемесячный платёж</span>
                            <strong>
                                {result.lowerMonthlyPayment === "FIRST"
                                    ? "Вариант 1"
                                    : result.lowerMonthlyPayment === "SECOND"
                                        ? "Вариант 2"
                                        : "Одинаково"}
                            </strong>
                        </div>

                        <div>
                            <span>Меньше переплата</span>
                            <strong>
                                {result.lowerTotalInterest === "FIRST"
                                    ? "Вариант 1"
                                    : result.lowerTotalInterest === "SECOND"
                                        ? "Вариант 2"
                                        : "Одинаково"}
                            </strong>
                        </div>
                    </div>

                    <p className="muted">
                        Разница рассчитана как «Вариант 2 минус Вариант 1».
                        Отрицательное значение означает, что Вариант 2 дешевле.
                    </p>
                </section>
            )}
        </section>
    );
}