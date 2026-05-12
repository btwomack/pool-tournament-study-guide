import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiClient from '../api/client';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, useStripe, useElements, CardElement } from '@stripe/react-stripe-js';

const stripePromise = loadStripe('pk_test_placeholder');

interface Tournament {
  id: string;
  name: string;
  entryFee: number;
  status: string;
}

const CheckoutForm: React.FC<{ tournament: Tournament }> = ({ tournament }) => {
  const stripe = useStripe();
  const elements = useElements();
  const navigate = useNavigate();

  const [playerName, setPlayerName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [succeeded, setSucceeded] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError(null);

    if (!stripe || !elements) {
      return;
    }

    try {
      const { data: { clientSecret } } = await apiClient.post(`/payments/create-payment-intent`, {
        amount: tournament.entryFee * 100,
        currency: 'usd',
        tournamentId: tournament.id,
        playerName,
        phoneNumber,
      });

      const cardElement = elements.getElement(CardElement);
      if (!cardElement) {
        setError('Card details not entered.');
        setLoading(false);
        return;
      }

      const { error: stripeError, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
        payment_method: {
          card: cardElement,
          billing_details: { name: playerName },
        },
      });

      if (stripeError) {
        setError(stripeError.message || 'Payment failed.');
      } else if (paymentIntent && paymentIntent.status === 'succeeded') {
        setSucceeded(true);
        navigate(`/tournament/${tournament.id}`);
      } else {
        setError('Payment did not succeed. Please try again.');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'An unexpected error occurred.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label htmlFor="playerName" className="block text-sm font-medium text-gray-300">Your Name</label>
        <input
          type="text"
          id="playerName"
          className="mt-1 block w-full rounded-md border-gray-700 shadow-sm bg-gray-800 text-white focus:border-indigo-500 focus:ring-indigo-500"
          value={playerName}
          onChange={(e) => setPlayerName(e.target.value)}
          required
        />
      </div>
      <div>
        <label htmlFor="phoneNumber" className="block text-sm font-medium text-gray-300">Phone Number (for SMS reminders)</label>
        <input
          type="tel"
          id="phoneNumber"
          className="mt-1 block w-full rounded-md border-gray-700 shadow-sm bg-gray-800 text-white focus:border-indigo-500 focus:ring-indigo-500"
          value={phoneNumber}
          onChange={(e) => setPhoneNumber(e.target.value)}
          placeholder="+15551234567"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-300">Card Details</label>
        <div className="mt-1 p-2 border border-gray-700 rounded-md bg-gray-800">
          <CardElement options={{ style: { base: { color: '#fff' } } }} />
        </div>
      </div>
      {error && <div className="text-red-500 text-sm">{error}</div>}
      {succeeded && <div className="text-green-500 text-sm">Payment Succeeded! Redirecting...</div>}
      <button
        type="submit"
        className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 focus:ring-offset-pool-table"
        disabled={!stripe || !elements || loading || succeeded}
      >
        {loading ? 'Processing...' : `Pay $${tournament.entryFee} and Join`}
      </button>
    </form>
  );
};

export const PlayerSignup: React.FC = () => {
  const { joinCode } = useParams<{ joinCode: string }>();
  const [tournament, setTournament] = useState<Tournament | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchTournament = async () => {
      if (!joinCode) return;
      try {
        const response = await apiClient.get<Tournament>(`/tournaments/join/${joinCode}`);
        setTournament(response.data);
      } catch (err) {
        console.error('Error fetching tournament:', err);
        setError('Tournament not found or an error occurred.');
      } finally {
        setLoading(false);
      }
    };
    fetchTournament();
  }, [joinCode]);

  if (loading) return <div className="min-h-screen bg-pool-felt text-white flex items-center justify-center">Loading...</div>;
  if (error) return <div className="min-h-screen bg-pool-felt text-red-500 flex items-center justify-center">{error}</div>;
  if (!tournament) return <div className="min-h-screen bg-pool-felt text-white flex items-center justify-center">Tournament not found.</div>;

  return (
    <div className="min-h-screen bg-pool-felt text-white p-8 flex items-center justify-center">
      <div className="bg-pool-table p-8 rounded-lg shadow-xl w-full max-w-md">
        <h1 className="text-3xl font-bold mb-4 text-center">Join {tournament.name}</h1>
        <p className="text-lg text-gray-300 mb-6 text-center">Entry Fee: ${tournament.entryFee}</p>

        <Elements stripe={stripePromise}>
          <CheckoutForm tournament={tournament} />
        </Elements>
      </div>
    </div>
  );
};
