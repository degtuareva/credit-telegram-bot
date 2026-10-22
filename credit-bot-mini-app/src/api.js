const API_BASE = "/api/mini-app";

async function request(path, options = {}) {
    const initData = window.Telegram?.WebApp?.initData;

    if (!initData) {
        throw new Error(
            "Откройте приложение через кнопку меню вашего бота в Telegram."
        );
    }

    const response = await fetch(`${API_BASE}${path}`, {
        ...options,

        headers: {
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            "X-Telegram-Init-Data": initData,
            ...options.headers,
        },
    });

    const body = await response.json().catch(() => null);

    if (!response.ok) {
        throw new Error(
            body?.message || `Ошибка запроса: ${response.status}`
        );
    }

    return body;
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
