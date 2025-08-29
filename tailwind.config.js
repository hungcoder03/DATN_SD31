/** @type {import('tailwindcss').Config} */
module.exports = {
  prefix: 'tw-',
  content: [
    "./src/main/resources/templates/**/*.{html,js}",
    "./src/main/resources/static/**/*.{html,js}"
  ],
  theme: {
    extend: {},
  },
  plugins: [],
  important: true,
} 
 