import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getDeletedMessages } from '../services/api';

const DeletedMessages = () => {
    const { chatId } = useParams();
    const [messages, setMessages] = useState([]);
    const [pagination, setPagination] = useState({ number: 0, totalPages: 0 });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
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
        fetchMessages();
    }, [chatId, pagination.number]);

    const handlePrevPage = () => {
        setPagination(prev => ({ ...prev, number: Math.max(prev.number - 1, 0) }));
    };

    const handleNextPage = () => {
        setPagination(prev => ({ ...prev, number: Math.min(prev.number + 1, prev.totalPages - 1) }));
    };

    if (loading) return <div className="loading">Загрузка...</div>;
    if (error) return <div className="error">{error}</div>;

    return (
        <div className="container">
            <h2>Удалённые сообщения для чата {chatId}</h2>
            {messages.length > 0 ? (
                <>
                    <table className="messages-table">
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>ID сообщения</th>
                            <th>Текст сообщения</th>
                            <th>ID пользователя</th>
                            <th>Время удаления</th>
                            <th>Причина</th>
                        </tr>
                        </thead>
                        <tbody>
                        {messages.map((msg) => (
                            <tr key={msg.id}>
                                <td>{msg.id}</td>
                                <td>{msg.messageId}</td>
                                <td>{msg.messageText}</td>
                                <td>{msg.userId}</td>
                                <td>{msg.deletionTime}</td>
                                <td>{msg.reason}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                    <div className="pagination">
                        <button
                            onClick={handlePrevPage}
                            disabled={pagination.number === 0}
                        >
                            Предыдущая
                        </button>
                        <span>
                            Страница {pagination.number + 1} из {pagination.totalPages}
                        </span>
                        <button
                            onClick={handleNextPage}
                            disabled={pagination.number >= pagination.totalPages - 1}
                        >
                            Следующая
                        </button>
                    </div>
                </>
            ) : (
                <p>Удалённых сообщений не найдено</p>
            )}
        </div>
    );
};

export default DeletedMessages;