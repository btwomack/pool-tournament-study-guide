import { useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export const useTournamentSocket = (tournamentId: string, onMessage: (message: any) => void) => {
  useEffect(() => {
    if (!tournamentId) return;

    const client = new Client({
      brokerURL: `ws://localhost:8080/ws`,
      connectHeaders: {
        login: 'user',
        passcode: 'password',
      },
      debug: (str) => {
        console.log('STOMP Debug:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      console.log('Connected to WebSocket');
      client.subscribe(`/topic/tournament/${tournamentId}`, (message) => {
        const body = JSON.parse(message.body);
        onMessage(body);
      });
    };

    client.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    client.activate();

    return () => {
      if (client.active) {
        client.deactivate();
      }
    };
  }, [tournamentId, onMessage]);
};
