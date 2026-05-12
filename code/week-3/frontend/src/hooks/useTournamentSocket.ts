import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

interface WebSocketMessage {
  type: 'BRACKET_UPDATE' | 'MATCH_RESULT' | 'BRACKET_RESET' | 'PLAYER_DROP';
  tournamentId?: string;
  matchId?: string;
  playerId?: string;
}

export const useTournamentSocket = (tournamentId: string, onMessage: (msg: WebSocketMessage) => void) => {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!tournamentId) return;

    const socket = new SockJS('http://localhost:8080/ws');
    const client = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log(str),
      onConnect: () => {
        setConnected(true);
        client.subscribe(`/topic/tournament/${tournamentId}`, (message) => {
          const body = JSON.parse(message.body);
          onMessage(body);
        });
      },
      onDisconnect: () => setConnected(false),
      onStompError: (frame) => console.error('STOMP error', frame),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [tournamentId, onMessage]);

  return { connected };
};
