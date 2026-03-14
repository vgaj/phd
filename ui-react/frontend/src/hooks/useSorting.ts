import { useState, useCallback } from 'react';

export type SortDirection = 'asc' | 'desc';

export interface SortState<T extends string> {
  column: T | null;
  direction: SortDirection;
}

export interface UseSortingResult<T extends string> {
  sort: SortState<T>;
  toggleSort: (col: T) => void;
}

export function useSorting<T extends string>(defaultColumn: T): UseSortingResult<T> {
  const [sort, setSort] = useState<SortState<T>>({
    column: defaultColumn,
    direction: 'desc',
  });

  const toggleSort = useCallback((col: T) => {
    setSort(prev => {
      if (prev.column === col) {
        return { column: col, direction: prev.direction === 'asc' ? 'desc' : 'asc' };
      }
      return { column: col, direction: 'desc' };
    });
  }, []);

  return { sort, toggleSort };
}
