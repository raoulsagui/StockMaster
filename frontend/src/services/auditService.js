import api from './api'

const auditService = {
    async findAll() {
        const response = await api.get('/audit')
        return response.data
    },

    async findByEntityType(type) {
        const response = await api.get(`/audit/entite/${type}`)
        return response.data
    },

    async findByEntity(type, id) {
        const response = await api.get(`/audit/entite/${type}/${id}`)
        return response.data
    },
}

export default auditService