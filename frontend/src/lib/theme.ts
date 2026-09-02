'use client';

export type Theme = 'light' | 'dark' | 'system';

export interface ThemeConfig {
  theme: Theme;
  syncedWithSystem: boolean;
  lastUpdated: number;
}

const STORAGE_KEY = 'klink_theme_config';

export function getThemeConfig(): ThemeConfig {
  if (typeof window === 'undefined') {
    return { theme: 'light', syncedWithSystem: true, lastUpdated: Date.now() };
  }

  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (parsed && (parsed.theme === 'light' || parsed.theme === 'dark' || parsed.theme === 'system')) {
        return parsed;
      }
    }
  } catch (e) {
    console.error('Tema yapılandırması okunamadı:', e);
  }

  // Fallback: Eski basit key veya sistem tercihi
  const legacyTheme = localStorage.getItem('klink_theme') as Theme | null;
  if (legacyTheme === 'light' || legacyTheme === 'dark') {
    return { theme: legacyTheme, syncedWithSystem: false, lastUpdated: Date.now() };
  }

  const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  return {
    theme: systemDark ? 'dark' : 'light',
    syncedWithSystem: true,
    lastUpdated: Date.now(),
  };
}

export function applyTheme(theme: Theme): void {
  if (typeof window === 'undefined') return;

  const root = document.documentElement;
  const isDark =
    theme === 'dark' ||
    (theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches);

  if (isDark) {
    root.classList.add('dark');
  } else {
    root.classList.remove('dark');
  }
}

export function saveThemeConfig(theme: Theme): ThemeConfig {
  const isSystem = theme === 'system';
  const config: ThemeConfig = {
    theme,
    syncedWithSystem: isSystem,
    lastUpdated: Date.now(),
  };

  if (typeof window !== 'undefined') {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(config));
      localStorage.setItem('klink_theme', theme);
    } catch (e) {
      console.error('Tema kaydedilemedi:', e);
    }
    applyTheme(theme);
  }

  return config;
}
