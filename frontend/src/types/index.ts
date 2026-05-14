// ─── Auth Types ───────────────────────────────────────────────────────────────
export type Role = 'SUPER_ADMIN' | 'COMPANY_ADMIN' | 'FLEET_MANAGER' | 'WAREHOUSE_MANAGER' | 'DRIVER' | 'CUSTOMER'

export interface AuthUser {
  userId: string
  email: string
  firstName: string
  lastName: string
  role: Role
  organizationId: string | null
}

export interface AuthTokens {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
}

export interface AuthState {
  user: AuthUser | null
  tokens: AuthTokens | null
  isAuthenticated: boolean
  isLoading: boolean
}

// ─── Shipment Types ───────────────────────────────────────────────────────────
export type ShipmentStatus =
  | 'CREATED' | 'ASSIGNED' | 'PICKED_UP' | 'IN_TRANSIT'
  | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'FAILED' | 'RETURNED' | 'CANCELLED'

export type PackageType = 'DOCUMENT' | 'PARCEL' | 'PALLET' | 'FREIGHT' | 'OVERSIZED'

export interface Address {
  addressLine1: string
  addressLine2?: string
  city: string
  state: string
  pincode: string
  country: string
  latitude?: number
  longitude?: number
  contactName: string
  contactPhone: string
}

export interface Shipment {
  id: string
  trackingNumber: string
  organizationId: string
  customerId: string
  status: ShipmentStatus
  pickupAddress: Address
  deliveryAddress: Address
  weightKg?: number
  packageType: PackageType
  packageDescription?: string
  declaredValue?: number
  shippingCost?: number
  assignedDriverId?: string
  assignedVehicleId?: string
  scheduledPickupAt?: string
  scheduledDeliveryAt?: string
  actualPickupAt?: string
  actualDeliveryAt?: string
  estimatedDeliveryAt?: string
  fragile: boolean
  requiresSignature: boolean
  cashOnDelivery: boolean
  codAmount?: number
  priority?: string
  createdAt: string
  updatedAt: string
}

export interface ShipmentStatusHistory {
  id: string
  status: ShipmentStatus
  remarks?: string
  latitude?: number
  longitude?: number
  locationName?: string
  createdAt: string
}

// ─── Vehicle / Fleet Types ────────────────────────────────────────────────────
export type VehicleType = 'BIKE' | 'SCOOTER' | 'AUTO' | 'VAN' | 'TRUCK_SMALL' | 'TRUCK_LARGE' | 'ELECTRIC_BIKE' | 'ELECTRIC_VAN'
export type VehicleStatus = 'AVAILABLE' | 'IN_USE' | 'MAINTENANCE' | 'BREAKDOWN' | 'RETIRED'
export type FuelType = 'PETROL' | 'DIESEL' | 'CNG' | 'ELECTRIC' | 'HYBRID'

export interface Vehicle {
  id: string
  registrationNumber: string
  type: VehicleType
  make: string
  model: string
  year: number
  status: VehicleStatus
  assignedDriverId?: string
  payloadCapacityKg?: number
  fuelType?: FuelType
  odometerKm?: number
  lastMaintenanceDate?: string
  nextMaintenanceDue?: string
  insuranceExpiryDate?: string
  createdAt: string
}

export interface FleetStats {
  total: number
  inUse: number
  available: number
  maintenance: number
  utilizationPercent: number
}

// ─── Driver Types ─────────────────────────────────────────────────────────────
export type DriverStatus = 'ONLINE' | 'OFFLINE' | 'BUSY' | 'ON_BREAK' | 'SUSPENDED'

export interface Driver {
  id: string
  userId: string
  organizationId: string
  firstName: string
  lastName: string
  phone: string
  licenseNumber?: string
  licenseExpiryDate?: string
  status: DriverStatus
  currentVehicleId?: string
  currentShipmentId?: string
  rating: number
  totalDeliveries: number
  successfulDeliveries: number
  totalEarnings: number
  backgroundVerified: boolean
  createdAt: string
}

// ─── Tracking Types ───────────────────────────────────────────────────────────
export interface DriverLocation {
  driverId: string
  latitude: number
  longitude: number
  heading: number
  speedKmh: number
  timestamp: string
  activeShipmentId?: string
}

export interface TrackingEvent {
  id: string
  driverId: string
  shipmentId?: string
  latitude: number
  longitude: number
  heading?: number
  speedKmh?: number
  eventType: string
  createdAt: string
}

// ─── Analytics Types ──────────────────────────────────────────────────────────
export interface DashboardSummary {
  date: string
  shipmentsToday: number
  deliveriesToday: number
  successRate: number
  failedDeliveries: number
  avgDeliveryMinutes: number
}

export interface DailyVolume {
  date: string
  count: number
}

// ─── API Response Types ───────────────────────────────────────────────────────
export interface PagedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export interface ApiError {
  status: number
  message: string
  code: string
  timestamp: string
  fieldErrors?: Record<string, string>
}
