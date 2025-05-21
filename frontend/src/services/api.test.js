import * as apiService from './api';
import axios from 'axios';


jest.mock('axios', () => {
    const mockAxiosInstance = {
        get: jest.fn(),
        post: jest.fn(),
        patch: jest.fn(),
        put: jest.fn(),
        delete: jest.fn(),
        interceptors: {
            request: { use: jest.fn((success, error) => {
                    mockAxiosInstance.interceptors.request.successCallback = success;
                    mockAxiosInstance.interceptors.request.errorCallback = error;
                }) },
            response: { use: jest.fn((success, error) => {
                    mockAxiosInstance.interceptors.response.successCallback = success;
                    mockAxiosInstance.interceptors.response.errorCallback = error;
                }) },
        },
    };
    return {
        create: jest.fn(() => mockAxiosInstance),
    };
});


describe('API Service', () => {
    let mockedApiInstance;
    let requestInterceptorSuccessCallback;
    let requestInterceptorErrorCallback;
    let responseInterceptorSuccessCallback;
    let responseInterceptorErrorCallback;

    beforeAll(() => {
        mockedApiInstance = axios.create.mock.results[0].value;

        if (mockedApiInstance.interceptors.request.successCallback) {
            requestInterceptorSuccessCallback = mockedApiInstance.interceptors.request.successCallback;
        }
        if (mockedApiInstance.interceptors.request.errorCallback) {
            requestInterceptorErrorCallback = mockedApiInstance.interceptors.request.errorCallback;
        }
        if (mockedApiInstance.interceptors.response.successCallback) {
            responseInterceptorSuccessCallback = mockedApiInstance.interceptors.response.successCallback;
        }
        if (mockedApiInstance.interceptors.response.errorCallback) {
            responseInterceptorErrorCallback = mockedApiInstance.interceptors.response.errorCallback;
        }
    });

    beforeEach(() => {
        jest.clearAllMocks();

        Object.defineProperty(window, 'localStorage', {
            value: {
                getItem: jest.fn(),
                removeItem: jest.fn(),
                setItem: jest.fn(),
                clear: jest.fn(),
            },
            writable: true,
            configurable: true,
        });

        const mockLocation = {
            href: 'http://localhost/',
            assign: jest.fn(),
            replace: jest.fn(),
        };
        Object.defineProperty(window, 'location', {
            value: mockLocation,
            writable: true,
            configurable: true,
        });
    });


    describe('authenticate', () => {
        test('should return data on successful authentication', async () => {
            const mockResponseData = { accessToken: 'mock_token', userId: 123 };
            mockedApiInstance.post.mockResolvedValueOnce({ data: mockResponseData });

            const result = await apiService.authenticate('user1', 'pass1');

            expect(mockedApiInstance.post).toHaveBeenCalledWith('/auth/authenticate', { login: 'user1', password: 'pass1' });
            expect(result).toEqual(mockResponseData);
        });


        test('should throw error and log on authentication failure', async () => {
            const mockError = { response: { status: 400, data: { message: 'Bad credentials' } } };
            mockedApiInstance.post.mockRejectedValueOnce(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(apiService.authenticate('wrong_user', 'wrong_pass')).rejects.toEqual(mockError);
            expect(mockedApiInstance.post).toHaveBeenCalledWith('/auth/authenticate', { login: 'wrong_user', password: 'wrong_pass' });
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка аутентификации:', mockError);
            consoleErrorSpy.mockRestore();
        });
    });


    describe('getChatList', () => {
        test('should return chat list data on success', async () => {
            const mockResponseData = [{ id: 1, name: 'Chat A' }, { id: 2, name: 'Chat B' }];
            mockedApiInstance.get.mockResolvedValueOnce({ data: mockResponseData });

            const result = await apiService.getChatList();

            expect(mockedApiInstance.get).toHaveBeenCalledWith('/admin/chats');
            expect(result).toEqual(mockResponseData);
        });


        test('should throw error and log on failure', async () => {
            const mockError = { response: { status: 500, data: { message: 'Server error' } } };
            mockedApiInstance.get.mockRejectedValueOnce(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(apiService.getChatList()).rejects.toEqual(mockError);
            expect(mockedApiInstance.get).toHaveBeenCalledWith('/admin/chats');
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка получения списка чатов:', mockError);
            consoleErrorSpy.mockRestore();
        });
    });


    describe('getChat', () => {
        test('should return chat data on success', async () => {
            const chatId = 123;
            const mockResponseData = { id: 123, name: 'Specific Chat', messages: [] };
            mockedApiInstance.get.mockResolvedValueOnce({ data: mockResponseData });

            const result = await apiService.getChat(chatId);

            expect(mockedApiInstance.get).toHaveBeenCalledWith(`/admin/chats/${chatId}`);
            expect(result).toEqual(mockResponseData);
        });


        test('should throw error and log on failure', async () => {
            const chatId = 123;
            const mockError = { response: { status: 404, data: { message: 'Chat not found' } } };
            mockedApiInstance.get.mockRejectedValueOnce(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(apiService.getChat(chatId)).rejects.toEqual(mockError);
            expect(mockedApiInstance.get).toHaveBeenCalledWith(`/admin/chats/${chatId}`);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка получения чата:', mockError);
            consoleErrorSpy.mockRestore();
        });
    });


    describe('getDeletedMessages', () => {
        test('should return deleted messages with default page', async () => {
            const chatId = 456;
            const mockResponseData = { content: [{ id: 1, text: 'Del Msg 1' }], page: { number: 1 } };
            mockedApiInstance.get.mockResolvedValueOnce({ data: mockResponseData });

            const result = await apiService.getDeletedMessages(chatId);

            expect(mockedApiInstance.get).toHaveBeenCalledWith(`/admin/chats/${chatId}/deleted-messages?page=1`);
            expect(result).toEqual(mockResponseData);
        });


        test('should return deleted messages with specified page', async () => {
            const chatId = 456;
            const page = 3;
            const mockResponseData = { content: [{ id: 10, text: 'Del Msg 10' }], page: { number: 3 } };
            mockedApiInstance.get.mockResolvedValueOnce({ data: mockResponseData });

            const result = await apiService.getDeletedMessages(chatId, page);

            expect(mockedApiInstance.get).toHaveBeenCalledWith(`/admin/chats/${chatId}/deleted-messages?page=${page}`);
            expect(result).toEqual(mockResponseData);
        });


        test('should throw error and log on failure', async () => {
            const chatId = 456;
            const mockError = { response: { status: 500, data: { message: 'Failed to retrieve deleted messages' } } };
            mockedApiInstance.get.mockRejectedValueOnce(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(apiService.getDeletedMessages(chatId)).rejects.toEqual(mockError);
            expect(mockedApiInstance.get).toHaveBeenCalledWith(`/admin/chats/${chatId}/deleted-messages?page=1`);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка получения удаленных сообщений:', mockError);
            consoleErrorSpy.mockRestore();
        });
    });


    describe('getUsers', () => {
        test('should return users with default page and no size', async () => {
            const mockResponseData = { content: [{ id: 1, name: 'User A' }], page: { number: 1, totalElements: 1 } };
            mockedApiInstance.get.mockResolvedValueOnce({ data: mockResponseData });

            const result = await apiService.getUsers();

            expect(mockedApiInstance.get).toHaveBeenCalledWith('/admin/users', { params: { page: 1 } });
            expect(result).toEqual({ users: mockResponseData.content, pagination: mockResponseData.page });
        });


        test('should return users with specified page and size', async () => {
            const page = 2;
            const size = 10;
            const mockResponseData = { content: [{ id: 11, name: 'User K' }], page: { number: 2, size: 10, totalElements: 20 } };
            mockedApiInstance.get.mockResolvedValueOnce({ data: mockResponseData });

            const result = await apiService.getUsers(page, size);

            expect(mockedApiInstance.get).toHaveBeenCalledWith('/admin/users', { params: { page, size } });
            expect(result).toEqual({ users: mockResponseData.content, pagination: mockResponseData.page });
        });

        test('should handle empty content gracefully', async () => {
            const mockResponseData = { content: [], page: { number: 1, totalElements: 0 } };
            mockedApiInstance.get.mockResolvedValueOnce({ data: mockResponseData });

            const result = await apiService.getUsers();

            expect(result.users).toEqual([]);
            expect(result.pagination).toEqual(mockResponseData.page);
        });


        test('should handle undefined content gracefully', async () => {
            const mockResponseData = { page: { number: 1, totalElements: 0 } };
            mockedApiInstance.get.mockResolvedValueOnce({ data: mockResponseData });

            const result = await apiService.getUsers();

            expect(result.users).toEqual([]);
            expect(result.pagination).toEqual(mockResponseData.page);
        });


        test('should return default pagination if response.data.page is missing', async () => {
            const page = 2;
            const size = 5;
            const mockResponseData = { content: [{ id: 1, name: 'User A' }] };
            mockedApiInstance.get.mockResolvedValueOnce({ data: mockResponseData });

            const result = await apiService.getUsers(page, size);

            expect(result.users).toEqual(mockResponseData.content);
            expect(result.pagination).toEqual({
                size: size,
                number: page,
                totalElements: 0,
                totalPages: 0
            });
        });


        test('should return default pagination with default size if response.data.page is missing and size is null', async () => {
            const page = 1;
            const mockResponseData = { content: [{ id: 1, name: 'User A' }] };
            mockedApiInstance.get.mockResolvedValueOnce({ data: mockResponseData });

            const result = await apiService.getUsers(page, null);

            expect(result.pagination).toEqual({
                size: 20,
                number: page,
                totalElements: 0,
                totalPages: 0
            });
        });


        test('should throw error and log it (with response.data) on failure', async () => {
            const errorData = { message: 'Failed to retrieve users' };
            const mockError = { response: { status: 500, data: errorData } };
            mockedApiInstance.get.mockRejectedValueOnce(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(apiService.getUsers()).rejects.toEqual(mockError);
            expect(mockedApiInstance.get).toHaveBeenCalledWith('/admin/users', { params: { page: 1 } });
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка получения списка пользователей:', errorData);
            consoleErrorSpy.mockRestore();
        });


        test('should throw error and log it (generic error) on failure when response.data is missing', async () => {
            const mockError = { response: { status: 500 }, message:"Network Error" };
            mockedApiInstance.get.mockRejectedValueOnce(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(apiService.getUsers()).rejects.toEqual(mockError);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка получения списка пользователей:', mockError);
            consoleErrorSpy.mockRestore();
        });
    });


    describe('getUser', () => {
        test('should return user data on success', async () => {
            const userId = 789;
            const mockResponseData = { id: 789, username: 'testuser' };
            mockedApiInstance.get.mockResolvedValueOnce({ data: mockResponseData });

            const result = await apiService.getUser(userId);

            expect(mockedApiInstance.get).toHaveBeenCalledWith(`/admin/users/${userId}`);
            expect(result).toEqual(mockResponseData);
        });


        test('should throw error and log on failure', async () => {
            const userId = 789;
            const mockError = { response: { status: 404, data: { message: 'User not found' } } };
            mockedApiInstance.get.mockRejectedValueOnce(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(apiService.getUser(userId)).rejects.toEqual(mockError);
            expect(mockedApiInstance.get).toHaveBeenCalledWith(`/admin/users/${userId}`);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка получения пользователя:', mockError);
            consoleErrorSpy.mockRestore();
        });
    });


    describe('getTextModerationSettings', () => {
        test('should return settings on success', async () => {
            const chatId = 'chat123';
            const mockResponseData = { emails: { enabled: true }, links: { enabled: false } };
            mockedApiInstance.get.mockResolvedValueOnce({ data: mockResponseData });

            const result = await apiService.getTextModerationSettings(chatId);


            expect(mockedApiInstance.get).toHaveBeenCalledWith(`/admin/settings/chats/${chatId}/text-moderation`);
            expect(result).toEqual(mockResponseData);
        });


        test('should throw error and log on failure', async () => {
            const chatId = 'chat456';
            const mockError = { response: { status: 500, data: { message: 'Failed to get settings' } } };
            mockedApiInstance.get.mockRejectedValueOnce(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(apiService.getTextModerationSettings(chatId)).rejects.toEqual(mockError);
            expect(mockedApiInstance.get).toHaveBeenCalledWith(`/admin/settings/chats/${chatId}/text-moderation`);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Ошибка получения настроек модерации:', mockError);
            consoleErrorSpy.mockRestore();
        });
    });


    describe('updateTextModerationSettings', () => {
        const chatId = 'chat789';
        const validSettings = { enabled: true, exclusionMode: false, exclusions: ["item1"] };

        test('should successfully update settings for a valid filter type', async () => {
            const filterType = 'links';
            const mockResponseData = { message: 'Settings updated successfully' };
            mockedApiInstance.patch.mockResolvedValueOnce({ data: mockResponseData });

            const result = await apiService.updateTextModerationSettings(chatId, filterType, { ...validSettings });

            expect(mockedApiInstance.patch).toHaveBeenCalledWith(
                `/admin/settings/chats/${chatId}/text-moderation/${filterType}`,
                validSettings
            );
            expect(result).toEqual(mockResponseData);
        });


        test('should throw error for an invalid filter type and not call api', async () => {
            const filterType = 'invalid-filter';
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(apiService.updateTextModerationSettings(chatId, filterType, { ...validSettings }))
                .rejects
                .toThrow(`Неверный тип фильтра: ${filterType}. Доступные типы: emails, tags, mentions, links, phone-numbers, bot-commands, custom-emojis`);

            expect(mockedApiInstance.patch).not.toHaveBeenCalled();
            expect(consoleErrorSpy).not.toHaveBeenCalled();
            consoleErrorSpy.mockRestore();
        });


        test.each([
            ['enabled', { exclusionMode: false, exclusions: [] }],
            ['exclusionMode', { enabled: true, exclusions: [] }],
            ['exclusions', { enabled: true, exclusionMode: false }],
        ])('should throw error if required setting "%s" is missing and not call api', async (missingField, incompleteSettings) => {
            const filterType = 'emails';
            await expect(apiService.updateTextModerationSettings(chatId, filterType, incompleteSettings))
                .rejects
                .toThrow('Для обновления настроек требуются поля: enabled, exclusionMode, exclusions');
            expect(mockedApiInstance.patch).not.toHaveBeenCalled();
        });


        test('should throw API error, log it, on update failure', async () => {
            const filterType = 'mentions';
            const mockError = { response: { status: 500, data: { message: 'Server update failed' } } };
            mockedApiInstance.patch.mockRejectedValueOnce(mockError);
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

            await expect(apiService.updateTextModerationSettings(chatId, filterType, { ...validSettings }))
                .rejects.toEqual(mockError);

            expect(mockedApiInstance.patch).toHaveBeenCalledWith(
                `/admin/settings/chats/${chatId}/text-moderation/${filterType}`,
                validSettings
            );
            expect(consoleErrorSpy).toHaveBeenCalledWith(`Ошибка обновления настроек модерации для ${filterType}:`, mockError);
            consoleErrorSpy.mockRestore();
        });
    });


    describe('Axios Interceptors', () => {
        describe('Request Interceptor', () => {
            test('should add Authorization header if token exists in localStorage', () => {
                expect(requestInterceptorSuccessCallback).toBeDefined();
                const mockToken = 'test-auth-token';
                window.localStorage.getItem.mockReturnValueOnce(mockToken);
                const config = { headers: {} };

                const newConfig = requestInterceptorSuccessCallback(config);

                expect(window.localStorage.getItem).toHaveBeenCalledWith('token');
                expect(newConfig.headers.Authorization).toBe(`Bearer ${mockToken}`);
                expect(newConfig).toBe(config);
            });


            test('should not add Authorization header if token does not exist', () => {
                expect(requestInterceptorSuccessCallback).toBeDefined();
                window.localStorage.getItem.mockReturnValueOnce(null);
                const config = { headers: {} };

                const newConfig = requestInterceptorSuccessCallback(config);

                expect(window.localStorage.getItem).toHaveBeenCalledWith('token');
                expect(newConfig.headers.Authorization).toBeUndefined();
            });


            test('should use token from localStorage even if Authorization header already exists (overwrite)', () => {
                expect(requestInterceptorSuccessCallback).toBeDefined();
                const mockToken = 'new-test-token';
                window.localStorage.getItem.mockReturnValueOnce(mockToken);
                const config = { headers: { Authorization: 'Bearer old-token' } };

                const newConfig = requestInterceptorSuccessCallback(config);
                expect(newConfig.headers.Authorization).toBe(`Bearer ${mockToken}`);
            });


            test('error handler should reject promise', async () => {
                expect(requestInterceptorErrorCallback).toBeDefined();
                const mockError = new Error('Request setup error');
                await expect(requestInterceptorErrorCallback(mockError)).rejects.toBe(mockError);
            });
        });

        describe('Response Interceptor', () => {
            test('success handler should return response directly', () => {
                expect(responseInterceptorSuccessCallback).toBeDefined();
                const mockResponse = { data: 'some data' };
                const result = responseInterceptorSuccessCallback(mockResponse);
                expect(result).toEqual(mockResponse);
            });


            test('error handler should re-throw error if not 401', async () => {
                expect(responseInterceptorErrorCallback).toBeDefined();
                const mockError = { response: { status: 500, data: 'Server Error' } };
                await expect(responseInterceptorErrorCallback(mockError)).rejects.toEqual(mockError);
                expect(window.localStorage.removeItem).not.toHaveBeenCalled();
                expect(window.location.assign).not.toHaveBeenCalled();
            });


            test('error handler should re-throw error if error.response is undefined', async () => {
                expect(responseInterceptorErrorCallback).toBeDefined();
                const mockError = new Error('Network Error');
                await expect(responseInterceptorErrorCallback(mockError)).rejects.toEqual(mockError);
                expect(window.localStorage.removeItem).not.toHaveBeenCalled();
                expect(window.location.assign).not.toHaveBeenCalled();
            });
        });
    });
});