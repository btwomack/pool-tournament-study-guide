import React from 'react';

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
  const groupedByRound = matches.reduce((acc, match) => {
    if (!acc[match.roundNumber]) {
      acc[match.roundNumber] = [];
    }
    acc[match.roundNumber].push(match);
    return acc;
  }, {} as Record<number, Match[]>);

  const rounds = Object.keys(groupedByRound).map(Number).sort((a, b) => a - b);

  return (
    <div className="overflow-x-auto">
      <div className="flex gap-8 p-4 bg-gray-900 rounded-lg">
        {rounds.map((round) => (
          <div key={round} className="flex flex-col gap-4 min-w-max">
            <h3 className="text-sm font-bold text-gray-400 uppercase">Round {round}</h3>
            {groupedByRound[round].map((match) => (
              <div
                key={match.id}
                onClick={() => onMatchClick?.(match)}
                className={`p-3 rounded border ${
                  match.isBye
                    ? 'bg-gray-700 border-gray-600'
                    : 'bg-pool-table border-indigo-500 cursor-pointer hover:border-indigo-300'
                } w-48`}
              >
                <p className="text-xs text-gray-400 mb-2">{match.positionLabel}</p>
                <div className="space-y-1">
                  <div className={`flex justify-between text-sm ${match.winner?.playerName === match.player1?.playerName ? 'font-bold text-green-400' : 'text-gray-300'}`}>
                    <span>{match.player1?.playerName || 'TBD'}</span>
                    <span>{match.player1Score}</span>
                  </div>
                  <div className={`flex justify-between text-sm ${match.winner?.playerName === match.player2?.playerName ? 'font-bold text-green-400' : 'text-gray-300'}`}>
                    <span>{match.player2?.playerName || 'TBD'}</span>
                    <span>{match.player2Score}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
};
