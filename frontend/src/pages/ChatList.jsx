import React, { useEffect, useState } from 'react';
import { getChatList } from '../services/api';
import { Link } from 'react-router-dom';

const ChatList = () => {
    const [chats, setChats] = useState([]);

    useEffect(() => {
        const fetchChats = async () => {
            try {
                const data = await getChatList();
                setChats(data);
            } catch (error) {
                console.error('Ошибка загрузки чатов:', error);
            }
        };
        fetchChats();
    }, []);

    return (
        <div>
            <h2>Список чатов</h2>
            <ul>
                {chats.map(chat => (
                    <li key={chat.id}>
                        <Link to={`/chats/${chat.id}`}>{chat.name}</Link>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default ChatList;
