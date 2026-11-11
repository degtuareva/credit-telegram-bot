const API_BASE = "/api/mini-app";

const TOKEN_KEY = "creditBotToken";

export function saveToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}

export function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

export function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
}

function isTelegramApp() {
    return Boolean(window.Telegram?.WebApp?.initData);
}

async function request(path, options = {}, config = {}) {
    const {
        base = API_BASE,
        authRequired = true,
    } = config;

    const headers = {
        ...(options.body
            ? { "Content-Type": "application/json" }
            : {}),
        ...options.headers,
    };

    if (authRequired) {
        if (isTelegramApp()) {
            headers["X-Telegram-Init-Data"] =
                window.Telegram.WebApp.initData;
        } else {
            const token = getToken();

            if (!token) {
                throw new Error("Требуется вход в аккаунт.");
            }

            headers.Authorization = `Bearer ${token}`;
        }
    }

    const response = await fetch(`${base}${path}`, {
        ...options,
        headers,
    });

    const body = await response.json().catch(() => null);

    if (!response.ok) {
        if (authRequired && response.status === 401) {
            clearToken();
        }

        const exception = new Error(
            body?.message ||
            body?.detail ||
            `Ошибка запроса: ${response.status}`
        );

        exception.status = response.status;
        throw exception;
    }

    return body;
}

export function register(input) {
    return request(
        "/register",
        {
            method: "POST",
            body: JSON.stringify(input),
        },
        {
            base: "/api/auth",
            authRequired: false,
        }
    );
}

export function login(input) {
    return request(
        "/login",
        {
            method: "POST",
            body: JSON.stringify(input),
        },
        {
            base: "/api/auth",
            authRequired: false,
        }
    );
}

export function loadMe() {
    return request("/me");
}

export function loadHistory() {
    return request("/history");
}

export function createCalculation(input) {
    return request("/calculations", {
        method: "POST",
        body: JSON.stringify(input),
    });
}

export function compareCalculations(first, second) {
    return request("/calculations/compare", {
        method: "POST",
        body: JSON.stringify({
            first,
            second,
        }),
    });
}

export function loadManagerAnalytics(from, to) {
    const params = new URLSearchParams({ from, to });

    return request(`/manager/analytics?${params.toString()}`);
}

export function deleteCalculation(id) {
    return request(`/calculation/${id}`, {
        method: "DELETE",
    });
}

export function calculateEarlyRepayment(input) {
    return request("/calculations/early-repayment", {
        method: "POST",
        body: JSON.stringify(input),
    });
}