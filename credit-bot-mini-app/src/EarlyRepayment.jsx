import { useState } from "react";
import "./App.css";
import { calculateEarlyRepayment } from "./api";

const moneyFormatter = new Intl.NumberFormat("ru-RU", {
    style: "currency",
    currency: "RUB",
    maximumFractionDigits: 2,
});

function money(value) {
    return moneyFormatter.format(Number(value ?? 0));
}

export default function EarlyRepayment() {
    const [form, setForm] = useState({
        amount: "500000",
        termMonths: "36",
        annualRate: "18.5",
        paymentType: "ANNUITY",
        earlyPayment: "50000",
        earlyPaymentMonth: "6",
        strategy: "REDUCE_TERM",
    });

    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

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
        setResult(null);
        setLoading(true);

        try {
            const response = await calculateEarlyRepayment({
                amount: form.amount.replace(",", "."),
                termMonths: Number(form.termMonths),
                annualRate: form.annualRate.replace(",", "."),
                earlyPaymentType: form.paymentType,
                earlyPayment: form.earlyPayment.replace(",", "."),
                earlyPaymentMonth: Number(form.earlyPaymentMonth),
                strategy: form.strategy,
            });

            setResult(response);
        } catch (exception) {
            setError(exception.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <section className="card">
            <h2>Досрочное погашение</h2>

            <form onSubmit={calculate}>
                <label>Сумма кредита, ₽</label>
                <input
                    name="amount"
                    type="number"
                    min="100"
                    step="0.01"
                    required
                    value={form.amount}
                    onChange={updateField}
                />

                <label>Срок, месяцев</label>
                <input
                    name="termMonths"
                    type="number"
                    min="1"
                    max="360"
                    required
                    value={form.termMonths}
                    onChange={updateField}
                />

                <label>Годовая ставка, %</label>
                <input
                    name="annualRate"
                    type="number"
                    min="0"
                    max="100"
                    step="0.01"
                    required
                    value={form.annualRate}
                    onChange={updateField}
                />

                <label>Тип платежа</label>
                <select
                    name="paymentType"
                    value={form.paymentType}
                    onChange={updateField}
                >
                    <option value="ANNUITY">Аннуитетный</option>
                    <option value="DIFFERENTIAL">
                        Дифференцированный
                    </option>
                </select>

                <label>Сумма досрочного платежа, ₽</label>
                <input
                    name="earlyPayment"
                    type="number"
                    min="1"
                    step="0.01"
                    required
                    value={form.earlyPayment}
                    onChange={updateField}
                />

                <label>Внести в месяце</label>
                <input
                    name="earlyPaymentMonth"
                    type="number"
                    min="1"
                    max="360"
                    required
                    value={form.earlyPaymentMonth}
                    onChange={updateField}
                />

                <label>Что уменьшить?</label>
                <select
                    name="strategy"
                    value={form.strategy}
                    onChange={updateField}
                >
                    <option value="REDUCE_TERM">
                        Срок кредита
                    </option>

                    <option value="REDUCE_PAYMENT">
                        Ежемесячный платёж
                    </option>
                </select>

                <button
                    className="primary"
                    type="submit"
                    disabled={loading}
                >
                    {loading ? "Рассчитываем…" : "Рассчитать"}
                </button>
            </form>

            {error && (
                <p className="message error" role="alert">
                    {error}
                </p>
            )}

            {result && (
                <>
                    <h3>Результат</h3>

                    <div className="summary">
                        <div>
                            <span>Экономия по процентам</span>
                            <strong>
                                {money(result.interestSaved)}
                            </strong>
                        </div>

                        <div>
                            <span>Срок сокращён на</span>
                            <strong>
                                {result.monthsSaved} мес.
                            </strong>
                        </div>
                    </div>

                    <div className="table-scroll">
                        <table>
                            <thead>
                            <tr>
                                <th>Показатель</th>
                                <th>Без досрочного</th>
                                <th>С досрочным</th>
                            </tr>
                            </thead>

                            <tbody>
                            <tr>
                                <td>Ежемесячный платёж</td>
                                <td>
                                    {money(result.baseMonthlyPayment)}
                                </td>
                                <td>
                                    {money(result.updatedMonthlyPayment)}
                                </td>
                            </tr>

                            <tr>
                                <td>Срок</td>
                                <td>
                                    {result.baseTermMonths} мес.
                                </td>
                                <td>
                                    {result.updatedTermMonths} мес.
                                </td>
                            </tr>

                            <tr>
                                <td>Переплата</td>
                                <td>
                                    {money(result.baseTotalInterest)}
                                </td>
                                <td>
                                    {money(result.updatedTotalInterest)}
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </>
            )}
        </section>
    );
}