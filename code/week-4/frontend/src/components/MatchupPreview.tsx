import React, { useState, useEffect, useCallback } from 'react';
import apiClient from '../api/client';

interface MatchupPreviewProps {
  matchId: string;
}

export const MatchupPreview: React.FC<MatchupPreviewProps> = ({ matchId }) => {
  const [preview, setPreview] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchPreview = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.get<any>(`/api/matches/${matchId}/preview`);
      setPreview(response.data.preview);
    } catch (err) {
      console.error('Error fetching matchup preview:', err);
      setError('Failed to fetch preview.');
    } finally {
      setLoading(false);
    }
  }, [matchId]);

  useEffect(() => {
    fetchPreview();
  }, [fetchPreview]);

  if (loading) return <p className="text-gray-400 italic">Generating preview...</p>;
  if (error) return <p className="text-red-500 italic">{error}</p>;
  if (!preview) return null;

  return (
    <div className="bg-indigo-900/50 p-4 rounded-lg border border-indigo-500/30 text-indigo-100 shadow-md">
      <h4 className="text-sm font-bold uppercase tracking-wider mb-2 text-indigo-300">AI Matchup Preview</h4>
      <p className="text-sm leading-relaxed">{preview}</p>
    </div>
  );
};
