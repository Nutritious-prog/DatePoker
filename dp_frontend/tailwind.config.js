/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./app/**/*.{js,jsx,ts,tsx}", "./components/**/*.{js,jsx,ts,tsx}"],
  presets: [require("nativewind/preset")],
  theme: {
    extend: {
      fontFamily: {
        rubik: ["Rubik-Regular", "sans-serif"],
        "rubik-bold": ["Rubik-Bold", "sans-serif"],
        "rubik-extrabold": ["Rubik-ExtraBold", "sans-serif"],
        "rubik-medium": ["Rubik-Medium", "sans-serif"],
        "rubik-semibold": ["Rubik-SemiBold", "sans-serif"],
        "rubik-light": ["Rubik-Light", "sans-serif"],
      },
      colors: {
        primary: {
          100: "#FF33660A",  // Light transparent version
          200: "#C00F0C1A",  // Slightly deeper transparent red
          300: "#FF3366",    // Vibrant primary pink/red
        },
        accent: {
          100: "#FBFBFD",    // Background and cards
        },
        black: {
          DEFAULT: "#000000",
          100: "#8C8E98",    // Lightest text
          200: "#666876",    // Medium text
          300: "#191D31",    // Darkest text
        },
        danger: "#F75555",   // You can keep this for errors or alerts
        background: "#FF33661A"
      },
    },
  },
  plugins: [],
};
