import request from '@/utils/request';
export const login = (data) => request({ url: '/auth/login', method: 'post', data });
export const sendCode = (email) => {
  const data = typeof email === 'string' ? { email } : email
  return request({
    url: '/auth/sendEmailCode',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}
export const register = (data) => request({ url: '/auth/register', method: 'post', data }); 
export const resetPassword = (data) => request({ url: '/auth/resetPassword', method: 'post', data });
export const getCurrentUser = () => request({ url: '/auth/currentUser', method: 'get' });

// 科目相关API
export const getSubjectOptions = () => request({ url: '/admin/subjects/options', method: 'get' });

// 画像测试题相关API
export const getAssessmentQuestionsList = (params) => request({ url: '/admin/assessment-questions/list', method: 'get', params });
export const addAssessmentQuestion = (data) => request({ url: '/admin/assessment-questions/add', method: 'post', data });
export const updateAssessmentQuestion = (id, data) => request({ url: `/admin/assessment-questions/update/${id}`, method: 'put', data });
export const deleteAssessmentQuestion = (id) => request({ url: `/admin/assessment-questions/delete/${id}`, method: 'delete' });
export const deleteBatchAssessmentQuestion = (ids) => request({ url: '/admin/assessment-questions/delete/batch', method: 'delete', data: ids });