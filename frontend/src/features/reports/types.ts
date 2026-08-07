export interface DeviceEventRow {
  id: number;
  deviceId: number;
  deviceImei: string;
  type: string;
  valueJson: string;
  eventDate: string;
}

export interface EventsQuery {
  from: string;
  to: string;
  type?: string;
  page?: number;
  size?: number;
}
