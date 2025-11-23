import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, Spin, message } from 'antd';
import { 
  TeamOutlined, 
  ReadOutlined, 
  CheckCircleOutlined, 
  CloseCircleOutlined 
} from '@ant-design/icons';
import attendanceService from '../services/attendanceService';

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalStudents: 0,
    totalSessions: 0,
    attendanceRate: 0,
    totalAbsences: 0
  });

  useEffect(() => {
    const fetchStatistics = async () => {
      try {
        const data = await attendanceService.getStatistics();
        // Map dữ liệu từ Backend
        setStats({
          totalStudents: data.totalStudents || 0,
          totalSessions: data.totalSessions || 0,
          attendanceRate: data.attendanceRate || 0,
          // Backend trả về 'absentCount', Frontend map sang 'totalAbsences'
          totalAbsences: data.absentCount || 0 
        });
      } catch (error) {
        console.error("Error fetching stats:", error);
        message.error("Không thể tải dữ liệu thống kê.");
      } finally {
        setLoading(false);
      }
    };

    fetchStatistics();
  }, []);

  if (loading) return <div className="flex justify-center mt-10"><Spin size="large" /></div>;

  return (
    <div>
      <h2 className="text-2xl font-semibold mb-6">Tổng quan hệ thống</h2>
      
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false} className="shadow-sm hover:shadow-md transition-shadow">
            <Statistic
              title="Tổng Sinh Viên"
              value={stats.totalStudents}
              prefix={<TeamOutlined className="text-blue-500" />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false} className="shadow-sm hover:shadow-md transition-shadow">
            <Statistic
              title="Tổng Buổi Học"
              value={stats.totalSessions}
              prefix={<ReadOutlined className="text-purple-500" />}
            />
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false} className="shadow-sm hover:shadow-md transition-shadow">
            <Statistic
              title="Tỉ lệ Đi Học"
              value={stats.attendanceRate}
              precision={1}
              valueStyle={{ color: stats.attendanceRate > 75 ? '#3f8600' : '#cf1322' }}
              prefix={<CheckCircleOutlined className="text-green-500" />}
              suffix="%"
            />
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false} className="shadow-sm hover:shadow-md transition-shadow">
            <Statistic
              title="Số Lượt Vắng"
              value={stats.totalAbsences}
              valueStyle={{ color: '#cf1322' }}
              prefix={<CloseCircleOutlined className="text-red-500" />}
            />
          </Card>
        </Col>
      </Row>

      <div className="mt-8 bg-white p-4 rounded shadow-sm min-h-[300px] flex items-center justify-center border border-gray-100 border-dashed">
        <span className="text-gray-400">Biểu đồ thống kê chi tiết sẽ hiển thị ở đây</span>
      </div>
    </div>
  );
};

export default Dashboard;