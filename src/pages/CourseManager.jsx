import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, DatePicker, message, Tag, Row, Col } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import courseService from '../services/courseService';
import { Link, useNavigate } from 'react-router-dom'; // Thêm useNavigate
import { CalendarOutlined } from '@ant-design/icons'; // Thêm icon


const CourseManager = () => {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  // 1. Hàm lấy danh sách khóa học
  const fetchCourses = async () => {
    setLoading(true);
    try {
      const data = await courseService.getAll();
      setCourses(data);
    } catch (error) {
      console.error("Error fetching courses:", error);
      message.error('Không thể tải danh sách khóa học');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses();
  }, []);

  // 2. Cấu hình các cột cho bảng
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 60,
    },
    {
      title: 'Mã môn',
      dataIndex: 'code',
      key: 'code',
      render: (text) => <Tag color="blue">{text}</Tag>,
    },
    {
      title: 'Tên môn học',
      dataIndex: 'name',
      key: 'name',
      render: (text) => <span className="font-semibold">{text}</span>,
    },
    {
      title: 'Mô tả',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: 'Ngày bắt đầu',
      dataIndex: 'startDate',
      key: 'startDate',
      render: (date) => date ? dayjs(date).format('DD/MM/YYYY') : '-',
    },
    {
      title: 'Ngày kết thúc',
      dataIndex: 'endDate',
      key: 'endDate',
      render: (date) => date ? dayjs(date).format('DD/MM/YYYY') : '-',
    },
  ];

  // 3. Xử lý khi submit form thêm mới
  const handleCreate = async (values) => {
    setSubmitting(true);
    try {
      // Format dữ liệu ngày tháng chuẩn yyyy-MM-dd cho Backend
      const payload = {
        ...values,
        startDate: values.startDate ? values.startDate.format('YYYY-MM-DD') : null,
        endDate: values.endDate ? values.endDate.format('YYYY-MM-DD') : null,
      };

      await courseService.create(payload);
      
      message.success('Tạo khóa học thành công!');
      setIsModalOpen(false);
      form.resetFields();
      fetchCourses(); // Tải lại danh sách sau khi thêm
    } catch (error) {
      console.error("Error creating course:", error);
      message.error('Lỗi khi tạo khóa học');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      {/* Header của trang: Tiêu đề + Nút thêm mới */}
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-2xl font-semibold m-0">Quản lý Khóa học</h2>
        <Button 
          type="primary" 
          icon={<PlusOutlined />} 
          onClick={() => setIsModalOpen(true)}
        >
          Thêm mới
        </Button>
      </div>

      {/* Bảng danh sách */}
      <Table 
        columns={columns} 
        dataSource={courses} 
        rowKey="id" 
        loading={loading}
        pagination={{ pageSize: 10 }}
        className="shadow-sm bg-white rounded-lg"
      />

      {/* Modal Form */}
      <Modal
        title="Thêm khóa học mới"
        open={isModalOpen}
        onCancel={() => setIsModalOpen(false)}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleCreate}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="code"
                label="Mã môn học"
                rules={[{ required: true, message: 'Vui lòng nhập mã môn!' }]}
              >
                <Input placeholder="VD: JAVA01" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="name"
                label="Tên môn học"
                rules={[{ required: true, message: 'Vui lòng nhập tên môn!' }]}
              >
                <Input placeholder="VD: Lập trình Java" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="description"
            label="Mô tả"
          >
            <Input.TextArea rows={3} placeholder="Mô tả nội dung..." />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="startDate"
                label="Ngày bắt đầu"
                rules={[{ required: true, message: 'Chọn ngày bắt đầu!' }]}
              >
                <DatePicker className="w-full" format="DD/MM/YYYY" placeholder="Chọn ngày" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="endDate"
                label="Ngày kết thúc"
                rules={[{ required: true, message: 'Chọn ngày kết thúc!' }]}
              >
                <DatePicker className="w-full" format="DD/MM/YYYY" placeholder="Chọn ngày" />
              </Form.Item>
            </Col>
          </Row>

          <div className="flex justify-end space-x-2 mt-4 border-t pt-4">
            <Button onClick={() => setIsModalOpen(false)}>Hủy</Button>
            <Button type="primary" htmlType="submit" loading={submitting}>
              Lưu khóa học
            </Button>
          </div>
        </Form>
      </Modal>
    </div>
  );
};

export default CourseManager;