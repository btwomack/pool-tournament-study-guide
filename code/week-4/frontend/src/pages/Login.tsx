import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/client';

export const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isRegister, setIsRegister] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      let response;
      if (isRegister) {
        response = await apiClient.post('/auth/register', { email, password });
        // After successful registration, automatically log in
        response = await apiClient.post('/auth/login', { email, password });
      } else {
        response = await apiClient.post('/auth/login', { email, password });
      }
      localStorage.setItem('jwt_token', response.data.token);
      // Decode JWT to get user ID (simplified for example)
      const payload = JSON.parse(atob(response.data.token.split('.')[1]));
      localStorage.setItem('userId', payload.userId);
      navigate('/admin');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Authentication failed. Please check your credentials.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-pool-felt text-white p-8 flex items-center justify-center">
      <div className="bg-pool-table p-8 rounded-lg shadow-xl w-full max-w-md">
        <h1 className="text-3xl font-bold mb-6 text-center">{isRegister ? 'Register' : 'Login'}</h1>
        {error && <p className="text-red-500 text-center mb-4">{error}</p>}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-300">Email</label>
            <input
              type="email"
              id="email"
              className="mt-1 block w-full rounded-md border-gray-700 shadow-sm bg-gray-800 text-white focus:border-indigo-500 focus:ring-indigo-500"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-300">Password</label>
            <input
              type="password"
              id="password"
              className="mt-1 block w-full rounded-md border-gray-700 shadow-sm bg-gray-800 text-white focus:border-indigo-500 focus:ring-indigo-500"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button
            type="submit"
            className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 focus:ring-offset-pool-table"
            disabled={loading}
          >
            {loading ? 'Processing...' : (isRegister ? 'Register' : 'Login')}
          </button>
        </form>
        <p className="mt-6 text-center text-gray-400">
          {isRegister ? 'Already have an account?' : 'Don\'t have an account?'}{' '}
          <button
            type="button"
            onClick={() => setIsRegister(!isRegister)}
            className="text-indigo-400 hover:text-indigo-600 font-medium"
          >
            {isRegister ? 'Login' : 'Register'}
          </button>
        </p>
      </div>
    </div>
  );
};
