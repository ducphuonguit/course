import axiosClient from '../api/axiosClient';

const attendanceService = {
  // Gọi API lấy thống kê tổng quan
  getStatistics: async () => {
    const url = '/attendance/statistics';
    const response = await axiosClient.get(url);
    return response.data;
  },
  
  // Bạn có thể thêm các hàm khác ở đây (ví dụ: lấy danh sách điểm danh)
};

export default attendanceService;
