import axiosClient from '../api/axiosClient';

const authService = {
  login: async (username, password) => {
    const url = '/auth/login';
    const response = await axiosClient.post(url, { username, password });
    return response.data;
  },

  getCurrentUser: async () => {
    const url = '/auth/me';
    const response = await axiosClient.get(url);
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('accessToken');
  }
};

export default authService;