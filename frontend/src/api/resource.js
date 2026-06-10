import request from '@/utils/request'

export const getResourcePage = (params) => request({ url: '/admin/resources/list', method: 'get', params })
export const addResource = (data) => request({ url: '/admin/resources/add', method: 'post', data })
export const updateResource = (id, data) => request({ url: `/admin/resources/update/${id}`, method: 'put', data })
export const deleteResource = (id) => request({ url: `/admin/resources/delete/${id}`, method: 'delete' })
export const batchDeleteResources = (ids) => request({ url: '/admin/resources/delete/batch', method: 'delete', data: ids })