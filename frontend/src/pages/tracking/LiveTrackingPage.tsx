import React, { useEffect, useState } from 'react'
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet'
import { Icon } from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { useQuery } from '@tanstack/react-query'
import { trackingApi } from '../../api'
import { useAppSelector } from '../../store'
import { useWebSocket } from '../../hooks/useWebSocket'
import { DriverLocation } from '../../types'
import { Wifi, WifiOff, Navigation, Package, MapPin } from 'lucide-react'
import clsx from 'clsx'

// Fix Leaflet default marker icons
const driverIcon = new Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
})

const deliveryIcon = new Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-green.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
})

export default function LiveTrackingPage() {
  const dark = useAppSelector(s => s.ui.darkMode)
  const { driverLocations, connected } = useAppSelector(s => s.tracking)
  const { subscribeToDriver } = useWebSocket()
  const [selectedDriver, setSelectedDriver] = useState<string | null>(null)
  const [trackInput, setTrackInput] = useState('')

  // Fetch all active driver locations on mount
  const { data: activeDrivers } = useQuery({
    queryKey: ['active-drivers'],
    queryFn: () => trackingApi.getAllActiveDrivers().then(r => r.data),
    refetchInterval: 30_000,
  })

  // Default center: New Delhi
  const mapCenter: [number, number] = [28.6139, 77.2090]

  const drivers = Object.values(driverLocations)

  return (
    <div className="h-full flex flex-col gap-4 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between flex-shrink-0">
        <div>
          <h1 className={clsx('text-2xl font-bold', dark ? 'text-white' : 'text-slate-900')}>
            Live Tracking
          </h1>
          <p className={clsx('text-sm mt-1', dark ? 'text-slate-400' : 'text-slate-500')}>
            {drivers.length} active drivers on map
          </p>
        </div>
        <div className="flex items-center gap-2">
          {connected
            ? <span className="flex items-center gap-1.5 text-emerald-500 text-sm font-medium">
                <span className="w-2 h-2 bg-emerald-500 rounded-full animate-pulse" />
                Live Updates
              </span>
            : <span className="flex items-center gap-1.5 text-slate-400 text-sm">
                <WifiOff className="w-4 h-4" /> Offline
              </span>
          }
        </div>
      </div>

      <div className="flex gap-4 flex-1 min-h-0">
        {/* Sidebar */}
        <div className={clsx(
          'w-80 flex-shrink-0 rounded-2xl border overflow-hidden flex flex-col',
          dark ? 'bg-slate-800 border-slate-700' : 'bg-white border-slate-200'
        )}>
          {/* Track shipment */}
          <div className={clsx('p-4 border-b', dark ? 'border-slate-700' : 'border-slate-200')}>
            <p className={clsx('text-sm font-semibold mb-2', dark ? 'text-white' : 'text-slate-800')}>
              Track Shipment
            </p>
            <div className="flex gap-2">
              <input
                value={trackInput}
                onChange={e => setTrackInput(e.target.value)}
                placeholder="Enter tracking number"
                className={clsx(
                  'flex-1 text-sm px-3 py-2 rounded-lg border focus:outline-none focus:ring-2 focus:ring-blue-500',
                  dark ? 'bg-slate-700 border-slate-600 text-white placeholder-slate-500' : 'bg-slate-50 border-slate-200 text-slate-900'
                )}
              />
              <button className="px-3 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-500 transition-colors">
                Track
              </button>
            </div>
          </div>

          {/* Driver list */}
          <div className="flex-1 overflow-y-auto p-2">
            <p className={clsx('text-xs font-semibold px-2 py-2 uppercase tracking-wider', dark ? 'text-slate-500' : 'text-slate-400')}>
              Active Drivers ({drivers.length})
            </p>
            {drivers.length === 0 && (
              <div className="flex flex-col items-center justify-center py-12 text-center">
                <Navigation className="w-8 h-8 text-slate-400 mb-2" />
                <p className={clsx('text-sm', dark ? 'text-slate-500' : 'text-slate-400')}>
                  No active drivers
                </p>
              </div>
            )}
            {drivers.map((driver) => (
              <button
                key={driver.driverId}
                onClick={() => setSelectedDriver(
                  selectedDriver === driver.driverId ? null : driver.driverId
                )}
                className={clsx(
                  'w-full text-left p-3 rounded-xl mb-1 transition-colors',
                  selectedDriver === driver.driverId
                    ? 'bg-blue-600 text-white'
                    : dark
                      ? 'hover:bg-slate-700 text-slate-300'
                      : 'hover:bg-slate-50 text-slate-700'
                )}
              >
                <div className="flex items-center gap-3">
                  <div className={clsx(
                    'w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold flex-shrink-0',
                    selectedDriver === driver.driverId ? 'bg-white/20' : 'bg-emerald-500'
                  )}>
                    D
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm font-medium truncate">Driver {driver.driverId.slice(0, 8)}</p>
                    <p className={clsx('text-xs truncate', selectedDriver === driver.driverId ? 'text-blue-200' : dark ? 'text-slate-500' : 'text-slate-400')}>
                      {driver.speedKmh.toFixed(1)} km/h · {new Date(driver.timestamp).toLocaleTimeString()}
                    </p>
                  </div>
                  {driver.activeShipmentId && (
                    <Package className="w-4 h-4 flex-shrink-0 text-amber-400" />
                  )}
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Map */}
        <div className="flex-1 rounded-2xl overflow-hidden border border-slate-200 dark:border-slate-700 min-h-0">
          <MapContainer
            center={mapCenter}
            zoom={11}
            style={{ height: '100%', width: '100%' }}
            className="z-0"
          >
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
              url={dark
                ? 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png'
                : 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'
              }
            />

            {/* Driver markers */}
            {drivers.map((driver) => (
              <Marker
                key={driver.driverId}
                position={[driver.latitude, driver.longitude]}
                icon={driverIcon}
                eventHandlers={{ click: () => setSelectedDriver(driver.driverId) }}
              >
                <Popup>
                  <div className="text-sm">
                    <p className="font-semibold">Driver {driver.driverId.slice(0, 8)}</p>
                    <p>Speed: {driver.speedKmh.toFixed(1)} km/h</p>
                    <p>Heading: {driver.heading.toFixed(0)}°</p>
                    {driver.activeShipmentId && (
                      <p className="text-blue-600">Active shipment</p>
                    )}
                    <p className="text-xs text-gray-500 mt-1">
                      {new Date(driver.timestamp).toLocaleTimeString()}
                    </p>
                  </div>
                </Popup>
              </Marker>
            ))}
          </MapContainer>
        </div>
      </div>
    </div>
  )
}
