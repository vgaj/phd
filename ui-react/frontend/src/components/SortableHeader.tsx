import TableCell from '@mui/material/TableCell';
import TableSortLabel from '@mui/material/TableSortLabel';
import { SortState } from '../hooks/useSorting';
import { SortColumn } from '../hooks/useFilters';

interface Props {
  column: SortColumn;
  label: string;
  sort: SortState<SortColumn>;
  onSort: (col: SortColumn) => void;
}

export default function SortableHeader({ column, label, sort, onSort }: Props) {
  return (
    <TableCell sortDirection={sort.column === column ? sort.direction : false}>
      <TableSortLabel
        active={sort.column === column}
        direction={sort.column === column ? sort.direction : 'asc'}
        onClick={() => onSort(column)}
      >
        {label}
      </TableSortLabel>
    </TableCell>
  );
}
