import axios from 'axios';

const API_URL = 'http://localhost:8080/api/v1';

const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

api.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

api.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('token');
            window.location = '/login';
        }
        return Promise.reject(error);
    }
);

export const authenticate = async (login, password) => {
    try {
        const response = await api.post('/auth/authenticate', { login, password });
        return response.data;
    } catch (error) {
        console.error('Ошибка аутентификации:', error);
        throw error;
    }
};

export const getChatList = async () => {
    try {
        const response = await api.get('/admin/chats');
        return response.data;
    } catch (error) {
        console.error('Ошибка получения списка чатов:', error);
        throw error;
    }
};

export const getChat = async (chatId) => {
    try {
        const response = await api.get(`/admin/chats/${chatId}`);
        return response.data;
    } catch (error) {
        console.error('Ошибка получения чата:', error);
        throw error;
    }
};

export const getDeletedMessages = async (chatId, page = 1) => {
    try {
        const response = await api.get(`/admin/chats/${chatId}/deleted-messages?page=${page}`);
        return response.data;
    } catch (error) {
        console.error('Ошибка получения удаленных сообщений:', error);
        throw error;
    }
};

export const getUsers = async (page = 1, size = null) => {
    try {
        const safePage = Math.max(1, Number(page));
        const params = { page: safePage };
        if (size !== null) {
            params.size = size;
        }
        const response = await api.get('/admin/users', { params });
        return {
            users: response.data.content || [],
            pagination: response.data.page || {
                size: size || 20,
                number: safePage,
                totalElements: 0,
                totalPages: 0
            }
        };
    } catch (error) {
        console.error('Ошибка получения списка пользователей:', error.response?.data || error);
        throw error;
    }
}

export const getUser = async (userId) => {
    try {
        const response = await api.get(`/admin/users/${userId}`);
        return response.data;
    } catch (error) {
        console.error('Ошибка получения пользователя:', error);
        throw error;
    }
};

export const getTextModerationSettings = async (chatId) => {
    try {
        const response = await api.get(`/admin/settings/chats/${chatId}/text-moderation`);
        return response.data;
    } catch (error) {
        console.error('Ошибка получения настроек модерации:', error);
        throw error;
    }
};

export const updateTextModerationSettings = async (chatId, filterType, settings) => {
    const allowedFilterTypes = [
        "emails", "tags", "mentions", "links",
        "phone-numbers", "bot-commands", "custom-emojis"
    ];

    if (!allowedFilterTypes.includes(filterType)) {
        throw new Error(`Неверный тип фильтра: ${filterType}. Доступные типы: ${allowedFilterTypes.join(', ')}`);
    }

    if (settings.enabled === undefined ||
        settings.exclusionMode === undefined ||
        settings.exclusions === undefined) {
        throw new Error('Для обновления настроек требуются поля: enabled, exclusionMode, exclusions');
    }

    try {
        const response = await api.patch(
            `/admin/settings/chats/${chatId}/text-moderation/${filterType}`,
            settings
        );
        return response.data;
    } catch (error) {
        console.error(`Ошибка обновления настроек модерации для ${filterType}:`, error);
        throw error;
    }
};

export const getSpamProtectionSettings = async (chatId) => {
    try {
        const response = await api.get(`/admin/settings/chats/${chatId}/spam-protection`);
        return response.data;
    } catch (error) {
        console.error('Ошибка получения настроек защиты от спама:', error);
        throw error;
    }
};

export const updateSpamProtectionSettings = async (chatId, settings) => {
    try {
        const response = await api.patch(
            `/admin/settings/chats/${chatId}/spam-protection`,
            settings
        );
        return response.data;
    } catch (error) {
        console.error('Ошибка обновления настроек защиты от спама:', error);
        throw error;
    }
};