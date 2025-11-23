import axiosClient from '../api/axiosClient';
import dayjs from 'dayjs';

const sessionService = {
  // Lấy danh sách buổi học theo Course ID
  getByCourseId: async (courseId) => {
    // --- MOCK DATA ---
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve([
          { 
            id: 101, 
            topic: 'Bài 1: Giới thiệu Java', 
            room: 'Phòng A101', 
            startTime: dayjs().add(1, 'day').hour(8).minute(0).toISOString(),
            endTime: dayjs().add(1, 'day').hour(10).minute(0).toISOString(),
          },
          { 
            id: 102, 
            topic: 'Bài 2: OOP cơ bản', 
            room: 'Phòng Lab 3', 
            startTime: dayjs().add(3, 'day').hour(13).minute(30).toISOString(),
            endTime: dayjs().add(3, 'day').hour(15).minute(30).toISOString(),
          },
        ]);
      }, 500);
    });
  },

  // Sinh mã QR cho một buổi học
  generateQr: async (sessionId) => {
    // --- MOCK DATA ---
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          sessionId: sessionId,
          token: `QR_TOKEN_${Math.random().toString(36).substring(7)}`, 
          expirationTime: dayjs().add(5, 'minute').toISOString()
        });
      }, 600);
    });
  },

  // --- MỚI: Lấy danh sách điểm danh của một buổi học ---
  getSessionAttendance: async (sessionId) => {
    // const url = `/attendance/session/${sessionId}`;
    // const response = await axiosClient.get(url);
    // return response.data;

    // --- MOCK DATA ---
    return new Promise((resolve) => {
      setTimeout(() => {
        // Giả lập dữ liệu trả về ngẫu nhiên cho sinh động
        resolve([
          { studentCode: 'SV001', studentName: 'Nguyễn Văn A', checkInTime: '2023-11-20T08:05:00', status: 'PRESENT' },
          { studentCode: 'SV002', studentName: 'Trần Thị B', checkInTime: '2023-11-20T08:15:00', status: 'LATE' },
          { studentCode: 'SV003', studentName: 'Lê Văn C', checkInTime: '2023-11-20T08:00:00', status: 'PRESENT' },
          { studentCode: 'SV004', studentName: 'Phạm Thị D', checkInTime: '2023-11-20T08:20:00', status: 'LATE' },
          { studentCode: 'SV005', studentName: 'Hoàng Văn E', checkInTime: '2023-11-20T07:55:00', status: 'PRESENT' },
        ]);
      }, 700);
    });
  }
};

export default sessionService;