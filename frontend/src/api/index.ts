import axios, { AxiosError, AxiosRequestConfig } from 'axios'
import { store } from '../store'
import { logout, setTokens } from '../store/slices/authSlice'

const BASE_URL = import.meta.env.VITE_API_URL || '/api'

export const apiClient = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

// ─── Request interceptor — attach JWT ─────────────────────────────────────────
apiClient.interceptors.request.use(
  (config) => {
    const state = store.getState()
    const token = state.auth.tokens?.accessToken
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ─── Response interceptor — handle 401 and token refresh ──────────────────────
let isRefreshing = false
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: Error) => void }> = []

function processQueue(error: Error | null, token: string | null) {
  failedQueue.forEach((p) => (error ? p.reject(error) : p.resolve(token!)))
  failedQueue = []
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean }

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then((token) => {
          originalRequest.headers = { ...originalRequest.headers, Authorization: `Bearer ${token}` }
          return apiClient(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = store.getState().auth.tokens?.refreshToken

      if (!refreshToken) {
        store.dispatch(logout())
        return Promise.reject(error)
      }

      try {
        const response = await axios.post(`${BASE_URL}/v1/auth/refresh`, { refreshToken })
        const { accessToken, refreshToken: newRefreshToken } = response.data
        store.dispatch(setTokens({ accessToken, refreshToken: newRefreshToken }))
        processQueue(null, accessToken)
        originalRequest.headers = { ...originalRequest.headers, Authorization: `Bearer ${accessToken}` }
        return apiClient(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError as Error, null)
        store.dispatch(logout())
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

// ─── API Methods ──────────────────────────────────────────────────────────────

// Auth
export const authApi = {
  login: (email: string, password: string) =>
    apiClient.post('/v1/auth/login', { email, password }),
  register: (data: object) =>
    apiClient.post('/v1/auth/register', data),
  refresh: (refreshToken: string) =>
    apiClient.post('/v1/auth/refresh', { refreshToken }),
  logout: (refreshToken?: string) =>
    apiClient.post('/v1/auth/logout', null, { params: { refreshToken } }),
}

// Shipments
export const shipmentsApi = {
  create: (data: object) => apiClient.post('/v1/shipments', data),
  list: (params: object) => apiClient.get('/v1/shipments', { params }),
  getByTracking: (trackingNumber: string) =>
    apiClient.get(`/v1/shipments/track/${trackingNumber}`),
  updateStatus: (id: string, data: object) =>
    apiClient.patch(`/v1/shipments/${id}/status`, data),
  assignDriver: (id: string, driverId: string, vehicleId: string) =>
    apiClient.post(`/v1/shipments/${id}/assign`, { driverId, vehicleId }),
}

// Fleet
export const fleetApi = {
  listVehicles: (params?: object) => apiClient.get('/v1/vehicles', { params }),
  registerVehicle: (data: object) => apiClient.post('/v1/vehicles', data),
  assignDriver: (vehicleId: string, driverId: string) =>
    apiClient.post(`/v1/vehicles/${vehicleId}/assign/${driverId}`),
  releaseVehicle: (vehicleId: string) =>
    apiClient.post(`/v1/vehicles/${vehicleId}/release`),
  getStats: () => apiClient.get('/v1/vehicles/stats/utilization'),
  logMaintenance: (vehicleId: string, data: object) =>
    apiClient.post(`/v1/vehicles/${vehicleId}/maintenance`, data),
}

// Drivers
export const driversApi = {
  list: (params?: object) => apiClient.get('/v1/drivers', { params }),
  getById: (id: string) => apiClient.get(`/v1/drivers/${id}`),
  updateStatus: (id: string, status: string) =>
    apiClient.patch(`/v1/drivers/${id}/status`, { status }),
}

// Tracking
export const trackingApi = {
  getDriverLocation: (driverId: string) =>
    apiClient.get(`/v1/tracking/drivers/${driverId}/location`),
  getShipmentLocation: (shipmentId: string) =>
    apiClient.get(`/v1/tracking/shipments/${shipmentId}/location`),
  getAllActiveDrivers: () => apiClient.get('/v1/tracking/drivers/active'),
  getShipmentHistory: (shipmentId: string) =>
    apiClient.get(`/v1/tracking/shipments/${shipmentId}/history`),
}

// Analytics
export const analyticsApi = {
  getDashboardSummary: () => apiClient.get('/v1/analytics/dashboard-summary'),
  getDeliverySuccessRate: (from: string, to: string) =>
    apiClient.get('/v1/analytics/delivery-success-rate', { params: { from, to } }),
  getShipmentVolume: (days?: number) =>
    apiClient.get('/v1/analytics/shipment-volume', { params: { days } }),
  getAvgDeliveryTime: (from: string, to: string) =>
    apiClient.get('/v1/analytics/avg-delivery-time', { params: { from, to } }),
}

// Route Optimization
export const routeApi = {
  optimize: (data: object) => apiClient.post('/v1/routes/optimize', data),
  findNearestDriver: (data: object) => apiClient.post('/v1/routes/nearest-driver', data),
}

export default apiClient
