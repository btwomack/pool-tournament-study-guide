import React, { useState, useEffect, useCallback } from 'react';
import apiClient from '../api/client';

interface CalcuttaBid {
  id: string;
  amount: number;
  bidder: { email: string };
  player: { playerName: string };
}

interface Player {
  id: string;
  playerName: string;
}

interface CalcuttaPanelProps {
  tournamentId: string;
  players: Player[];
}

export const CalcuttaPanel: React.FC<CalcuttaPanelProps> = ({ tournamentId, players }) => {
  const [bids, setBids] = useState<CalcuttaBid[]>([]);
  const [selectedPlayerId, setSelectedPlayerId] = useState<string>('');
  const [bidAmount, setBidAmount] = useState<number>(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchBids = useCallback(async () => {
    try {
      const response = await apiClient.get<CalcuttaBid[]>(`/tournaments/${tournamentId}/calcutta/bids`);
      setBids(response.data);
    } catch (err) {
      console.error('Error fetching bids:', err);
    }
  }, [tournamentId]);

  useEffect(() => {
    fetchBids();
  }, [fetchBids]);

  const handlePlaceBid = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await apiClient.post(`/tournaments/${tournamentId}/calcutta/bids`, null, {
        params: { playerId: selectedPlayerId, amount: bidAmount }
      });
      fetchBids();
      setBidAmount(0);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to place bid.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-pool-table p-6 rounded-lg shadow-xl text-white">
      <h2 className="text-2xl font-bold mb-4">Calcutta Auction</h2>
      {error && <p className="text-red-500 mb-4">{error}</p>}
      <form onSubmit={handlePlaceBid} className="space-y-4 mb-6">
        <div>
          <label className="block text-sm font-medium text-gray-300">Select Player</label>
          <select
            value={selectedPlayerId}
            onChange={(e) => setSelectedPlayerId(e.target.value)}
            className="mt-1 block w-full rounded-md border-gray-700 bg-gray-800 text-white"
            required
          >
            <option value="">Select a Player</option>
            {players.map((p) => (
              <option key={p.id} value={p.id}>{p.playerName}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-300">Bid Amount ($)</label>
          <input
            type="number"
            value={bidAmount}
            onChange={(e) => setBidAmount(parseFloat(e.target.value))}
            className="mt-1 block w-full rounded-md border-gray-700 bg-gray-800 text-white"
            min="1"
            required
          />
        </div>
        <button
          type="submit"
          className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2 px-4 rounded"
          disabled={loading}
        >
          {loading ? 'Placing Bid...' : 'Place Bid'}
        </button>
      </form>

      <div className="space-y-2">
        <h3 className="text-lg font-semibold border-b border-white/20 pb-2">Recent Bids</h3>
        {bids.length === 0 ? (
          <p className="text-gray-400">No bids placed yet.</p>
        ) : (
          <ul className="divide-y divide-gray-700">
            {bids.map((bid) => (
              <li key={bid.id} className="py-2 flex justify-between">
                <span>{bid.player.playerName}</span>
                <span className="font-bold text-green-400">${bid.amount} ({bid.bidder.email})</span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};
