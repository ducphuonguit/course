import axiosClient from '../api/axiosClient';

const courseService = {
  // Lấy danh sách khóa học
  getAll: async () => {
    const url = '/courses';
    const response = await axiosClient.get(url);
    return response.data;
  },

  // Tạo khóa học mới
  create: async (data) => {
    const url = '/courses';
    const response = await axiosClient.post(url, data);
    return response.data;
  },
  
  // Bạn có thể thêm update/delete tại đây sau này
};

export default courseService;