import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { getChat } from '../services/api';
import { CogIcon, TrashIcon } from '@heroicons/react/24/outline'; // Оставляем иконки

const Chat = () => {
    const { chatId } = useParams();
    const [chat, setChat] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchChat = async () => {
            setLoading(true);
            setError(null);
            try {
                // Имитация задержки для демонстрации загрузчика
                // await new Promise(resolve => setTimeout(resolve, 1000));
                const data = await getChat(chatId);
                setChat(data);
                if (!data) { // Если API вернуло null или undefined для чата
                    setError('Не удалось найти информацию о чате.');
                }
            } catch (err) {
                console.error('Ошибка загрузки чата:', err);
                setError('Не удалось загрузить информацию о чате. Пожалуйста, попробуйте еще раз.');
            } finally {
                setLoading(false);
            }
        };
        fetchChat();
    }, [chatId]);

    const handleBack = () => {
        navigate(-1); // Возвращает на предыдущую страницу
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-screen bg-gray-100">
                <div className="p-6 bg-white rounded-lg shadow-xl text-center">
                    <svg className="animate-spin h-8 w-8 text-[#3366ff] mx-auto mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    <p className="text-lg font-medium text-gray-700">Загрузка информации о чате...</p>
                </div>
            </div>
        );
    }

    if (error || !chat) { // Показываем ошибку если есть ошибка или чат не найден
        return (
            <div className="flex justify-center items-center min-h-screen bg-red-50 p-4">
                <div className="bg-white p-8 rounded-lg shadow-xl text-center max-w-md">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 text-red-500 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                    <h2 className="text-2xl font-semibold text-red-700 mb-3">
                        {error ? "Ошибка" : "Чат не найден"}
                    </h2>
                    <p className="text-md text-gray-600">
                        {error || "Не удалось найти информацию по указанному чату."}
                    </p>
                    <button
                        onClick={handleBack}
                        className="mt-6 inline-flex items-center px-6 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition duration-150 ease-in-out"
                    >
                        Назад
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-100 py-8 px-4 sm:px-6 lg:px-8 flex flex-col items-center">
            <div className="w-full max-w-lg"> {/* Можно использовать max-w-lg или max-w-xl для схожести с карточкой User */}
                <button
                    onClick={handleBack}
                    className="mb-6 inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition duration-150 ease-in-out group"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2 text-[#3366ff] group-hover:text-blue-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    Назад
                </button>

                <div className="bg-white shadow-xl rounded-lg overflow-hidden">
                    <div className="bg-[#3366ff] p-4 sm:p-6">
                        <h2 className="text-2xl sm:text-3xl font-bold text-white text-center">
                            Управление чатом: {chat.name || 'Без названия'}
                        </h2>
                    </div>

                    <div className="p-6 sm:p-8 space-y-4"> {/* Уменьшил space-y для кнопок */}
                        <Link
                            to={`/chats/${chatId}/deleted-messages`}
                            className="w-full flex items-center justify-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-yellow-500 hover:bg-yellow-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-400 transition duration-150 ease-in-out"
                        >
                            <TrashIcon className="h-5 w-5 mr-2" />
                            Удалённые сообщения
                        </Link>
                        <Link
                            to={`/chats/${chatId}/moderation`}
                            className="w-full flex items-center justify-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-indigo-500 hover:bg-indigo-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-400 transition duration-150 ease-in-out"
                        >
                            <CogIcon className="h-5 w-5 mr-2" />
                            Настройки модерации
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Chat;