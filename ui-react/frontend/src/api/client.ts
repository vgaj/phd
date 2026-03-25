export interface SummaryRow extends SummaryResult {
  scoreNumeric: number;
}

export interface SummaryResult {
  source: string;
  destination: string;
  sourceIp: string;
  destinationIp: string;
  lastSeen: string;
  isCurrent: string;
  score: string;
  details: string[];
}

export interface SummaryResponse {
  results: SummaryResult[];
  messages: string[];
}

export interface HostHistoryResponse {
  source: string;
  destination: string;
  history: string[];
}

export async function fetchSummary(): Promise<SummaryResponse> {
  const res = await fetch('/api/summary');
  if (!res.ok) throw new Error(`Failed to fetch summary: ${res.status}`);
  return res.json();
}

export async function fetchHistory(source: string, destination: string): Promise<HostHistoryResponse> {
  const params = new URLSearchParams({ source, destination });
  const res = await fetch(`/api/history?${params}`);
  if (!res.ok) throw new Error(`Failed to fetch history: ${res.status}`);
  return res.json();
}

