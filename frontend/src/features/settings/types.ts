export interface SecuritySettings {
  passwordMinLength: number;
  requireUppercase: boolean;
  requireDigit: boolean;
  requireSpecial: boolean;
  sessionExpirationMinutes: number;
}

export interface ApiKey {
  id: number;
  name: string;
  prefix: string;
  active: boolean;
  createdAt: string;
  revokedAt: string | null;
}

export interface ApiKeyCreated {
  id: number;
  name: string;
  prefix: string;
  apiKey: string;
}

export interface AuditLogEntry {
  id: number;
  userEmail: string | null;
  action: string;
  detailJson: string | null;
  actionDate: string;
}
