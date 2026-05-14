import React, { useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Provider } from 'react-redux'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'react-hot-toast'
import { store, useAppSelector } from './store'
import Layout from './components/layout/Layout'
import LoginPage from './pages/auth/LoginPage'
import RegisterPage from './pages/auth/RegisterPage'
import DashboardPage from './pages/dashboard/DashboardPage'
import ShipmentsPage from './pages/shipments/ShipmentsPage'
import LiveTrackingPage from './pages/tracking/LiveTrackingPage'
import FleetPage from './pages/fleet/FleetPage'
import DriversPage from './pages/drivers/DriversPage'
import AnalyticsPage from './pages/analytics/AnalyticsPage'
import NotificationsPage from './pages/notifications/NotificationsPage'
import WarehousePage from './pages/warehouses/WarehousePage'
import { useWebSocket } from './hooks/useWebSocket'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 2,
      refetchOnWindowFocus: false,
    },
  },
})

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAppSelector((s) => s.auth)
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return <>{children}</>
}

function AppContent() {
  const { isAuthenticated } = useAppSelector((s) => s.auth)
  const { darkMode } = useAppSelector((s) => s.ui)
  const { connect } = useWebSocket()

  useEffect(() => {
    document.documentElement.classList.toggle('dark', darkMode)
  }, [darkMode])

  useEffect(() => {
    if (isAuthenticated) connect()
  }, [isAuthenticated])

  return (
    <>
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/track/:trackingNumber" element={<LiveTrackingPage />} />

        {/* Protected routes inside layout */}
        <Route path="/" element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="shipments" element={<ShipmentsPage />} />
          <Route path="tracking" element={<LiveTrackingPage />} />
          <Route path="fleet" element={<FleetPage />} />
          <Route path="drivers" element={<DriversPage />} />
          <Route path="analytics" element={<AnalyticsPage />} />
          <Route path="notifications" element={<NotificationsPage />} />
          <Route path="warehouses" element={<WarehousePage />} />
        </Route>

        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>

      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            background: darkMode ? '#1e293b' : '#fff',
            color: darkMode ? '#f1f5f9' : '#1e293b',
            border: `1px solid ${darkMode ? '#334155' : '#e2e8f0'}`,
          },
        }}
      />
    </>
  )
}

export default function App() {
  return (
    <Provider store={store}>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <AppContent />
        </BrowserRouter>
      </QueryClientProvider>
    </Provider>
  )
}
