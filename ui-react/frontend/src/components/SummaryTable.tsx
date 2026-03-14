import { useState, useEffect } from 'react';
import Paper from '@mui/material/Paper';
import TableContainer from '@mui/material/TableContainer';
import Table from '@mui/material/Table';
import TableHead from '@mui/material/TableHead';
import TableBody from '@mui/material/TableBody';
import TableRow from '@mui/material/TableRow';
import TableCell from '@mui/material/TableCell';
import TablePagination from '@mui/material/TablePagination';
import IconButton from '@mui/material/IconButton';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import Collapse from '@mui/material/Collapse';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Alert from '@mui/material/Alert';
import CircularProgress from '@mui/material/CircularProgress';
import Box from '@mui/material/Box';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';

import { fetchSummary, SummaryRow } from '../api/client';
import { useSorting } from '../hooks/useSorting';
import { useFilters, SortColumn } from '../hooks/useFilters';
import SortableHeader from './SortableHeader';
import FilterBar from './FilterBar';

interface Props {
  onViewHistory: (source: string, destination: string) => void;
}

function rowKey(row: SummaryRow): string {
  return `${row.sourceIp}|${row.destinationIp}`;
}

function scoreChipColor(score: number): 'error' | 'warning' | 'success' {
  if (score >= 70) return 'error';
  if (score >= 30) return 'warning';
  return 'success';
}

export default function SummaryTable({ onViewHistory }: Props) {
  const [rows, setRows] = useState<SummaryRow[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [expandedRows, setExpandedRows] = useState<Set<string>>(new Set());
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);

  const { sort, toggleSort } = useSorting<SortColumn>('scoreNumeric');
  const { filters, setFilters, filtered, bounds } = useFilters(rows, sort);

  useEffect(() => {
    fetchSummary()
      .then(data => {
        const mapped: SummaryRow[] = data.results.map(r => ({
          ...r,
          scoreNumeric: parseFloat(r.score) || 0,
        }));
        setRows(mapped);
      })
      .catch(e => setError(String(e)))
      .finally(() => setLoading(false));
  }, []);

  // Reset page when filters or sort change
  useEffect(() => {
    setPage(0);
  }, [filters, sort]);

  const toggleExpand = (key: string) => {
    setExpandedRows(prev => {
      const next = new Set(prev);
      if (next.has(key)) {
        next.delete(key);
      } else {
        next.add(key);
      }
      return next;
    });
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  const pageRows = filtered.slice(page * rowsPerPage, (page + 1) * rowsPerPage);

  return (
    <Box>
      <Paper>
        <FilterBar
          filters={filters}
          setFilters={setFilters}
          bounds={bounds}
          resultCount={filtered.length}
          totalCount={rows.length}
        />

        <TableContainer>
          <Table stickyHeader size="small">
            <TableHead>
              <TableRow>
                <SortableHeader column="source" label="Source" sort={sort} onSort={toggleSort} />
                <SortableHeader column="destination" label="Destination" sort={sort} onSort={toggleSort} />
                <SortableHeader column="lastSeen" label="Last Seen" sort={sort} onSort={toggleSort} />
                <SortableHeader column="isCurrent" label="Current" sort={sort} onSort={toggleSort} />
                <SortableHeader column="scoreNumeric" label="Score" sort={sort} onSort={toggleSort} />
                <SortableHeader column="totalBytes" label="Bytes" sort={sort} onSort={toggleSort} />
                <SortableHeader column="totalTimes" label="Times" sort={sort} onSort={toggleSort} />
                <TableCell>Details</TableCell>
                <TableCell>Observations</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {pageRows.map(row => {
                const key = rowKey(row);
                const expanded = expandedRows.has(key);
                return (
                  <>
                    <TableRow key={key} hover>
                      <TableCell>{row.source}</TableCell>
                      <TableCell>{row.destination}</TableCell>
                      <TableCell>{row.lastSeen}</TableCell>
                      <TableCell>
                        <Chip
                          label={row.isCurrent}
                          color={row.isCurrent === 'Yes' ? 'success' : 'default'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={row.score}
                          color={scoreChipColor(row.scoreNumeric)}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>{row.totalBytes.toLocaleString()}</TableCell>
                      <TableCell>{row.totalTimes.toLocaleString()}</TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                          <IconButton size="small" onClick={() => toggleExpand(key)}>
                            {expanded ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />}
                          </IconButton>
                          {row.details.length} detail{row.details.length !== 1 ? 's' : ''}
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Button
                          size="small"
                          variant="outlined"
                          onClick={() => onViewHistory(row.sourceIp, row.destinationIp)}
                        >
                          View
                        </Button>
                      </TableCell>
                    </TableRow>
                    <TableRow key={`${key}-expand`}>
                      <TableCell colSpan={9} sx={{ py: 0, border: 0 }}>
                        <Collapse in={expanded} unmountOnExit>
                          <List dense sx={{ pl: 2 }}>
                            {row.details.map((d, i) => (
                              <ListItem key={i} sx={{ py: 0 }}>
                                <ListItemText primary={`• ${d}`} />
                              </ListItem>
                            ))}
                          </List>
                        </Collapse>
                      </TableCell>
                    </TableRow>
                  </>
                );
              })}
            </TableBody>
          </Table>
        </TableContainer>

        <TablePagination
          rowsPerPageOptions={[10, 25, 50, 100]}
          component="div"
          count={filtered.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          onRowsPerPageChange={e => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
        />
      </Paper>
    </Box>
  );
}
