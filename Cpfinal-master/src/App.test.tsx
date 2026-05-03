import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';

test('renders the Framescape inspired header', () => {
  render(<App />);
  const brand = screen.getByRole('button', { name: /framescape/i });
  expect(brand).toBeInTheDocument();
});
