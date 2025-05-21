import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getDeletedMessages } from '../services/api';
import {
    ArrowPathIcon,
    ChevronLeftIcon,
    ChevronRightIcon,
    ExclamationCircleIcon,
    TrashIcon,
    ClockIcon,
    UserIcon,
    ChatBubbleLeftEllipsisIcon,
    ArrowLeftIcon
} from '@heroicons/react/24/outline';

const DeletedMessages = () => {
    const { chatId } = useParams();
    const [messages, setMessages] = useState([]);
    const [pagination, setPagination] = useState({ number: 0, totalPages: 0 });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showDetails, setShowDetails] = useState(null);

    useEffect(() => {
        fetchMessages();
    }, [chatId, pagination.number]);

    const fetchMessages = async () => {
        try {
            setLoading(true);
            const data = await getDeletedMessages(chatId, pagination.number + 1);
            setMessages(data.content || []);
            setPagination(data.page || { number: 0, totalPages: 0 });
            setError(null);
        } catch (error) {
            console.error('Ошибка загрузки удалённых сообщений:', error);
            setError('Не удалось загрузить удалённые сообщения');
        } finally {
            setLoading(false);
        }
    };

    const handlePrevPage = () => {
        setPagination(prev => ({ ...prev, number: Math.max(prev.number - 1, 0) }));
    };

    const handleNextPage = () => {
        setPagination(prev => ({ ...prev, number: Math.min(prev.number + 1, prev.totalPages - 1) }));
    };

    const handleRefresh = () => {
        fetchMessages();
    };

    const toggleDetails = (id) => {
        setShowDetails(showDetails === id ? null : id);
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'Н/Д';
        const date = new Date(dateString);
        return date.toLocaleString('ru-RU', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen bg-gray-50">
                <div className="flex flex-col items-center">
                    <ArrowPathIcon className="w-12 h-12 text-blue-500 animate-spin" />
                    <p className="mt-4 text-lg font-medium text-gray-700">Загрузка данных...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex items-center justify-center min-h-screen bg-gray-50">
                <div className="max-w-md p-6 bg-white rounded-lg shadow-lg">
                    <div className="flex items-center justify-center mb-4">
                        <ExclamationCircleIcon className="w-12 h-12 text-red-500" />
                    </div>
                    <h2 className="mb-4 text-xl font-bold text-center text-gray-800">Произошла ошибка</h2>
                    <p className="mb-6 text-center text-gray-600">{error}</p>
                    <div className="flex justify-center">
                        <button
                            onClick={handleRefresh}
                            className="flex items-center px-4 py-2 font-medium text-white bg-blue-500 rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-300"
                        >
                            <ArrowPathIcon className="w-5 h-5 mr-2" />
                            Повторить загрузку
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-7xl px-4 py-8 mx-auto sm:px-6 lg:px-8">
                <div className="mb-8">
                    <div className="flex items-center justify-between mb-6">
                        <div className="flex items-center">
                            <Link to={`/chats/${chatId}`} className="flex items-center mr-4 text-blue-500 hover:text-blue-700">
                                <ArrowLeftIcon className="w-5 h-5 mr-1" />
                                <span>Назад</span>
                            </Link>
                            <h1 className="text-2xl font-bold text-gray-800 absolute left-1/2 transform -translate-x-1/2">
                                Удалённые сообщения
                            </h1>
                        </div>
                        <button
                            onClick={handleRefresh}
                            className="flex items-center px-3 py-2 text-sm font-medium text-blue-600 bg-blue-100 rounded-md hover:bg-blue-200 focus:outline-none focus:ring-2 focus:ring-blue-300"
                        >
                            <ArrowPathIcon className="w-4 h-4 mr-2" />
                            Обновить
                        </button>
                    </div>
                    <div className="p-4 mb-6 bg-white rounded-lg shadow-sm">
                        <div className="flex items-center">
                            <ChatBubbleLeftEllipsisIcon className="w-5 h-5 mr-2 text-blue-500" />
                            <h2 className="text-lg font-medium text-gray-700">Чат ID: {chatId}</h2>
                        </div>
                    </div>
                </div>

                {messages.length > 0 ? (
                    <div className="overflow-hidden bg-white shadow-sm sm:rounded-lg">
                        <div className="overflow-x-auto">
                            <div className="inline-block min-w-full align-middle">
                                <div className="overflow-hidden">
                                    <table className="min-w-full divide-y divide-gray-200">
                                        <thead className="bg-gray-50">
                                        <tr>
                                            <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">ID</th>
                                            <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">Сообщение</th>
                                            <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">Пользователь</th>
                                            <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">Удаление</th>
                                            <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">Действия</th>
                                        </tr>
                                        </thead>
                                        <tbody className="bg-white divide-y divide-gray-200">
                                        {messages.map((msg) => (
                                            <React.Fragment key={msg.id}>
                                                <tr className="transition-colors hover:bg-gray-50">
                                                    <td className="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">
                                                        <div className="font-mono">{msg.id}</div>
                                                    </td>
                                                    <td className="px-6 py-4 text-sm text-gray-500">
                                                        <div className="flex items-start">
                                                            <ChatBubbleLeftEllipsisIcon className="flex-shrink-0 w-5 h-5 mt-0.5 mr-2 text-gray-400" />
                                                            <div>
                                                                <div className="font-medium text-gray-900">ID: {msg.messageId}</div>
                                                                <div className="max-w-md overflow-hidden text-ellipsis">
                                                                    {msg.messageText && msg.messageText.length > 100
                                                                        ? `${msg.messageText.substring(0, 100)}...`
                                                                        : msg.messageText || '<Нет текста>'}
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </td>
                                                    <td className="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">
                                                        <div className="flex items-center">
                                                            <UserIcon className="w-5 h-5 mr-2 text-gray-400" />
                                                            <span>{msg.userId || 'Н/Д'}</span>
                                                        </div>
                                                    </td>
                                                    <td className="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">
                                                        <div className="flex items-center">
                                                            <ClockIcon className="w-5 h-5 mr-2 text-gray-400" />
                                                            <span>{formatDate(msg.deletionTime)}</span>
                                                        </div>
                                                    </td>
                                                    <td className="px-6 py-4 text-sm font-medium whitespace-nowrap">
                                                        <button
                                                            onClick={() => toggleDetails(msg.id)}
                                                            className="text-blue-600 hover:text-blue-900"
                                                        >
                                                            {showDetails === msg.id ? 'Скрыть' : 'Подробнее'}
                                                        </button>
                                                    </td>
                                                </tr>
                                                {showDetails === msg.id && (
                                                    <tr className="bg-gray-50">
                                                        <td colSpan="5" className="px-6 py-4">
                                                            <div className="p-4 bg-white rounded-md shadow-inner">
                                                                <h4 className="mb-3 text-lg font-medium text-gray-900">Подробная информация</h4>
                                                                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                                                                    <div>
                                                                        <h5 className="text-sm font-medium text-gray-500">ID сообщения</h5>
                                                                        <p className="text-gray-900">{msg.messageId}</p>
                                                                    </div>
                                                                    <div>
                                                                        <h5 className="text-sm font-medium text-gray-500">ID пользователя</h5>
                                                                        <p className="text-gray-900">{msg.userId || 'Не указан'}</p>
                                                                    </div>
                                                                    <div>
                                                                        <h5 className="text-sm font-medium text-gray-500">Время удаления</h5>
                                                                        <p className="text-gray-900">{formatDate(msg.deletionTime)}</p>
                                                                    </div>
                                                                    <div>
                                                                        <h5 className="text-sm font-medium text-gray-500">Причина удаления</h5>
                                                                        <p className="text-gray-900 break-words">
                                                                                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
                                                                                    <TrashIcon className="w-3 h-3 mr-1" />
                                                                                    {msg.reason || 'Не указана'}
                                                                                </span>
                                                                        </p>
                                                                    </div>
                                                                </div>
                                                                <div className="mt-4">
                                                                    <h5 className="text-sm font-medium text-gray-500">Полный текст сообщения</h5>
                                                                    <div className="p-3 mt-1 overflow-auto text-sm bg-gray-100 rounded-md max-h-32">
                                                                        {msg.messageText || '<Нет текста>'}
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                )}
                                            </React.Fragment>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>

                        <div className="flex items-center justify-between px-4 py-3 bg-white border-t border-gray-200 sm:px-6">
                            <div className="flex items-center text-sm text-gray-700">
                                <span>Показаны записи {pagination.number * messages.length + 1} - {Math.min((pagination.number + 1) * messages.length, pagination.totalPages * messages.length)} из {pagination.totalPages * messages.length}</span>
                            </div>
                            <div className="flex justify-between flex-1 sm:justify-end">
                                <button
                                    onClick={handlePrevPage}
                                    disabled={pagination.number === 0}
                                    className={`relative inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md ${
                                        pagination.number === 0
                                            ? 'cursor-not-allowed opacity-50'
                                            : 'hover:bg-gray-50'
                                    }`}
                                >
                                    <ChevronLeftIcon className="w-5 h-5 mr-2" />
                                    Назад
                                </button>
                                <button
                                    onClick={handleNextPage}
                                    disabled={pagination.number >= pagination.totalPages - 1}
                                    className={`relative inline-flex items-center px-4 py-2 ml-3 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md ${
                                        pagination.number >= pagination.totalPages - 1
                                            ? 'cursor-not-allowed opacity-50'
                                            : 'hover:bg-gray-50'
                                    }`}
                                >
                                    Вперёд
                                    <ChevronRightIcon className="w-5 h-5 ml-2" />
                                </button>
                            </div>
                        </div>
                    </div>
                ) : (
                    <div className="flex flex-col items-center justify-center p-12 bg-white rounded-lg shadow-sm">
                        <TrashIcon className="w-16 h-16 text-gray-300" />
                        <h3 className="mt-4 text-xl font-medium text-gray-900">Удалённых сообщений не найдено</h3>
                        <p className="mt-2 text-gray-500">В данном чате нет удалённых сообщений или они ещё не загружены.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default DeletedMessages;