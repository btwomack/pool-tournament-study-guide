/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'pool-green': '#0a5d00',
        'pool-felt': '#1a472a',
        'pool-table': '#3d2b1f',
      },
    },
  },
  plugins: [],
}
