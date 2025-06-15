const React = require('react');
const { render, screen, fireEvent, waitFor } = require('@testing-library/react');
require('@testing-library/jest-dom');

let mockNavigate;
let mockUserId;

jest.mock('react-router-dom', () => {
    return {
        useParams: () => ({ userId: mockUserId }),
        useNavigate: () => mockNavigate,
    };
}, { virtual: true });

jest.mock('../services/api', () => ({
    getUser: jest.fn(),
}));
const { getUser } = require('../services/api');

const User = require('../pages/User').default;

describe('User component', () => {
    beforeEach(() => {
        mockNavigate = jest.fn();
        mockUserId = '123';
        localStorage.clear();
        jest.clearAllMocks();
    });

    const renderComponent = () => render(React.createElement(User));

    test('показывает экран загрузки до получения данных', async () => {
        getUser.mockResolvedValueOnce({
            id: '123',
            firstName: 'Иван',
            lastName: 'Иванов',
            username: 'ivanov',
            additionDate: '2023-05-01T00:00:00Z',
        });

        renderComponent();

        expect(screen.getByText(/Загрузка информации о пользователе/i)).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText(/Информация о пользователе/i)).toBeInTheDocument();
        });
    });

    test('успешно отображает данные пользователя и кнопку "Назад"', async () => {
        const userData = {
            id: '123',
            firstName: 'Иван',
            lastName: 'Петров',
            username: 'petrov',
            additionDate: '2024-02-15T00:00:00Z',
        };
        getUser.mockResolvedValueOnce(userData);

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText(/Информация о пользователе/i)).toBeInTheDocument();
        });

        expect(screen.getByText('ID:')).toBeInTheDocument();
        expect(screen.getByText(userData.id)).toBeInTheDocument();

        expect(screen.getByText('Имя:')).toBeInTheDocument();
        expect(screen.getByText(userData.firstName)).toBeInTheDocument();

        expect(screen.getByText('Фамилия:')).toBeInTheDocument();
        expect(screen.getByText(userData.lastName)).toBeInTheDocument();

        expect(screen.getByText('Имя пользователя:')).toBeInTheDocument();
        expect(screen.getByText(`@${userData.username}`)).toBeInTheDocument();
        const dateFormatted = new Date(userData.additionDate).toLocaleDateString('ru-RU');

        expect(screen.getByText('Дата добавления:')).toBeInTheDocument();
        expect(screen.getByText(dateFormatted)).toBeInTheDocument();

        const backBtn = screen.getAllByRole('button').find(btn => /Назад/i.test(btn.textContent));
        expect(backBtn).toBeInTheDocument();
        fireEvent.click(backBtn);
        expect(mockNavigate).toHaveBeenCalledWith(-1);
    });

    test('если getUser возвращает null, показывает "Пользователь не найден" и кнопку назад', async () => {
        getUser.mockResolvedValueOnce(null);

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText(/Пользователь не найден/i)).toBeInTheDocument();
        });

        // Сообщение и кнопка Назад
        expect(screen.getByText(/Не удалось найти информацию по указанному ID/i)).toBeInTheDocument();
        const backBtn = screen.getAllByRole('button').find(btn => /Назад/i.test(btn.textContent));
        expect(backBtn).toBeInTheDocument();
        fireEvent.click(backBtn);
        expect(mockNavigate).toHaveBeenCalledWith(-1);
    });

    test('при ошибке загрузки показывает блок ошибки и кнопку назад', async () => {
        getUser.mockRejectedValueOnce(new Error('Network error'));

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText(/Ошибка/i)).toBeInTheDocument();
        });

        expect(screen.getByText(/Не удалось загрузить информацию о пользователе/i)).toBeInTheDocument();

        const backBtn = screen.getAllByRole('button').find(btn => /Назад/i.test(btn.textContent));
        expect(backBtn).toBeInTheDocument();
        fireEvent.click(backBtn);
        expect(mockNavigate).toHaveBeenCalledWith(-1);
    });

    test('вызывает getUser с корректным userId из useParams', async () => {
        mockUserId = '999';
        const userData = { id: '999', firstName: 'Test', lastName: 'User', username: 'testuser', additionDate: null };
        getUser.mockResolvedValueOnce(userData);

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText(/Информация о пользователе/i)).toBeInTheDocument();
        });

        expect(getUser).toHaveBeenCalledTimes(1);
        expect(getUser).toHaveBeenCalledWith('999');
    });
});
