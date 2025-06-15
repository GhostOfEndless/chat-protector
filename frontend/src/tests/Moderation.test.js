const React = require('react');
const { render, screen, fireEvent, waitFor } = require('@testing-library/react');
require('@testing-library/jest-dom');

let mockNavigate;
let mockChatId;

jest.mock('react-router-dom', () => {
    return {
        useParams: () => ({ chatId: mockChatId }),
        useNavigate: () => mockNavigate,
    };
}, { virtual: true });

jest.mock('../services/api', () => ({
    getTextModerationSettings: jest.fn(),
    updateTextModerationSettings: jest.fn(),
}));
const {
    getTextModerationSettings,
    updateTextModerationSettings,
} = require('../services/api');

const Moderation = require('../pages/Moderation').default;

describe('Moderation component', () => {
    beforeEach(() => {
        mockNavigate = jest.fn();
        mockChatId = 'chat123';
        jest.clearAllMocks();
        jest.spyOn(window, 'alert').mockImplementation(() => {});
        jest.spyOn(console, 'error').mockImplementation(() => {});
    });

    afterEach(() => {
        window.alert.mockRestore();
        console.error.mockRestore();
    });

    const renderComponent = () => render(React.createElement(Moderation));

    test('если chatId не указан — показывает ошибку и кнопку Назад', async () => {
        mockChatId = undefined;
        renderComponent();

        await waitFor(() => {
            expect(screen.getByText(/Идентификатор чата не указан/i)).toBeInTheDocument();
        });

        const backBtn = screen.getByRole('button', { name: /Назад/i });
        expect(backBtn).toBeInTheDocument();
        fireEvent.click(backBtn);
        expect(mockNavigate).toHaveBeenCalledWith(-1);
    });

    test('показывает индикатор загрузки, затем ошибку при отказе getTextModerationSettings', async () => {
        const mockError = {
            response: { data: { message: 'API Error' } }
        };
        getTextModerationSettings.mockRejectedValueOnce(mockError);

        renderComponent();

        expect(screen.getByText(/Загрузка настроек модерации/i)).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText(/Не удалось загрузить настройки модерации/i)).toBeInTheDocument();
        });

        const backBtn = screen.getByRole('button', { name: /Назад/i });
        fireEvent.click(backBtn);
        expect(mockNavigate).toHaveBeenCalledWith(-1);

        expect(console.error).toHaveBeenCalledWith(
            'Ошибка загрузки настроек модерации:',
            mockError.response.data
        );
    });

    test('показывает сообщение, если настроек нет', async () => {
        getTextModerationSettings.mockResolvedValueOnce({});

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText(/Настройки модерации не найдены для этого чата/i)).toBeInTheDocument();
            expect(screen.getByText(new RegExp(`ID чата: ${mockChatId}`, 'i'))).toBeInTheDocument();
        });
    });

    test('показывает сообщение, если есть данные, но нет поддерживаемых настроек', async () => {
        getTextModerationSettings.mockResolvedValueOnce({
            unsupportedFilterSettings: { enabled: true, exclusions: [] },
        });

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText(/Нет поддерживаемых настроек модерации/i)).toBeInTheDocument();
            expect(screen.getByText(/Доступные фильтры:/i)).toBeInTheDocument();
        });
    });

    test('валидация для разных типов фильтров', async () => {
        const initialData = {
            linksFilterSettings: { enabled: false, exclusions: [] },
            phoneNumbersFilterSettings: { enabled: false, exclusions: [] },
            tagsFilterSettings: { enabled: false, exclusions: [] },
        };

        getTextModerationSettings.mockResolvedValueOnce(initialData);
        updateTextModerationSettings.mockResolvedValue({});

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText('Ссылки')).toBeInTheDocument();
            expect(screen.getByText('Телефонные номера')).toBeInTheDocument();
            expect(screen.getByText('Теги')).toBeInTheDocument();
        });

        const linksInput = screen.getByPlaceholderText(/Введите ссылки/i);
        const linksAddBtn = screen.getAllByRole('button', { name: /Добавить/i })[0];

        fireEvent.change(linksInput, { target: { value: 'invalid-link' } });
        fireEvent.click(linksAddBtn);
        expect(window.alert).toHaveBeenCalledWith('Недопустимый формат для Ссылки');

        fireEvent.change(linksInput, { target: { value: 'https://example.com' } });
        fireEvent.click(linksAddBtn);
        expect(updateTextModerationSettings).toHaveBeenCalledWith('chat123', 'links', expect.any(Object));

        const phoneInput = screen.getByPlaceholderText(/Введите телефонные номера/i);
        const phoneAddBtn = screen.getAllByRole('button', { name: /Добавить/i })[1];

        fireEvent.change(phoneInput, { target: { value: '123' } });
        fireEvent.click(phoneAddBtn);
        expect(window.alert).toHaveBeenCalledWith('Недопустимый формат для Телефонные номера');

        fireEvent.change(phoneInput, { target: { value: '+1234567890' } });
        fireEvent.click(phoneAddBtn);
        expect(updateTextModerationSettings).toHaveBeenCalledWith('chat123', 'phone-numbers', expect.any(Object));

        const tagsInput = screen.getByPlaceholderText(/Введите теги/i);
        const tagsAddBtn = screen.getAllByRole('button', { name: /Добавить/i })[2];

        fireEvent.change(tagsInput, { target: { value: '#hashtag' } });
        fireEvent.click(tagsAddBtn);
        expect(updateTextModerationSettings).toHaveBeenCalledWith('chat123', 'tags', expect.any(Object));
    });

    test('alert при попытке добавить пустое значение', async () => {
        const initialData = {
            tagsFilterSettings: { enabled: true, exclusions: [] },
        };
        getTextModerationSettings.mockResolvedValueOnce(initialData);

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText('Теги')).toBeInTheDocument();
        });

        const input = screen.getByPlaceholderText(/Введите теги/i);
        const addBtn = screen.getByRole('button', { name: /Добавить/i });

        fireEvent.change(input, { target: { value: '' } });
        fireEvent.click(addBtn);
        expect(window.alert).toHaveBeenCalledWith('Пожалуйста, введите значение для исключения');

        fireEvent.change(input, { target: { value: '   ' } });
        fireEvent.click(addBtn);
        expect(window.alert).toHaveBeenCalledWith('Пожалуйста, введите значение для исключения');

        expect(updateTextModerationSettings).not.toHaveBeenCalled();
    });

    test('правильно нормализует ключи настроек из API', async () => {
        const initialData = {
            linksFilterSettings: { enabled: true, exclusions: ['link1'] },
            botCommandsFilterSettings: { enabled: false, exclusions: [] },
            customEmojisFilterSettings: { enabled: true, exclusions: [':emoji:'] },
            emailsFilterSettings: { enabled: false, exclusions: [] },
            mentionsFilterSettings: { enabled: true, exclusions: [] },
            phoneNumbersFilterSettings: { enabled: false, exclusions: [] },
            tagsFilterSettings: { enabled: true, exclusions: [] },
        };

        getTextModerationSettings.mockResolvedValueOnce(initialData);

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText('Ссылки')).toBeInTheDocument();
            expect(screen.getByText('Команды ботов')).toBeInTheDocument();
            expect(screen.getByText('Кастомные эмодзи')).toBeInTheDocument();
            expect(screen.getByText('Электронные адреса')).toBeInTheDocument();
            expect(screen.getByText('Упоминания')).toBeInTheDocument();
            expect(screen.getByText('Телефонные номера')).toBeInTheDocument();
            expect(screen.getByText('Теги')).toBeInTheDocument();
        });

        const zapreshchenoTexts = screen.getAllByText('Запрещено');
        const razreshenoTexts = screen.getAllByText('Разрешено');

        expect(zapreshchenoTexts).toHaveLength(4);
        expect(razreshenoTexts).toHaveLength(3);
    });

    test('обрабатывает ошибку недопустимого типа фильтра', async () => {
        const initialData = {
            emailsFilterSettings: { enabled: false, exclusions: [] },
        };

        getTextModerationSettings.mockResolvedValueOnce(initialData);

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText('Электронные адреса')).toBeInTheDocument();
        });

        updateTextModerationSettings.mockRejectedValueOnce(new Error('Недопустимый тип фильтра: invalid'));

        const checkbox = screen.getByRole('checkbox');
        fireEvent.click(checkbox);

        await waitFor(() => {
            expect(console.error).toHaveBeenCalledWith('Ошибка обновления модерации:', expect.any(Error));
        });
    });

    test('отображает вкладку "Общие" как активную', async () => {
        const initialData = {
            emailsFilterSettings: { enabled: false, exclusions: [] },
        };

        getTextModerationSettings.mockResolvedValueOnce(initialData);

        renderComponent();

        await waitFor(() => {
            const generalTab = screen.getByRole('button', { name: /Общие/i });
            expect(generalTab).toBeInTheDocument();
            expect(generalTab).toHaveClass('text-blue-600', 'border-b-2', 'border-blue-500');
        });
    });
});