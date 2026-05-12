import React, { useEffect, useState, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import apiClient from '../api/client';
import { useTournamentSocket } from '../hooks/useTournamentSocket';
import { Bracket } from '../components/Bracket';
import { PlayerList } from '../components/PlayerList';
import { QRCodeDisplay } from '../components/QRCodeDisplay';

interface Tournament {
  id: string;
  name: string;
  status: string;
  joinCode: string;
  bracketFormat: string;
  createdBy: { id: string; email: string };
}

interface PlayerRegistration {
  id: string;
  playerName: string;
  status: string;
  seedNumber: number;
  lossesCount: number;
}

interface Match {
  id: string;
  roundNumber: number;
  matchNumber: number;
  player1: { playerName: string } | null;
  player2: { playerName: string } | null;
  player1Score: number;
  player2Score: number;
  winner: { playerName: string } | null;
  isBye: boolean;
  positionLabel: string;
}

export const TournamentView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [tournament, setTournament] = useState<Tournament | null>(null);
  const [players, setPlayers] = useState<PlayerRegistration[]>([]);
  const [matches, setMatches] = useState<Match[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedMatch, setSelectedMatch] = useState<Match | null>(null);
  const [player1Score, setPlayer1Score] = useState<number>(0);
  const [player2Score, setPlayer2Score] = useState<number>(0);
  const [winnerId, setWinnerId] = useState<string | null>(null);

  const fetchTournamentData = useCallback(async () => {
    if (!id) return;
    try {
      const tournamentRes = await apiClient.get<Tournament>(`/tournaments/${id}`);
      setTournament(tournamentRes.data);

      const playersRes = await apiClient.get<PlayerRegistration[]>(`/tournaments/${id}/players`);
      setPlayers(playersRes.data);

      const matchesRes = await apiClient.get<Match[]>(`/tournaments/${id}/bracket`);
      setMatches(matchesRes.data);
    } catch (err) {
      console.error('Error fetching tournament data:', err);
      setError('Failed to load tournament data.');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchTournamentData();
  }, [fetchTournamentData]);

  const handleSocketMessage = useCallback((message: any) => {
    console.log('WebSocket message received:', message);
    // Refetch all data on any relevant update for simplicity
    fetchTournamentData();
  }, [fetchTournamentData]);

  useTournamentSocket(id || '', handleSocketMessage);

  const handleGenerateBracket = async () => {
    if (!id) return;
    try {
      await apiClient.post(`/tournaments/${id}/bracket`, { seedingMode: 'random' });
      fetchTournamentData();
    } catch (err) {
      console.error('Error generating bracket:', err);
      setError('Failed to generate bracket.');
    }
  };

  const handleMatchClick = (match: Match) => {
    setSelectedMatch(match);
    setPlayer1Score(match.player1Score || 0);
    setPlayer2Score(match.player2Score || 0);
    setWinnerId(match.winner?.id || null);
  };

  const handleRecordResult = async () => {
    if (!selectedMatch || !winnerId) return;
    try {
      await apiClient.put(`/tournaments/${tournament?.id}/matches/${selectedMatch.id}`, {
        player1Score,
        player2Score,
        winnerId,
      });
      setSelectedMatch(null);
      fetchTournamentData(); // Refresh data after recording result
    } catch (err) {
      console.error('Error recording result:', err);
      setError('Failed to record match result.');
    }
  };

  if (loading) return <div className="min-h-screen bg-pool-felt text-white flex items-center justify-center">Loading...</div>;
  if (error) return <div className="min-h-screen bg-pool-felt text-red-500 flex items-center justify-center">{error}</div>;
  if (!tournament) return <div className="min-h-screen bg-pool-felt text-white flex items-center justify-center">Tournament not found.</div>;

  const isAdmin = localStorage.getItem('userId') === tournament.createdBy.id; // Simplified admin check

  return (
    <div className="min-h-screen bg-pool-felt text-white p-8">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-4xl font-bold mb-4">{tournament.name}</h1>
        <p className="text-lg text-gray-300 mb-6">Status: {tournament.status}</p>

        {isAdmin && tournament.status === 'PENDING' && (
          <div className="mb-6 flex items-center space-x-4">
            <button
              onClick={handleGenerateBracket}
              className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
            >
              Generate Bracket
            </button>
            <QRCodeDisplay joinCode={tournament.joinCode} />
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
          <div className="lg:col-span-2">
            <h2 className="text-2xl font-semibold mb-4">Bracket ({tournament.bracketFormat.replace('_', ' ')})</h2>
            {matches.length > 0 ? (
              <Bracket matches={matches} onMatchClick={isAdmin ? handleMatchClick : undefined} />
            ) : (
              <p className="text-gray-400">Bracket not generated yet.</p>
            )}
          </div>
          <div>
            <h2 className="text-2xl font-semibold mb-4">Players</h2>
            <PlayerList players={players} />
          </div>
        </div>

        {selectedMatch && isAdmin && (
          <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50">
            <div className="bg-pool-table p-8 rounded-lg shadow-xl w-full max-w-md">
              <h2 className="text-2xl font-bold mb-4">Record Match Result</h2>
              <p className="text-lg mb-4">Match: {selectedMatch.positionLabel}</p>
              <form onSubmit={(e) => { e.preventDefault(); handleRecordResult(); }} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-300">{selectedMatch.player1?.playerName || 'Player 1'}</label>
                  <input
                    type="number"
                    value={player1Score}
                    onChange={(e) => setPlayer1Score(parseInt(e.target.value))}
                    className="mt-1 block w-full rounded-md border-gray-700 shadow-sm bg-gray-800 text-white"
                    min="0"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-300">{selectedMatch.player2?.playerName || 'Player 2'}</label>
                  <input
                    type="number"
                    value={player2Score}
                    onChange={(e) => setPlayer2Score(parseInt(e.target.value))}
                    className="mt-1 block w-full rounded-md border-gray-700 shadow-sm bg-gray-800 text-white"
                    min="0"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-300">Winner</label>
                  <select
                    value={winnerId || ''}
                    onChange={(e) => setWinnerId(e.target.value)}
                    className="mt-1 block w-full rounded-md border-gray-700 shadow-sm bg-gray-800 text-white"
                  >
                    <option value="">Select Winner</option>
                    {selectedMatch.player1 && <option value={selectedMatch.player1.id}>{selectedMatch.player1.playerName}</option>}
                    {selectedMatch.player2 && <option value={selectedMatch.player2.id}>{selectedMatch.player2.playerName}</option>}
                  </select>
                </div>
                <div className="flex justify-end space-x-4">
                  <button
                    type="button"
                    onClick={() => setSelectedMatch(null)}
                    className="bg-gray-600 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
                  >
                    Record Result
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
