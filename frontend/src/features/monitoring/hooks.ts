import { useEffect, useRef, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { acknowledgeAlert, fetchAlerts, fetchDeviceLocation, fetchDeviceStatus } from "./api";
import { LiveDeviceEvent } from "./types";
import { useAuthStore } from "@/store/auth-store";

const ALERTS_KEY = ["alerts"];

export function useDeviceStatus(deviceId: number) {
  return useQuery({
    queryKey: ["device-status", deviceId],
    queryFn: () => fetchDeviceStatus(deviceId),
    refetchInterval: 15000,
  });
}

export function useDeviceLocation(deviceId: number) {
  return useQuery({
    queryKey: ["device-location", deviceId],
    queryFn: () => fetchDeviceLocation(deviceId),
    refetchInterval: 15000,
  });
}

export function useAlerts(acknowledged?: boolean) {
  return useQuery({
    queryKey: [...ALERTS_KEY, acknowledged ?? "all"],
    queryFn: () => fetchAlerts(acknowledged),
    refetchInterval: 10000,
  });
}

export function useAcknowledgeAlert() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: acknowledgeAlert,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ALERTS_KEY }),
  });
}

/** Conecta a /ws/devices y acumula los últimos eventos en tiempo real del módulo Monitoreo. */
export function useLiveDeviceEvents(maxEvents = 20) {
  const accessToken = useAuthStore((state) => state.accessToken);
  const [events, setEvents] = useState<LiveDeviceEvent[]>([]);
  const [connected, setConnected] = useState(false);
  const socketRef = useRef<WebSocket | null>(null);

  useEffect(() => {
    if (!accessToken) return;
    const httpUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
    const wsUrl = httpUrl.replace(/^http/, "ws") + `/ws/devices?token=${accessToken}`;
    const socket = new WebSocket(wsUrl);
    socketRef.current = socket;

    socket.onopen = () => setConnected(true);
    socket.onclose = () => setConnected(false);
    socket.onmessage = (message) => {
      try {
        const event = JSON.parse(message.data) as LiveDeviceEvent;
        setEvents((prev) => [event, ...prev].slice(0, maxEvents));
      } catch {
        // ignore malformed frames
      }
    };

    return () => socket.close();
  }, [accessToken, maxEvents]);

  return { events, connected };
}
