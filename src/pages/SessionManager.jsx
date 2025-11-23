import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Table, Button, Modal, Tag, Space, message, Typography, Spin, Drawer, Badge } from 'antd';
import { 
  QrcodeOutlined, 
  ArrowLeftOutlined, 
  EnvironmentOutlined,
  ClockCircleOutlined,
  UserOutlined 
} from '@ant-design/icons';
import { QRCodeCanvas } from 'qrcode.react'; 
import dayjs from 'dayjs';
import sessionService from '../services/sessionService';

const { Title, Text } = Typography;

const SessionManager = () => {
  const { courseId } = useParams();
  const navigate = useNavigate();
  
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(false);
  
  // State cho Modal QR
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [qrLoading, setQrLoading] = useState(false);
  const [qrData, setQrData] = useState(null);

  // --- State MỚI cho Drawer điểm danh ---
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [attendanceList, setAttendanceList] = useState([]);
  const [attendanceLoading, setAttendanceLoading] = useState(false);
  const [selectedSession, setSelectedSession] = useState(null);

  // 1. Fetch danh sách buổi học
  useEffect(() => {
    const fetchSessions = async () => {
      if (!courseId) return;
      setLoading(true);
      try {
        const data = await sessionService.getByCourseId(courseId);
        setSessions(data);
      } catch (error) {
        message.error('Không thể tải danh sách buổi học');
      } finally {
        setLoading(false);
      }
    };
    fetchSessions();
  }, [courseId]);

  // 2. Xử lý QR Code
  const handleShowQr = async (session) => {
    setQrLoading(true);
    try {
      const data = await sessionService.generateQr(session.id);
      setQrData(data);
      setIsModalOpen(true);
    } catch (error) {
      message.error('Lỗi khi tạo mã QR');
    } finally {
      setQrLoading(false);
    }
  };

  // 3. --- MỚI: Xử lý xem danh sách điểm danh ---
  const handleViewAttendance = async (session) => {
    setSelectedSession(session);
    setIsDrawerOpen(true);
    setAttendanceLoading(true);
    try {
      const data = await sessionService.getSessionAttendance(session.id);
      setAttendanceList(data);
    } catch (error) {
      message.error('Không thể tải danh sách điểm danh');
    } finally {
      setAttendanceLoading(false);
    }
  };

  // Cấu hình cột cho bảng Buổi học
  const sessionColumns = [
    { title: 'ID', dataIndex: 'id', width: 60, align: 'center' },
    { 
      title: 'Chủ đề (Click xem chi tiết)', 
      dataIndex: 'topic', 
      render: (text, record) => (
        <a 
          onClick={() => handleViewAttendance(record)} 
          className="font-semibold text-blue-600 hover:underline cursor-pointer flex items-center gap-2"
        >
          {text} <UserOutlined style={{ fontSize: '12px' }}/>
        </a>
      )
    },
    { 
      title: 'Phòng học', 
      dataIndex: 'room', 
      render: (text) => <Tag icon={<EnvironmentOutlined />} color="cyan">{text}</Tag> 
    },
    { 
      title: 'Thời gian', 
      render: (_, record) => (
        <div className="text-sm text-gray-600">
           <div>{dayjs(record.startTime).format('HH:mm')} - {dayjs(record.endTime).format('HH:mm')}</div>
           <div className="text-xs text-gray-400">{dayjs(record.startTime).format('DD/MM/YYYY')}</div>
        </div>
      )
    },
    {
      title: 'Hành động',
      key: 'action',
      align: 'right',
      render: (_, record) => (
        <Button 
          type="primary" 
          icon={<QrcodeOutlined />} 
          onClick={() => handleShowQr(record)}
          className="bg-purple-600 hover:bg-purple-500 border-purple-600"
        >
          Chiếu QR
        </Button>
      ),
    },
  ];

  // --- MỚI: Cấu hình cột cho bảng Danh sách điểm danh (trong Drawer) ---
  const attendanceColumns = [
    { 
      title: 'Mã SV', 
      dataIndex: 'studentCode', 
      key: 'studentCode',
      render: (text) => <Text strong>{text}</Text>
    },
    { 
      title: 'Tên Sinh Viên', 
      dataIndex: 'studentName', 
      key: 'studentName' 
    },
    { 
      title: 'Giờ Check-in', 
      dataIndex: 'checkInTime', 
      key: 'checkInTime',
      render: (text) => (
        <Space>
          <ClockCircleOutlined className="text-gray-400"/>
          {text ? dayjs(text).format('HH:mm:ss') : '-'}
        </Space>
      )
    },
    { 
      title: 'Trạng thái', 
      dataIndex: 'status', 
      key: 'status',
      align: 'center',
      render: (status) => {
        let color = 'default';
        let label = 'Unknown';
        
        if (status === 'PRESENT') {
          color = 'success';
          label = 'Có mặt';
        } else if (status === 'LATE') {
          color = 'warning';
          label = 'Đi muộn';
        } else if (status === 'ABSENT') {
          color = 'error';
          label = 'Vắng';
        }

        return <Badge status={color} text={label} />;
      }
    },
  ];

  return (
    <div>
      <div className="flex items-center mb-6">
        <Button 
          icon={<ArrowLeftOutlined />} 
          onClick={() => navigate('/admin/courses')} 
          className="mr-4"
        >
          Quay lại
        </Button>
        <div>
          <Title level={3} style={{ margin: 0 }}>
            Quản lý Buổi học
          </Title>
          <Text type="secondary">Khóa học ID: #{courseId}</Text>
        </div>
      </div>

      <Table 
        columns={sessionColumns} 
        dataSource={sessions} 
        rowKey="id" 
        loading={loading} 
        className="shadow-sm rounded-lg border border-gray-100"
        pagination={false}
      />

      {/* MODAL QR CODE */}
      <Modal
        title={null}
        open={isModalOpen}
        onCancel={() => setIsModalOpen(false)}
        footer={null}
        centered
        width={380}
      >
        <div className="flex flex-col items-center justify-center p-4">
          <Title level={4} className="mb-4 text-purple-600">Điểm Danh QR Code</Title>
          {qrData ? (
            <>
              <div className="border-4 border-gray-800 p-3 rounded-xl bg-white shadow-inner">
                <QRCodeCanvas 
                  value={JSON.stringify({ sessionId: qrData.sessionId, token: qrData.token })} 
                  size={240}
                  level={"H"}
                  includeMargin={true}
                />
              </div>
              <div className="mt-6 text-center w-full">
                 <div className="bg-red-50 text-red-600 px-4 py-2 rounded-lg inline-block border border-red-100">
                    Hết hạn lúc: <strong>{dayjs(qrData.expirationTime).format('HH:mm:ss')}</strong>
                 </div>
                 <p className="mt-3 text-xs text-gray-400">Sinh viên sử dụng App LMS để quét mã này</p>
              </div>
            </>
          ) : (
            <div className="py-10"><Spin size="large" /></div>
          )}
        </div>
      </Modal>

      {/* DRAWER DANH SÁCH ĐIỂM DANH */}
      <Drawer
        title={
          <div>
            <div className="text-sm text-gray-500">Danh sách điểm danh</div>
            <div className="font-bold text-lg">{selectedSession?.topic}</div>
          </div>
        }
        placement="right"
        width={600}
        onClose={() => setIsDrawerOpen(false)}
        open={isDrawerOpen}
      >
        <Table
          columns={attendanceColumns}
          dataSource={attendanceList}
          loading={attendanceLoading}
          rowKey="studentCode"
          pagination={{ pageSize: 10 }}
        />
      </Drawer>
    </div>
  );
};

export default SessionManager;