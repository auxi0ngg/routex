import { useEffect, useRef, useCallback } from 'react'
import { Client, IMessage, StompSubscription } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAppDispatch, useAppSelector } from '../store'
import { updateDriverLocation, setConnected } from '../store/slices/trackingSlice'
import { DriverLocation } from '../types'

const WS_URL = import.meta.env.VITE_WS_URL || '/ws/tracking'

export function useWebSocket() {
  const dispatch = useAppDispatch()
  const { tokens } = useAppSelector((s) => s.auth)
  const clientRef = useRef<Client | null>(null)
  const subscriptions = useRef<StompSubscription[]>([])

  const connect = useCallback(() => {
    if (clientRef.current?.active) return

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${tokens?.accessToken ?? ''}`,
      },
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      reconnectDelay: 5000,

      onConnect: () => {
        dispatch(setConnected(true))
        console.log('[WS] Connected to RouteX tracking gateway')

        // Subscribe to all active driver locations
        const sub1 = client.subscribe('/topic/drivers/all', (msg: IMessage) => {
          try {
            const locations: DriverLocation[] = JSON.parse(msg.body)
            locations.forEach((loc) => dispatch(updateDriverLocation(loc)))
          } catch {}
        })
        subscriptions.current.push(sub1)
      },

      onDisconnect: () => {
        dispatch(setConnected(false))
        console.log('[WS] Disconnected from tracking gateway')
      },

      onStompError: (frame) => {
        console.error('[WS] STOMP error:', frame.headers['message'])
      },
    })

    client.activate()
    clientRef.current = client
  }, [dispatch, tokens?.accessToken])

  const subscribeToDriver = useCallback((driverId: string) => {
    const client = clientRef.current
    if (!client?.active) return

    const sub = client.subscribe(`/topic/driver/${driverId}`, (msg: IMessage) => {
      try {
        const location: DriverLocation = JSON.parse(msg.body)
        dispatch(updateDriverLocation(location))
      } catch {}
    })
    subscriptions.current.push(sub)
    return () => sub.unsubscribe()
  }, [dispatch])

  const subscribeToShipment = useCallback(
    (shipmentId: string, onUpdate: (data: any) => void) => {
      const client = clientRef.current
      if (!client?.active) return
      const sub = client.subscribe(`/topic/shipment/${shipmentId}`, (msg: IMessage) => {
        try { onUpdate(JSON.parse(msg.body)) } catch {}
      })
      subscriptions.current.push(sub)
      return () => sub.unsubscribe()
    },
    []
  )

  const disconnect = useCallback(() => {
    subscriptions.current.forEach((s) => s.unsubscribe())
    subscriptions.current = []
    clientRef.current?.deactivate()
    dispatch(setConnected(false))
  }, [dispatch])

  useEffect(() => {
    if (tokens?.accessToken) connect()
    return () => { disconnect() }
  }, [tokens?.accessToken])

  return { connect, disconnect, subscribeToDriver, subscribeToShipment }
}
