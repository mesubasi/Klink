'use client';

import React from 'react';
import Link from 'next/link';
import { ShieldCheck, Shield, Link2, LogOut, User, Sparkles, LayoutDashboard } from 'lucide-react';
import { Language, translations } from '@/lib/translations';
import { ApiClient } from '@/lib/api';
import { Button } from '@/components/ui/button';

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
      localStorage.removeItem('klink_user');
      localStorage.removeItem('swiftlink_user');
      window.location.href = '/login';
    }
  };

  return (
    <header className="sticky top-0 z-40 bg-white/80 backdrop-blur-md border-b border-zinc-200/80 px-4 sm:px-8 py-3 transition-all">
      <div className="max-w-7xl mx-auto flex items-center justify-between gap-4">
        {/* Brand Logo & Live Status */}
        <div className="flex items-center gap-3.5">
          <Link href="/" className="flex items-center gap-2.5 group">
            <div className="w-8 h-8 rounded-xl bg-zinc-950 text-white flex items-center justify-center shadow-xs group-hover:scale-105 transition-transform">
              <Link2 className="w-4 h-4 text-white" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="text-sm font-bold text-zinc-950 tracking-tight">
                  Klink
                </span>
                <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-mono font-semibold bg-zinc-100 text-zinc-700 border border-zinc-200/80">
                  v2.4
                </span>
              </div>
              <p className="text-[11px] text-zinc-400 font-medium hidden sm:block">
                {lang === 'tr' ? 'Yeni Nesil Link & Analitik Platformu' : 'Next-Gen Link & Telemetry Engine'}
              </p>
            </div>
          </Link>

          {/* Real-time Status Badge */}
          <div className="hidden lg:flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-50/80 border border-emerald-200/80 text-[11px] font-medium text-emerald-800">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
            <span>{lang === 'tr' ? 'Sistem Aktif (Redis & RabbitMQ)' : 'Online (<2ms Latency)'}</span>
          </div>
        </div>

        {/* User Profile, 2FA Settings, Language Switcher & Logout Button */}
        <div className="flex items-center gap-2 sm:gap-2.5">
          {/* User Badge */}
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-zinc-100/90 border border-zinc-200/80 text-xs font-medium text-zinc-800">
            <div className="w-4 h-4 rounded-full bg-zinc-900 text-white flex items-center justify-center text-[9px] font-bold">
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
                  ? 'border-emerald-200/90 bg-emerald-50/70 text-emerald-800 hover:bg-emerald-100/80'
                  : 'text-zinc-700'
              }`}
              title="2FA Güvenlik Ayarları"
            >
              {is2FAEnabled ? (
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-600 mr-1" />
              ) : (
                <Shield className="w-3.5 h-3.5 text-zinc-400 mr-1" />
              )}
              <span className="hidden md:inline">{is2FAEnabled ? '2FA Aktif' : '2FA Kur'}</span>
            </Button>
          )}

          {/* Language Switcher */}
          <div className="flex items-center p-0.5 rounded-lg bg-zinc-100 border border-zinc-200/80 text-xs font-semibold">
            <button
              onClick={() => onLanguageChange('tr')}
              className={`px-2 py-0.5 rounded-md transition-all cursor-pointer text-[11px] ${
                lang === 'tr'
                  ? 'bg-white text-zinc-950 shadow-2xs font-bold'
                  : 'text-zinc-500 hover:text-zinc-900'
              }`}
            >
              TR
            </button>
            <button
              onClick={() => onLanguageChange('en')}
              className={`px-2 py-0.5 rounded-md transition-all cursor-pointer text-[11px] ${
                lang === 'en'
                  ? 'bg-white text-zinc-950 shadow-2xs font-bold'
                  : 'text-zinc-500 hover:text-zinc-900'
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
            className="text-zinc-600 hover:text-red-600 hover:bg-red-50/80 p-2"
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

