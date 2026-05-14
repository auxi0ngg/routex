import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { DriverLocation } from '../../types'
interface TrackingState { driverLocations: Record<string, DriverLocation>; connected: boolean; }
const trackingSlice = createSlice({
  name: 'tracking',
  initialState: { driverLocations: {}, connected: false } as TrackingState,
  reducers: {
    updateDriverLocation(state, action: PayloadAction<DriverLocation>) {
      state.driverLocations[action.payload.driverId] = action.payload
    },
    setConnected(state, action: PayloadAction<boolean>) { state.connected = action.payload },
    clearLocations(state) { state.driverLocations = {} },
  }
})
export const { updateDriverLocation, setConnected, clearLocations } = trackingSlice.actions
export default trackingSlice.reducer
