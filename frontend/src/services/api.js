import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const submitAnalysis = (data) => api.post('/analyses', data).then(res => res.data);
export const getAnalyses = () => api.get('/analyses').then(res => res.data);
export const getAnalysis = (id) => api.get(`/analyses/${id}`).then(res => res.data);

export const submitFeedback = (data) => api.post('/feedback', data).then(res => res.data);
export const getFeedbackStats = () => api.get('/feedback/stats').then(res => res.data);

export const createConfig = (data) => api.post('/config', data).then(res => res.data);
export const getConfigs = () => api.get('/config').then(res => res.data);
export const getConfig = (id) => api.get(`/config/${id}`).then(res => res.data);
export const updateConfig = (id, data) => api.put(`/config/${id}`, data).then(res => res.data);
export const deleteConfig = (id) => api.delete(`/config/${id}`).then(res => res.data);

export default api;
