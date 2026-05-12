import React from 'react';

interface PlayerRegistration {
  id: string;
  playerName: string;
  status: string;
  seedNumber: number;
  lossesCount: number;
}

interface PlayerListProps {
  players: PlayerRegistration[];
}

export const PlayerList: React.FC<PlayerListProps> = ({ players }) => {
  return (
    <div className="bg-pool-table p-4 rounded-lg shadow-lg">
      <ul className="divide-y divide-gray-700">
        {players.length === 0 ? (
          <li className="py-2 text-gray-400">No players registered yet.</li>
        ) : (
          players.map((player) => (
            <li key={player.id} className="py-2 flex justify-between items-center">
              <span className="text-white font-medium">{player.playerName}</span>
              <span className="text-sm text-gray-400">{player.status} ({player.lossesCount} losses)</span>
            </li>
          ))
        )}
      </ul>
    </div>
  );
};
