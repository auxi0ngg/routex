import React from 'react'
import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import {
  LayoutDashboard, Package, MapPin, Truck, Users, BarChart3,
  Bell, Warehouse, Menu, X, Moon, Sun, LogOut, Wifi, WifiOff
} from 'lucide-react'
import { useAppDispatch, useAppSelector } from '../../store'
import { toggleDarkMode, toggleSidebar } from '../../store/slices/uiSlice'
import { logoutThunk } from '../../store/slices/authSlice'
import clsx from 'clsx'

const NAV_ITEMS = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/shipments', icon: Package, label: 'Shipments' },
  { to: '/tracking', icon: MapPin, label: 'Live Tracking' },
  { to: '/fleet', icon: Truck, label: 'Fleet' },
  { to: '/drivers', icon: Users, label: 'Drivers' },
  { to: '/warehouses', icon: Warehouse, label: 'Warehouses' },
  { to: '/analytics', icon: BarChart3, label: 'Analytics' },
  { to: '/notifications', icon: Bell, label: 'Notifications' },
]

export default function Layout() {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const { darkMode, sidebarCollapsed } = useAppSelector((s) => s.ui)
  const { user } = useAppSelector((s) => s.auth)
  const { connected } = useAppSelector((s) => s.tracking)

  const handleLogout = async () => {
    await dispatch(logoutThunk())
    navigate('/login')
  }

  return (
    <div className={clsx('flex h-screen overflow-hidden', darkMode ? 'dark bg-slate-900' : 'bg-slate-50')}>
      {/* Sidebar */}
      <aside className={clsx(
        'flex flex-col transition-all duration-300 border-r',
        darkMode ? 'bg-slate-900 border-slate-700' : 'bg-white border-slate-200',
        sidebarCollapsed ? 'w-16' : 'w-64'
      )}>
        {/* Logo */}
        <div className="flex items-center h-16 px-4 border-b border-slate-200 dark:border-slate-700">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center flex-shrink-0">
              <Truck className="w-4 h-4 text-white" />
            </div>
            {!sidebarCollapsed && (
              <span className="font-bold text-lg text-slate-900 dark:text-white tracking-tight">
                RouteX
              </span>
            )}
          </div>
        </div>

        {/* Nav */}
        <nav className="flex-1 overflow-y-auto p-2">
          {NAV_ITEMS.map(({ to, icon: Icon, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) => clsx(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg mb-1 transition-colors text-sm font-medium',
                isActive
                  ? 'bg-blue-600 text-white'
                  : darkMode
                    ? 'text-slate-400 hover:text-white hover:bg-slate-800'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
              )}
              title={sidebarCollapsed ? label : undefined}
            >
              <Icon className="w-5 h-5 flex-shrink-0" />
              {!sidebarCollapsed && <span>{label}</span>}
            </NavLink>
          ))}
        </nav>

        {/* Bottom controls */}
        <div className="p-2 border-t border-slate-200 dark:border-slate-700 space-y-1">
          <button
            onClick={() => dispatch(toggleDarkMode())}
            className={clsx(
              'flex items-center gap-3 w-full px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
              darkMode ? 'text-slate-400 hover:text-white hover:bg-slate-800' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
            )}
          >
            {darkMode ? <Sun className="w-5 h-5 flex-shrink-0" /> : <Moon className="w-5 h-5 flex-shrink-0" />}
            {!sidebarCollapsed && (darkMode ? 'Light Mode' : 'Dark Mode')}
          </button>
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 w-full px-3 py-2.5 rounded-lg text-sm font-medium text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
          >
            <LogOut className="w-5 h-5 flex-shrink-0" />
            {!sidebarCollapsed && 'Sign Out'}
          </button>
        </div>
      </aside>

      {/* Main content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Topbar */}
        <header className={clsx(
          'h-16 flex items-center justify-between px-6 border-b flex-shrink-0',
          darkMode ? 'bg-slate-900 border-slate-700' : 'bg-white border-slate-200'
        )}>
          <button
            onClick={() => dispatch(toggleSidebar())}
            className={clsx('p-2 rounded-lg transition-colors',
              darkMode ? 'text-slate-400 hover:bg-slate-800' : 'text-slate-500 hover:bg-slate-100'
            )}
          >
            {sidebarCollapsed ? <Menu className="w-5 h-5" /> : <X className="w-5 h-5" />}
          </button>

          <div className="flex items-center gap-4">
            {/* Live connection indicator */}
            <div className="flex items-center gap-2">
              {connected
                ? <Wifi className="w-4 h-4 text-emerald-500" />
                : <WifiOff className="w-4 h-4 text-slate-400" />
              }
              <span className={clsx('text-xs font-medium',
                connected ? 'text-emerald-500' : 'text-slate-400'
              )}>
                {connected ? 'Live' : 'Offline'}
              </span>
            </div>

            {/* User info */}
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white text-sm font-semibold">
                {user?.firstName?.[0]}{user?.lastName?.[0]}
              </div>
              <div className="hidden md:block">
                <div className={clsx('text-sm font-medium', darkMode ? 'text-white' : 'text-slate-900')}>
                  {user?.firstName} {user?.lastName}
                </div>
                <div className="text-xs text-slate-500">{user?.role?.replace('_', ' ')}</div>
              </div>
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
