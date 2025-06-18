var capturedRequestInterceptorFn;
var capturedResponseFulfilledFn;
var capturedResponseRejectedFn;

jest.mock('axios', () => {
    const mockAxiosInstance = {
        get: jest.fn(),
        post: jest.fn(),
        patch: jest.fn(),
        interceptors: {
            request: {
                use: jest.fn()
            },
            response: {
                use: jest.fn()
            }
        }
    };

    return {
        create: jest.fn(() => mockAxiosInstance),
        get: jest.fn(),
        post: jest.fn(),
        patch: jest.fn(),
    };
});

import axios from 'axios';
import {
    authenticate,
    getChatList,
    getChat,
    getDeletedMessages,
    getUsers,
    getUser,
    getTextModerationSettings,
    updateTextModerationSettings,
    getSpamProtectionSettings,
    updateSpamProtectionSettings
} from '../services/api';

const localStorageMock = (() => {
    let store = {};
    return {
        getItem: jest.fn(key => store[key] || null),
        setItem: jest.fn((key, value) => {
            store[key] = value.toString();
        }),
        removeItem: jest.fn(key => {
            delete store[key];
        }),
        clear: jest.fn(() => {
            store = {};
        })
    };
})();
Object.defineProperty(window, 'localStorage', {
    value: localStorageMock,
});

const originalWindowLocation = window.location;
beforeAll(() => {
    Object.defineProperty(window, 'location', {
        writable: true,
        value: { ...originalWindowLocation, assign: jest.fn() }
    });
});

afterAll(() => {
    Object.defineProperty(window, 'location', {
        writable: true,
        value: originalWindowLocation
    });
});


describe('api.js', () => {
    const mockedAxiosInstance = axios.create();

    beforeEach(() => {
        jest.clearAllMocks();

        localStorageMock.clear();
        window.location.assign.mockClear();

        mockedAxiosInstance.interceptors.request.use.mockClear();
        mockedAxiosInstance.interceptors.response.use.mockClear();

        mockedAxiosInstance.interceptors.request.use.mockImplementation((onFulfilled, onRejected) => {
            capturedRequestInterceptorFn = onFulfilled;
            return 1;
        });

        mockedAxiosInstance.interceptors.response.use.mockImplementation((onFulfilled, onRejected) => {
            capturedResponseFulfilledFn = onFulfilled;
            capturedResponseRejectedFn = onRejected;
            return 1;
        });

        delete require.cache[require.resolve('../services/api')];
        require('../services/api');

        mockedAxiosInstance.get.mockResolvedValue({});
        mockedAxiosInstance.post.mockResolvedValue({});
        mockedAxiosInstance.patch.mockResolvedValue({});
    });


    describe('Authentication Functions', () => {
        it('authenticate should call POST with correct data and return response data', async () => {
            const mockResponse = { data: { token: 'new-token', user: 'test-user' } };
            mockedAxiosInstance.post.mockResolvedValue(mockResponse);

            const result = await authenticate('user', 'password');

            expect(mockedAxiosInstance.post).toHaveBeenCalledWith('/auth/authenticate', { login: 'user', password: 'password' });
            expect(result).toEqual(mockResponse.data);
        });

        it('authenticate should throw an error if API call fails', async () => {
            const mockError = new Error('Auth failed');
            mockedAxiosInstance.post.mockRejectedValue(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(authenticate('user', 'password')).rejects.toThrow(mockError);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка аутентификации:', mockError);
            consoleErrorSpy.mockRestore();
        });
    });

    describe('Chat Functions', () => {
        it('getChatList should call GET /admin/chats and return response data', async () => {
            const mockResponse = { data: [{ id: 1, name: 'Chat 1' }] };
            mockedAxiosInstance.get.mockResolvedValue(mockResponse);

            const result = await getChatList();

            expect(mockedAxiosInstance.get).toHaveBeenCalledWith('/admin/chats');
            expect(result).toEqual(mockResponse.data);
        });

        it('getChatList should throw an error if API call fails', async () => {
            const mockError = new Error('Failed to get chat list');
            mockedAxiosInstance.get.mockRejectedValue(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(getChatList()).rejects.toThrow(mockError);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка получения списка чатов:', mockError);
            consoleErrorSpy.mockRestore();
        });

        it('getChat should call GET /admin/chats/:chatId and return response data', async () => {
            const mockResponse = { data: { id: 123, name: 'Specific Chat' } };
            mockedAxiosInstance.get.mockResolvedValue(mockResponse);

            const result = await getChat(123);

            expect(mockedAxiosInstance.get).toHaveBeenCalledWith('/admin/chats/123');
            expect(result).toEqual(mockResponse.data);
        });

        it('getChat should throw an error if API call fails', async () => {
            const mockError = new Error('Failed to get chat');
            mockedAxiosInstance.get.mockRejectedValue(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(getChat(123)).rejects.toThrow(mockError);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка получения чата:', mockError);
            consoleErrorSpy.mockRestore();
        });

        it('getDeletedMessages should call GET /admin/chats/:chatId/deleted-messages with page and return response data', async () => {
            const mockResponse = { data: [{ id: 1, text: 'Deleted Msg' }] };
            mockedAxiosInstance.get.mockResolvedValue(mockResponse);

            const result = await getDeletedMessages(456, 2);

            expect(mockedAxiosInstance.get).toHaveBeenCalledWith('/admin/chats/456/deleted-messages?page=2');
            expect(result).toEqual(mockResponse.data);
        });

        it('getDeletedMessages should use default page 1 if not provided', async () => {
            const mockResponse = { data: [{ id: 1, text: 'Deleted Msg' }] };
            mockedAxiosInstance.get.mockResolvedValue(mockResponse);

            const result = await getDeletedMessages(456);

            expect(mockedAxiosInstance.get).toHaveBeenCalledWith('/admin/chats/456/deleted-messages?page=1');
            expect(result).toEqual(mockResponse.data);
        });

        it('getDeletedMessages should throw an error if API call fails', async () => {
            const mockError = new Error('Failed to get deleted messages');
            mockedAxiosInstance.get.mockRejectedValue(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(getDeletedMessages(456)).rejects.toThrow(mockError);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка получения удаленных сообщений:', mockError);
            consoleErrorSpy.mockRestore();
        });
    });

    describe('User Functions', () => {
        it('getUsers should call GET /admin/users with page and size parameters and return users and pagination', async () => {
            const mockResponse = {
                data: {
                    content: [{ id: 1, name: 'User 1' }],
                    page: { size: 10, number: 1, totalElements: 1, totalPages: 1 }
                }
            };
            mockedAxiosInstance.get.mockResolvedValue(mockResponse);

            const result = await getUsers(1, 10);

            expect(mockedAxiosInstance.get).toHaveBeenCalledWith('/admin/users', { params: { page: 1, size: 10 } });
            expect(result).toEqual({
                users: [{ id: 1, name: 'User 1' }],
                pagination: { size: 10, number: 1, totalElements: 1, totalPages: 1 }
            });
        });

        it('getUsers should use default page 1 if invalid page is provided', async () => {
            const mockResponse = {
                data: {
                    content: [{ id: 1, name: 'User 1' }],
                    page: { size: 20, number: 1, totalElements: 1, totalPages: 1 }
                }
            };
            mockedAxiosInstance.get.mockResolvedValue(mockResponse);

            const result = await getUsers(0); // Invalid page

            expect(mockedAxiosInstance.get).toHaveBeenCalledWith('/admin/users', { params: { page: 1 } });
            expect(result.pagination.number).toBe(1);
        });

        it('getUsers should use default size 20 if size is null', async () => {
            const mockResponse = {
                data: {
                    content: [{ id: 1, name: 'User 1' }],
                    page: { size: 20, number: 1, totalElements: 1, totalPages: 1 }
                }
            };
            mockedAxiosInstance.get.mockResolvedValue(mockResponse);

            const result = await getUsers(1, null);

            expect(mockedAxiosInstance.get).toHaveBeenCalledWith('/admin/users', { params: { page: 1 } });
            expect(result.pagination.size).toBe(20);
        });

        it('getUsers should handle empty content and page data gracefully', async () => {
            const mockResponse = {
                data: {}
            };
            mockedAxiosInstance.get.mockResolvedValue(mockResponse);

            const result = await getUsers(1);

            expect(result.users).toEqual([]);
            expect(result.pagination).toEqual({
                size: 20,
                number: 1,
                totalElements: 0,
                totalPages: 0
            });
        });

        it('getUsers should throw an error if API call fails', async () => {
            const mockError = new Error('Failed to get users');
            mockedAxiosInstance.get.mockRejectedValue(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(getUsers()).rejects.toThrow(mockError);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка получения списка пользователей:', expect.any(Object));
            consoleErrorSpy.mockRestore();
        });

        it('getUser should call GET /admin/users/:userId and return response data', async () => {
            const mockResponse = { data: { id: 789, name: 'Specific User' } };
            mockedAxiosInstance.get.mockResolvedValue(mockResponse);

            const result = await getUser(789);

            expect(mockedAxiosInstance.get).toHaveBeenCalledWith('/admin/users/789');
            expect(result).toEqual(mockResponse.data);
        });

        it('getUser should throw an error if API call fails', async () => {
            const mockError = new Error('Failed to get user');
            mockedAxiosInstance.get.mockRejectedValue(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(getUser(789)).rejects.toThrow(mockError);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка получения пользователя:', mockError);
            consoleErrorSpy.mockRestore();
        });
    });

    describe('Text Moderation Settings Functions', () => {
        it('getTextModerationSettings should call GET and return response data', async () => {
            const mockResponse = { data: { enabled: true, filter: 'emails' } };
            mockedAxiosInstance.get.mockResolvedValue(mockResponse);

            const result = await getTextModerationSettings(1);

            expect(mockedAxiosInstance.get).toHaveBeenCalledWith('/admin/settings/chats/1/text-moderation');
            expect(result).toEqual(mockResponse.data);
        });

        it('getTextModerationSettings should throw an error if API call fails', async () => {
            const mockError = new Error('Failed to get moderation settings');
            mockedAxiosInstance.get.mockRejectedValue(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(getTextModerationSettings(1)).rejects.toThrow(mockError);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка получения настроек модерации:', mockError);
            consoleErrorSpy.mockRestore();
        });

        it('updateTextModerationSettings should call PATCH with correct data and return response data', async () => {
            const mockSettings = { enabled: true, exclusionMode: 'blacklist', exclusions: ['example.com'] };
            const mockResponse = { data: { success: true } };
            mockedAxiosInstance.patch.mockResolvedValue(mockResponse);

            const result = await updateTextModerationSettings(1, 'links', mockSettings);

            expect(mockedAxiosInstance.patch).toHaveBeenCalledWith(
                '/admin/settings/chats/1/text-moderation/links',
                mockSettings
            );
            expect(result).toEqual(mockResponse.data);
        });

        it('updateTextModerationSettings should throw an error for invalid filter type', async () => {
            const mockSettings = { enabled: true, exclusionMode: 'blacklist', exclusions: [] };
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(updateTextModerationSettings(1, 'invalid-type', mockSettings)).rejects.toThrow(
                'Неверный тип фильтра: invalid-type. Доступные типы: emails, tags, mentions, links, phone-numbers, bot-commands, custom-emojis'
            );
            expect(mockedAxiosInstance.patch).not.toHaveBeenCalled();
            consoleErrorSpy.mockRestore();
        });

        it('updateTextModerationSettings should throw an error if required settings fields are missing', async () => {
            const mockSettingsMissingEnabled = { exclusionMode: 'blacklist', exclusions: [] };
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(updateTextModerationSettings(1, 'emails', mockSettingsMissingEnabled)).rejects.toThrow(
                'Для обновления настроек требуются поля: enabled, exclusionMode, exclusions'
            );
            expect(mockedAxiosInstance.patch).not.toHaveBeenCalled();
            consoleErrorSpy.mockRestore();

            const mockSettingsMissingExclusionMode = { enabled: true, exclusions: [] };
            await expect(updateTextModerationSettings(1, 'emails', mockSettingsMissingExclusionMode)).rejects.toThrow(
                'Для обновления настроек требуются поля: enabled, exclusionMode, exclusions'
            );
            expect(mockedAxiosInstance.patch).not.toHaveBeenCalled();

            const mockSettingsMissingExclusions = { enabled: true, exclusionMode: 'blacklist' };
            await expect(updateTextModerationSettings(1, 'emails', mockSettingsMissingExclusions)).rejects.toThrow(
                'Для обновления настроек требуются поля: enabled, exclusionMode, exclusions'
            );
            expect(mockedAxiosInstance.patch).not.toHaveBeenCalled();
        });

        it('updateTextModerationSettings should throw an error if API call fails', async () => {
            const mockSettings = { enabled: true, exclusionMode: 'blacklist', exclusions: ['example.com'] };
            const mockError = new Error('Failed to update moderation settings');
            mockedAxiosInstance.patch.mockRejectedValue(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(updateTextModerationSettings(1, 'links', mockSettings)).rejects.toThrow(mockError);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка обновления настроек модерации для links:', mockError);
            consoleErrorSpy.mockRestore();
        });
    });

    describe('Spam Protection Settings Functions', () => {
        it('getSpamProtectionSettings should call GET and return response data', async () => {
            const mockResponse = { data: { enabled: true, threshold: 0.5 } };
            mockedAxiosInstance.get.mockResolvedValue(mockResponse);

            const result = await getSpamProtectionSettings(1);

            expect(mockedAxiosInstance.get).toHaveBeenCalledWith('/admin/settings/chats/1/spam-protection');
            expect(result).toEqual(mockResponse.data);
        });

        it('getSpamProtectionSettings should throw an error if API call fails', async () => {
            const mockError = new Error('Failed to get spam protection settings');
            mockedAxiosInstance.get.mockRejectedValue(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(getSpamProtectionSettings(1)).rejects.toThrow(mockError);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка получения настроек защиты от спама:', mockError);
            consoleErrorSpy.mockRestore();
        });

        it('updateSpamProtectionSettings should call PATCH with correct data and return response data', async () => {
            const mockSettings = { enabled: true, threshold: 0.7 };
            const mockResponse = { data: { success: true } };
            mockedAxiosInstance.patch.mockResolvedValue(mockResponse);

            const result = await updateSpamProtectionSettings(1, mockSettings);

            expect(mockedAxiosInstance.patch).toHaveBeenCalledWith(
                '/admin/settings/chats/1/spam-protection',
                mockSettings
            );
            expect(result).toEqual(mockResponse.data);
        });

        it('updateSpamProtectionSettings should throw an error if API call fails', async () => {
            const mockSettings = { enabled: true, threshold: 0.7 };
            const mockError = new Error('Failed to update spam protection settings');
            mockedAxiosInstance.patch.mockRejectedValue(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(updateSpamProtectionSettings(1, mockSettings)).rejects.toThrow(mockError);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка обновления настроек защиты от спама:', mockError);
            consoleErrorSpy.mockRestore();
        });
    });
});