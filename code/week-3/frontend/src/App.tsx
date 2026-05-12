import { Routes, Route } from 'react-router-dom';
import { AdminDashboard } from './pages/AdminDashboard';
import { CreateTournament } from './pages/CreateTournament';
import { TournamentView } from './pages/TournamentView';
import { PlayerSignup } from './pages/PlayerSignup';
import { Login } from './pages/Login';

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/admin" element={<AdminDashboard />} />
      <Route path="/admin/create" element={<CreateTournament />} />
      <Route path="/t/:joinCode" element={<PlayerSignup />} />
      <Route path="/tournament/:id" element={<TournamentView />} />
      <Route path="*" element={<div>404 Not Found</div>} />
    </Routes>
  );
}


export default App;
