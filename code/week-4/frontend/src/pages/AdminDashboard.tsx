import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/client';

interface Tournament {
  id: string;
  name: string;
  status: string;
  joinCode: string;
}

export const AdminDashboard: React.FC = () => {
  const [tournaments, setTournaments] = useState<Tournament[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('jwt_token');
    if (!token) {
      navigate('/login');
      return;
    }

    const fetchTournaments = async () => {
      try {
        const response = await apiClient.get('/tournaments');
        setTournaments(response.data);
      } catch (error) {
        console.error('Error fetching tournaments:', error);
      }
    };
    fetchTournaments();
  }, [navigate]);

  return (
    <div className="min-h-screen bg-pool-felt text-white p-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold mb-8">Admin Dashboard</h1>

        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-semibold">Your Tournaments</h2>
          <Link to="/admin/create" className="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded">
            Create New Tournament
          </Link>
        </div>

        {tournaments.length === 0 ? (
          <p className="text-gray-400">No tournaments created yet. Start by creating one!</p>
        ) : (
          <div className="bg-pool-table rounded-lg shadow-lg overflow-hidden">
            <table className="min-w-full divide-y divide-gray-700">
              <thead className="bg-gray-700">
                <tr>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Name</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Status</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Join Code</th>
                  <th scope="col" className="relative px-6 py-3"><span className="sr-only">Actions</span></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-800">
                {tournaments.map((t) => (
                  <tr key={t.id} className="hover:bg-gray-800">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-white">{t.name}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-300">{t.status}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-300">{t.joinCode}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <Link to={`/tournament/${t.id}`} className="text-indigo-400 hover:text-indigo-600 mr-4">View</Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
