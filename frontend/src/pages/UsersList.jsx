import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getUsers } from '../services/api';

const UsersList = () => {
    const [users, setUsers] = useState([]);
    const [pagination, setPagination] = useState({
        number: 1, // Страница 1 по умолчанию
        totalElements: 0,
        totalPages: 0
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                setLoading(true);
                console.log('Запрос страницы:', pagination.number); // Отладка
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
                const errorMessage = error.response?.data?.properties?.errors?.join(', ') || 'Не удалось загрузить список пользователей';
                setError(errorMessage);
            } finally {
                setLoading(false);
            }
        };
        fetchUsers();
    }, [pagination.number]);

    const handlePageChange = (newPage) => {
        // Гарантируем, что newPage >= 1
        if (newPage >= 1 && newPage <= pagination.totalPages) {
            setPagination(prev => ({ ...prev, number: newPage }));
        }
    };

    if (loading) return <div className="loading">Загрузка списка пользователей...</div>;
    if (error) return <div className="error">{error}</div>;

    return (
        <div className="users-container">
            <h2>Список пользователей</h2>
            {users.length > 0 ? (
                <>
                    <table className="users-table">
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Имя</th>
                            <th>Фамилия</th>
                            <th>Имя пользователя</th>
                            <th>Дата добавления</th>
                            <th>Действия</th>
                        </tr>
                        </thead>
                        <tbody>
                        {users.map(user => (
                            <tr key={user.id}>
                                <td>{user.id}</td>
                                <td>{user.firstName || '-'}</td>
                                <td>{user.lastName || '-'}</td>
                                <td>{user.username || '-'}</td>
                                <td>{formatDate(user.additionDate)}</td>
                                <td>
                                    <Link to={`/users/${user.id}`} className="view-button">
                                        Просмотр
                                    </Link>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    {pagination.totalPages > 1 && (
                        <div className="pagination">
                            <button
                                onClick={() => handlePageChange(pagination.number - 1)}
                                disabled={pagination.number === 1}
                                className="pagination-button"
                            >
                                « Предыдущая
                            </button>

                            <span className="pagination-info">
                                Страница {pagination.number} из {pagination.totalPages}
                            </span>

                            <button
                                onClick={() => handlePageChange(pagination.number + 1)}
                                disabled={pagination.number === pagination.totalPages}
                                className="pagination-button"
                            >
                                Следующая »
                            </button>
                        </div>
                    )}
                </>
            ) : (
                <p>Пользователей не найдено</p>
            )}
        </div>
    );
};

const formatDate = (dateString) => {
    if (!dateString) return '-';
    try {
        const date = new Date(dateString);
        return date.toLocaleString('ru-RU', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (e) {
        return dateString;
    }
};

export default UsersList;