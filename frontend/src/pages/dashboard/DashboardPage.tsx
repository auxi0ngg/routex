import React from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  Package, Truck, Users, TrendingUp, Clock, CheckCircle,
  XCircle, ArrowUpRight, RefreshCw
} from 'lucide-react'
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts'
import { analyticsApi, fleetApi } from '../../api'
import { useAppSelector } from '../../store'
import clsx from 'clsx'

function StatCard({ title, value, subtitle, icon: Icon, trend, color }: {
  title: string; value: string | number; subtitle: string;
  icon: React.ElementType; trend?: number; color: string;
}) {
  const dark = useAppSelector(s => s.ui.darkMode)
  return (
    <div className={clsx(
      'rounded-2xl p-6 border transition-all hover:shadow-lg',
      dark ? 'bg-slate-800 border-slate-700' : 'bg-white border-slate-200'
    )}>
      <div className="flex items-start justify-between">
        <div>
          <p className={clsx('text-sm font-medium', dark ? 'text-slate-400' : 'text-slate-500')}>{title}</p>
          <p className={clsx('text-3xl font-bold mt-1', dark ? 'text-white' : 'text-slate-900')}>{value}</p>
          <p className={clsx('text-sm mt-1', dark ? 'text-slate-500' : 'text-slate-400')}>{subtitle}</p>
        </div>
        <div className={clsx('w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0', color)}>
          <Icon className="w-6 h-6 text-white" />
        </div>
      </div>
      {trend !== undefined && (
        <div className={clsx('flex items-center gap-1 mt-4 text-sm font-medium', trend >= 0 ? 'text-emerald-500' : 'text-red-500')}>
          <ArrowUpRight className={clsx('w-4 h-4', trend < 0 && 'rotate-180')} />
          <span>{Math.abs(trend)}% vs yesterday</span>
        </div>
      )}
    </div>
  )
}

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444']

export default function DashboardPage() {
  const dark = useAppSelector(s => s.ui.darkMode)
  const { user } = useAppSelector(s => s.auth)

  const { data: summary, isLoading: summaryLoading, refetch } = useQuery({
    queryKey: ['dashboard-summary'],
    queryFn: () => analyticsApi.getDashboardSummary().then(r => r.data),
    refetchInterval: 60_000,
  })

  const { data: volumeData } = useQuery({
    queryKey: ['shipment-volume'],
    queryFn: () => analyticsApi.getShipmentVolume(14).then(r => r.data),
  })

  const { data: fleetStats } = useQuery({
    queryKey: ['fleet-stats'],
    queryFn: () => fleetApi.getStats().then(r => r.data),
  })

  const fleetPieData = fleetStats ? [
    { name: 'In Use', value: fleetStats.inUse },
    { name: 'Available', value: fleetStats.available },
    { name: 'Maintenance', value: fleetStats.maintenance },
  ] : []

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className={clsx('text-2xl font-bold', dark ? 'text-white' : 'text-slate-900')}>
            Good {getGreeting()}, {user?.firstName} 👋
          </h1>
          <p className={clsx('text-sm mt-1', dark ? 'text-slate-400' : 'text-slate-500')}>
            Here's what's happening with your logistics today.
          </p>
        </div>
        <button
          onClick={() => refetch()}
          className={clsx(
            'flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-colors border',
            dark ? 'border-slate-700 text-slate-300 hover:bg-slate-800' : 'border-slate-200 text-slate-600 hover:bg-slate-50'
          )}
        >
          <RefreshCw className="w-4 h-4" />
          Refresh
        </button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Shipments Today"
          value={summaryLoading ? '—' : summary?.shipmentsToday ?? 0}
          subtitle="Total created today"
          icon={Package}
          trend={5.2}
          color="bg-blue-600"
        />
        <StatCard
          title="Deliveries Completed"
          value={summaryLoading ? '—' : summary?.deliveriesToday ?? 0}
          subtitle="Successfully delivered"
          icon={CheckCircle}
          trend={2.8}
          color="bg-emerald-600"
        />
        <StatCard
          title="Success Rate"
          value={summaryLoading ? '—' : `${summary?.successRate ?? 0}%`}
          subtitle="Delivery success rate"
          icon={TrendingUp}
          trend={0.5}
          color="bg-violet-600"
        />
        <StatCard
          title="Avg Delivery Time"
          value={summaryLoading ? '—' : `${summary?.avgDeliveryMinutes ?? 0}m`}
          subtitle="Average minutes per delivery"
          icon={Clock}
          trend={-3.1}
          color="bg-amber-600"
        />
      </div>

      {/* Charts row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Volume Chart */}
        <div className={clsx('lg:col-span-2 rounded-2xl p-6 border', dark ? 'bg-slate-800 border-slate-700' : 'bg-white border-slate-200')}>
          <h3 className={clsx('text-base font-semibold mb-4', dark ? 'text-white' : 'text-slate-900')}>
            Shipment Volume (14 days)
          </h3>
          <ResponsiveContainer width="100%" height={220}>
            <AreaChart data={volumeData?.daily ?? []}>
              <defs>
                <linearGradient id="colorVolume" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke={dark ? '#334155' : '#f1f5f9'} />
              <XAxis dataKey="date" tick={{ fontSize: 11, fill: dark ? '#94a3b8' : '#64748b' }} />
              <YAxis tick={{ fontSize: 11, fill: dark ? '#94a3b8' : '#64748b' }} />
              <Tooltip
                contentStyle={{
                  background: dark ? '#1e293b' : '#fff',
                  border: `1px solid ${dark ? '#334155' : '#e2e8f0'}`,
                  borderRadius: 8,
                  color: dark ? '#f1f5f9' : '#1e293b'
                }}
              />
              <Area type="monotone" dataKey="count" stroke="#3b82f6" strokeWidth={2} fill="url(#colorVolume)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Fleet Pie */}
        <div className={clsx('rounded-2xl p-6 border', dark ? 'bg-slate-800 border-slate-700' : 'bg-white border-slate-200')}>
          <h3 className={clsx('text-base font-semibold mb-4', dark ? 'text-white' : 'text-slate-900')}>
            Fleet Status
          </h3>
          {fleetStats && (
            <>
              <ResponsiveContainer width="100%" height={180}>
                <PieChart>
                  <Pie data={fleetPieData} cx="50%" cy="50%" innerRadius={50} outerRadius={80} paddingAngle={3} dataKey="value">
                    {fleetPieData.map((_, i) => <Cell key={i} fill={COLORS[i]} />)}
                  </Pie>
                  <Legend iconType="circle" iconSize={8} wrapperStyle={{ fontSize: 12, color: dark ? '#94a3b8' : '#64748b' }} />
                </PieChart>
              </ResponsiveContainer>
              <div className="text-center">
                <p className={clsx('text-2xl font-bold', dark ? 'text-white' : 'text-slate-900')}>
                  {fleetStats.total}
                </p>
                <p className={clsx('text-xs', dark ? 'text-slate-400' : 'text-slate-500')}>Total vehicles</p>
              </div>
            </>
          )}
        </div>
      </div>

      {/* Recent activity placeholder */}
      <div className={clsx('rounded-2xl border p-6', dark ? 'bg-slate-800 border-slate-700' : 'bg-white border-slate-200')}>
        <h3 className={clsx('text-base font-semibold mb-4', dark ? 'text-white' : 'text-slate-900')}>Recent Activity</h3>
        <div className="space-y-3">
          {[
            { icon: Package, label: 'Shipment RTX928374 created', time: '2 min ago', color: 'bg-blue-600' },
            { icon: CheckCircle, label: 'Shipment RTX928100 delivered', time: '15 min ago', color: 'bg-emerald-600' },
            { icon: Truck, label: 'Driver Rahul Singh went online', time: '32 min ago', color: 'bg-violet-600' },
            { icon: XCircle, label: 'Delivery RTX927900 failed — rescheduled', time: '1 hr ago', color: 'bg-amber-600' },
          ].map((item, i) => (
            <div key={i} className="flex items-center gap-4">
              <div className={clsx('w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0', item.color)}>
                <item.icon className="w-4 h-4 text-white" />
              </div>
              <div className="flex-1 min-w-0">
                <p className={clsx('text-sm font-medium truncate', dark ? 'text-white' : 'text-slate-900')}>{item.label}</p>
              </div>
              <span className={clsx('text-xs whitespace-nowrap', dark ? 'text-slate-500' : 'text-slate-400')}>{item.time}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

function getGreeting() {
  const h = new Date().getHours()
  if (h < 12) return 'morning'
  if (h < 17) return 'afternoon'
  return 'evening'
}
