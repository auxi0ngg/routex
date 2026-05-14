import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit'
import { AuthState, AuthUser, AuthTokens } from '../../types'
import { authApi } from '../../api'

const STORAGE_KEY = 'routex_auth'

function loadFromStorage(): Partial<AuthState> {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : {}
  } catch { return {} }
}

function saveToStorage(state: AuthState) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ user: state.user, tokens: state.tokens }))
  } catch {}
}

const persisted = loadFromStorage()

const initialState: AuthState = {
  user: persisted.user ?? null,
  tokens: persisted.tokens ?? null,
  isAuthenticated: !!persisted.tokens?.accessToken,
  isLoading: false,
}

// ─── Async thunks ─────────────────────────────────────────────────────────────

export const loginThunk = createAsyncThunk(
  'auth/login',
  async ({ email, password }: { email: string; password: string }, { rejectWithValue }) => {
    try {
      const { data } = await authApi.login(email, password)
      return data
    } catch (err: any) {
      return rejectWithValue(err.response?.data?.message ?? 'Login failed')
    }
  }
)

export const logoutThunk = createAsyncThunk('auth/logout', async (_, { getState }) => {
  const state = getState() as { auth: AuthState }
  try {
    await authApi.logout(state.auth.tokens?.refreshToken)
  } catch {}
})

// ─── Slice ────────────────────────────────────────────────────────────────────

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setTokens(state, action: PayloadAction<{ accessToken: string; refreshToken: string }>) {
      if (state.tokens) {
        state.tokens.accessToken = action.payload.accessToken
        state.tokens.refreshToken = action.payload.refreshToken
        saveToStorage(state)
      }
    },
    logout(state) {
      state.user = null
      state.tokens = null
      state.isAuthenticated = false
      localStorage.removeItem(STORAGE_KEY)
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginThunk.pending, (state) => { state.isLoading = true })
      .addCase(loginThunk.fulfilled, (state, action) => {
        const data = action.payload
        state.tokens = {
          accessToken: data.accessToken,
          refreshToken: data.refreshToken,
          tokenType: data.tokenType,
          expiresIn: data.expiresIn,
        }
        state.user = {
          userId: data.userId,
          email: data.email,
          firstName: data.firstName,
          lastName: data.lastName,
          role: data.role,
          organizationId: data.organizationId,
        }
        state.isAuthenticated = true
        state.isLoading = false
        saveToStorage(state)
      })
      .addCase(loginThunk.rejected, (state) => { state.isLoading = false })
      .addCase(logoutThunk.fulfilled, (state) => {
        state.user = null
        state.tokens = null
        state.isAuthenticated = false
        localStorage.removeItem(STORAGE_KEY)
      })
  },
})

export const { setTokens, logout } = authSlice.actions
export default authSlice.reducer
