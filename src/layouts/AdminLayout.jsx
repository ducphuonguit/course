import React, { useState } from 'react';
import { Layout, Menu, Button, theme, Avatar, Dropdown, message } from 'antd';
import { 
  DashboardOutlined, 
  BookOutlined, 
  TeamOutlined, 
  MenuFoldOutlined, 
  MenuUnfoldOutlined, 
  UserOutlined, 
  LogoutOutlined 
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import authService from '../services/authService';

const { Header, Sider, Content } = Layout;

const AdminLayout = () => {
  const [collapsed, setCollapsed] = useState(false);
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();
  
  const navigate = useNavigate();
  const location = useLocation();

  // Xử lý đăng xuất
  const handleLogout = () => {
    authService.logout();
    message.success('Đã đăng xuất');
    navigate('/login');
  };

  // Menu item mapping
  const menuItems = [
    {
      key: '/admin/dashboard',
      icon: <DashboardOutlined />,
      label: 'Dashboard',
    },
    {
      key: '/admin/courses',
      icon: <BookOutlined />,
      label: 'Quản lý Khóa học',
    },
    {
      key: '/admin/students',
      icon: <TeamOutlined />,
      label: 'Quản lý Sinh viên',
    },
  ];

  const userMenu = (
    <Menu items={[
      { key: 'logout', label: 'Đăng xuất', icon: <LogoutOutlined />, onClick: handleLogout }
    ]} />
  );

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed} theme="dark">
        <div className="h-16 flex items-center justify-center bg-white bg-opacity-10 m-2 rounded text-white font-bold overflow-hidden whitespace-nowrap">
          {collapsed ? 'LMS' : 'LMS ADMIN'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          defaultSelectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header style={{ padding: 0, background: colorBgContainer }} className="flex justify-between items-center pr-4">
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{ fontSize: '16px', width: 64, height: 64 }}
          />
          <Dropdown overlay={userMenu} placement="bottomRight">
            <div className="flex items-center cursor-pointer hover:bg-gray-100 px-4 h-full transition-colors">
              <Avatar icon={<UserOutlined />} className="mr-2" />
              <span className="font-medium">Admin User</span>
            </div>
          </Dropdown>
        </Header>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            minHeight: 280,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
            overflow: 'auto'
          }}
        >
          {/* Nơi hiển thị nội dung các trang con (Dashboard, Students...) */}
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default AdminLayout;