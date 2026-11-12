import {useState} from "react";
import {login,register,saveToken} from "./api.js";

export default function Login({onSuccess}){
    const [mode,setMode]=useState("login");
    const [loading,setLoading]=useState(false);
    const[error,setError]=useState("");

    const [form,setForm]=useState({
        email: "",
        password: "",
        firstName: "",
        lastName: "",
    });

    const registering=mode==="register";

    function  updateField(event){
        const {name,value}=event.target;
        setForm((current)=>({
            ...current,
            [name]:value,
        }));
    }
    function switchMode(){
        setMode((current)=>
        current==="login"?"register":"login"
        );
        setError("");
    }
    async function submit(event){
        event.preventDefault();
        setError("");
        setLoading(true);
        try {
            const credentials={
                email: form.email.trim(),
                password:form.password,
            };
           const response=registering
           ?await register({
                   ...credentials,
                   firstName:form.firstName.trim(),
                   lastName:form.lastName.trim(),
               })
               :await login(credentials);
           if(!response?.token){
               throw new Error(
                   "Сервер не вернул токен авторизации."
               );
           }
           saveToken(response.token);
        }catch (exception){
            setError(exception.message);
        }finally {
            setLoading(false);
        }
    }
    return(
        <section className="card">
            <h2>
                {registering?"Регистрация":"Вход в приложение"}
            </h2>
            <p className="muted">
                Войдите по email и паролю.
                Открывать Telegram для этого не нужно.
            </p>

            {error&&(
                <p className="message error" role="alert">
                    {error}
                </p>
            )}
            <form onSubmit={submit}>
                {registering && (
                    <>
                        <label htmlFor="auth-first-name">
                            Имя
                        </label>
                        <input
                            id="auth-first-name"
                            name="firstName"
                            autoComplete="given-name"
                            required
                            value={form.firstName}
                            onChange={updateField}
                        />
                        <label htmlFor="auth-first-name">
                            Фамилия
                        </label>
                        <input
                            id="auth-first-name"
                            name="lastName"
                            autoComplete="family-name"
                            value={form.lastName}
                            onChange={updateField}
                        />
                    </>
                )}
                <label htmlFor="auth-email">Email</label>
                <input
                    id="auth-email"
                    name="email"
                    autoComplete="email"
                    required
                    value={form.email}
                    onChange={updateField}
                />
                <label htmlFor="auth-password">Пароль</label>
                <input
                    id="auth-password"
                    name="password"
                    type="password"
                    autoComplete={
                    registering
                        ?"new-password"
                        :"current-password"
                    }
                    required
                    value={form.password}
                    onChange={updateField}
                />
                <button
                    className="primary"
                    type="submit"
                    disabled={loading}
                    >
                    {loading
                    ?"Подождите..."
                    :registering
                    ?"Создать аккаунт"
                    :"Войти"}
                </button>
            </form>

            <button
                type="button"
                disabled={loading}
                onClick={switchMode}
                >
                {registering
                ?"Уже есть аккаунт? Войти"
                :"Нет аккаунта? Зарегистрироваться"}
            </button>
        </section>
    )
}