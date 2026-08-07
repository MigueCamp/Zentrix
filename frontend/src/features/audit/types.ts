export interface AuditLogEntry {
  id: number;
  userEmail: string;
  action: string;
  detailJson: string | null;
  actionDate: string;
}

export interface AuditLogPage {
  content: AuditLogEntry[];
  totalElements: number;
}
