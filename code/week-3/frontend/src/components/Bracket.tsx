import React from 'react';
import { MatchCard } from './MatchCard';

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

interface BracketProps {
  matches: Match[];
  onMatchClick?: (match: Match) => void;
}

export const Bracket: React.FC<BracketProps> = ({ matches, onMatchClick }) => {
  const rounds = Array.from(new Set(matches.map(m => m.roundNumber))).sort((a, b) => a - b);

  return (
    <div className="flex gap-8 overflow-x-auto p-4 bg-pool-felt rounded-lg shadow-xl min-h-[600px]">
      {rounds.map(round => (
        <div key={round} className="flex flex-col justify-around gap-4 min-w-[200px]">
          <h3 className="text-white font-bold text-center border-b border-white/20 pb-2">
            Round {round}
          </h3>
          {matches
            .filter(m => m.roundNumber === round)
            .sort((a, b) => a.matchNumber - b.matchNumber)
            .map(match => (
              <div key={match.id} className="relative">
                <MatchCard 
                  match={match} 
                  onClick={() => onMatchClick?.(match)}
                />
              </div>
            ))}
        </div>
      ))}
    </div>
  );
};
