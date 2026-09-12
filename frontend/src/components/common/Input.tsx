import React from 'react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
}

export const Input: React.FC<InputProps> = ({ label, error, className = '', ...props }) => {
  return (
    <div className="flex flex-col mb-4">
      <input
        placeholder={label}
        className={`px-5 py-3.5 bg-gray-50/70 border rounded-2xl text-sm font-medium focus:outline-none focus:ring-2 focus:ring-orange-500/40 focus:border-orange-500 transition-all focus:bg-white ${
          error ? 'border-red-400 focus:ring-red-400 focus:border-red-400' : 'border-transparent hover:border-gray-200'
        } ${className}`}
        {...props}
      />
      {error && <p className="mt-1.5 ml-1 text-xs font-medium text-red-500">{error}</p>}
    </div>
  );
};
