import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getUser } from '../services/api';

const User = () => {
    const { userId } = useParams();
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchUser = async () => {
            try {
                setLoading(true);
                const data = await getUser(userId);
                setUser(data);
                setError(null);
            } catch (error) {
                console.error('Ошибка загрузки пользователя:', error);
                setError('Не удалось загрузить информацию о пользователе');
            } finally {
                setLoading(false);
            }
        };
        fetchUser();
    }, [userId]);

    const handleBack = () => {
        navigate(-1);
    };

    if (loading) return <div className="loading">Загрузка информации о пользователе...</div>;
    if (error) return <div className="error">{error}</div>;
    if (!user) return <div className="not-found">Пользователь не найден</div>;

    return (
        <div className="user-details-container">
            <h2>Информация о пользователе</h2>
            <div className="user-card">
                <div className="user-field">
                    <span className="field-label">ID:</span>
                    <span className="field-value">{user.id}</span>
                </div>
                <div className="user-field">
                    <span className="field-label">Имя:</span>
                    <span className="field-value">{user.firstName || '-'}</span>
                </div>
                <div className="user-field">
                    <span className="field-label">Фамилия:</span>
                    <span className="field-value">{user.lastName || '-'}</span>
                </div>
                <div className="user-field">
                    <span className="field-label">Имя пользователя:</span>
                    <span className="field-value">{user.username || '-'}</span>
                </div>
                <div className="user-field">
                    <span className="field-label">Дата добавления:</span>
                    <span className="field-value">{user.additionDate || '-'}</span>
                </div>
            </div>
            <button onClick={handleBack} className="back-button">
                Назад
            </button>
        </div>
    );
};

export default User;