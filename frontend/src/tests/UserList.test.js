const React = require('react');
const { render, screen, fireEvent, waitFor } = require('@testing-library/react');
require('@testing-library/jest-dom');

jest.mock('react-router-dom', () => {
    const React = require('react');
    return {
        BrowserRouter: ({ children }) => React.createElement(React.Fragment, null, children),
        Link: ({ children, to, ...rest }) =>
            React.createElement('a', { href: to, ...rest }, children),
    };
}, { virtual: true });

jest.mock('../services/api', () => ({
    getUsers: jest.fn(),
}));
const { getUsers } = require('../services/api');

const UsersList = require('../pages/UsersList').default;

describe('UsersList', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    const renderWithRouter = (ui) => {
        const { BrowserRouter } = require('react-router-dom');
        return render(React.createElement(BrowserRouter, null, ui));
    };

    test('показывает индикатор загрузки и затем пустой список без кнопок', async () => {
        getUsers.mockResolvedValueOnce({
            users: [],
            pagination: { number: 1, totalElements: 0, totalPages: 0 },
        });

        renderWithRouter(React.createElement(UsersList));

        expect(screen.getByText(/Загрузка списка пользователей/i)).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText(/Пользователи не найдены/i)).toBeInTheDocument();
        });

        expect(screen.queryByRole('button', { name: /След/i })).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /Пред/i })).not.toBeInTheDocument();

        expect(getUsers).toHaveBeenCalledTimes(1);
        expect(getUsers).toHaveBeenCalledWith(1);
    });

    test('показывает ошибку при неуспешном запросе', async () => {
        const errorResponse = {
            response: {
                data: {
                    properties: { errors: ['Ошибка A', 'Ошибка B'] },
                },
            },
        };
        getUsers.mockRejectedValueOnce(errorResponse);

        renderWithRouter(React.createElement(UsersList));

        expect(screen.getByText(/Загрузка списка пользователей/i)).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText(/Ошибка загрузки/i)).toBeInTheDocument();
        });

        expect(screen.getByText(/Ошибка A, Ошибка B/)).toBeInTheDocument();
        const btn = screen.getByRole('button', { name: /Обновить страницу/i });
        expect(btn).toBeInTheDocument();

        expect(getUsers).toHaveBeenCalledTimes(1);
        expect(getUsers).toHaveBeenCalledWith(1);
    });

    test('показывает ошибку с общим сообщением при отсутствии детальных ошибок', async () => {
        const errorResponse = {
            response: {
                data: {},
            },
        };
        getUsers.mockRejectedValueOnce(errorResponse);

        renderWithRouter(React.createElement(UsersList));

        await waitFor(() => {
            expect(screen.getByText(/Ошибка загрузки/i)).toBeInTheDocument();
        });

        expect(screen.getByText(/Не удалось загрузить список пользователей/)).toBeInTheDocument();
    });

    test('показывает ошибку при полном отсутствии response', async () => {
        const errorResponse = new Error('Network error');
        getUsers.mockRejectedValueOnce(errorResponse);

        renderWithRouter(React.createElement(UsersList));

        await waitFor(() => {
            expect(screen.getByText(/Ошибка загрузки/i)).toBeInTheDocument();
        });

        expect(screen.getByText(/Не удалось загрузить список пользователей/)).toBeInTheDocument();
    });

    test('рендерит таблицу с данными пользователей и обрабатывает пропуски', async () => {
        const users = [
            {
                id: 10,
                firstName: 'Иван',
                lastName: 'Иванов',
                username: 'ivanov',
                additionDate: '2023-05-01T12:34:56Z',
            },
            {
                id: 11,
                firstName: '',
                lastName: null,
                username: '',
                additionDate: '',
            },
        ];
        getUsers.mockResolvedValueOnce({
            users,
            pagination: { number: 1, totalElements: 2, totalPages: 1 },
        });

        renderWithRouter(React.createElement(UsersList));

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: /Список пользователей Telegram/i })).toBeInTheDocument();
        });

        expect(screen.getByText('10')).toBeInTheDocument();
        expect(screen.getByText('Иван')).toBeInTheDocument();
        expect(screen.getByText('Иванов')).toBeInTheDocument();
        expect(screen.getByText('@ivanov')).toBeInTheDocument();
        const dateCell = screen.getAllByText(/\d{2}\.\d{2}\.\d{4}/)[0];
        expect(dateCell).toBeInTheDocument();

        expect(screen.getByText('11')).toBeInTheDocument();
        const noEls = screen.getAllByText('Нет');
        expect(noEls.length).toBeGreaterThanOrEqual(3);
        expect(screen.getByText('Не указана')).toBeInTheDocument();

        // Проверяем, что кнопки пагинации не отображаются при totalPages = 1
        expect(screen.queryByRole('button', { name: /След/i })).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /Пред/i })).not.toBeInTheDocument();
    });


    test('проверяет отображение ссылок на просмотр пользователей', async () => {
        const users = [
            {
                id: 123,
                firstName: 'Тест',
                lastName: 'Пользователь',
                username: 'testuser',
                additionDate: '2023-01-01T00:00:00Z',
            },
        ];

        getUsers.mockResolvedValueOnce({
            users,
            pagination: { number: 1, totalElements: 1, totalPages: 1 },
        });

        renderWithRouter(React.createElement(UsersList));

        await waitFor(() => {
            expect(screen.getByText('Тест')).toBeInTheDocument();
        });

        const viewLink = screen.getByRole('link', { name: /Просмотр/i });
        expect(viewLink).toBeInTheDocument();
        expect(viewLink).toHaveAttribute('href', '/users/123');
    });

    test('проверяет обработку пользователей без username', async () => {
        const users = [
            {
                id: 1,
                firstName: 'Без',
                lastName: 'Username',
                username: null,
                additionDate: '2023-01-01T00:00:00Z',
            },
        ];

        getUsers.mockResolvedValueOnce({
            users,
            pagination: { number: 1, totalElements: 1, totalPages: 1 },
        });

        renderWithRouter(React.createElement(UsersList));

        await waitFor(() => {
            expect(screen.getByText('Без')).toBeInTheDocument();
        });

        // Проверяем, что для пользователя без username отображается "Нет"
        const usernameCell = screen.getByText('Без').closest('tr').querySelector('td:nth-child(4)');
        expect(usernameCell).toHaveTextContent('Нет');
    });

    test('проверяет мобильную пагинацию', async () => {
        const users = [
            { id: 1, firstName: 'Тест', lastName: 'Мобильный', username: 'mobile', additionDate: '2023-01-01T00:00:00Z' },
        ];

        getUsers.mockResolvedValueOnce({
            users,
            pagination: { number: 1, totalElements: 10, totalPages: 3 },
        });

        renderWithRouter(React.createElement(UsersList));

        await waitFor(() => {
            expect(screen.getByText('Тест')).toBeInTheDocument();
        });

        const mobileButtons = screen.getAllByRole('button');
        const prevButtons = mobileButtons.filter(btn => btn.textContent.includes('Предыдущая'));
        const nextButtons = mobileButtons.filter(btn => btn.textContent.includes('Следующая'));

        expect(prevButtons.length).toBeGreaterThan(0);
        expect(nextButtons.length).toBeGreaterThan(0);
    });
});