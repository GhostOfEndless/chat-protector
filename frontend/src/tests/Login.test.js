const React = require('react');
const { render, screen, fireEvent, waitFor } = require('@testing-library/react');
require('@testing-library/jest-dom');

let mockNavigate;

jest.mock('react-router-dom', () => {
    return {
        useNavigate: () => mockNavigate,
    };
}, { virtual: true });

jest.mock('../services/api', () => ({
    authenticate: jest.fn(),
}));
const { authenticate } = require('../services/api');

const Login = require('../pages/Login').default;

describe('Login component', () => {
    beforeEach(() => {
        mockNavigate = jest.fn();
        localStorage.clear();
        jest.clearAllMocks();
    });

    const setup = () => {
        render(React.createElement(Login));
        const loginInput = screen.getByLabelText(/Логин/i);
        const passwordInput = screen.getByLabelText(/Пароль/i);
        const submitButton = screen.getByRole('button', { name: /Войти|Выполняется вход/i });
        return { loginInput, passwordInput, submitButton };
    };

    test('рендерит поля и кнопку по умолчанию', () => {
        const { loginInput, passwordInput, submitButton } = setup();
        expect(loginInput).toBeInTheDocument();
        expect(passwordInput).toBeInTheDocument();
        expect(submitButton).toBeEnabled();
        expect(submitButton).toHaveTextContent('Войти');
        expect(screen.queryByText(/Ошибка входа/i)).not.toBeInTheDocument();
    });

    test('успешный вход: вызывает authenticate, сохраняет токен и навигирует', async () => {
        const { loginInput, passwordInput, submitButton } = setup();
        fireEvent.change(loginInput, { target: { value: 'user1' } });
        fireEvent.change(passwordInput, { target: { value: 'pass1' } });

        authenticate.mockResolvedValueOnce({ token: 'abc123' });

        fireEvent.click(submitButton);

        expect(submitButton).toBeDisabled();
        expect(submitButton).toHaveTextContent(/Выполняется вход/i);

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/chats');
            expect(localStorage.getItem('token')).toBe('abc123');
        });

        expect(submitButton).toBeEnabled();
        expect(submitButton).toHaveTextContent('Войти');
        expect(screen.queryByText(/Ошибка входа/i)).not.toBeInTheDocument();
    });

    test('неуспешный вход: отображает сообщение об ошибке и не навигирует', async () => {
        const { loginInput, passwordInput, submitButton } = setup();
        fireEvent.change(loginInput, { target: { value: 'user2' } });
        fireEvent.change(passwordInput, { target: { value: 'wrongpass' } });

        const error = {
            response: { data: { message: 'Неверные данные' } },
        };
        authenticate.mockRejectedValueOnce(error);

        fireEvent.click(submitButton);

        expect(submitButton).toBeDisabled();
        expect(submitButton).toHaveTextContent(/Выполняется вход/i);

        await waitFor(() => {
            expect(screen.getByText(/Ошибка входа: Неверные данные/i)).toBeInTheDocument();
            expect(mockNavigate).not.toHaveBeenCalled();
            expect(localStorage.getItem('token')).toBeNull();
        });

        expect(submitButton).toBeEnabled();
        expect(submitButton).toHaveTextContent('Войти');
    });

    test('неуспешный вход без response.data.message: отображает общее сообщение', async () => {
        const { loginInput, passwordInput, submitButton } = setup();
        fireEvent.change(loginInput, { target: { value: '' } });
        fireEvent.change(passwordInput, { target: { value: '' } });

        const error = new Error('Network Error');
        authenticate.mockRejectedValueOnce(error);

        fireEvent.click(submitButton);

        await waitFor(() => {
            expect(screen.getByText(/Ошибка входа: Network Error/i)).toBeInTheDocument();
            expect(mockNavigate).not.toHaveBeenCalled();
        });
    });
});