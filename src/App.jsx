import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import AdminLayout from './layouts/AdminLayout';
// SỬA DÒNG NÀY: Thêm "Page" vào tên file
import Dashboard from './pages/DashboardPage'; 
import CourseManager from './pages/CourseManager';

const PlaceholderPage = ({ title }) => (
  <div className="p-8 text-center bg-gray-50 border border-dashed rounded text-gray-500">
    Tính năng <strong>{title}</strong> đang phát triển.
  </div>
);

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          
          <Route path="courses" element={<CourseManager />} />
          
          <Route path="students" element={<PlaceholderPage title="Quản lý Sinh viên" />} />
        </Route>

        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;