'use client';

import React from 'react';
import Link from 'next/link';
import { ShieldCheck, Shield, Link2, LogOut, User, Sparkles, LayoutDashboard } from 'lucide-react';
import { Language, translations } from '@/lib/translations';
import { ApiClient } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { ThemeToggle } from '@/components/ThemeToggle';

interface NavbarProps {
  lang: Language;
  onLanguageChange: (lang: Language) => void;
  username: string;
  is2FAEnabled?: boolean;
  onOpen2FA?: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ lang, onLanguageChange, username, is2FAEnabled = false, onOpen2FA }) => {
  const t = translations[lang];

  const handleLogout = async () => {
    try {
      await ApiClient.logoutUser(lang);
    } catch (e) {
      console.error(e);
    } finally {
      const isAdmin = username === 'admin';
      localStorage.removeItem('klink_user');
      localStorage.removeItem('swiftlink_user');
      window.location.href = isAdmin ? '/admin/login' : '/login';
    }
  };

  return (
    <header className="sticky top-0 z-40 bg-white/80 dark:bg-zinc-900/80 backdrop-blur-md border-b border-zinc-200/80 dark:border-zinc-800 px-4 sm:px-8 py-3 transition-colors duration-200">
      <div className="max-w-7xl mx-auto flex items-center justify-between gap-4">
        {/* Brand Logo & Live Status */}
        <div className="flex items-center gap-3.5">
          <Link href="/" className="flex items-center gap-2.5 group">
            <div className="w-8 h-8 rounded-xl bg-zinc-950 dark:bg-white text-white dark:text-zinc-950 flex items-center justify-center shadow-xs group-hover:scale-105 transition-transform">
              <Link2 className="w-4 h-4" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="text-sm font-bold text-zinc-950 dark:text-white tracking-tight">
                  Klink
                </span>
                <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-mono font-semibold bg-zinc-100 dark:bg-zinc-800 text-zinc-700 dark:text-zinc-300 border border-zinc-200/80 dark:border-zinc-700">
                  v2.4
                </span>
              </div>
              <p className="text-[11px] text-zinc-400 dark:text-zinc-500 font-medium hidden sm:block">
                {lang === 'tr' ? 'Yeni Nesil Link & Analitik Platformu' : 'Next-Gen Link & Telemetry Engine'}
              </p>
            </div>
          </Link>

          {/* Real-time Status Badge */}
          <div className="hidden lg:flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-50/80 dark:bg-emerald-950/40 border border-emerald-200/80 dark:border-emerald-800 text-[11px] font-medium text-emerald-800 dark:text-emerald-300">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
            <span>{lang === 'tr' ? 'Sistem Aktif (Redis & RabbitMQ)' : 'Online (<2ms Latency)'}</span>
          </div>
        </div>

        {/* User Profile, 2FA Settings, Theme Toggle, Language Switcher & Logout Button */}
        <div className="flex items-center gap-2 sm:gap-2.5">
          {/* User Badge */}
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-zinc-100/90 dark:bg-zinc-800/80 border border-zinc-200/80 dark:border-zinc-700 text-xs font-medium text-zinc-800 dark:text-zinc-200">
            <div className="w-4 h-4 rounded-full bg-zinc-900 dark:bg-zinc-700 text-white flex items-center justify-center text-[9px] font-bold">
              {username ? username[0].toUpperCase() : 'U'}
            </div>
            <span className="max-w-[100px] sm:max-w-[130px] truncate font-mono text-[11px]">@{username}</span>
          </div>

          {/* 2FA Security Button */}
          {onOpen2FA && (
            <Button
              variant={is2FAEnabled ? "outline" : "secondary"}
              size="sm"
              onClick={onOpen2FA}
              className={`text-xs ${
                is2FAEnabled
                  ? 'border-emerald-200/90 dark:border-emerald-700/50 bg-emerald-50/70 dark:bg-emerald-950/40 text-emerald-800 dark:text-emerald-300 hover:bg-emerald-100/80 dark:hover:bg-emerald-900/50'
                  : 'text-zinc-700 dark:text-zinc-300 dark:bg-zinc-800'
              }`}
              title="2FA Güvenlik Ayarları"
            >
              {is2FAEnabled ? (
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400 mr-1" />
              ) : (
                <Shield className="w-3.5 h-3.5 text-zinc-400 mr-1" />
              )}
              <span className="hidden md:inline">{is2FAEnabled ? '2FA Aktif' : '2FA Kur'}</span>
            </Button>
          )}

          {/* Theme Toggle (Dark / Light) */}
          <ThemeToggle />

          {/* Language Switcher */}
          <div className="flex items-center p-0.5 rounded-lg bg-zinc-100 dark:bg-zinc-800 border border-zinc-200/80 dark:border-zinc-700 text-xs font-semibold">
            <button
              onClick={() => onLanguageChange('tr')}
              className={`px-2 py-0.5 rounded-md transition-all cursor-pointer text-[11px] ${
                lang === 'tr'
                  ? 'bg-white dark:bg-zinc-700 text-zinc-950 dark:text-white shadow-2xs font-bold'
                  : 'text-zinc-500 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-white'
              }`}
            >
              TR
            </button>
            <button
              onClick={() => onLanguageChange('en')}
              className={`px-2 py-0.5 rounded-md transition-all cursor-pointer text-[11px] ${
                lang === 'en'
                  ? 'bg-white dark:bg-zinc-700 text-zinc-950 dark:text-white shadow-2xs font-bold'
                  : 'text-zinc-500 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-white'
              }`}
            >
              EN
            </button>
          </div>

          {/* Logout Button */}
          <Button
            variant="ghost"
            size="sm"
            onClick={handleLogout}
            className="text-zinc-600 dark:text-zinc-400 hover:text-red-600 dark:hover:text-red-400 hover:bg-red-50/80 dark:hover:bg-red-950/30 p-2"
            title="Oturumu Kapat"
          >
            <LogOut className="w-3.5 h-3.5" />
            <span className="hidden sm:inline text-xs">{lang === 'tr' ? 'Çıkış' : 'Logout'}</span>
          </Button>
        </div>
      </div>
    </header>
  );
};
