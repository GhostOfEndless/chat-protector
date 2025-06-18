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
    getSpamProtectionSettings: jest.fn(),
    updateSpamProtectionSettings: jest.fn(),
}));
const {
    getSpamProtectionSettings,
    updateSpamProtectionSettings,
} = require('../services/api');

const SpamProtection = require('../pages/SpamProtection').default;

describe('SpamProtection component', () => {
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

    const renderComponent = () => render(React.createElement(SpamProtection));

    test('если chatId не указан — показывает ошибку и кнопку Назад', async () => {
        mockChatId = undefined;
        renderComponent();

        await waitFor(() => {
            expect(screen.getByText(/Идентификатор чата не указан/i)).toBeInTheDocument();
        });
        const backBtn = screen.getAllByRole('button').find(btn => /Назад/i.test(btn.textContent));
        expect(backBtn).toBeInTheDocument();
        fireEvent.click(backBtn);
        expect(mockNavigate).toHaveBeenCalledWith(-1);
    });

    test('показывает индикатор загрузки, затем ошибку при неуспешном getSpamProtectionSettings', async () => {
        getSpamProtectionSettings.mockRejectedValueOnce(new Error('Network fail'));

        renderComponent();

        expect(screen.getByText(/Загрузка настроек защиты от спама/i)).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText(/Не удалось загрузить настройки защиты от спама/i)).toBeInTheDocument();
        });
        const backBtn = screen.getAllByRole('button').find(btn => /Назад/i.test(btn.textContent));
        expect(backBtn).toBeInTheDocument();
        fireEvent.click(backBtn);
        expect(mockNavigate).toHaveBeenCalledWith(-1);
    });

    test('успешно загружает настройки: toggle, изменение периода, добавление и удаление исключений', async () => {
        const initialSettings = {
            enabled: true,
            coolDownPeriod: 10,
            exclusions: ['u1'],
        };
        getSpamProtectionSettings.mockResolvedValueOnce(initialSettings);
        updateSpamProtectionSettings
            .mockResolvedValueOnce({ ...initialSettings, enabled: false })
            .mockResolvedValueOnce({ ...initialSettings, enabled: true })
            .mockResolvedValueOnce({ ...initialSettings, coolDownPeriod: 20, enabled: true, exclusions: ['u1'] })
            .mockResolvedValueOnce({ ...initialSettings, coolDownPeriod: 20, enabled: true, exclusions: ['u1', 'u2'] })
            .mockResolvedValueOnce({ ...initialSettings, coolDownPeriod: 20, enabled: true, exclusions: ['u2'] });

        const { container } = renderComponent();

        await waitFor(() => {
            const titles = screen.getAllByText(/Защита от спама/i);
            expect(titles.length).toBeGreaterThanOrEqual(1);
        });

        const checkbox = screen.getByRole('checkbox');
        expect(checkbox).toBeChecked();
        fireEvent.click(checkbox);
        await waitFor(() => {
            expect(screen.getByRole('checkbox')).not.toBeChecked();
        });
        fireEvent.click(screen.getByRole('checkbox'));
        await waitFor(() => {
            expect(screen.getByRole('checkbox')).toBeChecked();
        });

        const periodInput = screen.getByRole('spinbutton');
        expect(periodInput).toHaveValue(initialSettings.coolDownPeriod);
        fireEvent.change(periodInput, { target: { value: '20' } });
        const saveBtn = screen.getByRole('button', { name: /Сохранить/i });
        expect(saveBtn).toBeEnabled();
        fireEvent.click(saveBtn);
        await waitFor(() => {
            expect(screen.getByRole('spinbutton')).toHaveValue(20);
        });
        expect(screen.getByRole('button', { name: /Сохранить/i })).toBeDisabled();

        expect(screen.getByText('u1')).toBeInTheDocument();
        const exclusionInput = screen.getByPlaceholderText(/ID пользователя/i);
        const addBtn = screen.getByRole('button', { name: /Добавить/i });
        fireEvent.change(exclusionInput, { target: { value: 'u2' } });
        expect(addBtn).toBeEnabled();
        fireEvent.click(addBtn);
        await waitFor(() => {
            expect(screen.getByText('u2')).toBeInTheDocument();
            expect(exclusionInput).toHaveValue('');
        });

        const removeButtons = screen.getAllByRole('button').filter(btn => {
            const parent = btn.closest('div');
            return parent && parent.textContent.includes('u1');
        });
        expect(removeButtons.length).toBeGreaterThanOrEqual(1);
        fireEvent.click(removeButtons[0]);
        await waitFor(() => {
            expect(screen.queryByText('u1')).not.toBeInTheDocument();
            expect(screen.getByText('u2')).toBeInTheDocument();
        });
    });

    test('при добавлении уже существующего исключения вызывает alert и не вызывает API', async () => {
        const settings = { enabled: true, coolDownPeriod: 5, exclusions: ['u1'] };
        getSpamProtectionSettings.mockResolvedValueOnce(settings);

        renderComponent();
        await waitFor(() => {
            const titles = screen.getAllByText(/Защита от спама/i);
            expect(titles.length).toBeGreaterThanOrEqual(1);
        });

        const exclusionInput = screen.getByPlaceholderText(/ID пользователя/i);
        const addBtn = screen.getByRole('button', { name: /Добавить/i });

        fireEvent.change(exclusionInput, { target: { value: 'u1' } });
        fireEvent.click(addBtn);
        expect(window.alert).toHaveBeenCalledWith('Этот пользователь уже в списке исключений');
        expect(updateSpamProtectionSettings).not.toHaveBeenCalled();
    });

    test('при ошибке updateSpamProtectionSettings показывает alert', async () => {
        const settings = { enabled: false, coolDownPeriod: 0, exclusions: [] };
        getSpamProtectionSettings.mockResolvedValueOnce(settings);
        updateSpamProtectionSettings.mockRejectedValueOnce(new Error('Fail update'));

        renderComponent();
        await waitFor(() => {
            const titles = screen.getAllByText(/Защита от спама/i);
            expect(titles.length).toBeGreaterThanOrEqual(1);
        });

        const checkbox = screen.getByRole('checkbox');
        expect(checkbox).not.toBeChecked();
        fireEvent.click(checkbox);
        await waitFor(() => {
            expect(window.alert).toHaveBeenCalledWith(expect.stringContaining('Ошибка при обновлении настроек'));
        });
    });
});