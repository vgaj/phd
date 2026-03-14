import { useState } from 'react';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Container from '@mui/material/Container';
import Box from '@mui/material/Box';
import SummaryTable from './components/SummaryTable';
import HostHistory from './components/HostHistory';

type View =
  | { type: 'summary' }
  | { type: 'history'; source: string; destination: string };

export default function App() {
  const [view, setView] = useState<View>({ type: 'summary' });

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div">
            Phone Home Detector
          </Typography>
        </Toolbar>
      </AppBar>
      <Container maxWidth="xl" sx={{ mt: 3, mb: 3, flex: 1 }}>
        {view.type === 'history' ? (
          <HostHistory
            source={view.source}
            destination={view.destination}
            onBack={() => setView({ type: 'summary' })}
          />
        ) : (
          <SummaryTable
            onViewHistory={(source, destination) =>
              setView({ type: 'history', source, destination })
            }
          />
        )}
      </Container>
    </Box>
  );
}
