import { createSlice, PayloadAction } from '@reduxjs/toolkit'
interface UIState { darkMode: boolean; sidebarCollapsed: boolean; }
const initial: UIState = { darkMode: localStorage.getItem('darkMode') === 'true', sidebarCollapsed: false }
const uiSlice = createSlice({
  name: 'ui', initialState: initial,
  reducers: {
    toggleDarkMode(state) { state.darkMode = !state.darkMode; localStorage.setItem('darkMode', String(state.darkMode)) },
    toggleSidebar(state) { state.sidebarCollapsed = !state.sidebarCollapsed },
  }
})
export const { toggleDarkMode, toggleSidebar } = uiSlice.actions
export default uiSlice.reducer
