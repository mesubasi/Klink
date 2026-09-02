'use client';

import React, { useEffect, useState } from 'react';
import { Sun, Moon } from 'lucide-react';
import { getThemeConfig, saveThemeConfig, Theme } from '@/lib/theme';

interface ThemeToggleProps {
  className?: string;
}

export const ThemeToggle: React.FC<ThemeToggleProps> = ({ className = '' }) => {
  const [currentTheme, setCurrentTheme] = useState<'light' | 'dark'>('light');
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const config = getThemeConfig();
    const isDark =
      config.theme === 'dark' ||
      (config.theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches);
    setCurrentTheme(isDark ? 'dark' : 'light');

    // Sistem tercihi değişim dinleyicisi
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const handleChange = (e: MediaQueryListEvent) => {
      const cfg = getThemeConfig();
      if (cfg.syncedWithSystem) {
        const nextTheme = e.matches ? 'dark' : 'light';
        setCurrentTheme(nextTheme);
        saveThemeConfig('system');
      }
    };

    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }, []);

  const toggleTheme = () => {
    const nextTheme: Theme = currentTheme === 'dark' ? 'light' : 'dark';
    setCurrentTheme(nextTheme);
    saveThemeConfig(nextTheme);
  };

  if (!mounted) {
    return (
      <div className={`w-8 h-8 rounded-lg bg-zinc-100 dark:bg-zinc-800 border border-zinc-200/80 dark:border-zinc-700 animate-pulse ${className}`} />
    );
  }

  return (
    <button
      onClick={toggleTheme}
      type="button"
      className={`relative inline-flex items-center justify-center w-8 h-8 rounded-lg bg-zinc-100 dark:bg-zinc-800 border border-zinc-200/80 dark:border-zinc-700/80 text-zinc-700 dark:text-zinc-200 hover:bg-zinc-200/70 dark:hover:bg-zinc-700/70 transition-all cursor-pointer shadow-2xs ${className}`}
      title={currentTheme === 'dark' ? 'Açık Moda Geç' : 'Karanlık Modu Aç'}
      aria-label="Tema Değiştir"
    >
      {currentTheme === 'dark' ? (
        <Sun className="w-4 h-4 text-amber-400 transition-transform duration-300 rotate-0 scale-100" />
      ) : (
        <Moon className="w-4 h-4 text-zinc-700 transition-transform duration-300 rotate-0 scale-100" />
      )}
    </button>
  );
};
