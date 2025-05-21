import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getUsers } from '../services/api';

const formatDate = (dateString) => {
    if (!dateString) return <span className="text-gray-400 italic">Не указана</span>;
    try {
        const date = new Date(dateString);
        return date.toLocaleString('ru-RU', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
        });
    } catch (e) {
        console.error("Invalid date format:", dateString);
        return <span className="text-red-500 italic">Ошибка даты</span>;
    }
};


const UsersList = () => {
    const [users, setUsers] = useState([]);
    const [pagination, setPagination] = useState({
        number: 1,
        totalElements: 0,
        totalPages: 0
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                setLoading(true);
                const result = await getUsers(pagination.number);
                setUsers(result.users);
                setPagination({
                    number: result.pagination.number,
                    totalElements: result.pagination.totalElements,
                    totalPages: result.pagination.totalPages
                });
                setError(null);
            } catch (error) {
                console.error('Ошибка загрузки пользователей:', error.response?.data || error);
                const errorMessage = error.response?.data?.properties?.errors?.join(', ') || 'Не удалось загрузить список пользователей. Попробуйте обновить страницу.';
                setError(errorMessage);
            } finally {
                setLoading(false);
            }
        };
        fetchUsers();
    }, [pagination.number]);

    const handlePageChange = (newPage) => {
        if (newPage >= 1 && newPage <= pagination.totalPages && newPage !== pagination.number) {
            setPagination(prev => ({ ...prev, number: newPage }));
        }
    };

    if (loading) return (
        <div className="flex justify-center items-center min-h-[calc(100vh-150px)] bg-gray-100">
            <div className="p-6 bg-white rounded-lg shadow-xl text-center">
                <svg className="animate-spin h-8 w-8 text-[#3366ff] mx-auto mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <p className="text-lg font-medium text-gray-700">Загрузка списка пользователей...</p>
            </div>
        </div>
    );

    if (error) return (
        <div className="flex justify-center items-center min-h-[calc(100vh-150px)] bg-red-50 p-4">
            <div className="bg-white p-8 rounded-lg shadow-xl text-center max-w-lg">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 text-[#3366ff] mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                <h2 className="text-2xl font-semibold text-red-700 mb-3">Ошибка загрузки</h2>
                <p className="text-md text-gray-600">{error}</p>
                <button
                    onClick={() => window.location.reload()}
                    className="mt-6 inline-flex items-center px-6 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition duration-150 ease-in-out"
                >
                    Обновить страницу
                </button>
            </div>
        </div>
    );

    return (
        <div className="min-h-screen bg-gray-100 py-8 px-4 sm:px-6 lg:px-8">
            <div className="max-w-6xl mx-auto">
                <h2 className="text-3xl font-bold text-gray-800 mb-8 text-center sm:text-left">
                    Список пользователей Telegram
                </h2>

                {users.length > 0 ? (
                    <div className="bg-white shadow-xl rounded-lg overflow-x-auto">
                        <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                            <tr>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Имя</th>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Фамилия</th>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Username</th>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Дата добавления</th>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Действия</th>
                            </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                            {users.map((user, index) => (
                                <tr key={user.id} className={index % 2 === 0 ? 'bg-white' : 'bg-gray-50 hover:bg-gray-100'}>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{user.id}</td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">{user.firstName || <span className="text-gray-400 italic">Нет</span>}</td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">{user.lastName || <span className="text-gray-400 italic">Нет</span>}</td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">{user.username ? `@${user.username}` : <span className="text-gray-400 italic">Нет</span>}</td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">{formatDate(user.additionDate)}</td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                        <Link
                                            to={`/users/${user.id}`}
                                            className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md shadow-sm text-white bg-[#3366ff] hover:bg-[#2255ee] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#3366ff] transition-colors duration-150"
                                        >
                                            Просмотр
                                        </Link>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                        {pagination.totalPages > 1 && (
                            <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6 rounded-b-lg">
                                <div className="flex-1 flex justify-between sm:hidden">
                                    <button
                                        onClick={() => handlePageChange(pagination.number - 1)}
                                        disabled={pagination.number === 1}
                                        className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        Предыдущая
                                    </button>
                                    <button
                                        onClick={() => handlePageChange(pagination.number + 1)}
                                        disabled={pagination.number === pagination.totalPages}
                                        className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        Следующая
                                    </button>
                                </div>
                                <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                                    <div>
                                        <p className="text-sm text-gray-700">
                                            Показано <span className="font-medium">{(pagination.number - 1) * (users.length / (pagination.totalPages > 0 ? pagination.totalPages/pagination.totalPages : 1)) + 1}</span>
                                            {' '} по <span className="font-medium">{Math.min(pagination.number * (users.length / (pagination.totalPages > 0 ? pagination.totalPages/pagination.totalPages : 1)), pagination.totalElements)}</span>
                                            {' '} из <span className="font-medium">{pagination.totalElements}</span> результатов
                                        </p>
                                    </div>
                                    <div>
                                        <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                                            <button
                                                onClick={() => handlePageChange(pagination.number - 1)}
                                                disabled={pagination.number === 1}
                                                className="relative inline-flex items-center px-3 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                                            >
                                                <span className="sr-only">Предыдущая</span>
                                                Пред.
                                            </button>
                                            <span className="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700">
                                                {pagination.number} / {pagination.totalPages}
                                            </span>
                                            <button
                                                onClick={() => handlePageChange(pagination.number + 1)}
                                                disabled={pagination.number === pagination.totalPages}
                                                className="relative inline-flex items-center px-3 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                                            >
                                                <span className="sr-only">Следующая</span>
                                                След.
                                            </button>
                                        </nav>
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>
                ) : (
                    <div className="bg-white shadow-xl rounded-lg p-12 text-center">
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 text-gray-400 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 14l9-5-9-5-9 5 9 5z" />
                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z" />
                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 14l9-5-9-5-9 5 9 5zm0 0l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14zm-4 6v-7.5l4-2.222" />
                        </svg>
                        <p className="text-xl font-medium text-gray-600">Пользователи не найдены</p>
                        <p className="text-sm text-gray-500 mt-1">В системе пока нет зарегистрированных пользователей или они не соответствуют критериям поиска.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default UsersList;