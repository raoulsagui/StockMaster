import api from './api'

const codeBarresService = {
    async getQRCodeProduit(id) {
        const response = await api.get(`/codebarres/produit/${id}`)
        return response.data
    },

    async scannerProduit(code) {
        const response = await api.get(`/codebarres/produit/scan`, { params: { code } })
        return response.data
    },

    async getQRCodeZone(id) {
        const response = await api.get(`/codebarres/zone/${id}`)
        return response.data
    },
}

export default codeBarresService