import React, { useState } from 'react';
import QRCode from 'qrcode.react';
import { QrCodeIcon } from 'lucide-react';

interface QRCodeDisplayProps {
  joinCode: string;
}

export const QRCodeDisplay: React.FC<QRCodeDisplayProps> = ({ joinCode }) => {
  const [showQR, setShowQR] = useState(false);
  const joinUrl = `${window.location.origin}/t/${joinCode}`;

  return (
    <div className="relative">
      <button
        onClick={() => setShowQR(!showQR)}
        className="bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2 px-4 rounded flex items-center space-x-2"
      >
        <QrCodeIcon size={20} />
        <span>Show Join QR Code</span>
      </button>

      {showQR && (
        <div className="absolute top-full left-1/2 -translate-x-1/2 mt-2 p-4 bg-white rounded-lg shadow-xl z-10">
          <p className="text-center text-gray-800 font-semibold mb-2">Scan to Join</p>
          <QRCode value={joinUrl} size={128} level="H" />
          <p className="text-center text-gray-600 text-sm mt-2">{joinCode}</p>
        </div>
      )}
    </div>
  );
};
