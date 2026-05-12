import React from 'react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

interface Match {
  id: string;
  player1: { playerName: string } | null;
  player2: { playerName: string } | null;
  player1Score: number;
  player2Score: number;
  winner: { playerName: string } | null;
  isBye: boolean;
  positionLabel: string;
}

interface MatchCardProps {
  match: Match;
  onClick?: () => void;
}

export const MatchCard: React.FC<MatchCardProps> = ({ match, onClick }) => {
  if (match.isBye) {
    return (
      <div className="bg-white/10 p-2 rounded text-white/50 text-xs italic text-center">
        Bye - {match.player1?.playerName || 'TBD'} advances
      </div>
    );
  }

  return (
    <div 
      onClick={onClick}
      className={cn(
        "bg-white rounded shadow-md overflow-hidden cursor-pointer hover:ring-2 hover:ring-yellow-400 transition-all min-w-[180px]",
        match.winner && "opacity-80"
      )}
    >
      <div className="bg-gray-100 text-[10px] px-2 py-0.5 font-bold text-gray-500 uppercase">
        {match.positionLabel}
      </div>
      <div className="p-2 space-y-1">
        <div className={cn(
          "flex justify-between items-center text-sm",
          match.winner?.playerName === match.player1?.playerName ? "font-bold text-green-700" : "text-gray-700"
        )}>
          <span className="truncate">{match.player1?.playerName || 'TBD'}</span>
          <span className="bg-gray-200 px-1.5 rounded tabular-nums">{match.player1Score}</span>
        </div>
        <div className={cn(
          "flex justify-between items-center text-sm",
          match.winner?.playerName === match.player2?.playerName ? "font-bold text-green-700" : "text-gray-700"
        )}>
          <span className="truncate">{match.player2?.playerName || 'TBD'}</span>
          <span className="bg-gray-200 px-1.5 rounded tabular-nums">{match.player2Score}</span>
        </div>
      </div>
    </div>
  );
};
