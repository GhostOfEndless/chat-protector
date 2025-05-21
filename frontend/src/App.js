import React from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import ChatList from './pages/ChatList';
import Chat from './pages/Chat';
import User from './pages/User';
import Moderation from './pages/Moderation';
import DeletedMessages from './pages/DeletedMessages';
import ProtectedRoute from './components/ProtectedRoute';
import UsersList from './pages/UsersList';
import Home from './pages/Home';

const App = () => (
    <Router>
        <Routes>
            <Route path="/login" element={<Login />} />

            <Route element={<ProtectedRoute />}>
                <Route path="/home" element={<Home />} />
                <Route path="/chats" element={<ChatList />} />
                <Route path="/chats/:chatId" element={<Chat />} />
                <Route path="/chats/:chatId/moderation" element={<Moderation />} />
                <Route path="/chats/:chatId/deleted-messages" element={<DeletedMessages />} />
                <Route path="/users" element={<UsersList />} />
                <Route path="/users/:userId" element={<User />} />
            </Route>

            <Route path="/" element={<Navigate to="/login" />} />

        </Routes>
    </Router>
);

export default App;