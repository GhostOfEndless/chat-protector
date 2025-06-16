const React = require('react');
const { render, screen, fireEvent, waitFor } = require('@testing-library/react');
require('@testing-library/jest-dom');

let mockNavigate;
let mockChatId;

jest.mock('react-router-dom', () => {
    const React = require('react');
    return {
        useParams: () => ({ chatId: mockChatId }),
        useNavigate: () => mockNavigate,
        Link: ({ to, children, ...rest }) => React.createElement('a', { href: to, ...rest }, children),
    };
}, { virtual: true });

jest.mock('../services/api', () => ({
    getChat: jest.fn(),
}));
const { getChat } = require('../services/api');

const Chat = require('../pages/Chat').default;

describe('Chat component', () => {
    beforeEach(() => {
        mockNavigate = jest.fn();
        mockChatId = 'chat123';
        jest.clearAllMocks();
        jest.spyOn(console, 'error').mockImplementation(() => {});
    });
    afterEach(() => {
        console.error.mockRestore();
    });

    const renderComponent = () => render(React.createElement(Chat));

    test('показывает индикатор загрузки, затем рендерит данные чата при успешном ответе', async () => {
        const chatData = { name: 'Test Chat' };
        getChat.mockResolvedValueOnce(chatData);

        renderComponent();

        expect(screen.getByText(/Загрузка информации о чате/i)).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText(/Управление чатом: Test Chat/i)).toBeInTheDocument();
        });

        const backBtn = screen.getByRole('button', { name: /Назад к списку чатов/i });
        expect(backBtn).toBeInTheDocument();
        fireEvent.click(backBtn);
        expect(mockNavigate).toHaveBeenCalledWith('/chats');

        const base = `/chats/${mockChatId}`;
        const deletedLink = screen.getByRole('link', { name: /Удалённые сообщения/i });
        expect(deletedLink).toHaveAttribute('href', `${base}/deleted-messages`);
        const modLink = screen.getByRole('link', { name: /Настройки модерации/i });
        expect(modLink).toHaveAttribute('href', `${base}/moderation`);
        const spamLink = screen.getByRole('link', { name: /Защита от спама/i });
        expect(spamLink).toHaveAttribute('href', `${base}/spam-protection`);
    });


    test('при ошибке getChat показывает сообщение об ошибке и кнопку Назад', async () => {
        getChat.mockRejectedValueOnce(new Error('Network error'));

        renderComponent();

        expect(screen.getByText(/Загрузка информации о чате/i)).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText(/^Ошибка$/i)).toBeInTheDocument();
            expect(screen.getByText(/Не удалось загрузить информацию о чате\. Пожалуйста, попробуйте еще раз\./i)).toBeInTheDocument();
        });

        const backBtn = screen.getByRole('button', { name: /Назад к списку чатов/i });
        expect(backBtn).toBeInTheDocument();
        fireEvent.click(backBtn);
        expect(mockNavigate).toHaveBeenCalledWith('/chats');
    });

    test('рендерит заголовок без названия, если chat.name отсутствует', async () => {
        getChat.mockResolvedValueOnce({ name: '' });

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText(/Управление чатом: Без названия/i)).toBeInTheDocument();
        });
    });

    test('вызывает getChat с корректным chatId из useParams', async () => {
        mockChatId = 'otherId';
        getChat.mockResolvedValueOnce({ name: 'Another' });

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText(/Управление чатом: Another/i)).toBeInTheDocument();
        });
        expect(getChat).toHaveBeenCalledTimes(1);
        expect(getChat).toHaveBeenCalledWith('otherId');
    });
});