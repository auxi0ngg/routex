import React, { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Package, Plus, Search, Filter, ChevronLeft, ChevronRight } from 'lucide-react'
import { shipmentsApi } from '../../api'
import { Shipment, ShipmentStatus } from '../../types'
import { useAppSelector } from '../../store'
import clsx from 'clsx'
import { format } from 'date-fns'

const STATUS_COLORS: Record<ShipmentStatus, string> = {
  CREATED: 'bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-400',
  ASSIGNED: 'bg-violet-100 text-violet-700 dark:bg-violet-900/40 dark:text-violet-400',
  PICKED_UP: 'bg-amber-100 text-amber-700 dark:bg-amber-900/40 dark:text-amber-400',
  IN_TRANSIT: 'bg-orange-100 text-orange-700 dark:bg-orange-900/40 dark:text-orange-400',
  OUT_FOR_DELIVERY: 'bg-sky-100 text-sky-700 dark:bg-sky-900/40 dark:text-sky-400',
  DELIVERED: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-400',
  FAILED: 'bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-400',
  RETURNED: 'bg-slate-100 text-slate-700 dark:bg-slate-700 dark:text-slate-400',
  CANCELLED: 'bg-slate-100 text-slate-500 dark:bg-slate-700 dark:text-slate-500',
}

const STATUS_OPTIONS: ShipmentStatus[] = [
  'CREATED', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT',
  'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED', 'RETURNED'
]

export default function ShipmentsPage() {
  const dark = useAppSelector(s => s.ui.darkMode)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<ShipmentStatus | ''>('')
  const [page, setPage] = useState(0)
  const PAGE_SIZE = 15

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ['shipments', page, statusFilter],
    queryFn: () => shipmentsApi.list({
      page,
      size: PAGE_SIZE,
      ...(statusFilter ? { status: statusFilter } : {})
    }).then(r => r.data),
    placeholderData: (prev) => prev,
  })

  const shipments: Shipment[] = data?.content ?? []
  const totalPages = data?.totalPages ?? 0

  return (
    <div className="space-y-5 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className={clsx('text-2xl font-bold', dark ? 'text-white' : 'text-slate-900')}>Shipments</h1>
          <p className={clsx('text-sm mt-1', dark ? 'text-slate-400' : 'text-slate-500')}>
            {data?.totalElements ?? '—'} total shipments
          </p>
        </div>
        <button className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-xl text-sm font-medium transition-colors shadow-lg shadow-blue-600/20">
          <Plus className="w-4 h-4" />
          New Shipment
        </button>
      </div>

      {/* Filters */}
      <div className={clsx('flex items-center gap-3 p-4 rounded-2xl border', dark ? 'bg-slate-800 border-slate-700' : 'bg-white border-slate-200')}>
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search by tracking number..."
            className={clsx(
              'w-full pl-10 pr-4 py-2 rounded-lg border text-sm focus:outline-none focus:ring-2 focus:ring-blue-500',
              dark ? 'bg-slate-700 border-slate-600 text-white placeholder-slate-500' : 'bg-slate-50 border-slate-200'
            )}
          />
        </div>
        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-slate-400" />
          <select
            value={statusFilter}
            onChange={e => { setStatusFilter(e.target.value as ShipmentStatus | ''); setPage(0) }}
            className={clsx(
              'text-sm px-3 py-2 rounded-lg border focus:outline-none focus:ring-2 focus:ring-blue-500',
              dark ? 'bg-slate-700 border-slate-600 text-white' : 'bg-slate-50 border-slate-200 text-slate-700'
            )}
          >
            <option value="">All Statuses</option>
            {STATUS_OPTIONS.map(s => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
          </select>
        </div>
      </div>

      {/* Table */}
      <div className={clsx('rounded-2xl border overflow-hidden', dark ? 'bg-slate-800 border-slate-700' : 'bg-white border-slate-200')}>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className={clsx('text-xs font-semibold uppercase tracking-wider', dark ? 'bg-slate-900/50 text-slate-400' : 'bg-slate-50 text-slate-500')}>
                <th className="px-6 py-4 text-left">Tracking #</th>
                <th className="px-6 py-4 text-left">Status</th>
                <th className="px-6 py-4 text-left">Pickup</th>
                <th className="px-6 py-4 text-left">Delivery</th>
                <th className="px-6 py-4 text-left">Package</th>
                <th className="px-6 py-4 text-left">Created</th>
                <th className="px-6 py-4 text-left">Actions</th>
              </tr>
            </thead>
            <tbody className={clsx('divide-y', dark ? 'divide-slate-700' : 'divide-slate-100')}>
              {isLoading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i}>
                    {Array.from({ length: 7 }).map((_, j) => (
                      <td key={j} className="px-6 py-4">
                        <div className={clsx('h-4 rounded animate-pulse', dark ? 'bg-slate-700' : 'bg-slate-100')}
                          style={{ width: `${60 + Math.random() * 40}%` }} />
                      </td>
                    ))}
                  </tr>
                ))
              ) : shipments.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-16 text-center">
                    <Package className="w-12 h-12 text-slate-400 mx-auto mb-3" />
                    <p className={clsx('text-sm', dark ? 'text-slate-500' : 'text-slate-400')}>No shipments found</p>
                  </td>
                </tr>
              ) : shipments.map((shipment) => (
                <tr key={shipment.id} className={clsx('transition-colors', dark ? 'hover:bg-slate-700/50' : 'hover:bg-slate-50')}>
                  <td className="px-6 py-4">
                    <span className={clsx('font-mono text-sm font-semibold', dark ? 'text-blue-400' : 'text-blue-600')}>
                      {shipment.trackingNumber}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className={clsx('inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium', STATUS_COLORS[shipment.status])}>
                      {shipment.status.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className={clsx('text-sm', dark ? 'text-slate-300' : 'text-slate-700')}>
                      {shipment.pickupAddress.city}, {shipment.pickupAddress.state}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className={clsx('text-sm', dark ? 'text-slate-300' : 'text-slate-700')}>
                      {shipment.deliveryAddress.city}, {shipment.deliveryAddress.state}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className={clsx('text-sm', dark ? 'text-slate-400' : 'text-slate-500')}>
                      {shipment.packageType}{shipment.weightKg ? ` · ${shipment.weightKg}kg` : ''}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className={clsx('text-sm', dark ? 'text-slate-400' : 'text-slate-500')}>
                      {format(new Date(shipment.createdAt), 'MMM d, yyyy')}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <button className="text-blue-500 hover:text-blue-400 text-sm font-medium transition-colors">
                      View
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className={clsx('flex items-center justify-between px-6 py-4 border-t', dark ? 'border-slate-700' : 'border-slate-100')}>
            <p className={clsx('text-sm', dark ? 'text-slate-400' : 'text-slate-500')}>
              Page {page + 1} of {totalPages}
            </p>
            <div className="flex gap-2">
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
                className={clsx('p-2 rounded-lg transition-colors disabled:opacity-40', dark ? 'hover:bg-slate-700 text-slate-300' : 'hover:bg-slate-100 text-slate-600')}
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className={clsx('p-2 rounded-lg transition-colors disabled:opacity-40', dark ? 'hover:bg-slate-700 text-slate-300' : 'hover:bg-slate-100 text-slate-600')}
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
