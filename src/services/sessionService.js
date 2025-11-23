import axiosClient from '../api/axiosClient';

const sessionService = {
  // Lấy danh sách buổi học theo Course ID (Real API)
  getByCourseId: async (courseId) => {
    const url = `/admin/sessions/course/${courseId}`;
    const response = await axiosClient.get(url);
    // Mapping dữ liệu từ Backend về format Frontend mong muốn
    return response.data.map(session => ({
      id: session.id,
      // Backend Session không có 'topic', tạm dùng ngày tháng làm topic
      topic: `Buổi học ngày ${session.sessionDate ? session.sessionDate.split('T')[0] : ''}`,
      // Backend Session không có 'room', tạm để trống
      room: 'N/A', 
      startTime: session.startTime,
      endTime: session.endTime,
      ...session
    }));
  },

  // Sinh mã QR cho một buổi học (Real API)
  generateQr: async (sessionId) => {
    // Gọi API: POST /api/admin/sessions/{id}/generate-qr?validityMinutes=10
    const url = `/admin/sessions/${sessionId}/generate-qr?validityMinutes=10`;
    const response = await axiosClient.post(url);
    
    // Mapping response backend (qrToken, expiresAt) sang format frontend (token, expirationTime)
    return {
      sessionId: response.data.sessionId,
      token: response.data.qrToken,
      expirationTime: response.data.expiresAt
    };
  },

  // Lấy danh sách điểm danh của một buổi học (Real API)
  getSessionAttendance: async (sessionId) => {
    const url = `/attendance/session/${sessionId}`;
    const response = await axiosClient.get(url);
    
    // Mapping response backend (studentNumber, checkedAt) sang format frontend
    return response.data.map(record => ({
      studentCode: record.studentNumber,
      studentName: record.studentName,
      checkInTime: record.checkedAt,
      status: record.status
    }));
  }
};

export default sessionService;