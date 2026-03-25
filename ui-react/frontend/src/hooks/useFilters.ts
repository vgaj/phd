import { useState, useMemo } from 'react';
import { SummaryRow } from '../api/client';
import { SortState } from './useSorting';

export type SortColumn =
  | 'source'
  | 'destination'
  | 'lastSeen'
  | 'isCurrent'
  | 'scoreNumeric';

export type LastSeenWindow = 'hour' | 'day' | 'week' | 'all';

export interface FilterState {
  source: string;
  destination: string;
  currentOnly: boolean;
  minScore: number;
  lastSeenWindow: LastSeenWindow;
}

export interface UseFiltersResult {
  filters: FilterState;
  setFilters: React.Dispatch<React.SetStateAction<FilterState>>;
  filtered: SummaryRow[];
  sourceOptions: string[];
  destinationOptions: string[];
}

function comparator(a: SummaryRow, b: SummaryRow, sort: SortState<SortColumn>): number {
  if (!sort.column) return 0;
  const col = sort.column;
  let cmp = 0;

  if (col === 'scoreNumeric') {
    cmp = (a[col] as number) - (b[col] as number);
  } else if (col === 'lastSeen') {
    cmp = new Date(a.lastSeen).getTime() - new Date(b.lastSeen).getTime();
  } else {
    cmp = String(a[col]).localeCompare(String(b[col]));
  }

  return sort.direction === 'asc' ? cmp : -cmp;
}

export function useFilters(rows: SummaryRow[], sort: SortState<SortColumn>): UseFiltersResult {
  const [filters, setFilters] = useState<FilterState>({
    source: '',
    destination: '',
    currentOnly: false,
    minScore: 0,
    lastSeenWindow: 'all',
  });

  const sourceOptions = useMemo<string[]>(
    () => ['', ...Array.from(new Set(rows.map(r => r.source))).sort()],
    [rows]
  );

  const destinationOptions = useMemo<string[]>(
    () => ['', ...Array.from(new Set(rows.map(r => r.destination))).sort()],
    [rows]
  );

  const filtered = useMemo<SummaryRow[]>(() => {
    let result = rows;

    if (filters.source) {
      result = result.filter(r => r.source === filters.source);
    }

    if (filters.destination) {
      result = result.filter(r => r.destination === filters.destination);
    }

    if (filters.currentOnly) {
      result = result.filter(r => r.isCurrent === 'Yes');
    }

    if (filters.minScore > 0) {
      result = result.filter(r => r.scoreNumeric >= filters.minScore);
    }

    if (filters.lastSeenWindow !== 'all') {
      const windowMs = { hour: 3600000, day: 86400000, week: 604800000 }[filters.lastSeenWindow];
      const cutoff = Date.now() - windowMs;
      result = result.filter(r => new Date(r.lastSeen).getTime() >= cutoff);
    }

    if (sort.column) {
      result = [...result].sort((a, b) => comparator(a, b, sort));
    }

    return result;
  }, [rows, filters, sort]);

  return { filters, setFilters, filtered, sourceOptions, destinationOptions };
}
