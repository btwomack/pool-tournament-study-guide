import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/client';

export const CreateTournament: React.FC = () => {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [entryFee, setEntryFee] = useState(0);
  const [raceToDefault, setRaceToDefault] = useState(3);
  const [bracketFormat, setBracketFormat] = useState('SINGLE_ELIMINATION');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const response = await apiClient.post('/tournaments', {
        name,
        entryFee,
        raceToDefault,
        bracketFormat,
      });
      navigate(`/tournament/${response.data.id}`);
    } catch (err) {
      setError('Failed to create tournament. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-pool-felt text-white p-8 flex items-center justify-center">
      <div className="bg-pool-table p-8 rounded-lg shadow-xl w-full max-w-md">
        <h1 className="text-3xl font-bold mb-6 text-center">Create New Tournament</h1>
        {error && <p className="text-red-500 text-center mb-4">{error}</p>}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="name" className="block text-sm font-medium text-gray-300">Tournament Name</label>
            <input
              type="text"
              id="name"
              className="mt-1 block w-full rounded-md border-gray-700 shadow-sm bg-gray-800 text-white focus:border-indigo-500 focus:ring-indigo-500"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>
          <div>
            <label htmlFor="entryFee" className="block text-sm font-medium text-gray-300">Entry Fee ($)</label>
            <input
              type="number"
              id="entryFee"
              className="mt-1 block w-full rounded-md border-gray-700 shadow-sm bg-gray-800 text-white focus:border-indigo-500 focus:ring-indigo-500"
              value={entryFee}
              onChange={(e) => setEntryFee(parseInt(e.target.value))}
              min="0"
              required
            />
          </div>
          <div>
            <label htmlFor="raceToDefault" className="block text-sm font-medium text-gray-300">Race To (Default)</label>
            <input
              type="number"
              id="raceToDefault"
              className="mt-1 block w-full rounded-md border-gray-700 shadow-sm bg-gray-800 text-white focus:border-indigo-500 focus:ring-indigo-500"
              value={raceToDefault}
              onChange={(e) => setRaceToDefault(parseInt(e.target.value))}
              min="1"
              required
            />
          </div>
          <div>
            <label htmlFor="bracketFormat" className="block text-sm font-medium text-gray-300">Bracket Format</label>
            <select
              id="bracketFormat"
              className="mt-1 block w-full rounded-md border-gray-700 shadow-sm bg-gray-800 text-white focus:border-indigo-500 focus:ring-indigo-500"
              value={bracketFormat}
              onChange={(e) => setBracketFormat(e.target.value)}
              required
            >
              <option value="SINGLE_ELIMINATION">Single Elimination</option>
              <option value="DOUBLE_ELIMINATION">Double Elimination</option>
            </select>
          </div>
          <button
            type="submit"
            className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 focus:ring-offset-pool-table"
            disabled={loading}
          >
            {loading ? 'Creating...' : 'Create Tournament'}
          </button>
        </form>
      </div>
    </div>
  );
};
